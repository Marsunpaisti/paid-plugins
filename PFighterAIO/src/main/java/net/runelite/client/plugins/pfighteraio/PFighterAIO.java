package net.runelite.client.plugins.pfighteraio;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;

import static net.runelite.api.ObjectID.*;
import static net.runelite.api.ProjectileID.CANNONBALL;
import static net.runelite.api.ProjectileID.GRANITE_CANNONBALL;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.paistisuite.PScript;
import net.runelite.client.plugins.paistisuite.PaistiSuite;
import net.runelite.client.plugins.paistisuite.api.*;
import net.runelite.client.plugins.paistisuite.api.WebWalker.WalkingCondition;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.DaxWalker;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.local_pathfinding.Reachable;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.Keyboard;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.RSTile;
import net.runelite.client.plugins.paistisuite.api.types.Filters;
import net.runelite.client.plugins.paistisuite.api.types.PGroundItem;
import net.runelite.client.plugins.paistisuite.api.types.PItem;
import net.runelite.client.plugins.paistisuite.api.types.PTileObject;
import net.runelite.client.plugins.pfighteraio.states.*;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Extension
@PluginDependency(PaistiSuite.class)
@PluginDescriptor(
        name = "PFighterAIO",
        enabledByDefault = false,
        description = "Fully configurable all-in-one fighter",
        tags = {"combat", "magic", "fighter", "paisti"}
)

@Slf4j
@Singleton
public class PFighterAIO extends PScript {
    private ExecutorService prayerFlickExecutor;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9]+)");
    private static boolean skipProjectileCheckThisTick = false;
    int nextRunAt = PUtils.random(25,65);
    int nextEatAt;
    long lastAntiAfk;
    long antiAfkDelay = PUtils.randomNormal(240000, 295000);
    private boolean usingSavedFightTile;
    private boolean usingSavedSafeSpot;
    private boolean usingSavedCannonTile;

    private LicenseValidator licenseValidator;
    private ExecutorService validatorExecutor;
    public PBreakScheduler breakScheduler;
    PFighterAIOSettings settings = new PFighterAIOSettings();
    List<State> states;
    public BankingState bankingState;
    public LootItemsState lootItemsState;
    public FightEnemiesState fightEnemiesState;
    public DrinkPotionsState drinkPotionsState;
    public SetupCannonState setupCannonState;
    public TakeBreaksState takeBreaksState;
    public WalkToFightAreaState walkToFightAreaState;
    public WorldhopState worldhopState;
    State currentState;
    State previousState;

    @Inject
    private PFighterAIOConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private PFighterAIOOverlay overlay;
    @Inject
    private PFighterAIOOverlayMinimap minimapoverlay;
    @Inject
    private ConfigManager configManager;

    @Provides
    PFighterAIOConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PFighterAIOConfig.class);
    }

    @Override
    protected void startUp()
    {
        if (PUtils.getClient() != null && PUtils.getClient().getGameState() == GameState.LOGGED_IN){
            readConfig();
        }
        overlayManager.add(overlay);
        overlayManager.add(minimapoverlay);
    }

    public boolean shouldPray() {
        if (isRunning()){
            if (PPlayer.get().getInteracting() != null){
                if (settings.getValidTargetFilterWithoutDistance().test((NPC)PPlayer.get().getInteracting())) {
                    return true;
                }
            }

            if (PObjects.findNPC(settings.getValidTargetFilterWithoutDistance().and(n -> n.getInteracting() != null && n.getInteracting() == PPlayer.get())) != null){
                return true;
            }
        } else if (settings.isAssistFlickPrayers()){
            if (PPlayer.get().getInteracting() != null && PPlayer.get().getInteracting() instanceof NPC){
                if (Filters.NPCs.actionsContains("Attack").test((NPC)PPlayer.get().getInteracting())) {
                    return true;
                }
            }
            if (PObjects.findNPC(Filters.NPCs.actionsContains("Attack").and(n -> n.getInteracting() != null && n.getInteracting() == PPlayer.get())) != null){
                return true;
            }
        }
        return false;
    }

    @Subscribe
    private void onGameTick(GameTick event){
        skipProjectileCheckThisTick = false;
        if (((isRunning() && settings.isFlickQuickPrayers()) || settings.isAssistFlickPrayers()) && PSkills.getCurrentLevel(Skill.PRAYER) > 0) {
            if (shouldPray()){
                if (PVars.getVarbit(Varbits.QUICK_PRAYER) > 0){
                    prayerFlickExecutor.submit(() -> {
                        PUtils.sleepNormal(25, 240);
                        Widget quickPray = PWidgets.get(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
                        PInteraction.widget(quickPray, "Deactivate");
                        PUtils.sleepNormal(90, 150);
                        PInteraction.widget(quickPray, "Activate");
                    });
                } else {
                    prayerFlickExecutor.submit(() -> {
                        PUtils.sleepNormal(25, 240);
                        Widget quickPray = PWidgets.get(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
                        PInteraction.widget(quickPray, "Activate");
                    });
                }
            } else {
                if (PVars.getVarbit(Varbits.QUICK_PRAYER) != 0){
                    prayerFlickExecutor.submit(() -> {
                        PUtils.sleepNormal(100, 350);
                            Widget quickPray = PWidgets.get(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
                            PInteraction.widget(quickPray, "Deactivate");
                    });
                }
            }
        }
    }

    @Subscribe
    protected void onGameStateChanged(GameStateChanged event){
        if (event.getGameState() == GameState.LOGGED_IN){
            readConfig();
        }
    }

    @Override
    protected synchronized void onStart() {
        PUtils.sendGameMessage("AiO Fighter started!");
        readConfig();
        breakScheduler = new PBreakScheduler(settings.getBreakMinIntervalMinutes(), settings.getBreakMaxIntervalMinutes(), settings.getBreakMinDurationSeconds(), settings.getBreakMaxDurationSeconds());
        fightEnemiesState = new FightEnemiesState(this, settings);
        lootItemsState = new LootItemsState(this, settings);
        walkToFightAreaState = new WalkToFightAreaState(this, settings);
        bankingState = new BankingState(this, settings);
        takeBreaksState = new TakeBreaksState(this, settings);
        setupCannonState = new SetupCannonState(this, settings);
        drinkPotionsState = new DrinkPotionsState(this, settings);
        worldhopState = new WorldhopState(this, settings);

        settings.setStartedTimestamp(Instant.now());

        if (usingSavedSafeSpot){
            PUtils.sendGameMessage("Loaded safespot from last config.");
        }
        if (usingSavedFightTile){
            PUtils.sendGameMessage("Loaded fight area from last config.");
        }
        if (usingSavedCannonTile){
            PUtils.sendGameMessage("Loaded cannon tile from last config.");
        }
        DaxWalker.setCredentials(PaistiSuite.getDaxCredentialsProvider());
        DaxWalker.getInstance().allowTeleports = true;
        states = new ArrayList<State>();
        states.add(this.bankingState);
        states.add(this.takeBreaksState);
        states.add(this.worldhopState);
        states.add(this.drinkPotionsState);
        states.add(this.lootItemsState);
        states.add(this.setupCannonState);
        states.add(this.fightEnemiesState);
        states.add(this.walkToFightAreaState);
        currentState = null;
        settings.setCurrentStateName("Null");

        licenseValidator = new LicenseValidator("PFIGHTERAIO", 600, settings.getApiKey());
        validatorExecutor = Executors.newSingleThreadExecutor();
        validatorExecutor.submit(() -> {
            licenseValidator.startValidating();
        });
    }

    private synchronized void readConfig(){
        // Targeting & tiles
        log.info("Reading targeting & tiles settings");
        settings.setSearchRadius(config.searchRadius());
        settings.setEnemiesToTarget(PUtils.parseCommaSeparated(config.enemyNames()));
        settings.setSafeSpotForCombat(config.enableSafeSpot());
        settings.setSafeSpotForLogout(config.exitInSafeSpot());
        settings.setEnablePathfind(config.enablePathfind());
        settings.setValidTargetFilter(createValidTargetFilter(false));
        settings.setValidTargetFilterWithoutDistance(createValidTargetFilter(true));

        // Eating
        log.info("Reading eating settings");
        settings.setFoodsToEat(PUtils.parseCommaSeparated(config.foodNames()));
        settings.setValidFoodFilter(createValidFoodFilter());
        settings.setMaxEatHp(Math.min(PSkills.getActualLevel(Skill.HITPOINTS), config.maxEatHP()));
        settings.setMinEatHp( Math.min(config.minEatHP(), settings.getMaxEatHp()));
        nextEatAt = (int)PUtils.randomNormal(settings.getMinEatHp(), settings.getMaxEatHp());
        settings.setStopWhenOutOfFood(config.stopWhenOutOfFood());
        settings.setUsePotions(config.usePotions());
        settings.setMinPotionBoost(Math.max(1, config.minPotionBoost()));
        settings.setMaxPotionBoost(Math.max(config.minPotionBoost(), Math.max(config.maxPotionBoost(), 8)));
        settings.setMinPotionBoost(Math.min(settings.getMinPotionBoost(), settings.getMaxPotionBoost()));

        // Looting
        log.info("Reading looting settings");
        settings.setLootNames( PUtils.parseCommaSeparated(config.lootNames()));
        settings.setEatFoodForLoot(config.eatForLoot());
        settings.setForceLoot(config.forceLoot());
        settings.setLootGEValue(config.lootGEValue());
        settings.setValidLootFilter(createValidLootFilter());
        settings.setReservedInventorySlots(0);
        settings.setEnableAlching(config.enableAlching());
        settings.setAlchMaxPriceDifference(config.alchMaxPriceDifference());
        settings.setAlchMinHAValue(config.alchMinHAValue());

        // Banking
        log.info("Reading banking settings");
        settings.setBankTile(null);
        if (config.bankLocation() != PFighterBanks.AUTODETECT){
            settings.setBankTile(config.bankLocation().getWorldPoint());
        }
        settings.setBankForFood(config.bankForFood());
        settings.setBankForLoot(config.bankForLoot());
        settings.setBankingEnabled(config.enableBanking());
        settings.setTeleportWhileBanking(config.teleportWhileBanking());
        ArrayList<BankingState.WithdrawItem> itemsToWithdraw = new ArrayList<BankingState.WithdrawItem>();
        try {
            String[] split = PUtils.parseNewlineSeparated(config.withdrawItems());
            Arrays.stream(split).forEach(s -> log.info("Row: " + s));
            for (String s : split){
                if (s.length() <= 0) continue;
                String[] parts = s.split(":");
                String nameOrId = parts[0];
                int quantity = Integer.parseInt(parts[1].strip());
                itemsToWithdraw.add(new BankingState.WithdrawItem(nameOrId, quantity));
            }
        } catch (Exception e){
            PUtils.sendGameMessage("Error when trying to read withdraw items list");
        }
        settings.setItemsToWithdraw(itemsToWithdraw);

        // Breaks
        log.info("Reading breaks settings");
        settings.setEnableBreaks(config.enableBreaks());
        settings.setSafeSpotForBreaks(config.safeSpotForBreaks());
        settings.setBreakMinIntervalMinutes(config.minBreakIntervalMinutes());
        settings.setBreakMaxIntervalMinutes(Math.max(config.maxBreakIntervalMinutes(), settings.getBreakMinIntervalMinutes()));
        settings.setBreakMinDurationSeconds(config.minBreakDurationSeconds());
        settings.setBreakMaxDurationSeconds(Math.max(config.maxBreakDurationSeconds(), settings.getBreakMinDurationSeconds()));

        // Prayer
        log.info("Reading prayer settings");
        settings.setFlickQuickPrayers(config.flickQuickPrayers());
        settings.setAssistFlickPrayers(config.assistFlickPrayers());
        if (settings.isFlickQuickPrayers() || settings.isAssistFlickPrayers()) {
            if (prayerFlickExecutor == null) prayerFlickExecutor = Executors.newSingleThreadExecutor();
        }

        // Cannoning
        log.info("Reading cannon settings");
        settings.setUseCannon(config.useCannon());
        if (settings.isUseCannon()){
            settings.setReservedInventorySlots(settings.getReservedInventorySlots() + 4);
        }

        // Stored tiles
        log.info("Reading stored tiles");
        if (settings.getSearchRadiusCenter() == null){
            usingSavedFightTile = true;
            settings.setSearchRadiusCenter(config.storedFightTile());
        }

        if (settings.getSafeSpot() == null && config.storedFightTile() != null && config.storedSafeSpotTile().distanceTo2D(config.storedFightTile()) < 50) {
            settings.setSafeSpot(config.storedSafeSpotTile());
            usingSavedSafeSpot = true;
        }

        if (settings.getCannonTile() == null && settings.isUseCannon() && config.storedCannonTile() != null && config.storedCannonTile().distanceTo2D(config.storedFightTile()) < 50) {
            usingSavedCannonTile = true;
            settings.setCannonTile(config.storedCannonTile());
        }

        // World hopping
        log.info("Reading worldhop settings");
        settings.setWorldhopIfPlayerTalks(config.worldhopIfPlayerTalks());
        settings.setWorldhopIfTooManyPlayers(config.worldhopIfTooManyPlayers());
        settings.setWorldhopPlayerLimit(config.worldhopPlayerLimit());
        settings.setWorldhopInSafespot(config.worldHopInSafespot());

        // Licensing
        settings.setApiKey(config.apiKey());

        log.info(settings.toString());
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        GameObject gameObject = event.getGameObject();
        if (gameObject.getId() == CANNON_BASE && !settings.isCannonPlaced())
        {
            Player localPlayer = PPlayer.get();
            if (localPlayer.getWorldLocation().distanceTo(gameObject.getWorldLocation()) <= 2
                    && localPlayer.getAnimation() == AnimationID.BURYING_BONES)
            {
                log.info("Cannon placement started");
                settings.setCannonFinished(false);
                settings.setCannonPlaced(true);
                settings.setCurrentCannonPos(gameObject.getWorldLocation());
                settings.setCurrentCannonWorld(PUtils.getClient().getWorld());
            }
        }
    }

    public void handleWorldHopOnChatMessage(ChatMessage event){
        if (event.getType() != ChatMessageType.PUBLICCHAT) return;
        if (event.getSender().equalsIgnoreCase(PPlayer.get().getName())) return;

        if (isRunning() && settings.isWorldhopIfPlayerTalks() && worldhopState != null) {
            worldhopState.setWorldhopRequested(true);
        }
    }

    public void handleChatMessageCannonVars(ChatMessage event){

        // Cannon stuff
        if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE)
        {
            return;
        }

        if (event.getMessage() == null) return;

        if (event.getMessage().equals("You add the furnace."))
        {
            settings.setCannonPlaced(true);
            settings.setCannonFinished(true);
            settings.setCannonBallsLeft(0);
        }

        if (event.getMessage().contains("You pick up the cannon")
                || event.getMessage().contains("Your cannon has decayed. Speak to Nulodion to get a new one!")
                || event.getMessage().contains("Your cannon has been destroyed!"))
        {
            settings.setCannonPlaced(false);
            settings.setCannonBallsLeft(0);
        }

        if (event.getMessage().startsWith("You load the cannon with"))
        {
            Matcher m = NUMBER_PATTERN.matcher(event.getMessage());
            if (m.find())
            {
                // The cannon will usually refill to MAX_CBALLS, but if the
                // player didn't have enough cannonballs in their inventory,
                // it could fill up less than that. Filling the cannon to
                // cballsLeft + amt is not always accurate though because our
                // counter doesn't decrease if the player has been too far away
                // from the cannon due to the projectiels not being in memory,
                // so our counter can be higher than it is supposed to be.
                int amt = 0;
                try {
                    amt = Integer.parseInt(m.group());
                } catch (Exception e){
                    log.error("Error parsing cannonball amount: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }


                if (settings.getCannonBallsLeft() + amt >= 30)
                {
                    skipProjectileCheckThisTick = true;
                    settings.setCannonBallsLeft(30);
                }
                else
                {
                    settings.setCannonBallsLeft(settings.getCannonBallsLeft() + amt);
                }

            }
            else if (event.getMessage().equals("You load the cannon with one cannonball."))
            {
                if (settings.getCannonBallsLeft() + 1 >= 30)
                {
                    skipProjectileCheckThisTick = true;
                    settings.setCannonBallsLeft(30);
                }
                else
                {
                    settings.setCannonBallsLeft(settings.getCannonBallsLeft() + 1);
                }
            }
        }

        if (event.getMessage().contains("Your cannon is out of ammo!"))
        {
            skipProjectileCheckThisTick = true;
            // If the player was out of range of the cannon, some cannonballs
            // may have been used without the client knowing, so having this
            // extra check is a good idea.
            settings.setCannonBallsLeft(0);
        }

        if (event.getMessage().startsWith("You unload your cannon and receive Cannonball")
                || event.getMessage().startsWith("You unload your cannon and receive Granite cannonball"))
        {
            skipProjectileCheckThisTick = true;
            settings.setCannonBallsLeft(0);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        handleWorldHopOnChatMessage(event);
        handleChatMessageCannonVars(event);
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event)
    {
        Projectile projectile = event.getProjectile();

        if ((projectile.getId() == CANNONBALL || projectile.getId() == GRANITE_CANNONBALL) && settings.getCurrentCannonPos() != null && settings.getCurrentCannonWorld() == PUtils.getClient().getWorld())
        {
            WorldPoint projectileLoc = WorldPoint.fromLocal(PUtils.getClient(), projectile.getX1(), projectile.getY1(), PUtils.getClient().getPlane());

            //Check to see if projectile x,y is 0 else it will continuously decrease while ball is flying.
            if (projectileLoc.equals(settings.getCurrentCannonPos()) && projectile.getX() == 0 && projectile.getY() == 0)
            {
                // When there's a chat message about cannon reloaded/unloaded/out of ammo,
                // the message event runs before the projectile event. However they run
                // in the opposite order on the server. So if both fires in the same tick,
                // we don't want to update the cannonball counter if it was set to a specific
                // amount.
                if (!skipProjectileCheckThisTick)
                {
                    settings.setCannonBallsLeft(settings.getCannonBallsLeft() - 1);
                }
            }
        }
    }

    public PTileObject getCannon(){
        WorldPoint searchPos;
        WorldPoint currentCannonPos = settings.getCurrentCannonPos();
        searchPos = new WorldPoint(currentCannonPos.getX(), currentCannonPos.getY(), currentCannonPos.getPlane());
        return PObjects.findObject(Filters.Objects.idEquals(5,6,7,8,9).and((ob) -> ob.getWorldLocation().distanceTo(searchPos) <= 2));
    }

    @Override
    protected synchronized void onStop() {
        PUtils.sendGameMessage("AiO Fighter stopped!");
        settings.setSearchRadiusCenter(null);
        settings.setSafeSpot(null);
        settings.setCannonTile(null);
        licenseValidator.requestStop();
    }

    @Override
    protected void shutDown() {
        requestStop();
        licenseValidator.requestStop();
        overlayManager.remove(overlay);
        overlayManager.remove(minimapoverlay);
    }

    public State getValidState(){
        for (State s : states) {
            if (s.condition()) return s;
        }
        return null;
    }

    private boolean checkValidator(){
        if (!licenseValidator.isValid() && !fightEnemiesState.inCombat()) {
            log.info("License Validation error: " + licenseValidator.getLastError());
            PUtils.sendGameMessage("License validation error: " + licenseValidator.getLastError());
            requestStop();
            return false;
        }
        return true;
    }

    @Override
    protected void loop() {
        PUtils.sleepFlat(50, 150);
        if (PUtils.getClient().getGameState() != GameState.LOGGED_IN) return;

        if (!checkValidator()) return;
        if (handleStopConditions()) return;
        handleEating();
        if (isStopRequested()) return;
        handleRun();
        if (isStopRequested()) return;
        handleAntiAfk();
        handleLevelUps();

        previousState = currentState;
        currentState = getValidState();
        if(!checkValidator()) return;
        if (currentState != null) {
            if (previousState != currentState){
                log.info("Entered state: " + currentState.getName());
            }
            settings.setCurrentStateName(currentState.chainedName());
            currentState.loop();
        } else {
            log.info("Looking for state...");
            settings.setCurrentStateName("Looking for state...");
        }
    }

    private void handleLevelUps(){
        if (PDialogue.isConversationWindowUp()){
            List<Widget> options = PDialogue.getDialogueOptions();
            if (options.stream().anyMatch(o -> o.getText().contains("Click here to continue"))){
                PDialogue.clickHereToContinue();
            }
        }
    }

    private void handleAntiAfk(){
        if (System.currentTimeMillis() - lastAntiAfk >= antiAfkDelay) {
            lastAntiAfk = System.currentTimeMillis();
            antiAfkDelay = PUtils.randomNormal(240000, 295000);
            Keyboard.typeKeysInt(KeyEvent.VK_BACK_SPACE);
            PUtils.sleepNormal(100, 200);
        }
    }

    private boolean executeStop(){
        if (PBanking.isBankOpen() || PBanking.isDepositBoxOpen()) {
            PBanking.closeBank();
            PUtils.sleepNormal(700, 1500);
        }

        if (settings.isSafeSpotForLogout() && PPlayer.location().distanceTo(settings.getSafeSpot()) != 0 && PPlayer.distanceTo(settings.getSafeSpot()) <= 45) {
            if (!PWalking.sceneWalk(settings.getSafeSpot())) {
                DaxWalker.getInstance().allowTeleports = false;
                DaxWalker.walkTo(new RSTile(settings.getSafeSpot()), walkingCondition);
            }

            return true;
        }

        if (!fightEnemiesState.inCombat()){
            PUtils.logout();
            if  (PUtils.waitCondition(1500, () -> PUtils.getClient().getGameState() != GameState.LOGGED_IN)) {
                requestStop();
            } else {
                PUtils.sleepNormal(700, 2500);
            }
            return true;
        } else if (fightEnemiesState.inCombat()){
            PUtils.sleepNormal(700, 1500);
            return true;
        }

        return false;
    }

    private boolean handleStopConditions(){
        if (settings.isStopWhenOutOfFood() && PInventory.findItem(settings.getValidFoodFilter()) == null) {
            log.info("Stopping. No food remaining.");
            settings.setCurrentStateName("Stopping. No food remaining.");
            executeStop();
            return true;
        }

        if (this.bankingState.bankingFailure){
            log.info("Stopping. Banking failed.");
            settings.setCurrentStateName("Stopping. Banking failed.");
            executeStop();
            return true;
        }

        return false;
    }

    private boolean handleEating(){
        if (PSkills.getCurrentLevel(Skill.HITPOINTS) <= nextEatAt){
            nextEatAt = (int)PUtils.randomNormal(settings.getMinEatHp(), settings.getMaxEatHp());
            log.info("Next eat at " + nextEatAt);
            NPC targetBeforeEating = null;
            if (currentState == fightEnemiesState
                    && fightEnemiesState.inCombat()
                    && settings.getValidTargetFilter().test((NPC)PPlayer.get().getInteracting())
            ) {
                targetBeforeEating = (NPC)PPlayer.get().getInteracting();
            }

            boolean success = eatFood();
            if (success && targetBeforeEating != null && PUtils.random(1,5) <= 4) {
                log.info("Re-targeting current enemy after eating");
                if (PInteraction.npc(targetBeforeEating, "Attack")){
                    PUtils.sleepNormal(100, 700);
                }
            }
        }
        return false;
    }

    public boolean eatFood(){
        PItem food = PInventory.findItem(settings.getValidFoodFilter());
        if (food != null) log.info("Eating " + food.getName());
        if (PInteraction.item(food, "Eat")){
            log.info("Ate food");
            PUtils.sleepNormal(300, 1000);
            return true;
        }
        log.info("Failed to eat food!");
        return false;
    }

    public WalkingCondition walkingCondition = () -> {
        if (isStopRequested()) return WalkingCondition.State.EXIT_OUT_WALKER_FAIL;
        if (PUtils.getClient().getGameState() != GameState.LOGGED_IN && PUtils.getClient().getGameState() != GameState.LOADING) return WalkingCondition.State.EXIT_OUT_WALKER_FAIL;
        handleRun();
        handleEating();
        return WalkingCondition.State.CONTINUE_WALKER;
    };

    public void handleRun(){
        if (!PWalking.isRunEnabled() && PWalking.getRunEnergy() > nextRunAt){
            nextRunAt = PUtils.random(25, 65);
            PWalking.setRunEnabled(true);
            PUtils.sleepNormal(300, 1500, 500, 1200);
        }
    }

    public List<NPC> getValidTargets(){
        return PObjects.findAllNPCs(settings.getValidTargetFilter());
    }

    private synchronized Predicate<NPC> createValidTargetFilter(boolean ignoreDistance){
        Predicate<NPC> filter = (NPC n) -> {
            return (ignoreDistance || (n.getWorldLocation() != null && n.getWorldLocation().distanceToHypotenuse(settings.getSearchRadiusCenter()) <= settings.getSearchRadius()))
                    && Filters.NPCs.nameOrIdEquals(settings.getEnemiesToTarget()).test(n)
                    && (n.getInteracting() == null || n.getInteracting().equals(PPlayer.get()))
                    && !n.isDead();
        };

        if (config.enablePathfind()) filter = filter.and(this::isReachable);
        return filter;
    };

    private synchronized Predicate<PItem> createValidFoodFilter(){
        return Filters.Items.nameOrIdEquals(settings.getFoodsToEat());
    }

    private synchronized Predicate<PGroundItem> createValidLootFilter(){
        Predicate<PGroundItem> filter = Filters.GroundItems.nameContainsOrIdEquals(settings.getLootNames());
        if (settings.getLootGEValue() > 0) filter = filter.or(Filters.GroundItems.SlotPriceAtLeast(settings.getLootGEValue()));
        filter = filter.and(item -> item.getLocation().distanceToHypotenuse(settings.getSearchRadiusCenter()) <= (settings.getSearchRadius()+2));
        filter = filter.and(item -> lootItemsState.haveSpaceForItem(item));
        if (config.lootOwnKills()) filter = filter.and(item -> item.getLootType() == PGroundItem.LootType.PVM);
        filter = filter.and(item -> isReachable(item.getLocation()));
        return filter;
    }

    public Boolean isReachable(WorldPoint p){
        Reachable r = new Reachable();
        return r.canReach(new RSTile(p));
    }

    public Boolean isReachable(NPC n){
        Reachable r = new Reachable();
        return r.canReach(new RSTile(n.getWorldLocation()));
    }

    @Subscribe
    private synchronized void onConfigChanged(ConfigChanged event){
        if (!event.getGroup().equalsIgnoreCase("PFighterAIO")) return;
        readConfig();
    }

    @Subscribe
    private synchronized void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
    {
        if (!configButtonClicked.getGroup().equalsIgnoreCase("PFighterAIO")) return;
        if (PPlayer.get() == null && PUtils.getClient().getGameState() != GameState.LOGGED_IN) return;

        if (configButtonClicked.getKey().equalsIgnoreCase("startButton"))
        {
            Player player = PPlayer.get();
            try {
                super.start();
            } catch (Exception e){
                log.error(e.toString());
                e.printStackTrace();
            }
        } else if (configButtonClicked.getKey().equalsIgnoreCase("stopButton")){
            requestStop();
        } else if (configButtonClicked.getKey().equalsIgnoreCase("setFightAreaButton")) {
            PUtils.sendGameMessage("Fight area set to your position!");
            configManager.setConfiguration("PFighterAIO", "storedFightTile", PPlayer.location());
            settings.setSearchRadiusCenter(PPlayer.location());
            usingSavedFightTile = false;
        } else if (configButtonClicked.getKey().equalsIgnoreCase("setSafeSpotButton")) {
            PUtils.sendGameMessage("Safe spot set to your position!");
            configManager.setConfiguration("PFighterAIO", "storedSafeSpotTile", PPlayer.location());
            settings.setSafeSpot(PPlayer.location());
            usingSavedSafeSpot = false;
        } else if (configButtonClicked.getKey().equalsIgnoreCase("setCannonTileButton")){
            PUtils.sendGameMessage("Cannon tile set to your position!");
            configManager.setConfiguration("PFighterAIO", "storedCannonTile", PPlayer.location());
            settings.setCannonTile(PPlayer.location());
            usingSavedCannonTile = false;
        }
    }
}
