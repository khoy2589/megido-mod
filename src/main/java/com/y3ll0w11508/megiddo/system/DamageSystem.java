package com.y3ll0w11508.megiddo.system;

import com.y3ll0w11508.megiddo.Megiddo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

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

        // 1. Visual Effect
        Vec3 targetPos = target.position();
        Vec3 attackerPos = attacker.position();
        VisualSystem.spawnFullMegiddoEffect(world, targetPos, attackerPos);

        // 2. คำนวณความเสียหาย
        float maxHealth = target.getMaxHealth();
        Megiddo.LOGGER.info("💀 Target HP: {}/{}", target.getHealth(), maxHealth);

        // 3. สร้างความเสียหาย
        target.hurtServer(world ,world.damageSources().magic(), maxHealth);
        target.setRemainingFireTicks(100); // 5 วินาที (20 ticks = 1 วินาที)

        // 4. เสียง
        world.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.FIRECHARGE_USE,
                SoundSource.HOSTILE,
                1.0f,
                1.5f
        );
        world.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.5f, 2.0f);

        Megiddo.LOGGER.info("✅ Megiddo fired successfully!");
    }

    /**
     * ยิงหลายเป้าหมายพร้อมกัน
     * พร้อม Grid น้ำบนฟ้า
     *
     * @param attacker ผู้โจมตี
     * @param targets รายชื่อเป้าหมาย
     */
    @SuppressWarnings("resource")
    public static void fireMegiddoBatch(Player attacker, Iterable<LivingEntity> targets) {
        // ✅ แก้ไขตรงนี้
        if (!(attacker.level() instanceof ServerLevel world)) return;

        Vec3 playerPos = attacker.position();
        VisualSystem.spawnWaterGrid(world, playerPos, 60.0, 30);

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