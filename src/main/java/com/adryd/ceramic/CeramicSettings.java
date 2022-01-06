package com.adryd.ceramic;

import carpet.settings.Rule;

import static carpet.settings.RuleCategory.*;

public class CeramicSettings {
    private static final String ADMINISTRATION = "administration";
    private static final String GAME_MECHANICS = "gameMechanics";
    private static final String FABRICATION = "fabricationPort";
    private static final String TIS_ADDITIONS = "tisAdditionsPort";

    @Rule(desc = "Disable OPs bypass whitelist", category = {ADMINISTRATION})
    public static boolean disableOpsBypassWhitelist = false;

    @Rule(desc = "Enable whitelist when all OPs are offline", category = {ADMINISTRATION})
    public static boolean opsMonitorWhitelist = false;

    @Rule(desc = "(cursed impl) Always re-download server resource pack", category = {ADMINISTRATION})
    public static boolean alwaysRefreshResourcePack = false;

    @Rule(desc = "Hide server brand", category = {ADMINISTRATION})
    public static boolean hideServerBrand = false;

    @Rule(desc = "Enables /mods command", category = {COMMAND})
    public static boolean commandMods = true;

    @Rule(desc = "Enables /home command", category = {COMMAND})
    public static boolean commandHome = true;

    @Rule(desc = "Prevents creative players from attacking invulnerable mobs", category = {CREATIVE})
    public static boolean invulnerableAreImmuneToCreativePlayers = false;

    @Rule(desc = "Disable turtle egg and farmland trampling", category = {GAME_MECHANICS})
    public static boolean disableBlockTrampling = false;

    @Rule(desc = "Note blocks play on landing (Fabrication port)", category = {FABRICATION, GAME_MECHANICS})
    public static boolean noteBlocksPlayOnLanding = false;

    @Rule(desc = "Tune note blocks with sticks (Fabrication port)", category = {FABRICATION, GAME_MECHANICS})
    public static boolean noteBlocksTuneWithSticks = false;

    @Rule(desc = "Tune note blocks backwards when sneaking (Fabrication port)", category = {FABRICATION, GAME_MECHANICS})
    public static boolean noteBlocksTuneBackwards = false;

    @Rule(desc = "Disable chat anti-spam (TIS Additions port)", category = {TIS_ADDITIONS, CREATIVE})
    public static boolean disableMessageCooldown = false;

    @Rule(desc = "Disable creative item drop cooldown (TIS Additions port)", category = {TIS_ADDITIONS, CREATIVE})
    public static boolean disableCreativeItemDropCooldown = false;

    @Rule(desc = "Disable enchant command restrictions (TIS Additions port)", category = {TIS_ADDITIONS})
    public static boolean enchantCommandDisableRestrictions = false;

    @Rule(desc = "Send server debug info", category = {CREATIVE})
    public static boolean sendServerDebugInfo = false;

    @Rule(desc = "Send server debug info distance", category = {CREATIVE})
    public static double sendServerDebugInfoDistance = 256.0;

    @Rule(desc = "Send server debug info distance", category = {CREATIVE})
    public static boolean sendServerDebugInfoUnlimitedDistance = false;

    // By mstrodl
    @Rule(desc = "A backport of brigadier#90. Makes stringified NBTs support more JSON-like string escapes", category = {CREATIVE})
    public static boolean allowBrigadierStringEscapes = false;
}
