package net.runelite.client.plugins.pfighteraio.states;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.util.Text;
import net.runelite.client.game.NPCManager;
import net.runelite.client.plugins.paistisuite.PaistiSuite;
import net.runelite.client.plugins.paistisuite.api.*;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.local_pathfinding.Reachable;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.RSTile;
import net.runelite.client.plugins.paistisuite.api.types.Filters;
import net.runelite.client.plugins.paistisuite.api.types.PItem;
import net.runelite.client.plugins.pfighteraio.PFighterAIO;
import net.runelite.client.plugins.pfighteraio.PFighterAIOSettings;
import net.runelite.http.api.hiscore.HiscoreResult;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

@Slf4j
public class FightEnemiesState extends State {
    public static Map<String, String> slayerItems = Map.ofEntries(
            entry("Lizard", "Ice cooler"),
            entry("Desert Lizard", "Ice cooler"),
            entry("Small Lizard", "Ice cooler"),
            entry("Rockslug", "Bag of salt"),
            entry("Giant rockslug", "Bag of salt")

    );
    public FightEnemiesState(PFighterAIO plugin, PFighterAIOSettings settings){
        super(plugin, settings);
    }
    public long targetClickedTimestamp = System.currentTimeMillis();
    public NPC lastTarget;
    public boolean usedSlayerItem = false;

    @Override
    public String getName() {
        return "Fight enemies";
    }

    @Override
    public void loop() {
        super.loop();

        // Slayer items
        NPC slayerItemTarget = getCurrentTarget() != null ? getCurrentTarget() : lastTarget;
        if (!usedSlayerItem && slayerItemTarget != null && settings.isUseSlayerItems() && slayerItems.getOrDefault((slayerItemTarget.getName() != null ? slayerItemTarget.getName() : ""), null) != null){
            int maxHp = getMaxHp(slayerItemTarget);
            int exactHp = getExactHp(slayerItemTarget.getHealthRatio(), slayerItemTarget.getHealthScale(), maxHp);
            if ((exactHp <= 3 && exactHp != -1) || slayerItemTarget.isDead()){
                log.info("Using slayer item");
                PUtils.sleepNormal(200, 1500, 100, 350);
                useSlayerItemOnTarget(slayerItemTarget);
                return;
            }
        }

        // In combat and outside safespot
        if (inCombat() && settings.isSafeSpotForCombat() && PPlayer.location().distanceTo(settings.getSafeSpot()) > 0 && PPlayer.location().distanceTo2D(settings.getSafeSpot()) < 40) {
            PWalking.sceneWalk(settings.getSafeSpot());
            PUtils.waitCondition(PUtils.random(700, 1300), () -> PPlayer.isMoving());
            PUtils.waitCondition(PUtils.random(4000, 6000), () -> !PPlayer.isMoving());
            PUtils.sleepNormal(100, 400);
            // Attack target again after moving to safespot
            if (!attackLastTarget()) attackNewTarget();
            return;
        }

        // No combat and no target
        if (!inCombat() && !isInteracting() ){
            log.info("No combat - Trying to attack new target");
            PUtils.sleepNormal(500, 3500, 300, 800);
            if (plugin.isStopRequested()) return;
            if (inCombat() || isInteracting()) return;
            attackNewTarget();

            // Run to safespot after attack animation starts to play
            if (settings.isSafeSpotForCombat() && PPlayer.location().distanceTo(settings.getSafeSpot()) > 0) {
                PUtils.waitCondition(PUtils.random(2000, 3000), () -> PPlayer.get().getAnimation() != -1);
                if (PPlayer.location().distanceTo(settings.getSafeSpot()) == 0) return;
                PUtils.sleepNormal(100, 800, 150, 200);
                PWalking.sceneWalk(settings.getSafeSpot());
                PUtils.waitCondition(PUtils.random(700, 1000), () -> PPlayer.isMoving());
                PUtils.waitCondition(PUtils.random(3000, 4500), () -> !PPlayer.isMoving());
                PUtils.sleepNormal(100, 800, 150, 200);
                // Attack target again after moving to safespot
                if (!attackLastTarget()) attackNewTarget();
            }
            return;
        }

        // No combat and trying to target timeout check
        if (!inCombat() && isInteracting()){
            if (!isCurrentTargetValid() || (System.currentTimeMillis() - targetClickedTimestamp >= 3000 && !PPlayer.isMoving())){
                log.info("Stuck trying to target enemy - Trying to attack new target");
                PUtils.sleepNormal(300, 1500, 250, 400);
                if (plugin.isStopRequested()) return;
                attackNewTarget();
            }

            return;
        }

        // Current target is not suitable anymore
        if (getCurrentTarget() != null && !isCurrentTargetValid() && !getCurrentTarget().isDead()){
            log.info("Current target not valid - Trying to attack new target");
            PUtils.sleepNormal(300, 1500, 250, 400);
            if (plugin.isStopRequested()) return;
            attackNewTarget();
        }
    }

    public void useSlayerItemOnTarget(NPC target){
        if (target == null) return;
        String itemName = slayerItems.getOrDefault(target.getName(), null);
        if (itemName != null){
            log.info("Trying to use slayer item on target");
            PItem slayerItem = PInventory.findItem(Filters.Items.nameEquals(itemName));
            if (slayerItem != null){
                log.info("Found slayer item. Using " + slayerItem.getName() + " on " + target.getName());
                PInteraction.useItemOnNpc(slayerItem, target);
                usedSlayerItem = true;
                PUtils.sleepNormal(650, 1500, 150, 800);
            } else {
                log.info("No slayer item found for target");
            }
        }
    }

    public boolean attackNewTarget(){
        NPC target = getNewTarget();
        if (PInteraction.npc(target, "Attack")) {
            targetClickedTimestamp = System.currentTimeMillis();
            lastTarget = target;
            usedSlayerItem = false;
            return PUtils.waitCondition(PUtils.random(700, 1300), this::isInteracting);
        }

        return false;
    }

    public boolean attackLastTarget(){
        if (lastTarget == null || !settings.getValidTargetFilter().test(lastTarget)) return false;
        if (PInteraction.npc(lastTarget, "Attack")) {
            targetClickedTimestamp = System.currentTimeMillis();
            usedSlayerItem = false;
            return PUtils.waitCondition(PUtils.random(700, 1300), this::isInteracting);
        }
        return false;
    }

    public boolean isCurrentTargetValid(){
        NPC interacting = (NPC)PPlayer.get().getInteracting();
        if (interacting != null){
            return settings.getValidTargetFilterWithoutDistance().test(interacting);
        }

        return false;
    }

    public NPC getNewTarget(){
        List<NPC> targets = plugin.getValidTargets();
        if (targets.size() < 1) return null;
        targets.sort(targetPrioritySorter);
        if (targets.size() >= 2 && PUtils.random(1,8) <= 1 && !(targets.get(0).getInteracting() != null && targets.get(0).getInteracting().equals(PPlayer.get()))) {
            return targets.get(1);
        }
        return targets.get(0);
    }

    public int pathFindDistanceTo(WorldPoint p){
        Reachable r = new Reachable();
        return r.getDistance(new RSTile(p));
    }

    private double distanceTo(NPC n){
        return PPlayer.distanceTo(n);
    }

    public Comparator<NPC> targetPrioritySorter = (a, b) -> {
        boolean aTargetingUs = a.getInteracting() != null && a.getInteracting().equals(PPlayer.get());
        boolean bTargetingUs = b.getInteracting() != null && b.getInteracting().equals(PPlayer.get());
        if (aTargetingUs && !bTargetingUs) return -1;
        if (bTargetingUs && !aTargetingUs) return 1;
        if (settings.isEnablePathfind()) {
            return pathFindDistanceTo(a.getWorldLocation()) - pathFindDistanceTo(b.getWorldLocation());
        } else {
            return (int)Math.round(distanceTo(a)) - (int)Math.round(distanceTo(b));
        }
    };

    public boolean inCombat(){
        NPC npc = (NPC)PPlayer.get().getInteracting();
        if (npc == null) return false;
        if (npc.getInteracting() != null && npc.getInteracting().equals(PPlayer.get())) return true;
        return false;
    }

    public NPC getCurrentTarget(){
        NPC npc = (NPC)PPlayer.get().getInteracting();
        return npc;
    }

    public boolean isInteracting(){
        NPC npc = (NPC)PPlayer.get().getInteracting();
        return npc != null;
    }

    int getMaxHp(Actor actor)
    {
        if (actor instanceof NPC)
        {
            return PaistiSuite.getInstance().npcManager.getHealth(((NPC) actor).getId());
        }

        return -1;
    }

    static int getExactHp(int ratio, int health, int maxHp)
    {
        if (ratio < 0 || health <= 0 || maxHp == -1)
        {
            return -1;
        }

        int exactHealth = 0;

        // This is the reverse of the calculation of healthRatio done by the server
        // which is: healthRatio = 1 + (healthScale - 1) * health / maxHealth (if health > 0, 0 otherwise)
        // It's able to recover the exact health if maxHealth <= healthScale.
        if (ratio > 0)
        {
            int minHealth = 1;
            int maxHealth;
            if (health > 1)
            {
                if (ratio > 1)
                {
                    // This doesn't apply if healthRatio = 1, because of the special case in the server calculation that
                    // health = 0 forces healthRatio = 0 instead of the expected healthRatio = 1
                    minHealth = (maxHp * (ratio - 1) + health - 2) / (health - 1);
                }
                maxHealth = (maxHp * ratio - 1) / (health - 1);
                if (maxHealth > maxHp)
                {
                    maxHealth = maxHp;
                }
            }
            else
            {
                // If healthScale is 1, healthRatio will always be 1 unless health = 0
                // so we know nothing about the upper limit except that it can't be higher than maxHealth
                maxHealth = maxHp;
            }
            // Take the average of min and max possible healths
            exactHealth = (minHealth + maxHealth + 1) / 2;
        }

        return exactHealth;
    }

    @Override
    public boolean condition() {
        return getNewTarget() != null || inCombat();
    }
}
