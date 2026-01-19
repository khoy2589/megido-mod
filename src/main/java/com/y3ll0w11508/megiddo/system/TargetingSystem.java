package com.y3ll0w11508.megiddo.system;

import com.y3ll0w11508.megiddo.Megiddo;

// ⚠️ MOJANG MAPPINGS - ใช้สำหรับ 1.21.11
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * ระบบค้นหาเป้าหมายสำหรับ Megiddo
 * Phase 1: ทดสอบด้วย Command ก่อน
 *
 * Mapping: Mojang (Official)
 */
public class TargetingSystem {

    // Whitelist: Mob ที่ห้ามโจมตี
    private static final List<EntityType<?>> WHITELIST = List.of(
//            EntityType.VILLAGER,
//            EntityType.IRON_GOLEM,
            EntityType.CAT,
            EntityType.WOLF//,
//            EntityType.HORSE,
//            EntityType.DONKEY
    );

    /**
     * หาศัตรูรอบตัวผู้เล่น
     */
    public static List<LivingEntity> findTargets(Player player, double minRadius, double maxRadius) {
        Level level = player.level();  // Yarn: getWorld()
        List<LivingEntity> validTargets = new ArrayList<>();

        Megiddo.LOGGER.info("🔍 Starting target scan...");
        Megiddo.LOGGER.info("📍 Player position: {}, {}, {}",
                player.getX(), player.getY(), player.getZ());

        // สร้างกล่องค้นหา (Bounding Box)
        AABB searchArea = player.getBoundingBox().inflate(maxRadius, 20.0, maxRadius);
        // Yarn: expand() -> Mojang: inflate()

        // ค้นหา Living Entity ทั้งหมด
        List<LivingEntity> allEntities = level.getEntitiesOfClass(
                // Yarn: getEntitiesByClass -> Mojang: getEntitiesOfClass
                LivingEntity.class,
                searchArea,
                entity -> entity != player
        );

        Megiddo.LOGGER.info("👁️ Found {} total entities", allEntities.size());

        // กรองตามเงื่อนไข
        for (LivingEntity entity : allEntities) {
            // เช็ค 1: มีชีวิต
            if (!entity.isAlive()) {
                Megiddo.LOGGER.debug("⏭️ Skip: {} - dead", getEntityName(entity));
                continue;
            }

            // เช็ค 2: ไม่อยู่ใน Whitelist
            if (WHITELIST.contains(entity.getType())) {
                Megiddo.LOGGER.debug("⏭️ Skip: {} - whitelisted", getEntityName(entity));
                continue;
            }

            // เช็ค 3: ไม่ใช่เพื่อน
            if (entity.isAlliedTo(player)) {
                // Yarn: isTeammate() -> Mojang: isAlliedTo()
                Megiddo.LOGGER.debug("⏭️ Skip: {} - ally", getEntityName(entity));
                continue;
            }

            // เช็ค 4: อยู่ในระยะ
            double distance = player.distanceTo(entity);
            if (distance < minRadius || distance > maxRadius) {
                Megiddo.LOGGER.debug("⏭️ Skip: {} - distance {} out of range",
                        getEntityName(entity), String.format("%.1f", distance));
                continue;
            }

            // เช็ค 5: มองเห็นท้องฟ้า (ไม่ได้ใช้)
            /*
            if (!canSeeSky(entity)) {
                Megiddo.LOGGER.debug("⏭️ Skip: {} - no sky", getEntityName(entity));
                continue;
            }
             */
            // แสดงสถานะว่าเป้าหมายอยู่กลางแจ้งหรือในร่ม (เพื่อ debug)
            String locationStatus = canSeeSky(entity) ? "outdoor ☀️" : "indoor 🏠";




            Megiddo.LOGGER.info("✅ Valid target: {} ({}blocks)",
                    getEntityName(entity), String.format("%.1f", distance));
            validTargets.add(entity);
        }

        Megiddo.LOGGER.info("🎯 Total valid targets: {}", validTargets.size());
        return validTargets;
    }

    /**
     * เช็คมองเห็นท้องฟ้า
     */
    private static boolean canSeeSky(LivingEntity entity) {
        return entity.level().canSeeSky(entity.blockPosition());
    }

    /**
     * หา Block ที่บังท้องฟ้า (สำหรับสร้างจุดหักเหพิเศษ)
     *
     * @return ตำแหน่งใต้ block ที่บัง, หรือ null ถ้ามองเห็นท้องฟ้า
     */
    public static net.minecraft.core.BlockPos findBlockingBlock(LivingEntity entity) {
        if (canSeeSky(entity)) {
            return null; // มองเห็นท้องฟ้า ไม่มี block บัง
        }

        // หา block แรกที่บังจากด้านบน
        net.minecraft.core.BlockPos entityPos = entity.blockPosition();
        Level level = entity.level();

        // วนหาจากหัวศัตรูขึ้นไปจนถึงความสูง 30 blocks
        for (int y = entityPos.getY() + 2; y <= entityPos.getY() + 30; y++) {
            net.minecraft.core.BlockPos checkPos = new net.minecraft.core.BlockPos(
                    entityPos.getX(),
                    y,
                    entityPos.getZ()
            );

            // เช็คว่า block นี้ทึบหรือไม่
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(checkPos);

            if (!state.isAir() && state.canOcclude()) {
                Megiddo.LOGGER.debug("🧱 Found blocking block at Y={} ({})",
                        y, state.getBlock().getName().getString());
                return checkPos;
            }
        }

        Megiddo.LOGGER.debug("⚠️ canSeeSky=false but no solid block found - using fallback");
        // ถ้าไม่เจอ block ทึบ ให้ใช้ตำแหน่ง 5 blocks เหนือศัตรู
        return entityPos.above(5);
    }

    /**
     * ดึงชื่อ Entity
     */
    private static String getEntityName(LivingEntity entity) {
        return entity.getType().getDescription().getString();
    }

    /**
     * Debug: แสดงรายชื่อศัตรูในแชท
     */
    public static void debugPrintTargets(ServerPlayer player, double minRadius, double maxRadius) {
        List<LivingEntity> targets = findTargets(player, minRadius, maxRadius);

        // Yarn: sendMessage(text, false)
        // Mojang: sendSystemMessage(component)
        player.sendSystemMessage(Component.literal("§6═══════════════════════════"));
        player.sendSystemMessage(Component.literal("§6  Megiddo Targeting Debug"));
        player.sendSystemMessage(Component.literal("§6═══════════════════════════"));
        player.sendSystemMessage(Component.literal("§eRange: §f" + minRadius + " - " + maxRadius + " blocks"));
        player.sendSystemMessage(Component.literal("§eFound: §a" + targets.size() + " §etargets"));
        player.sendSystemMessage(Component.literal(""));

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c  (No valid targets found)"));
        } else {
            for (int i = 0; i < targets.size(); i++) {
                LivingEntity target = targets.get(i);
                String name = getEntityName(target);
                double distance = Math.round(player.distanceTo(target) * 10) / 10.0;
                String location = canSeeSky(target) ? "§a☀ Outdoor" : "§b🏠 Indoor";

                player.sendSystemMessage(Component.literal(
                        "§f  " + (i + 1) + ". §7" + name + " §8(§f" + distance + "m§8)" + location
                ));
            }
        }

        player.sendSystemMessage(Component.literal("§6═══════════════════════════"));
    }
}