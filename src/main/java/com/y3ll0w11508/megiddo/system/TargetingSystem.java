package com.y3ll0w11508.megiddo.system;

import com.y3ll0w11508.megiddo.Megiddo;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * ระบบค้นหาเป้าหมายสำหรับ Megiddo
 * Phase 1: ทดสอบด้วย Command ก่อน ยังไม่ต่อกับ UI
 */
public class TargetingSystem {

    // Whitelist: Mob ที่ห้ามโจมตี
    private static final List<EntityType<?>> WHITELIST = List.of(
            EntityType.VILLAGER,
            EntityType.IRON_GOLEM,
            EntityType.CAT,
            EntityType.WOLF,
            EntityType.HORSE,
            EntityType.DONKEY
    );

    /**
     * หาศัตรูรอบตัวผู้เล่น
     * @param player ผู้เล่น
     * @param minRadius รัศมีต่ำสุด (blocks)
     * @param maxRadius รัศมีสูงสุด (blocks)
     * @return รายชื่อศัตรูที่หาเจอ
     */
    public static List<LivingEntity> findTargets(PlayerEntity player, double minRadius, double maxRadius) {
        World world = player.getWorld();
        List<LivingEntity> validTargets = new ArrayList<>();

        Megiddo.LOGGER.info("Starting target scan...");
        Megiddo.LOGGER.info("Player position: {}, {}, {}", player.getX(), player.getY(), player.getZ());

        // สร้างกล่องค้นหา (Bounding Box) รอบตัวผู้เล่น
        Box searchArea = player.getBoundingBox().expand(maxRadius, 20.0, maxRadius);

        // ค้นหา Living Entity ทั้งหมดในพื้นที่
        List<LivingEntity> allEntities = world.getEntitiesByClass(
                LivingEntity.class,
                searchArea,
                entity -> entity != player // ไม่รวมตัวเอง
        );

        Megiddo.LOGGER.info("Found {} total entities in search area", allEntities.size());

        // กรองตามเงื่อนไข
        for (LivingEntity entity : allEntities) {
            // เช็ค 1: ต้องมีชีวิตอยู่
            if (!entity.isAlive()) {
                Megiddo.LOGGER.debug("Skipping {} - not alive", entity.getType().getName().getString());
                continue;
            }

            // เช็ค 2: ต้องไม่อยู่ใน Whitelist
            if (WHITELIST.contains(entity.getType())) {
                Megiddo.LOGGER.debug("Skipping {} - in whitelist", entity.getType().getName().getString());
                continue;
            }

            // เช็ค 3: ไม่ใช่เพื่อนร่วมทีม
            if (entity.isTeammate(player)) {
                Megiddo.LOGGER.debug("Skipping {} - teammate", entity.getType().getName().getString());
                continue;
            }

            // เช็ค 4: ต้องอยู่ในระยะที่กำหนด
            double distance = player.distanceTo(entity);
            if (distance < minRadius || distance > maxRadius) {
                Megiddo.LOGGER.debug("Skipping {} - distance {} out of range",
                        entity.getType().getName().getString(), distance);
                continue;
            }

            // เช็ค 5 (Optional): ต้องมองเห็นท้องฟ้าได้ (Megiddo ใช้แสงอาทิตย์)
            if (!canSeeSky(entity)) {
                Megiddo.LOGGER.debug("Skipping {} - cannot see sky", entity.getType().getName().getString());
                continue;
            }

            Megiddo.LOGGER.info("Valid target found: {} at distance {}",
                    entity.getType().getName().getString(), distance);
            validTargets.add(entity);
        }

        Megiddo.LOGGER.info("Total valid targets: {}", validTargets.size());
        return validTargets;
    }

    /**
     * เช็คว่า Entity มองเห็นท้องฟ้าได้หรือไม่
     * (อยู่ในถ้ำหรือใต้หลังคาคาจะยิงไม่ได้)
     */
    private static boolean canSeeSky(LivingEntity entity) {
        return entity.getWorld().isSkyVisible(entity.getBlockPos());
    }

    /**
     * Debug: แสดงรายชื่อศัตรูในแชท
     */
    public static void debugPrintTargets(PlayerEntity player, double minRadius, double maxRadius) {
        List<LivingEntity> targets = findTargets(player, minRadius, maxRadius);

        player.sendMessage(Text.literal("§6=== Megiddo Targeting Debug ==="), false);
        player.sendMessage(Text.literal("§eRange: §f" + minRadius + " - " + maxRadius + " blocks"), false);
        player.sendMessage(Text.literal("§eFound §a" + targets.size() + " §etargets:"), false);

        if (targets.isEmpty()) {
            player.sendMessage(Text.literal("§c  (No valid targets)"), false);
        } else {
            for (LivingEntity target : targets) {
                String name = target.getType().getName().getString();
                double distance = Math.round(player.distanceTo(target) * 10) / 10.0;
                player.sendMessage(Text.literal("§f  • §7" + name + " §f(" + distance + "m)"), false);
            }
        }

        player.sendMessage(Text.literal("§6================================"), false);
    }
}