package com.y3ll0w11508.megiddo.system;

import com.y3ll0w11508.megiddo.Megiddo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;

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
        if (target.level().isClientSide) return; // Server-side only

        ServerLevel world = (ServerLevel) target.level();

        Megiddo.LOGGER.info("⚡ Firing Megiddo at {}", target.getType().getDescription().getString());

        // 1. คำนวณความเสียหาย = HP สูงสุด (Instant Kill)
        float maxHealth = target.getMaxHealth();
        Megiddo.LOGGER.info("💀 Target HP: {}/{}", target.getHealth(), maxHealth);

        // 2. สร้างความเสียหายแบบ "Magic" (ทะลุเกราะ)
        target.hurt(world.damageSources().magic(), maxHealth);

        // 3. เพิ่ม Effect: ติดไฟ (เพราะเป็นความร้อนจากแสง)
        target.setRemainingFireTicks(100); // 5 วินาที (20 ticks = 1 วินาที)

        // 4. เล่นเสียง: เสียงพุ่งเลเซอร์
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

        // 5. เล่นเสียง: เสียง Impact
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

        // 6. Particle Effect พื้นฐาน (จะพัฒนาต่อใน Phase 4.2)
        spawnBasicImpactParticles(world, target);

        Megiddo.LOGGER.info("✅ Megiddo fired successfully!");
    }

    /**
     * ยิงหลายเป้าหมายพร้อมกัน
     *
     * @param attacker ผู้โจมตี
     * @param targets รายชื่อเป้าหมาย
     */
    public static void fireMegiddoBatch(Player attacker, Iterable<LivingEntity> targets) {
        int count = 0;
        for (LivingEntity target : targets) {
            fireMegiddo(attacker, target);
            count++;
        }
        Megiddo.LOGGER.info("🎯 Fired Megiddo at {} targets", count);
    }

    /**
     * Particle Effect พื้นฐาน (ตรงจุดกระทบ)
     * Phase 4.2 จะเพิ่ม Visual ที่สวยกว่านี้
     */
    private static void spawnBasicImpactParticles(ServerLevel world, LivingEntity target) {
        double x = target.getX();
        double y = target.getY() + target.getBbHeight() / 2; // กลางตัว
        double z = target.getZ();

        // Explosion Particle
        world.sendParticles(
                ParticleTypes.EXPLOSION,
                x, y, z,
                1,    // Count
                0.0,  // Delta X
                0.0,  // Delta Y
                0.0,  // Delta Z
                0.0   // Speed
        );

        // Smoke
        world.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                x, y, z,
                10,
                0.3, 0.5, 0.3,
                0.05
        );

        // Fire
        world.sendParticles(
                ParticleTypes.FLAME,
                x, y, z,
                15,
                0.4, 0.3, 0.4,
                0.1
        );
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