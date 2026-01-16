package com.y3ll0w11508.megiddo;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.y3ll0w11508.megiddo.system.TargetingSystem;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Command สำหรับทดสอบระบบ Megiddo
 * ใช้: /megiddo test <minRange> <maxRange>
 */
public class MegiddoCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("megiddo")
                        .then(CommandManager.literal("test")
                                .then(CommandManager.argument("minRange", DoubleArgumentType.doubleArg(0, 100))
                                        .then(CommandManager.argument("maxRange", DoubleArgumentType.doubleArg(0, 100))
                                                .executes(MegiddoCommand::executeTest)
                                        )
                                )
                        )
                        .then(CommandManager.literal("fire")
                                .then(CommandManager.argument("minRange", DoubleArgumentType.doubleArg(0, 100))
                                        .then(CommandManager.argument("maxRange", DoubleArgumentType.doubleArg(0, 100))
                                                .executes(MegiddoCommand::executeFire)
                                        )
                                )
                        )
        );
    }

    /**
     * Command: /megiddo test 5 60
     * แสดงรายชื่อศัตรูที่หาเจอ (ไม่ยิง)
     */
    private static int executeTest(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            double minRange = DoubleArgumentType.getDouble(context, "minRange");
            double maxRange = DoubleArgumentType.getDouble(context, "maxRange");

            // เรียกใช้ Targeting System
            TargetingSystem.debugPrintTargets(player, minRange, maxRange);

            return 1; // Success
        } else {
            source.sendError(Text.literal("This command must be used by a player!"));
            return 0;
        }
    }

    /**
     * Command: /megiddo fire 5 60
     * ยิงจริง (ทดสอบ Damage System)
     */
    private static int executeFire(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (source.getEntity() instanceof ServerPlayerEntity player) {
            double minRange = DoubleArgumentType.getDouble(context, "minRange");
            double maxRange = DoubleArgumentType.getDouble(context, "maxRange");

            // TODO: เรียกใช้ MegiddoSystem.fire() เมื่อทำเสร็จแล้ว
            player.sendMessage(Text.literal("§c⚠ Fire system not implemented yet!"), false);

            return 1;
        } else {
            source.sendError(Text.literal("This command must be used by a player!"));
            return 0;
        }
    }
}