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
import net.runelite.client.plugins.pfighteraio.PFighterAIOSettings;

@Slf4j
public class TakeBreaksState extends State {
    private boolean currentlyTakingBreak;
    public TakeBreaksState(PFighterAIO plugin, PFighterAIOSettings settings) {
        super(plugin, settings);
        this.currentlyTakingBreak = false;
    }

    @Override
    public boolean condition() {
        if (plugin.breakScheduler == null || !settings.isEnableBreaks() || plugin.fightEnemiesState.inCombat()) return false;
        return this.currentlyTakingBreak || plugin.breakScheduler.shouldTakeBreak();
    }

    public boolean isCurrentlyTakingBreak(){
        return this.currentlyTakingBreak;
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

        if (this.currentlyTakingBreak && settings.isSafeSpotForBreaks() && settings.getSafeSpot() != null && PPlayer.distanceTo(settings.getSafeSpot()) < 40 && PPlayer.distanceTo(settings.getSafeSpot()) > 0 && !PPlayer.isMoving()){
            if (Reachable.getMap().canReach(settings.getSafeSpot())){
                PWalking.sceneWalk(settings.getSafeSpot());
                PUtils.sleepNormal(1200, 1900);
            } else {
                DaxWalker.getInstance().allowTeleports = false;
                DaxWalker.walkTo(new RSTile(settings.getSafeSpot()));
            }
        }
    }
}
