package com.y3ll0w11508.megiddo;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.y3ll0w11508.megiddo.system.TargetingSystem;

// ⚠️ MOJANG MAPPINGS - ใช้สำหรับ 1.21.11
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Command สำหรับทดสอบระบบ Megiddo
 * ใช้: /megiddo test <minRange> <maxRange>
 *
 * Mapping: Mojang (Official)
 */
public class MegiddoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        Megiddo.LOGGER.info("Building /megiddo command tree...");

        dispatcher.register(
                Commands.literal("megiddo")
                        .then(Commands.literal("test")
                                .then(Commands.argument("minRange", DoubleArgumentType.doubleArg(0, 99999))
                                        .then(Commands.argument("maxRange", DoubleArgumentType.doubleArg(0, 99999))
                                                .executes(MegiddoCommand::executeTest)
                                        )
                                )
                        )
                        .then(Commands.literal("fire")
                                .then(Commands.argument("minRange", DoubleArgumentType.doubleArg(0, 99999))
                                        .then(Commands.argument("maxRange", DoubleArgumentType.doubleArg(0, 99999))
                                                .executes(MegiddoCommand::executeFire)
                                        )
                                )
                        )
        );

        Megiddo.LOGGER.info("✅ Command tree built successfully!");
    }

    /**
     * /megiddo test 5 60 - แสดงรายชื่อศัตรู
     */
    private static int executeTest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Megiddo.LOGGER.info("📋 Executing /megiddo test...");

        if (source.getEntity() instanceof ServerPlayer player) {
            double minRange = DoubleArgumentType.getDouble(context, "minRange");
            double maxRange = DoubleArgumentType.getDouble(context, "maxRange");

            Megiddo.LOGGER.info("👤 Player: {}, Range: {}-{}",
                    player.getName().getString(), minRange, maxRange);

            // เรียกใช้ Targeting System
            TargetingSystem.debugPrintTargets(player, minRange, maxRange);

            return 1; // Command.SINGLE_SUCCESS
        } else {
            source.sendFailure(Component.literal("❌ This command must be used by a player!"));
            return 0;
        }
    }

    /**
     * /megiddo fire 5 60 - ยิงจริง (ยังไม่ implement)
     */
    private static int executeFire(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Megiddo.LOGGER.info("🔥 Executing /megiddo fire...");

        if (source.getEntity() instanceof ServerPlayer player) {
            double minRange = DoubleArgumentType.getDouble(context, "minRange");
            double maxRange = DoubleArgumentType.getDouble(context, "maxRange");

//            อันนี้มันล็อค ID ผู้ใช้นิ
            com.y3ll0w11508.megiddo.system.MegiddoSystem.fireInstant(player, minRange, maxRange);

            return 1;
        } else {
            source.sendFailure(Component.literal("❌ This command must be used by a player!"));
            return 0;
        }
    }
}