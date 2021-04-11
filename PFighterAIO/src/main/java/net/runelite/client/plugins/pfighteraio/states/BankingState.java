package net.runelite.client.plugins.pfighteraio.states;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.paistisuite.api.*;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.DaxWalker;
import net.runelite.client.plugins.paistisuite.api.WebWalker.wrappers.RSTile;
import net.runelite.client.plugins.paistisuite.api.types.Filters;
import net.runelite.client.plugins.paistisuite.api.types.PItem;
import net.runelite.client.plugins.pfighteraio.PFighterAIO;
import net.runelite.client.plugins.pfighteraio.PFighterAIOSettings;

@Slf4j
public class BankingState extends State {
    private boolean shouldBank = false;
    private int attempts = 0;
    private int maxAttempts = 5;
    public boolean bankingFailure;

    @Value
    @AllArgsConstructor
    public static class WithdrawItem {
        String nameOrId;
        int quantity;
    }

    public BankingState(PFighterAIO plugin, PFighterAIOSettings settings) {
        super(plugin, settings);
        bankingFailure = false;
    }

    public boolean requestBanking(){
        if (!settings.isBankingEnabled()) return false;
        shouldBank = true;
        return true;
    }

    @Override
    public boolean condition() {
        if (!settings.isBankingEnabled() || bankingFailure) return false;

        // No food -> go bank
        if (settings.isBankForFood() && PInventory.findAllItems(settings.getValidFoodFilter()).size() == 0){
            shouldBank = true;
        }

        // No prayer pots -> go bank
        if (settings.isBankForPrayerPots() && PInventory.findAllItems(Filters.Items.nameContains("Prayer potion")).size() == 0){
            shouldBank = true;
        }

        // No inventory space & no food to eat for space -> go bank
        if (settings.isBankForLoot() && PInventory.getEmptySlots() <= settings.getReservedInventorySlots() && (!settings.isEatFoodForLoot() || PInventory.findItem(settings.getValidFoodFilter()) == null)){
            shouldBank = true;
        }

        // Slayer task done
        if (settings.isSlayerTaskCompleted() && settings.isBankForSlayerTask()){
            shouldBank = true;
        }

        return shouldBank;
    }

    @Override
    public String getName() {
        return "Banking";
    }

    @Override
    public void loop() {
        super.loop();
        if (attempts >= maxAttempts) {
            bankingFailure = true;
        }

        if (settings.isUseCannon() && settings.isCannonPlaced()){
            plugin.setupCannonState.pickupCannon();
            return;
        }

        if (PBanking.openBank()) {
            if (!PUtils.waitCondition(PUtils.random(14000, 16000), PBanking::isBankOpen)) {
                walkToBank();
                return;
            }
            PUtils.sleepNormal(100, 600, 33, 200);

            PItem herbSack = PInventory.findItem(Filters.Items.nameContains("herb sack"));
            if (herbSack != null){
                PInteraction.item(herbSack, "Empty");
                PUtils.sleepNormal(400, 1000, 100, 600);
            }

            PItem lootingBag = PInventory.findItem(Filters.Items.nameContains("looting bag"));
            if (lootingBag != null){
                PInteraction.item(lootingBag, "Empty");
                PUtils.sleepNormal(400, 1000, 100, 600);
            }

            PBanking.depositInventory();
            PUtils.sleepNormal(500, 1500, 100, 800);
            if (!withdrawDesiredInventory()) {
                bankingFailure = true;
                return;
            }
            attempts = 0;
            shouldBank = false;
            return;
        }

        walkToBank();
    }

    public void walkToBank(){
        DaxWalker.getInstance().allowTeleports = settings.isTeleportWhileBanking();
        if (settings.getBankTile() == null && !DaxWalker.walkToBank(plugin.walkingCondition)) {
            attempts++;
        } else if (settings.getBankTile() != null && !DaxWalker.walkTo(new RSTile(settings.getBankTile()), plugin.walkingCondition)) {
            attempts++;
        } else {
            PUtils.waitCondition(PUtils.random(12000, 18000), () -> !PPlayer.isMoving());
            if (settings.isBankForSlayerTask() && settings.isSlayerTaskCompleted()){
                plugin.requestStop();
                return;
            }
        }
    }

    public boolean withdrawDesiredInventory(){
        for (WithdrawItem i : settings.getItemsToWithdraw()){
            if (!PBanking.withdrawItem(i.nameOrId, i.quantity)) {
                PUtils.sendGameMessage("Unable to withdraw item: " + i.nameOrId);
                return false;
            } else {
                PUtils.sleepNormal(300, 1000, 100, 600);
            }
        }
        return true;
    }
}
