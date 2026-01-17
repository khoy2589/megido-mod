package com.y3ll0w11508.megiddo.system;

import com.y3ll0w11508.megiddo.Megiddo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/**
 * ระบบหลักของ Megiddo
 * จัดการ Targeting, Visual, Damage แบบครบวงจร
 *
 * Phase 4.3: Integration
 */
public class MegiddoSystem {

    // เก็บคิวของการยิงแต่ละผู้เล่น (สำหรับทำ Delay ระหว่างเป้าหมาย)
    private static final Map<UUID, Queue<TargetData>> FIRING_QUEUE = new HashMap<>();

    // Delay ระหว่างการยิงแต่ละเป้าหมาย (ticks)
    private static final int DELAY_BETWEEN_SHOTS = 3; // 0.15 วินาที

    /**
     * เริ่มยิง Megiddo (แบบมี Delay - ยิงทีละตัว)
     * เหมาะสำหรับใช้กับ UI Toggle
     *
     * @param player ผู้เล่น
     * @param minRadius รัศมีต่ำสุด
     * @param maxRadius รัศมีสูงสุด
     */
    public static void activate(ServerPlayer player, double minRadius, double maxRadius) {
        ServerLevel world = player.serverLevel();

        Megiddo.LOGGER.info("🎯 Activating Megiddo for {}", player.getName().getString());

        // 1. หาเป้าหมาย
        List<LivingEntity> targets = TargetingSystem.findTargets(player, minRadius, maxRadius);

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§eNo valid targets found!"));
            return;
        }

        // 2. สร้างคิวสำหรับยิงทีละตัว
        Queue<TargetData> queue = new LinkedList<>();
        int tickDelay = 0;

        for (LivingEntity target : targets) {
            queue.add(new TargetData(target, tickDelay));
            tickDelay += DELAY_BETWEEN_SHOTS;
        }

        FIRING_QUEUE.put(player.getUUID(), queue);

        // 3. แสดง Grid น้ำ (ครั้งเดียว)
        Vec3 playerPos = player.position();
        VisualSystem.spawnWaterGrid(world, playerPos, maxRadius, 30);

        player.sendSystemMessage(Component.literal(
                "§6⚡ Megiddo Activated! §f" + targets.size() + " targets locked."
        ));

        Megiddo.LOGGER.info("✅ Megiddo activated: {} targets queued", targets.size());
    }

    /**
     * Tick Loop สำหรับยิงทีละเป้าหมาย
     * ต้องเรียกจาก PlayerTickEvent หรือ Mixin
     *
     * @param player ผู้เล่น
     */
    public static void tick(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Queue<TargetData> queue = FIRING_QUEUE.get(playerId);

        if (queue == null || queue.isEmpty()) {
            FIRING_QUEUE.remove(playerId);
            return;
        }

        ServerLevel world = player.serverLevel();
        TargetData data = queue.peek();

        // เช็คว่าถึงเวลายิงยัง
        data.currentTick++;
        if (data.currentTick >= data.fireAtTick) {
            LivingEntity target = data.target;

            // ยิง!
            if (target.isAlive()) {
                DamageSystem.fireMegiddo(player, target);
            } else {
                Megiddo.LOGGER.debug("⏭️ Skipping dead target: {}",
                        target.getType().getDescription().getString());
            }

            queue.poll(); // ลบเป้าหมายนี้ออกจากคิว
        }
    }

    /**
     * ยิงแบบ Instant (ไม่มี Delay) - สำหรับ Command
     * ยิงทุกเป้าหมายพร้อมกัน
     *
     * @param player ผู้เล่น
     * @param minRadius รัศมีต่ำสุด
     * @param maxRadius รัศมีสูงสุด
     */
    public static void fireInstant(ServerPlayer player, double minRadius, double maxRadius) {
        ServerLevel world = player.serverLevel();

        Megiddo.LOGGER.info("⚡ Firing instant Megiddo");

        // 1. หาเป้าหมาย
        List<LivingEntity> targets = TargetingSystem.findTargets(player, minRadius, maxRadius);

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§eNo valid targets found!"));
            return;
        }

        // 2. ยิงทุกเป้าหมายพร้อมกัน
        DamageSystem.fireMegiddoBatch(player, targets);

        // 3. แจ้งผลลัพธ์
        player.sendSystemMessage(Component.literal(
                "§6⚡ Megiddo! §c" + targets.size() + " §6targets eliminated!"
        ));

        Megiddo.LOGGER.info("✅ Instant Megiddo complete: {} targets", targets.size());
    }

    /**
     * หยุด Megiddo ของผู้เล่น
     *
     * @param player ผู้เล่น
     */
    public static void deactivate(ServerPlayer player) {
        Queue<TargetData> removed = FIRING_QUEUE.remove(player.getUUID());

        if (removed != null && !removed.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cMegiddo deactivated."));
            Megiddo.LOGGER.info("🛑 Megiddo deactivated for {}", player.getName().getString());
        }
    }

    /**
     * เช็คว่าผู้เล่นกำลังใช้ Megiddo อยู่หรือไม่
     */
    public static boolean isActive(ServerPlayer player) {
        Queue<TargetData> queue = FIRING_QUEUE.get(player.getUUID());
        return queue != null && !queue.isEmpty();
    }

    /**
     * ล้างคิวทั้งหมด (เรียกเมื่อ Server หยุด)
     */
    public static void clearAll() {
        int count = FIRING_QUEUE.size();
        FIRING_QUEUE.clear();
        Megiddo.LOGGER.info("🧹 Cleared {} active Megiddo queues", count);
    }

    // ========== Inner Class ==========

    /**
     * เก็บข้อมูลเป้าหมายและเวลาที่จะยิง
     */
    private static class TargetData {
        final LivingEntity target;
        final int fireAtTick;
        int currentTick = 0;

        TargetData(LivingEntity target, int fireAtTick) {
            this.target = target;
            this.fireAtTick = fireAtTick;
        }
    }
}