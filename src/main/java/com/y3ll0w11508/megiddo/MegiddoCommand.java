package com.y3ll0w11508.megiddo;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.y3ll0w11508.megiddo.system.TargetingSystem;
import net.minecraft.commands.CommandSourceStack; // Yarn: ServerCommandSource
import net.minecraft.commands.Commands; // Yarn: CommandManager
import net.minecraft.network.chat.Component; // Yarn: Text
import net.minecraft.server.level.ServerPlayer; // Yarn: ServerPlayerEntity

public class MegiddoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("megiddo")
                        .then(Commands.literal("test")
                                .then(Commands.argument("minRange", DoubleArgumentType.doubleArg(0, 100))
                                        .then(Commands.argument("maxRange", DoubleArgumentType.doubleArg(0, 100))
                                                .executes(MegiddoCommand::executeTest)
                                        )
                                )
                        )
                        .then(Commands.literal("fire")
                                .then(Commands.argument("minRange", DoubleArgumentType.doubleArg(0, 100))
                                        .then(Commands.argument("maxRange", DoubleArgumentType.doubleArg(0, 100))
                                                .executes(MegiddoCommand::executeFire)
                                        )
                                )
                        )
        );
    }

    private static int executeTest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer player) {
            double minRange = DoubleArgumentType.getDouble(context, "minRange");
            double maxRange = DoubleArgumentType.getDouble(context, "maxRange");

            // เรียกใช้ Targeting System
            TargetingSystem.debugPrintTargets(player, minRange, maxRange);

            return 1; // Success
        } else {
            // sendError -> sendFailure
            source.sendFailure(Component.literal("This command must be used by a player!"));
            return 0;
        }
    }

    private static int executeFire(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (source.getEntity() instanceof ServerPlayer player) {
            double minRange = DoubleArgumentType.getDouble(context, "minRange");
            double maxRange = DoubleArgumentType.getDouble(context, "maxRange");

            // sendMessage -> displayClientMessage
            player.displayClientMessage(Component.literal("§c⚠ Fire system not implemented yet!"), false);

            return 1;
        } else {
            source.sendFailure(Component.literal("This command must be used by a player!"));
            return 0;
        }
    }
}