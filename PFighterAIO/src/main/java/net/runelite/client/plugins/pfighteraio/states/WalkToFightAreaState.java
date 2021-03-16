package net.runelite.client.plugins.pfighteraio.states;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.paistisuite.api.PPlayer;
import net.runelite.client.plugins.paistisuite.api.PUtils;
import net.runelite.client.plugins.paistisuite.api.PWalking;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.DaxWalker;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.RSTile;
import net.runelite.client.plugins.pfighteraio.PFighterAIO;
import net.runelite.client.plugins.pfighteraio.PFighterAIOSettings;

@Slf4j
public class WalkToFightAreaState extends State {
    public WalkToFightAreaState(PFighterAIO plugin, PFighterAIOSettings settings) {
        super(plugin, settings);
    }

    @Override
    public boolean condition() {
        return !settings.getSearchRadiusCenter().isInScene(PUtils.getClient())
                || settings.getSearchRadiusCenter().distanceTo(PPlayer.location()) > settings.getSearchRadius();
    }

    @Override
    public String getName() {
        return "Walk to fight area";
    }

    @Override
    public void loop(){
        super.loop();
        WorldPoint target = (settings.isSafeSpotForCombat() && settings.getSafeSpot() != null) ? settings.getSafeSpot() : settings.getSearchRadiusCenter();

        DaxWalker.getInstance().allowTeleports = settings.isTeleportWhileBanking();
        if (!PPlayer.isMoving()) {
            if (target.isInScene(PUtils.getClient()) && plugin.isReachable(target) && PWalking.sceneWalk(target)){
                PUtils.sleepNormal(650, 1500);
            } else if (!DaxWalker.walkTo(new RSTile(target), plugin.walkingCondition)) {
                log.info("Unable to walk to fight area!");
                PUtils.sendGameMessage("Unable to walk to fight area!");
                PUtils.sleepNormal(650, 1500);
                plugin.requestStop();
            }
        }
    }
}
