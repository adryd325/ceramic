package com.adryd.ceramic;

import carpet.api.settings.Rule;

import static carpet.api.settings.RuleCategory.*;

public class CeramicSettings {
    private static final String ADMINISTRATION = "administration";
    private static final String GAME_MECHANICS = "gameMechanics";
    private static final String FABRICATION = "fabricationPort";
    private static final String TIS_ADDITIONS = "tisAdditionsPort";

    @Rule(categories = {ADMINISTRATION})
    public static boolean disableOpsBypassWhitelist = false;

    @Rule(categories = {ADMINISTRATION})
    public static boolean opsMonitorWhitelist = false;

    @Rule(categories = {ADMINISTRATION})
    public static boolean alwaysRefreshResourcePack = false;

    @Rule(categories = {ADMINISTRATION})
    public static boolean hideServerBrand = false;

    @Rule(categories = {COMMAND})
    public static boolean commandMods = true;

    @Rule(categories = {COMMAND})
    public static boolean commandHome = true;

    @Rule(categories = {CREATIVE})
    public static boolean invulnerableAreImmuneToCreativePlayers = false;

    @Rule(categories = {GAME_MECHANICS})
    public static boolean disableBlockTrampling = false;

    @Rule(categories = {FABRICATION, GAME_MECHANICS})
    public static boolean noteBlocksPlayOnLanding = false;

    @Rule(categories = {FABRICATION, GAME_MECHANICS})
    public static boolean noteBlocksTuneWithSticks = false;

    @Rule(categories = {FABRICATION, GAME_MECHANICS})
    public static boolean noteBlocksTuneBackwards = false;

    @Rule(categories = {TIS_ADDITIONS, CREATIVE})
    public static boolean disableMessageCooldown = false;

    @Rule(categories = {TIS_ADDITIONS, CREATIVE})
    public static boolean disableCreativeItemDropCooldown = false;

    @Rule(categories = {TIS_ADDITIONS})
    public static boolean enchantCommandDisableRestrictions = false;

    @Rule(categories = {CREATIVE})
    public static boolean sendServerDebugInfo = false;

    @Rule(categories = {CREATIVE})
    public static double sendServerDebugInfoDistance = 256.0;

    @Rule(categories = {CREATIVE})
    public static boolean sendServerDebugInfoUnlimitedDistance = false;

    @Rule(categories = {GAME_MECHANICS})
    public static boolean creepersDropBlocks = false;

    @Rule(categories = {GAME_MECHANICS})
    public static boolean creepersHealthExplosionStrength = false;
}
