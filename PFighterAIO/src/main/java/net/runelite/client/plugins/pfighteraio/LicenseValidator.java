package net.runelite.client.plugins.pfighteraio;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.json.Json;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.json.JsonObject;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.json.JsonValue;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

@Slf4j
public class LicenseValidator {
    private Instant lastSuccessfulValidation;
    private Instant created;
    private int periodSeconds;
    private String sessionId;
    private String apiKey;
    private String licenseType;
    private String lastError;
    private boolean shouldStop;

    public LicenseValidator(String licenseType, int periodSeconds, String apiKey) {
        this.created = Instant.now();
        this.apiKey = apiKey;
        this.licenseType = licenseType;
        this.periodSeconds = periodSeconds;
    }

    public void startValidating() {
        startSession();
        while (true) {
            synchronized (this){
                if (shouldStop) {
                    log.info("License validator stopped");
                    return;
                }
            }
            try {
                Thread.sleep(periodSeconds*1000);
            } catch (InterruptedException e){
                log.error(e.toString());
            }
            synchronized (this){
                if (shouldStop) {
                    log.info("License validator stopped");
                    return;
                }
            }
            refreshSession();
        }
    }

    public synchronized void requestStop(){
        this.shouldStop = true;
    }

    private synchronized void setLastError(String value){
        this.lastError = value;
    }

    public synchronized String getLastError(){
        return this.lastError;
    }

    private void startSession(){
        JsonObject payload = Json.object().add("data", Json.object().add("apiKey", apiKey).add("licenseType", licenseType));
        JsonObject activationResult = post("https://europe-west1-paistiplugins.cloudfunctions.net/startSession", payload);
        if (activationResult != null && activationResult.get("result") != null) {
            synchronized (this){
                sessionId = activationResult.get("result").asString();
                lastSuccessfulValidation = Instant.now();
                log.info("Session ID: " + sessionId);
            }
        } else {
            if (activationResult == null) {
                log.error("Unable to validate license.");
                setLastError("Unable to validate license");
            }
            JsonObject error = (JsonObject)activationResult.get("error");
            if (error != null) {
                log.error("Unable to validate license: " + error.get("message").toString());
                setLastError("Unable to validate license: " + error.get("message").toString());
            } else {
                log.error("Unable to validate license.");
                setLastError("Unable to validate license.");
            }
        }
    }

    private void refreshSession(){
        JsonObject payload = new JsonObject(); //Json.object().add("data", Json.object().add("apiKey", apiKey).add("licenseType", licenseType).add("sessionId", sessionId));
        JsonObject data = new JsonObject();
        data.set("apiKey", apiKey);
        data.set("licenseType", licenseType);
        data.set("sessionId", sessionId);
        payload.set("data", data);
        JsonObject activationResult = post("https://europe-west1-paistiplugins.cloudfunctions.net/refreshSession", payload);
        if (activationResult != null && activationResult.get("result") != null) {
            synchronized (this){
                lastSuccessfulValidation = Instant.now();
                log.info("Successfully refreshed session");
            }
        } else {
            if (activationResult == null) {
                log.error("Unable to validate license.");
                setLastError("Unable to validate license");
            }
            JsonObject error = (JsonObject)activationResult.get("error");
            if (error != null) {
                log.error("Unable to validate license: " + error.get("message").toString());
                setLastError("Unable to validate license: " + error.get("message").toString());
            } else {
                log.error("Unable to validate license.");
                setLastError("Unable to validate license.");
            }
        }
    }

    private JsonObject post(String url, JsonObject payload) {
        HttpsURLConnection con = null;
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            SSLContext.setDefault(ctx);
            URL startEndpoint = new URL(url);
            con = (HttpsURLConnection) startEndpoint.openConnection();
            con.setRequestMethod("POST");
            con.addRequestProperty("content-type", "application/json");
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, Charset.forName("UTF-8"));
            payload.writeTo(osw);
            osw.flush();
            osw.close();
            con.connect();
        } catch (Exception e){
            log.error(e.toString());
        }

        if (con == null) return null;
        
        String res;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(con.getInputStream());
        } catch (IOException e){
            log.error(e.toString());
            bis = new BufferedInputStream(con.getErrorStream());
        }

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            int resByte = bis.read();
            while (resByte != -1){
                buf.write((byte) resByte);
                resByte = bis.read();
            }
        } catch (Exception e){
            log.error(e.toString());
            return null;
        }

        res = buf.toString();
        return (JsonObject)Json.parse(res);
    }

    // https://europe-west1-paistiplugins.cloudfunctions.net/startSession
    // https://europe-west1-paistiplugins.cloudfunctions.net/refreshSession
    public synchronized boolean isValid(){
        if (Duration.between(created, Instant.now()).getSeconds() <= 10) return true;
        if (lastSuccessfulValidation == null) return false;
        return Duration.between(lastSuccessfulValidation, Instant.now()).getSeconds() <= periodSeconds + 10;
    }
}
