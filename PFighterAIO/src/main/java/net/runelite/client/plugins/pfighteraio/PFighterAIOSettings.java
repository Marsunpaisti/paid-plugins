package net.runelite.client.plugins.pfighteraio;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.ToString;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.paistisuite.api.PBreakScheduler;
import net.runelite.client.plugins.paistisuite.api.PUtils;
import net.runelite.client.plugins.paistisuite.api.types.PGroundItem;
import net.runelite.client.plugins.paistisuite.api.types.PItem;
import net.runelite.client.plugins.pfighteraio.states.*;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@ToString
public class PFighterAIOSettings {
    private ReentrantLock overlayVars = new ReentrantLock();

    public WorldPoint getSearchRadiusCenter(){
        synchronized (overlayVars){
            return this.searchRadiusCenter;
        }
    }

    public void setSearchRadiusCenter(WorldPoint val){
        synchronized (overlayVars){
            this.searchRadiusCenter = val;
        }
    }

    public WorldPoint getSafeSpot(){
        synchronized (overlayVars){
            return this.safeSpot;
        }
    }

    public void setSafeSpot(WorldPoint val){
        synchronized (overlayVars){
            this.safeSpot = val;
        }
    }

    public WorldPoint getCannonTile(){
        synchronized (overlayVars){
            return this.cannonTile;
        }
    }

    public void setCannonTile(WorldPoint val){
        synchronized (overlayVars){
            this.cannonTile = val;
        }
    }

    public int getSearchRadius(){
        synchronized (overlayVars){
            return this.searchRadius;
        }
    }

    public void setSearchRadius(int val){
        synchronized (overlayVars){
            this.searchRadius = val;
        }
    }

    public Instant getStartedTimestamp(){
        synchronized (overlayVars){
            return this.startedTimestamp;
        }
    }

    public void setStartedTimestamp(Instant val){
        synchronized (overlayVars){
            this.startedTimestamp = val;
        }
    }

    public Predicate<NPC> getValidTargetFilter(){
        synchronized (overlayVars){
            return validTargetFilter;
        }
    }

    public void setValidTargetFilter(Predicate<NPC> val){
        synchronized (overlayVars){
            validTargetFilter = val;
        }
    }

    public String getCurrentStateName(){
        synchronized (overlayVars){
            return this.currentStateName;
        }
    }

    public void setCurrentStateName(String val){
        synchronized (overlayVars){
            this.currentStateName = val;
        }
    }


    private Instant startedTimestamp;
    private String currentStateName;
    private int searchRadius;
    private WorldPoint safeSpot;
    private WorldPoint searchRadiusCenter;
    private WorldPoint cannonTile;
    private Predicate<NPC> validTargetFilter;

    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean enablePathfind;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int minEatHp;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int maxEatHp;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int reservedInventorySlots;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private WorldPoint bankTile;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private String[] enemiesToTarget;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private String[] foodsToEat;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private String[] lootNames;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int lootGEValue;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private Predicate<NPC> validTargetFilterWithoutDistance;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private Predicate<PGroundItem> validLootFilter;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private Predicate<PItem> validFoodFilter;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean stopWhenOutOfFood;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean eatFoodForLoot;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean safeSpotForCombat;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean safeSpotForLogout;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean forceLoot;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean bankingEnabled;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean bankForFood;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean bankForLoot;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean teleportWhileBanking;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean usePotions;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int minPotionBoost;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int maxPotionBoost;
    @ToString.Exclude
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private String apiKey;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private List<BankingState.WithdrawItem> itemsToWithdraw;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private long lastAntiAfk = System.currentTimeMillis();
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private long antiAfkDelay = PUtils.randomNormal(120000, 270000);
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private PBreakScheduler breakScheduler = null;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean safeSpotForBreaks;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean enableBreaks;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean useCannon;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int cannonBallsLeft;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean cannonPlaced;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean cannonFinished;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private WorldPoint currentCannonPos;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int currentCannonWorld;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean normalQuickPrayers;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean flickQuickPrayers;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean assistFlickPrayers;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean drinkPrayerPotions;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int minPrayerPotPoints;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int maxPrayerPotPoints;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean enableAlching;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int alchMinHAValue;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int alchMaxPriceDifference;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int breakMinIntervalMinutes;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int breakMaxIntervalMinutes;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int breakMinDurationSeconds;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int breakMaxDurationSeconds;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean worldhopIfTooManyPlayers;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean worldhopIfPlayerTalks;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean worldhopInSafespot;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean slayerTaskCompleted;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean bankForSlayerTask;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean bankForPrayerPots;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private boolean useSlayerItems;
    @Getter(onMethod_={@Synchronized}) @Setter(onMethod_={@Synchronized})
    private int worldhopPlayerLimit;

}
