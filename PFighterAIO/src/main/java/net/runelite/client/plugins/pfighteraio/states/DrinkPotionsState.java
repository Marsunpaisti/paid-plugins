package net.runelite.client.plugins.pfighteraio.states;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;
import net.runelite.client.plugins.paistisuite.api.*;
import net.runelite.client.plugins.paistisuite.api.types.Filters;
import net.runelite.client.plugins.paistisuite.api.types.PItem;
import net.runelite.client.plugins.pfighteraio.PFighterAIO;
import net.runelite.client.plugins.pfighteraio.PFighterAIOSettings;

import java.util.function.Predicate;

@Slf4j
public class DrinkPotionsState extends State {
    int nextDrinkBonus;

    public DrinkPotionsState(PFighterAIO plugin, PFighterAIOSettings settings) {
        super(plugin, settings);
        nextDrinkBonus = PUtils.random(settings.getMinPotionBoost(), settings.getMaxPotionBoost());
    }

    public String getName(){
        return "Drinking potion";
    }

    @Override
    public boolean condition() {
        if (!settings.isUsePotions()) return false;
        if (plugin.fightEnemiesState.inCombat()) return false;
        if (settings.getSearchRadiusCenter() != null && settings.getSearchRadiusCenter().distanceTo2D(PPlayer.getWorldLocation()) > 30) return false;

        if (PSkills.getCurrentLevel(Skill.ATTACK) < PSkills.getActualLevel(Skill.ATTACK) + nextDrinkBonus){
            if (PInventory.findItem(potionFilterForSkill(Skill.ATTACK)) != null) return true;
        }
        if (PSkills.getCurrentLevel(Skill.STRENGTH) < PSkills.getActualLevel(Skill.STRENGTH) + nextDrinkBonus){
            if (PInventory.findItem(potionFilterForSkill(Skill.STRENGTH)) != null) return true;
        }
        if (PSkills.getCurrentLevel(Skill.DEFENCE) < PSkills.getActualLevel(Skill.DEFENCE) + nextDrinkBonus){
            if (PInventory.findItem(potionFilterForSkill(Skill.DEFENCE)) != null) return true;
        }
        if (PSkills.getCurrentLevel(Skill.MAGIC) < PSkills.getActualLevel(Skill.MAGIC) + nextDrinkBonus){
            if (PInventory.findItem(potionFilterForSkill(Skill.MAGIC)) != null) return true;
        }
        if (PSkills.getCurrentLevel(Skill.RANGED) < PSkills.getActualLevel(Skill.RANGED) + nextDrinkBonus){
            if (PInventory.findItem(potionFilterForSkill(Skill.RANGED)) != null) return true;
        }
        return false;
    }

    public Predicate<PItem> potionFilterForSkill(Skill skill){
        if (skill == Skill.ATTACK){
            return Filters.Items.nameContains("Super Attack", "Super Combat Potion", "Combat Potion", "Attack Potion");
        }
        if (skill == Skill.STRENGTH){
            return Filters.Items.nameContains("Super Strength", "Super Combat Potion", "Combat Potion", "Strength Potion");
        }
        if (skill == Skill.DEFENCE){
            return Filters.Items.nameContains("Super Defence", "Super Combat Potion", "Defence Potion");
        }
        if (skill == Skill.RANGED){
            return Filters.Items.nameContains("Ranging Potion", "Bastion Potion");
        }
        if (skill == Skill.MAGIC){
            return Filters.Items.nameContains("Magic Potion");
        }
        return null;
    }

    @Override
    public void loop() {
        super.loop();
        PItem potion = null;
        if (potion == null && PSkills.getCurrentLevel(Skill.ATTACK) < PSkills.getActualLevel(Skill.ATTACK) + nextDrinkBonus){
            potion = PInventory.findItem(potionFilterForSkill(Skill.ATTACK));
        }
        if (potion == null && PSkills.getCurrentLevel(Skill.STRENGTH) < PSkills.getActualLevel(Skill.STRENGTH) + nextDrinkBonus){
            potion = PInventory.findItem(potionFilterForSkill(Skill.STRENGTH));
        }
        if (potion == null && PSkills.getCurrentLevel(Skill.DEFENCE) < PSkills.getActualLevel(Skill.DEFENCE) + nextDrinkBonus){
            potion = PInventory.findItem(potionFilterForSkill(Skill.DEFENCE));
        }
        if (potion == null && PSkills.getCurrentLevel(Skill.MAGIC) < PSkills.getActualLevel(Skill.MAGIC) + nextDrinkBonus){
            potion = PInventory.findItem(potionFilterForSkill(Skill.MAGIC));
        }
        if (potion == null && PSkills.getCurrentLevel(Skill.RANGED) < PSkills.getActualLevel(Skill.RANGED) + nextDrinkBonus){
            potion = PInventory.findItem(potionFilterForSkill(Skill.RANGED));
        }
        if (potion != null && PInteraction.item(potion, "Drink")) {
            nextDrinkBonus = PUtils.random(settings.getMinPotionBoost(), settings.getMaxPotionBoost());
            PUtils.sleepNormal(1800, 2600);
        }
    }
}
