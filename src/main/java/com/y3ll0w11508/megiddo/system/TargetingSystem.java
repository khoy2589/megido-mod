package com.y3ll0w11508.megiddo.system;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player; // Yarn: PlayerEntity
import net.minecraft.network.chat.Component; // Yarn: Text
import net.minecraft.world.phys.AABB; // Yarn: Box
import net.minecraft.world.level.Level; // Yarn: World
import java.util.ArrayList;
import java.util.List;

public class TargetingSystem {

    private static final List<EntityType<?>> WHITELIST = List.of(
            EntityType.VILLAGER,
            EntityType.IRON_GOLEM,
            EntityType.CAT,
            EntityType.WOLF,
            EntityType.HORSE,
            EntityType.DONKEY
    );

    public static List<LivingEntity> findTargets(Player player, double minRadius, double maxRadius) {
        Level level = player.level(); // Yarn: getWorld() -> level()
        List<LivingEntity> validTargets = new ArrayList<>();

        // Yarn: getBoundingBox().expand(...) -> Mojang: inflate(...)
        AABB searchArea = player.getBoundingBox().inflate(maxRadius, 20.0, maxRadius);

        // Yarn: getEntitiesByClass -> Mojang: getEntitiesOfClass
        List<LivingEntity> allEntities = level.getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                entity -> entity != player
        );

        for (LivingEntity entity : allEntities) {
            if (!entity.isAlive()) continue;
            if (WHITELIST.contains(entity.getType())) continue;
            if (entity.isAlliedTo(player)) continue; // Yarn: isTeammate

            double distance = player.distanceTo(entity);
            if (distance < minRadius || distance > maxRadius) continue;

            if (!canSeeSky(entity)) continue;

            validTargets.add(entity);
        }

        return validTargets;
    }

    private static boolean canSeeSky(LivingEntity entity) {
        // Yarn: isSkyVisible(getBlockPos()) -> Mojang: canSeeSky(blockPosition())
        return entity.level().canSeeSky(entity.blockPosition());
    }

    public static void debugPrintTargets(Player player, double minRadius, double maxRadius) {
        List<LivingEntity> targets = findTargets(player, minRadius, maxRadius);

        // sendMessage -> displayClientMessage
        player.displayClientMessage(Component.literal("=== Megiddo Targeting Debug ==="), false);
        player.displayClientMessage(Component.literal("Range: " + minRadius + " - " + maxRadius + " blocks"), false);
        player.displayClientMessage(Component.literal("Found " + targets.size() + " targets:"), false);

        for (LivingEntity target : targets) {
            // getName() -> getDescription()
            String name = target.getType().getDescription().getString();
            double distance = Math.round(player.distanceTo(target) * 10) / 10.0;

            player.displayClientMessage(Component.literal("  • " + name + " (" + distance + "m)"), false);
        }
    }
}