package net.runelite.client.plugins.pfighteraio.states;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.paistisuite.api.*;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.DaxWalker;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.local_pathfinding.Reachable;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.RSTile;
import net.runelite.client.plugins.pfighteraio.PFighterAIO;

@Slf4j
public class TakeBreaksState extends State {
    private boolean currentlyTakingBreak;
    public TakeBreaksState(PFighterAIO plugin) {
        super(plugin);
        this.currentlyTakingBreak = false;
    }

    @Override
    public boolean condition() {
        if (plugin.breakScheduler == null || !plugin.enableBreaks || plugin.fightEnemiesState.inCombat()) return false;
        return this.currentlyTakingBreak || plugin.breakScheduler.shouldTakeBreak();
    }

    @Override
    public String getName() {
        return "Taking a break";
    }

    @Override
    public void loop(){
        super.loop();


        if (!currentlyTakingBreak && plugin.breakScheduler.shouldTakeBreak()){
            plugin.breakScheduler.startBreak();
            log.info("Taking a break for " + plugin.breakScheduler.getCurrentBreakDuration() + " seconds.");
            PUtils.sendGameMessage("Taking a break for " + plugin.breakScheduler.getCurrentBreakDuration() + " seconds.");
            this.currentlyTakingBreak = true;
            return;
        }

        if (this.currentlyTakingBreak && plugin.breakScheduler.shouldEndBreak()){
            plugin.breakScheduler.endBreak();
            log.info("Next break in " + plugin.breakScheduler.getTimeUntiNextBreak() + " seconds.");
            PUtils.sendGameMessage("Next break in " + plugin.breakScheduler.getTimeUntiNextBreak() + " seconds.");

            this.currentlyTakingBreak = false;
            return;
        }

        if (this.currentlyTakingBreak && plugin.safeSpotForBreaks && plugin.safeSpot != null && PPlayer.distanceTo(plugin.safeSpot) < 40 && PPlayer.distanceTo(plugin.safeSpot) > 0 && !PPlayer.isMoving()){
            if (Reachable.getMap().canReach(plugin.safeSpot)){
                PWalking.sceneWalk(plugin.safeSpot);
            } else {
                DaxWalker.getInstance().allowTeleports = false;
                DaxWalker.walkTo(new RSTile(plugin.safeSpot));
            }
        }
    }
}
