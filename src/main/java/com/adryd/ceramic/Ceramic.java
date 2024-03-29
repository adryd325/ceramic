package com.adryd.ceramic;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.api.settings.SettingsManager;
import com.adryd.ceramic.command.HomeCommand;
import com.adryd.ceramic.command.ModsCommand;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class Ceramic implements CarpetExtension, ModInitializer {

    public static final SettingsManager settingsManager;
    private static final String MOD_ID = "ceramic";
    private static final String MOD_NAME;
    private static final Version MOD_VERSION;
    private static final FabricLoader loader = FabricLoader.getInstance();

    static {
        ModMetadata metadata = loader.getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
        MOD_NAME = metadata.getName();
        MOD_VERSION = metadata.getVersion();
        settingsManager = new SettingsManager(MOD_VERSION.getFriendlyString(), MOD_ID, MOD_NAME);
    }

    public static final Logger logger = LogManager.getLogger(MOD_ID);

    @Override
    public String version() {
        return MOD_VERSION.getFriendlyString();
    }

    public static String modId() {
        return MOD_ID;
    }

    public static String modName() {
        return MOD_NAME;
    }

    @Override
    public void onInitialize() {
        logger.debug("loaded ceramic");
        CarpetServer.manageExtension(new Ceramic());
    }

    @Override
    public void onGameStarted() {
        settingsManager.parseSettingsClass(CeramicSettings.class);
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, final CommandRegistryAccess commandBuildContext) {
        settingsManager.registerCommand(dispatcher, commandBuildContext);

        ModsCommand.register(dispatcher);
        HomeCommand.register(dispatcher);
    }

    @Override
    public Map<String, String> canHasTranslations(String lang) {
        return CeramicTranslations.getTranslationFromResourcePath(lang);
    }
}
