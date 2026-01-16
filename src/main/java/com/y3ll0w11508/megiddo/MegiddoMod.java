package com.y3ll0w11508.megiddo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Megiddo Mod - Physics Magic System
 * สร้างระบบ Megiddo แบบ Rimuru จาก Tensura
 */
public class MegiddoMod implements ModInitializer {

    public static final String MOD_ID = "megiddo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Megiddo Mod...");

        // ลงทะเบียน Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            MegiddoCommand.register(dispatcher);
        });

        // TODO: ลงทะเบียน Networking (Phase 2)
        // TODO: ลงทะเบียน Items (ถ้ามี)

        LOGGER.info("Megiddo Mod initialized successfully!");
    }
}