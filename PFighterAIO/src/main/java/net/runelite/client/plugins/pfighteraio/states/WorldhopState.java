package net.runelite.client.plugins.pfighteraio.states;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.api.queries.PlayerQuery;
import net.runelite.client.plugins.paistisuite.api.*;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.DaxWalker;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.local_pathfinding.Reachable;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.RSTile;
import net.runelite.client.plugins.paistisuite.api.types.Filters;
import net.runelite.client.plugins.paistisuite.api.types.PItem;
import net.runelite.client.plugins.pfighteraio.PFighterAIO;

import java.util.function.Predicate;

@Slf4j
public class WorldhopState extends State {
    public boolean isWorldhopRequested;
    private int attempts = 0;
    private int maxAttempts = 8;
    private boolean worldHopFailure = false;

    public WorldhopState(PFighterAIO plugin) {
        super(plugin);
        isWorldhopRequested = false;
    }

    public String getName(){
        return "World hopping";
    }

    public synchronized void setWorldhopRequested(boolean val){
        this.isWorldhopRequested = val;
    }

    public synchronized boolean isWorldhopRequested(){
        return this.isWorldhopRequested;
    }

    @Override
    public boolean condition() {
        if (!plugin.worldhopIfTooManyPlayers && !plugin.worldhopIfPlayerTalks) return false;
        if (plugin.fightEnemiesState.inCombat()) return false;
        if (worldHopFailure) return false;

        if (plugin.worldhopIfTooManyPlayers){
            Integer playerCount = PUtils.clientOnly(() -> new PlayerQuery().isWithinDistance(PPlayer.getWorldLocation(), 30).result(PUtils.getClient()).size(), "getPlayerCount");
            if (playerCount != null && playerCount >= plugin.worldhopPlayerLimit + 1){
                isWorldhopRequested = true;
                return true;
            }
        }
        return isWorldhopRequested();
    }

    @Override
    public void loop() {
        super.loop();
        if (attempts >= maxAttempts) {
            PUtils.sendGameMessage("World hopping failure. Too many attempts.");
            worldHopFailure = true;
            setWorldhopRequested(false);
            return;
        }
        if (!PPlayer.isMoving()
                && plugin.worldhopInSafespot
                && plugin.safeSpot != null
                && PPlayer.getWorldLocation().distanceTo2D(plugin.safeSpot) > 0
                && PPlayer.getWorldLocation().distanceTo2D(plugin.safeSpot) < 45) {
            if (Reachable.getMap().canReach(plugin.safeSpot)) {
                PWalking.sceneWalk(plugin.safeSpot);
                PUtils.sleepNormal(200, 400);
                PUtils.waitCondition(1800, () -> PPlayer.isMoving());
                attempts++;
            } else {
                attempts++;
                DaxWalker.getInstance().allowTeleports = false;
                DaxWalker.walkTo(new RSTile(plugin.safeSpot), plugin.walkingCondition);
            }
            return;
        }

        if (plugin.worldhopInSafespot && plugin.safeSpot != null && PPlayer.getWorldLocation().distanceTo2D(plugin.safeSpot) > 0 && PPlayer.getWorldLocation().distanceTo2D(plugin.safeSpot) < 45) return;

        attempts++;
        if (PWorldHopper.hop()){
            attempts = 0;
            setWorldhopRequested(false);
            return;
        }
    }
}
