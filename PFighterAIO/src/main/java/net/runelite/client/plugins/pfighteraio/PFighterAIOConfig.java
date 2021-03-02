package net.runelite.client.plugins.pfighteraio;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.*;

@ConfigGroup("PFighterAIO")
public interface PFighterAIOConfig extends Config
{
    @ConfigItem(
            keyName = "enableOverlay",
            name = "Enable overlay",
            description = "Enable drawing of the overlay",
            position = 1
    )
    default boolean enableOverlay()
    {
        return false;
    }

    @ConfigSection(
            name = "Targeting",
            description = "Enter enemy names/ids to target!!!",
            position = 11,
            closedByDefault = true,
            keyName = "targetingSection"
    )
    String targetingsection = "Targeting";

    @ConfigItem(
            keyName = "enemyNames",
            name = "",
            description = "Enter enemy names/ids to target!!!",
            position = 12,
            title = targetingsection,
            section = targetingsection
    )
    default String enemyNames()
    {
        return "Goblin, Cow";
    }

    @Range(
            min = 1,
            max = 20
    )
    @ConfigItem(
            keyName = "searchRadius",
            name = "Search radius",
            description = "The distance (in tiles) to search for targets.",
            section = targetingsection,
            title = targetingsection,
            position = 17
    )
    default int searchRadius()
    {
        return 10;
    }

    @ConfigItem(
            keyName = "enablePathfind",
            name = "Check pathfinding",
            description = "Enable to also check that a valid path to target can be found. May impact performance.",
            section = targetingsection,
            position = 18
    )
    default boolean enablePathfind()
    {
        return false;
    }

    @ConfigItem(
            keyName = "setFightAreaButton",
            name = "Set fighting area",
            description = "Set fighting area to where you are standing",
            section = targetingsection,
            position = 19
    )
    default Button setFightAreaButton()
    {
        return new Button();
    }

    @ConfigSection(
            name = "Eating",
            description = "Enter food names/ids to eat",
            position = 15,
            closedByDefault = true,
            keyName = "eatingsection"
    )
    String eatingsection = "Eating";

    @ConfigItem(
            keyName = "foodNames",
            name = "",
            description = "Food names or IDs to eat",
            section = eatingsection,
            position = 21
    )
    default String foodNames()
    {
        return "Shrimps, Cabbage";
    }

    @ConfigItem(
            keyName = "minEatHP",
            name = "Minimum Eat HP",
            description = "Minimum HP to eat. Bot will always eat below this value.",
            section = eatingsection,
            position = 25

    )
    default int minEatHP()
    {
        return 10;
    }

    @ConfigItem(
            keyName = "maxEatHP",
            name = "Maximum Eat HP",
            description = "Highest HP that bot sometimes eats at (random between min and max eat hp)",
            section = eatingsection,
            position = 30
    )
    default int maxEatHP()
    {
        return 20;
    }

    @ConfigItem(
            keyName = "stopWhenOutOfFood",
            name = "Stop when out of food",
            description = "Stops and logs out when out of food",
            section = eatingsection,
            position = 31
    )
    default boolean stopWhenOutOfFood()
    {
        return false;
    }

    @ConfigSection(
            name = "Looting",
            description = "Enter loot names/ids to pick up",
            position = 15,
            closedByDefault = true,
            keyName = "lootingsection"
    )
    String lootingsection = "Looting";

    @ConfigItem(
            keyName = "lootNames",
            name = "",
            description = "",
            section = lootingsection,
            position = 51
    )
    default String lootNames()
    {
        return "Clue, champion";
    }

    @ConfigItem(
            keyName = "lootGEValue",
            name = "Loot if value>X",
            description = "Loot items that are more valuable than X. 0 to disable",
            section = lootingsection,
            position = 52
    )
    default int lootGEValue()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "lootOwnKills",
            name = "Only loot your kills",
            description = "Makes the bot ignore drops from other players.",
            section = lootingsection,
            position = 53
    )
    default boolean lootOwnKills()
    {
        return false;
    }

    @ConfigItem(
            keyName = "eatForLoot",
            name = "Eat to make space for loot",
            description = "Makes the bot eat food to get more inventory space for loot.",
            section = lootingsection,
            position = 54
    )
    default boolean eatForLoot()
    {
        return false;
    }

    @ConfigItem(
            keyName = "forceLoot",
            name = "Force loot old drops",
            description = "Makes the bot eat force loot items when they have been on the ground for too long.",
            section = lootingsection,
            position = 55
    )
    default boolean forceLoot()
    {
        return false;
    }


    @ConfigSection(
            name = "Safe spotting",
            description = "Enter enemy names/ids to target",
            position = 15,
            closedByDefault = true,
            keyName = "safespotsection"
    )
    String safespotsection = "Safe spotting";

    @ConfigItem(
            keyName = "setSafeSpotButton",
            name = "Set safespot tile",
            description = "Set safespot tile where you are standing",
            section = safespotsection,
            position = 71
    )
    default Button setSafeSpotButton()
    {
        return new Button();
    }
    @ConfigItem(
            keyName = "enableSafeSpot",
            name = "Use safespot for combat",
            description = "Run to safespot tile in combat",
            section = safespotsection,
            position = 72
    )
    default boolean enableSafeSpot()
    {
        return false;
    }
    @ConfigItem(
            keyName = "exitInSafeSpot",
            name = "Safespot when stopping",
            description = "Run to safespot and logout when stopping script",
            section = safespotsection,
            position = 73
    )
    default boolean exitInSafeSpot()
    {
        return false;
    }

    @ConfigSection(
            name = "Banking",
            description = "Banking options",
            position = 84,
            closedByDefault = true,
            keyName = "bankingSection"
    )
    String bankingSection = "Banking";

    @ConfigItem(
            keyName = "enableBanking",
            name = "Enable banking",
            description = "Enable banking",
            section = bankingSection,
            position = 85
    )
    default boolean enableBanking()
    {
        return false;
    }

    @ConfigItem(
            keyName = "bankForFood",
            name = "Bank when out of food",
            description = "Bank when out of food",
            section = bankingSection,
            position = 86
    )
    default boolean bankForFood()
    {
        return false;
    }

    @ConfigItem(
            keyName = "bankForLoot",
            name = "Bank when inventory is full",
            description = "Bank when inventory is full",
            section = bankingSection,
            position = 86
    )
    default boolean bankForLoot()
    {
        return false;
    }

    @ConfigItem(
            keyName = "withdrawItems",
            name = "Withdraw items list",
            description = "Items to withdraw when banking",
            section = bankingSection,
            position = 88
    )
    default String withdrawItems()
    {
        return "";
    }

    @ConfigItem(
            keyName = "startButton",
            name = "Start",
            description = "Start",
            position = 101
    )
    default Button startButton()
    {
        return new Button();
    }

    @ConfigItem(
            keyName = "stopButton",
            name = "Stop",
            description = "Stop",
            position = 102
    )
    default Button stopButton()
    {
        return new Button();
    }

    @ConfigItem (
            keyName = "storedFightTile",
            hidden = true,
            name = "Stored fight tile",
            description = "Used to save last used fight tile",
            position = 103
    )
    default WorldPoint storedFightTile()
    {
        return null;
    }

    @ConfigItem (
            keyName = "storedSafeSpotTile",
            hidden = true,
            name = "Stored safe spot tile",
            description = "Used to save last used safe spot tile",
            position = 104
    )
    default WorldPoint storedSafeSpotTile()
    {
        return null;
    }
}

