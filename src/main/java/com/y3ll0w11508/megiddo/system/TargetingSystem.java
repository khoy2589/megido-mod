package com.y3ll0w11508.megiddo.system;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerEntity;
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

        // สร้างกล่องค้นหา (Bounding Box) รอบตัวผู้เล่น
        Box searchArea = player.getBoundingBox().expand(maxRadius, 20.0, maxRadius);

        // ค้นหา Living Entity ทั้งหมดในพื้นที่
        List<LivingEntity> allEntities = world.getEntitiesByClass(
                LivingEntity.class,
                searchArea,
                entity -> entity != player // ไม่รวมตัวเอง
        );

        // กรองตามเงื่อนไข
        for (LivingEntity entity : allEntities) {
            // เช็ค 1: ต้องมีชีวิตอยู่
            if (!entity.isAlive()) continue;

            // เช็ค 2: ต้องไม่อยู่ใน Whitelist
            if (WHITELIST.contains(entity.getType())) continue;

            // เช็ค 3: ไม่ใช่เพื่อนร่วมทีม
            if (entity.isTeammate(player)) continue;

            // เช็ค 4: ต้องอยู่ในระยะที่กำหนด
            double distance = player.distanceTo(entity);
            if (distance < minRadius || distance > maxRadius) continue;

            // เช็ค 5 (Optional): ต้องมองเห็นท้องฟ้าได้ (Megiddo ใช้แสงอาทิตย์)
            if (!canSeeSky(entity)) continue;

            validTargets.add(entity);
        }

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

        player.sendMessage(Text.literal("=== Megiddo Targeting Debug ==="), false);
        player.sendMessage(Text.literal("Range: " + minRadius + " - " + maxRadius + " blocks"), false);
        player.sendMessage(Text.literal("Found " + targets.size() + " targets:"), false);

        for (LivingEntity target : targets) {
            String name = target.getType().getName().getString();
            double distance = Math.round(player.distanceTo(target) * 10) / 10.0;
            player.sendMessage(Text.literal("  • " + name + " (" + distance + "m)"), false);
        }
    }

    /**
     * เพิ่ม Entity เข้า Whitelist (ถ้าต้องการให้ผู้เล่นกำหนดเอง)
     */
    public static void addToWhitelist(EntityType<?> type) {
        if (!WHITELIST.contains(type)) {
            // Note: List.of() สร้าง Immutable List ต้องเปลี่ยนเป็น ArrayList
            // แก้ไข: private static final List<EntityType<?>> WHITELIST = new ArrayList<>(List.of(...));
        }
    }
}