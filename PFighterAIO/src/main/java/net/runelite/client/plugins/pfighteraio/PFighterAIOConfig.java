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
            description = "Enter enemy names/ids to target",
            position = 11,
            closedByDefault = true,
            keyName = "targetingSection"
    )
    String targetingsection = "Targeting";

    @ConfigItem(
            keyName = "enemyNames",
            name = "",
            description = "Enter enemy names/ids to target",
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
            description = "Enable to also check that a valid path to target can be found.",
            section = targetingsection,
            position = 18
    )
    default boolean enablePathfind()
    {
        return true;
    }
    @ConfigItem(
            keyName = "useSlayerItems",
            name = "Use slayer items",
            description = "Uses slayer items on enemies.",
            section = targetingsection,
            position = 19
    )
    default boolean useSlayerItems()
    {
        return false;
    }

    @ConfigItem(
            keyName = "setFightAreaButton",
            name = "Set fighting area",
            description = "Set fighting area to where you are standing",
            section = targetingsection,
            position = 20
    )
    default Button setFightAreaButton()
    {
        return new Button();
    }

    @ConfigSection(
            name = "Eating",
            description = "Enter food names/ids to eat",
            position = 21,
            closedByDefault = true,
            keyName = "eatingsection"
    )
    String eatingsection = "Eating";

    @ConfigItem(
            keyName = "foodNames",
            name = "",
            description = "Food names or IDs to eat",
            section = eatingsection,
            position = 22
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
    @ConfigItem(
            keyName = "usePotions",
            name = "Use potions",
            description = "Drinks potions automatically when under a certain stat bonus",
            section = eatingsection,
            position = 33
    )
    default boolean usePotions()
    {
        return false;
    }

    @ConfigItem(
            keyName = "minPotionBoost",
            name = "Min potion boost range",
            description = "Minimum random range for stat boost. When current skill boost is below the randomed value, auto drinks potion.",
            section = eatingsection,
            position = 34,
            hidden = true,
            unhide = "usePotions"

    )
    default int minPotionBoost()
    {
        return 3;
    }

    @ConfigItem(
            keyName = "maxPotionBoost",
            name = "Max potion boost range",
            description = "Maximim random range for stat boost. When current skill boost is below the randomed value, auto drinks potion.",
            section = eatingsection,
            position = 35,
            hidden = true,
            unhide = "usePotions"
    )
    default int maxPotionBoost()
    {
        return 5;
    }

    @ConfigSection(
            name = "Looting",
            description = "Enter loot names/ids to pick up",
            position = 50,
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
        return true;
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

    @ConfigItem(
            keyName = "enableAlching",
            name = "Enable alching",
            description = "Enable high alchemy feature",
            section = lootingsection,
            position = 57
    )
    default boolean enableAlching()
    {
        return false;
    }

    @ConfigItem(
            keyName = "alchMinHAValue",
            name = "Alch Min HA value",
            description = "Alches items whose HA value is at least this",
            section = lootingsection,
            position = 58,
            hidden = true,
            unhide = "enableAlching"
    )
    default int alchMinHAValue()
    {
        return 400;
    }

    @ConfigItem(
            keyName = "alchMaxPriceDifference",
            name = "Alch Max price difference",
            description = "Dont alch item if its GE value is this much higher than HA value",
            section = lootingsection,
            position = 59,
            hidden = true,
            unhide = "enableAlching"
    )
    default int alchMaxPriceDifference()
    {
        return 6000;
    }


    @ConfigSection(
            name = "Safe spotting",
            description = "Enter enemy names/ids to target",
            position = 70,
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
            keyName = "bankLocation",
            name = "Bank location",
            description = "Select the bank location to use",
            position = 86,
            section = bankingSection
    )
    default PFighterBanks bankLocation()
    {
        return PFighterBanks.AUTODETECT;
    }

    @ConfigItem(
            keyName = "bankForFood",
            name = "Bank when out of food",
            description = "Bank when out of food",
            section = bankingSection,
            position = 87
    )
    default boolean bankForFood()
    {
        return false;
    }

    @ConfigItem(
            keyName = "bankForPrayerPots",
            name = "Bank when out of prayer pots",
            description = "Bank when out of prayer potions",
            section = bankingSection,
            position = 88
    )
    default boolean bankForPrayerPots()
    {
        return false;
    }

    @ConfigItem(
            keyName = "bankForLoot",
            name = "Bank when inventory is full",
            description = "Bank when inventory is full",
            section = bankingSection,
            position = 89
    )
    default boolean bankForLoot()
    {
        return false;
    }

    @ConfigItem(
            keyName = "bankForSlayerTask",
            name = "Bank & stop after slayer task",
            description = "Go to bank and stop script after task is finished",
            section = bankingSection,
            position = 90
    )
    default boolean bankForSlayerTask()
    {
        return false;
    }

    @ConfigItem(
            keyName = "teleportWhileBanking",
            name = "Use teleports to bank",
            description = "Uses available teleport items from inventory to get to bank and back",
            section = bankingSection,
            position = 91
    )
    default boolean teleportWhileBanking()
    {
        return false;
    }

    @ConfigItem(
            keyName = "withdrawItems",
            name = "Withdraw items list",
            description = "Items to withdraw when banking",
            section = bankingSection,
            position = 92
    )
    default String withdrawItems()
    {
        return "";
    }

    @ConfigSection(
            name = "AFK Breaks",
            description = "Break options",
            position = 95,
            closedByDefault = true,
            keyName = "breakSection"
    )
    String breakSection = "Break Options";

    @ConfigItem(
            keyName = "enableBreaks",
            name = "Enable breaks",
            description = "Take breaks on a schedule",
            section = breakSection,
            position = 96
    )
    default boolean enableBreaks()
    {
        return false;
    }

    @ConfigItem(
            keyName = "safeSpotForBreaks",
            name = "Break in safespot",
            description = "Go to safespot before starting break",
            section = breakSection,
            position = 97
    )
    default boolean safeSpotForBreaks()
    {
        return true;
    }
    @ConfigItem(
            keyName = "minBreakIntervalMinutes",
            name = "Min Interval (minutes)",
            description = "Breaks will happen every min - max minutes",
            section = breakSection,
            position = 98

    )
    default int minBreakIntervalMinutes()
    {
        return 5;
    }

    @ConfigItem(
            keyName = "maxBreakIntervalMinutes",
            name = "Max Interval (minutes)",
            description = "Breaks will happen every min - max minutes",
            section = breakSection,
            position = 99
    )
    default int maxBreakIntervalMinutes()
    {
        return 15;
    }

    @ConfigItem(
            keyName = "minBreakDurationSeconds",
            name = "Min Duration (seconds)",
            description = "Breaks will last min - max seconds",
            section = breakSection,
            position = 100

    )
    default int minBreakDurationSeconds()
    {
        return 25;
    }

    @ConfigItem(
            keyName = "maxBreakDurationSeconds",
            name = "Max Duration (seconds)",
            description = "Breaks will last min - max seconds",
            section = breakSection,
            position = 101
    )
    default int maxBreakDurationSeconds()
    {
        return 120;
    }

    @ConfigSection(
            name = "Prayer",
            description = "Prayer settings",
            position = 154,
            closedByDefault = true,
            keyName = "prayerSection"
    )
    String prayerSection = "Prayer settings";

    @ConfigItem(
            keyName = "drinkPrayerPotions",
            name = "Drink prayer potions",
            description = "Enable drinking prayer potions",
            section = prayerSection,
            position = 155
    )
    default boolean drinkPrayerPotions()
    {
        return false;
    }

    @ConfigItem(
            keyName = "minPrayerPotPoints",
            name = "Prayer potion min",
            description = "Minimum prayer points to drink prayer pot at",
            section = prayerSection,
            position = 158,
            hidden = true,
            unhide = "drinkPrayerPotions"
    )
    default int minPrayerPotPoints()
    {
        return 2;
    }

    @ConfigItem(
            keyName = "maxPrayerPotPoints",
            name = "Prayer potion max",
            description = "Highest prayer that to drink prayer pot at (random between min and max prayer potion range)",
            section = prayerSection,
            position = 159,
            hidden = true,
            unhide = "drinkPrayerPotions"
    )
    default int maxPrayerPotPoints()
    {
        return 10;
    }

    @ConfigItem(
            keyName = "normalQuickPrayers",
            name = "Enable quick prayers",
            description = "Normally enables quick prayers in fight area (no flicking)",
            section = prayerSection,
            disabledBy = "flickQuickPrayers | assistFlickPrayers",
            position = 160
    )
    default boolean normalQuickPrayers()
    {
        return false;
    }

    @ConfigItem(
            keyName = "flickQuickPrayers",
            name = "Flick quick prayers in combat",
            description = "Makes bot flick prayers when in combat with targets",
            section = prayerSection,
            disabledBy = "normalQuickPrayers",
            position = 161
    )
    default boolean flickQuickPrayers()
    {
        return false;
    }

    @ConfigItem(
            keyName = "assistFlickPrayers",
            name = "Use as combat assistant",
            description = "Flicks quick prayers for you when script is not on, so you can play manually with prayer flick assist",
            section = prayerSection,
            disabledBy = "normalQuickPrayers",
            position = 162
    )
    default boolean assistFlickPrayers()
    {
        return false;
    }

    @ConfigSection(
            name = "BETA: Cannon",
            description = "Cannon options",
            position = 170,
            closedByDefault = true,
            keyName = "cannonSection"
    )
    String cannonSection = "BETA: Cannon";

    @ConfigItem(
            keyName = "useCannon",
            name = "Use cannon",
            description = "Use dwarf cannon to fight",
            section = cannonSection,
            position = 171
    )
    default boolean useCannon()
    {
        return false;
    }

    @ConfigItem(
            keyName = "setCannonTileButton",
            name = "Set cannon tile",
            description = "Sets cannon tile where you are standing",
            section = cannonSection,
            position = 172
    )
    default Button setCannonTileButton()
    {
        return new Button();
    }

    @ConfigSection(
            name = "BETA: Worldhopping",
            description = "Worldhopping options",
            position = 179,
            closedByDefault = true,
            keyName = "worldhopSection"
    )
    String worldhopSection = "BETA: Worldhopping";

    @ConfigItem(
            keyName = "worldHopInSafespot",
            name = "Go to safe spot before hop",
            description = "Go to safe spot before hopping worlds",
            section = worldhopSection,
            position = 180
    )
    default boolean worldHopInSafespot()
    {
        return true;
    }

    @ConfigItem(
            keyName = "worldhopIfPlayerTalks",
            name = "Worldhop if a player talks",
            description = "Worldhop if a player talks in the area",
            section = worldhopSection,
            position = 181
    )
    default boolean worldhopIfPlayerTalks()
    {
        return false;
    }

    @ConfigItem(
            keyName = "worldhopIfTooManyPlayers",
            name = "Worldhop if > X players",
            description = "Worldhop if too many players are nearby",
            section = worldhopSection,
            position = 182
    )
    default boolean worldhopIfTooManyPlayers()
    {
        return false;
    }

    @ConfigItem(
            keyName = "worldhopPlayerLimit",
            name = "Player limit",
            description = "Minimum limit of players to find nearby before worldhopping",
            section = worldhopSection,
            position = 183,
            hidden = true,
            unhide = "worldhopIfTooManyPlayers"

    )
    default int worldhopPlayerLimit()
    {
        return 2;
    }

    @ConfigItem(
            keyName = "apiKey",
            name = "API Key",
            description = "Your secret API/License key",
            position = 195
    )
    default String apiKey()
    {
        return "";
    }

    @ConfigItem(
            keyName = "startButton",
            name = "Start",
            description = "Start",
            position = 196
    )
    default Button startButton()
    {
        return new Button();
    }

    @ConfigItem(
            keyName = "stopButton",
            name = "Stop",
            description = "Stop",
            position = 197
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
            position = 210
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
            position = 211
    )
    default WorldPoint storedSafeSpotTile()
    {
        return null;
    }

    @ConfigItem (
            keyName = "storedCannonTile",
            hidden = true,
            name = "Stored cannon tile",
            description = "Used to save last used cannon tile",
            position = 212
    )
    default WorldPoint storedCannonTile()
    {
        return null;
    }
}

