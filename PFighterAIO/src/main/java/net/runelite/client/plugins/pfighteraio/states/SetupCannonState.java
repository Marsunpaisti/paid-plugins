package net.runelite.client.plugins.pfighteraio.states;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.paistisuite.api.*;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.DaxWalker;
import net.runelite.client.plugins.paistisuite.api.WebWalker.walker_engine.local_pathfinding.Reachable;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.RSTile;
import net.runelite.client.plugins.paistisuite.api.types.Filters;
import net.runelite.client.plugins.paistisuite.api.types.PItem;
import net.runelite.client.plugins.paistisuite.api.types.PTileObject;
import net.runelite.client.plugins.pfighteraio.PFighterAIO;

import java.util.List;

@Slf4j
public class SetupCannonState extends State {
    public int nextCannonReload = PUtils.random(5, 15);
    public int attempts = 0;
    public int maxAttempts = 7;
    public boolean cannoningFailure = false;

    public SetupCannonState(PFighterAIO plugin) {
        super(plugin);
    }

    @Override
    public boolean condition() {
        if (!plugin.useCannon) return false;
        if (cannoningFailure && !plugin.isCannonPlaced()) return false;
        if (cannoningFailure && plugin.isCannonPlaced()) return true;
        if (PPlayer.distanceTo(plugin.searchRadiusCenter) > plugin.searchRadius + 7) return false;
        if (plugin.cannonTile == null) return false;
        if (plugin.searchRadiusCenter != null){
            if (plugin.cannonTile.distanceTo(plugin.searchRadiusCenter) > plugin.searchRadius + 30) {
                // Dont try to use cannon if the tile is set too far
                return false;
            }
        }

        // Place cannon
        if (!plugin.isCannonPlaced()) {
            boolean haveBalls = false;
            boolean haveBarrels = false;
            boolean haveStand = false;
            boolean haveFurnace = false;
            boolean haveBase = false;
            List<PItem> items = PInventory.getAllItems();
            for (PItem i : items){
                if (i.getName().equalsIgnoreCase("Cannonball")) {
                    haveBalls = true;
                } else if (i.getName().equalsIgnoreCase("Cannon base")) {
                    haveBase = true;
                } else if (i.getName().equalsIgnoreCase("Cannon barrels")) {
                    haveBarrels = true;
                } else if (i.getName().equalsIgnoreCase("Cannon stand")) {
                    haveStand = true;
                } else if (i.getName().equalsIgnoreCase("Cannon furnace")) {
                    haveFurnace = true;
                }
            }
            return haveBalls && haveBase && haveBarrels && haveStand && haveFurnace;
        }

        // Pickup cannon or reload
        if (plugin.isCannonPlaced()) {
            if (PInventory.getCount("Cannonball") > 0 && (plugin.getCannonBallsLeft() <= nextCannonReload || isCannonBroken())){
                return true;
            } else if (PInventory.getCount("Cannonball") == 0 && plugin.getCannonBallsLeft() <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "Handling cannon";
    }

    @Override
    public void loop() {
        super.loop();
        if (attempts >= maxAttempts) {
            cannoningFailure = true;
            return;
        }
        int cannonBalls = PInventory.getCount("Cannonball");
        if (!plugin.isCannonPlaced() && cannonBalls > 0) {
            log.info("Setting up cannon");
            setupCannon();
            return;
        }

        if (plugin.isCannonPlaced() && cannonBalls <= 0){
            log.info("Picking up cannon");
            pickupCannon();
            return;
        }

        if (plugin.isCannonPlaced() && plugin.isCannonFinished() && cannonBalls > 0 && (plugin.getCannonBallsLeft() <= nextCannonReload || isCannonBroken())) {
            log.info("Reloading cannon");
            reloadCannon();
            return;
        }
    }

    public void reloadCannon(){
        if (!plugin.isCannonPlaced()) return;

        if (!canReachCannon()){
            DaxWalker.getInstance().allowTeleports = false;
            attempts++;
            if (!DaxWalker.walkTo(new RSTile(plugin.cannonTile.dx(1)), plugin.walkingCondition)
                    && !DaxWalker.walkTo(new RSTile(plugin.cannonTile.dx(-1)), plugin.walkingCondition)
                    && !DaxWalker.walkTo(new RSTile(plugin.cannonTile.dy(1)), plugin.walkingCondition)
                    && !DaxWalker.walkTo(new RSTile(plugin.cannonTile.dy(-1)), plugin.walkingCondition)) {
                cannoningFailure = true;
                return;
            }
        }

        if (canReachCannon()){
            PTileObject cannon = plugin.getCannon();
            int ballsBefore = plugin.getCannonBallsLeft();
            attempts++;
            if (PInteraction.tileObject(cannon, "Fire", "Repair") && PUtils.waitCondition(PUtils.random(5000, 7000), () -> plugin.getCannonBallsLeft() > ballsBefore)){
                attempts = 0;
                nextCannonReload = PUtils.random(5, 15);
                return;
            } else {
                log.info("Failed reloading cannon");
                PUtils.sleepNormal(300, 700);
            }
        }
    }

    public void setupCannon(){
        if (plugin.isCannonPlaced()) return;
        if (plugin.cannonTile.distanceTo(PPlayer.getWorldLocation()) > 0 && !PPlayer.isMoving()) {
            if (plugin.isReachable(plugin.cannonTile)) {
                PWalking.sceneWalk(plugin.cannonTile);
                attempts++;
                PUtils.sleepNormal(700, 900);
                PUtils.waitCondition(PUtils.random(2000, 3000), () -> !PPlayer.isMoving());
            } else {
                DaxWalker.getInstance().allowTeleports = false;
                DaxWalker.walkTo(new RSTile(plugin.cannonTile), plugin.walkingCondition);
                attempts++;
            }
            return;
        }

        if (plugin.cannonTile.distanceTo(PPlayer.getWorldLocation()) == 0){
            attempts++;
            PItem cannonbase = PInventory.findItem(Filters.Items.nameEquals("Cannon base"));
            if (PInteraction.item(cannonbase, "Set-up")) {
                if (PUtils.waitCondition(PUtils.random(6000, 8000), () -> plugin.isCannonPlaced() && plugin.isCannonFinished())){
                    PUtils.sleepNormal(650, 1000);
                    PTileObject cannon = plugin.getCannon();
                    String[] actions = cannon.getDef().getActions();
                    PInteraction.tileObject(cannon, "Fire");
                    PUtils.sleepNormal(700, 1300);
                    attempts = 0;
                } else {
                    log.info("Timed out when waiting for cannon to appear");
                }
            } else {
                log.info("Failed setting up cannon base");
                PUtils.sleepNormal(300, 800);
            }
        }
    }

    public boolean isCannonBroken() {
        PTileObject cannon = plugin.getCannon();
        return cannon != null && cannon.getDef().getId() == 5;
    }

    public boolean canReachCannon(){
        return plugin.isReachable(plugin.getCurrentCannonPos().dy(1))
                || plugin.isReachable(plugin.getCurrentCannonPos().dy(-1))
                || plugin.isReachable(plugin.getCurrentCannonPos().dx(1))
                || plugin.isReachable(plugin.getCurrentCannonPos().dx(-1));
    }

    public void pickupCannon(){
        if (!plugin.isCannonPlaced()) return;

        if (!canReachCannon()){
            attempts++;
            DaxWalker.getInstance().allowTeleports = false;
            if (!DaxWalker.walkTo(new RSTile(plugin.cannonTile.dx(1)), plugin.walkingCondition)
                    && !DaxWalker.walkTo(new RSTile(plugin.cannonTile.dx(-1)), plugin.walkingCondition)
                    && !DaxWalker.walkTo(new RSTile(plugin.cannonTile.dy(1)), plugin.walkingCondition)
                    && !DaxWalker.walkTo(new RSTile(plugin.cannonTile.dy(-1)), plugin.walkingCondition)) {
                cannoningFailure = true;
                return;
            }
        }

        if (canReachCannon()){
            attempts++;
            PTileObject cannon = plugin.getCannon();
            PInteraction.tileObject(cannon, "Pick-up");
            if (PUtils.waitCondition(PUtils.random(5000, 7000), () -> !plugin.isCannonPlaced())){
                attempts = 0;
                return;
            }
        }
    }
}
