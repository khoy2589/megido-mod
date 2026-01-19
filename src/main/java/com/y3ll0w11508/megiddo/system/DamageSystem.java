package com.y3ll0w11508.megiddo.system;

import com.y3ll0w11508.megiddo.Megiddo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * ระบบสร้างความเสียหายของ Megiddo
 * Instant Kill = Max HP ของเป้าหมาย
 *
 * Phase 4.1: Basic Damage + Sound
 */
public class DamageSystem {

    /**
     * ยิง Megiddo ใส่เป้าหมายเดียว
     *
     * @param attacker ผู้โจมตี
     * @param target เป้าหมาย
     */
    public static void fireMegiddo(Player attacker, LivingEntity target) {
        // เช็คว่าเป็น Server-side
        if (!(target.level() instanceof ServerLevel world)) return;

        Megiddo.LOGGER.info("⚡ Firing Megiddo at {}", target.getType().getDescription().getString());

        Vec3 targetPos = target.position();
        Vec3 attackerPos = attacker.position();

        // 1. เช็คว่าเป้าหมายอยู่ในร่มหรือไม่
        net.minecraft.core.BlockPos blockingBlock =
                com.y3ll0w11508.megiddo.system.TargetingSystem.findBlockingBlock(target);

        if (blockingBlock != null) {
            // 🏠 เป้าหมายอยู่ในร่ม - ใช้การหักเหแบบซับซ้อน
            Megiddo.LOGGER.info("🏠 Target is indoors, using advanced refraction");
            VisualSystem.spawnIndoorMegiddoEffect(world, targetPos, attackerPos, blockingBlock);
        } else {
            // ☀️ เป้าหมายอยู่กลางแจ้ง - ใช้การหักเหปกติ
            Megiddo.LOGGER.info("☀️ Target is outdoors, using normal refraction");
            VisualSystem.spawnFullMegiddoEffect(world, targetPos, attackerPos);
        }

        // 2. คำนวณความเสียหาย = HP สูงสุด (Instant Kill)
        float maxHealth = target.getMaxHealth();
        Megiddo.LOGGER.info("💀 Target HP: {}/{}", target.getHealth(), maxHealth);

        // 3. สร้างความเสียหายแบบ "Magic" (ทะลุเกราะ)
        // ✅ ใช้ hurtServer แทน hurt (ไม่ deprecated)
        target.hurtServer(world, world.damageSources().magic(), maxHealth);

        // 4. เพิ่ม Effect: ติดไฟ (เพราะเป็นความร้อนจากแสง)
        target.setRemainingFireTicks(100); // 5 วินาที (20 ticks = 1 วินาที)


        // 5. เล่นเสียง: เสียงพุ่งเลเซอร์
        world.playSound(
                null, // ให้ทุกคนในบริเวณได้ยิน
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.FIRECHARGE_USE, // เสียงไฟพุ่ง
                SoundSource.HOSTILE,
                1.0f, // Volume
                1.5f  // Pitch (สูงหน่อยให้ฟังดูเหมือนเลเซอร์)
        );

        // 6. เล่นเสียง: เสียง Impact
        world.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                0.5f, // Volume ต่ำกว่า
                2.0f  // Pitch สูง
        );

        Megiddo.LOGGER.info("✅ Megiddo fired successfully!");
    }

    /**
     * ยิงหลายเป้าหมายพร้อมกัน
     * พร้อม Grid น้ำบนฟ้า
     *
     * @param attacker ผู้โจมตี
     * @param targets รายชื่อเป้าหมาย
     */
    public static void fireMegiddoBatch(Player attacker, Iterable<LivingEntity> targets) {
        // เช็คว่าเป็น Server-side
        if (!(attacker.level() instanceof ServerLevel world)) return;

        // 1. สร้าง Grid น้ำครั้งเดียว (ครอบคลุมพื้นที่ทั้งหมด)
        Vec3 playerPos = attacker.position();
        VisualSystem.spawnWaterGrid(world, playerPos, 60.0, 30);

        // 2. ยิงแต่ละเป้าหมาย
        int count = 0;
        for (LivingEntity target : targets) {
            fireMegiddo(attacker, target);
            count++;
        }

        Megiddo.LOGGER.info("🎯 Fired Megiddo at {} targets", count);
    }

    /**
     * ยิงแบบมี Delay (สำหรับใช้ใน Tick Loop)
     * จะใช้ตอน Phase 4.3
     *
     * @return true ถ้ายิงเสร็จแล้ว, false ถ้ายังรอ
     */
    public static boolean fireWithDelay(Player attacker, LivingEntity target,
                                        int currentTick, int targetTick) {
        if (currentTick >= targetTick) {
            fireMegiddo(attacker, target);
            return true; // Done
        }
        return false; // Waiting
    }
}