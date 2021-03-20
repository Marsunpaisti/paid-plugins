package net.runelite.client.plugins.pfighteraio;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.json.Json;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.json.JsonObject;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.json.JsonValue;
import okhttp3.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LicenseValidator {
    private OkHttpClient httpClient;
    private Instant lastSuccessfulValidation;
    private Instant created;
    private int periodSeconds;
    private String sessionId;
    private String apiKey;
    private String licenseType;
    private String lastError;
    private boolean shouldStop;

    public LicenseValidator(String licenseType, int periodSeconds, String apiKey) {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        this.created = Instant.now();
        this.apiKey = apiKey;
        this.licenseType = licenseType;
        this.periodSeconds = periodSeconds;
    }

    public void startValidating() {
        startSession();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e){
                log.error(e.toString());
            }
            synchronized (this){
                if (shouldStop) {
                    stopSession();
                    log.info("License validator stopped");
                    return;
                }
            }
            if (Duration.between(lastSuccessfulValidation, Instant.now()).getSeconds() >= periodSeconds) {
                refreshSession();
            }
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

    private boolean stopSession() {
        JsonObject payload = new JsonObject(); //Json.object().add("data", Json.object().add("apiKey", apiKey).add("licenseType", licenseType).add("sessionId", sessionId));
        JsonObject data = new JsonObject();
        data.set("apiKey", apiKey);
        data.set("licenseType", licenseType);
        data.set("sessionId", sessionId);
        payload.set("data", data);
        JsonObject stopResult = post("https://europe-west1-paistiplugins.cloudfunctions.net/stopSession", payload);
        if (stopResult != null && stopResult.get("result") != null) {
            log.info("Successfully stopped session");
            return true;
        }

        if (stopResult == null) {
            log.error("Unable to stop session.");
        }
        JsonObject error = (JsonObject)stopResult.get("error");
        if (error != null) {
            log.error("Unable to stop session: " + error.get("message").toString());
        } else {
            log.error("Unable to stop session.");
        }

        return false;
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
                log.error("Unable to validate license. (Null activation result)");
                setLastError("Unable to validate license. (Null activation result)");
            }
            JsonObject error = (JsonObject)activationResult.get("error");
            if (error != null) {
                log.error("Unable to validate license: " + error.get("message").toString());
                setLastError("Unable to validate license: " + error.get("message").toString());
            } else {
                log.error("Unable to validate license. (No error from server)");
                setLastError("Unable to validate license. (No error from server");
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
        JsonObject refreshResult = post("https://europe-west1-paistiplugins.cloudfunctions.net/refreshSession", payload);
        if (refreshResult != null && refreshResult.get("result") != null) {
            synchronized (this){
                lastSuccessfulValidation = Instant.now();
                log.info("Successfully refreshed session");
            }
        } else {
            if (refreshResult == null) {
                log.error("Unable to validate license. (Null refresh result)");
                setLastError("Unable to validate license. (Null refresh result)");
            }
            JsonObject error = (JsonObject)refreshResult.get("error");
            if (error != null) {
                log.error("Unable to validate license: " + error.get("message").toString());
                setLastError("Unable to validate license: " + error.get("message").toString());
            } else {
                log.error("Unable to validate license. (No error from server)");
                setLastError("Unable to validate license. (No error from server)");
            }
        }
    }

    private JsonObject post(String url, JsonObject payload){
        HttpUrl httpUrl = HttpUrl.parse(url);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload.toString());
        Request request = new Request.Builder()
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .url(httpUrl)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute())
        {
            ResponseBody responseBody = response.body();
            if (responseBody == null)
            {
                log.error("Null response body from backend");
                return null;
            }

            String responseBodyString = responseBody.string();
            if (responseBodyString.isEmpty())
            {
                log.error("Empty response body from backend");
                return null;
            }

            return (JsonObject) Json.parse(responseBodyString);
        }
        catch (Exception e)
        {
            log.error("Error in POST: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // https://europe-west1-paistiplugins.cloudfunctions.net/startSession
    // https://europe-west1-paistiplugins.cloudfunctions.net/refreshSession
    public synchronized boolean isValid(){
        if (Duration.between(created, Instant.now()).getSeconds() <= 20) return true;
        if (lastSuccessfulValidation == null) return false;
        return Duration.between(lastSuccessfulValidation, Instant.now()).getSeconds() <= periodSeconds + 20;
    }
}
