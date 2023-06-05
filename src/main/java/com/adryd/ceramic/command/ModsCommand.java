package com.adryd.ceramic.command;

import carpet.settings.SettingsManager;
import carpet.utils.CommandHelper;
import com.adryd.ceramic.CeramicSettings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.*;

import static net.minecraft.server.command.CommandManager.literal;

public class ModsCommand {
    public static Text getInformationText(ModMetadata modMeta) {
        return Texts.bracketed(Text.literal(modMeta.getId())).styled((style) -> {
            Style temp = style.withColor(Formatting.GREEN)
                    .withInsertion(StringArgumentType.escapeIfRequired(modMeta.getName()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Text.literal("")).append(modMeta.getName()).append("\n").append(modMeta.getVersion().toString())));
            if (modMeta.getContact().get("sources").isPresent()) {
                return temp.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, modMeta.getContact().get("sources").get()));
            }
            return temp;
        });
    }

    public static boolean shouldShow(ModMetadata modMeta, boolean showAll) {
        String modId = modMeta.getId();
        if (modId.equals("java")) return false;
        if (showAll) return true;
        if (
                modMeta.containsCustomValue("fabric-loom:generated")
                        && modMeta.getCustomValue("fabric-loom:generated").getType() == CvType.BOOLEAN
                        && modMeta.getCustomValue("fabric-loom:generated").getAsBoolean()
        ) return false;
        if (modId.equals("minecraft")) return false;
        if (modId.startsWith("fabric-") && !modId.equals("fabric-api")) return false;
        return true;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = literal("mods")
                .requires((player) -> CommandHelper.canUseCommand(player, CeramicSettings.commandMods))
                .then(CommandManager.literal("all").executes((context) -> execute(context.getSource(), true)))
                .executes((context) -> execute(context.getSource(), false));
        dispatcher.register(command);
    }

    public static int execute(ServerCommandSource source, boolean showAll) {
        // New arraylist because for some reason .toList makes an immutable collection
        List<? extends Text> mods = new ArrayList<>(
                FabricLoader
                        .getInstance()
                        .getAllMods()
                        .stream()
                        .filter((modContainer) -> shouldShow(modContainer.getMetadata(), showAll))
                        .map((modContainer) -> getInformationText(modContainer.getMetadata()))
                        .toList()
        );

        // Sort alphabetically
        Collections.sort(mods, new Comparator<Text>() {
            @Override
            public int compare(Text o1, Text o2) {
                return o1.getString().compareTo(o2.getString());
            }
        });

        source.sendFeedback(
                () -> Text.literal("There are " + mods.size() + " mods loaded" + (showAll ? "" : "*") + ": ")
                        .append(Texts.join(mods, Text.literal(", ")))
                , false);
        return 1;
    }
}