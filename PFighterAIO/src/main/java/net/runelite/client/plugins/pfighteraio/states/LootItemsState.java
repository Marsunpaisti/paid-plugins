package net.runelite.client.plugins.pfighteraio.states;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.client.plugins.paistisuite.api.*;
import net.runelite.client.plugins.paistisuite.api.WebWalker.shared.helpers.magic.RuneElement;
import net.runelite.client.plugins.paistisuite.api.types.Filters;
import net.runelite.client.plugins.paistisuite.api.types.PGroundItem;
import net.runelite.client.plugins.paistisuite.api.types.PItem;
import net.runelite.client.plugins.paistisuite.api.types.Spells;
import net.runelite.client.plugins.pfighteraio.PFighterAIO;
import net.runelite.client.plugins.pfighteraio.PFighterAIOSettings;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class LootItemsState extends State {
    public LootItemsState(PFighterAIO plugin, PFighterAIOSettings settings) {
        super(plugin, settings);
    }

    @Override
    public boolean condition() {
        if (settings.isForceLoot()) {
            List<PGroundItem> loot = getLootableItems();
            if (loot != null && loot.size() > 0){
                boolean force = loot.stream()
                        .anyMatch(item -> item.getSpawnTime() != null && Duration.between(item.getSpawnTime(), Instant.now()).getSeconds() >= 40);

                if (force && haveSpaceForItem(loot.get(0))) {
                    return true;
                }
            }
        }
        if (plugin.fightEnemiesState.inCombat()) {
            return false;
        }
        if (plugin.fightEnemiesState.getCurrentTarget() != null) {
            return false;
        }

        PGroundItem nextLoot = getNextLootableItem();
        return nextLoot != null;
    }

    @Override
    public String getName() {
        return "Loot items";
    }

    @Override
    public void loop(){
        super.loop();

        PGroundItem target = getNextLootableItem();
        if (target != null){

            // Eat food to make space if necessary
            if (PInventory.getEmptySlots() <= settings.getReservedInventorySlots() && settings.isEatFoodForLoot()){
                List<PItem> foodItems = PInventory.findAllItems(settings.getValidFoodFilter());
                int quantityBefore = foodItems.size();
                if (quantityBefore == 0) return;
                if (PInteraction.item(foodItems.get(0), "Eat")){
                    log.info("Eating food to make space for loot");
                    PUtils.waitCondition(PUtils.random(700, 1300), () -> PInventory.findAllItems(settings.getValidFoodFilter()).size() < quantityBefore);
                }
            }
            PUtils.sleepNormal(100, 300);
            log.info("Looting " + target.getName() + " price: " + target.getPricePerSlot());
            if (plugin.isStopRequested()) return;
            int countBefore = PInventory.getCount(target.getName());
            if (PInteraction.groundItem(target, "Take"))
            {
                if (plugin.isStopRequested()) return;
                // Wait for movement to stop
                if (PPlayer.location().distanceTo(target.getLocation()) >= 1){
                    PUtils.waitCondition(PUtils.random(1400, 2200), PPlayer::isMoving);
                }
                PUtils.waitCondition(PUtils.random(4000, 6000), () -> !PPlayer.isMoving());

                // Wait for item to appear in inventory
                if (PUtils.waitCondition(PUtils.random(1900, 2900), () -> PInventory.getCount(target.getName()) > countBefore)) {
                    if (plugin.isStopRequested()) return;
                    // Maybe alch item
                    if (settings.isEnableAlching()) {
                        if (RuneElement.FIRE.getCount() < 5 || RuneElement.NATURE.getCount() < 1 || PSkills.getCurrentLevel(Skill.MAGIC) < 55) {
                            log.info("Cannot alch item, no runes left or magic level is too low");
                        } else {
                            int slotHAPrice = target.getHaPrice();
                            int slotGEPrice = target.getGePrice();
                            int priceDifference = slotGEPrice - slotHAPrice;
                            if (priceDifference <= settings.getAlchMaxPriceDifference() && slotHAPrice >= settings.getAlchMinHAValue()) {
                                PUtils.sleepNormal(300, 700);
                                PItem looted = PInventory.findItem(Filters.Items.idEquals(target.getId()));
                                if (looted != null) {
                                    log.info("Alching " + looted.getName() + " GE price difference is " + priceDifference);
                                    PInteraction.useSpellOnItem(Spells.HIGH_LEVEL_ALCHEMY, looted);
                                }
                            }
                        }
                    }
                }
                PUtils.sleepNormal(100, 900, 80, 300);
            }
        }
    }

    public Comparator<PGroundItem> lootPrioritySorter = (a, b) -> {
        int distA = (int)Math.round(PPlayer.distanceTo(a.getLocation()));
        int distB = (int)Math.round(PPlayer.distanceTo(a.getLocation()));
        int ageA = a.getSpawnTime() != null ? (int)Duration.between(a.getSpawnTime(), Instant.now()).getSeconds() : 15;
        int ageB = b.getSpawnTime() != null ? (int)Duration.between(b.getSpawnTime(), Instant.now()).getSeconds() : 15;
        int sortA = distA - (int)Math.floor(ageA*0.5);
        int sortB = distB - (int)Math.floor(ageB*0.5);
        return sortA-sortB;
    };

    public boolean haveSpaceForItem(PGroundItem item){
        if (PInventory.getEmptySlots() > settings.getReservedInventorySlots()){
            return true;
        }
        if (item.isStackable() && PInventory.findItem(Filters.Items.idEquals(item.getId())) != null){
            return true;
        }

        if (settings.isEatFoodForLoot() && PInventory.findItem(settings.getValidFoodFilter()) != null) {
            return true;
        }

        return false;
    }

    public PGroundItem getNextLootableItem(){
        return getLootableItems()
                .stream()
                .sorted(lootPrioritySorter)
                .findFirst()
                .orElse(null);
    }

    public List<PGroundItem> getLootableItems(){
        return PGroundItems.findGroundItems(settings.getValidLootFilter());
    }
}
