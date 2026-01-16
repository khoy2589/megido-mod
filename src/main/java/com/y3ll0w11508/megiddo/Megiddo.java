package com.y3ll0w11508.megiddo;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Megiddo Mod - Physics Magic System
 * สร้างระบบ Megiddo แบบ Rimuru จาก Tensura
 */
public class Megiddo implements ModInitializer {

	public static final String MOD_ID = "megiddo";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("========================================");
		LOGGER.info("Initializing Megiddo Mod...");
		LOGGER.info("========================================");

		// ลงทะเบียน Commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LOGGER.info("Registering Megiddo commands...");
			MegiddoCommand.register(dispatcher);
			LOGGER.info("Commands registered successfully!");
		});

		LOGGER.info("Megiddo Mod initialized successfully!");
		LOGGER.info("Use /megiddo test <min> <max> to test targeting");
		LOGGER.info("========================================");
	}
}