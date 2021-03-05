package net.runelite.client.plugins.pfighteraio.states;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.paistisuite.api.*;
import net.runelite.client.plugins.paistisuite.api.WebWalker.api_lib.DaxWalker;
import net.runelite.client.plugins.pfighteraio.PFighterAIO;

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

    public BankingState(PFighterAIO plugin) {
        super(plugin);
        bankingFailure = false;
    }

    public boolean requestBanking(){
        if (!plugin.bankingEnabled) return false;
        shouldBank = true;
        return true;
    }

    @Override
    public boolean condition() {
        if (!plugin.bankingEnabled) return false;

        // No food -> go bank
        if (plugin.bankForFood && PInventory.findAllItems(plugin.validFoodFilter).size() == 0){
            shouldBank = true;
        }

        // No inventory space & no food to eat for space -> go bank
        if (plugin.bankForLoot && PInventory.getEmptySlots() <= plugin.reservedInventorySlots && (!plugin.eatFoodForLoot || PInventory.findItem(plugin.validFoodFilter) == null)){
            shouldBank = true;
        }

        return shouldBank;
    }

    @Override
    public String getName() {
        return "Banking";
    }

    @Override
    public void loop(){
        super.loop();
        if (attempts >= maxAttempts) {
            bankingFailure = true;
        }

        if (PBanking.openBank()){
             if (!PUtils.waitCondition(PUtils.random(14000, 16000), PBanking::isBankOpen)){
                 attempts++;
                 return;
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

        DaxWalker.getInstance().allowTeleports = plugin.teleportWhileBanking;
        if (!DaxWalker.walkToBank(plugin.walkingCondition)) {
            attempts++;
            return;
        } else {
            PUtils.waitCondition(PUtils.random(12000, 18000), () -> !PPlayer.isMoving());
            attempts = 0;
            return;
        }
    }

    public boolean withdrawDesiredInventory(){
        for (WithdrawItem i : plugin.itemsToWithdraw){
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
