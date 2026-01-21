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

import static com.y3ll0w11508.megiddo.system.MegiddoConfig.*;

/**
 * ระบบสร้างความเสียหายของ Megiddo
 * Instant Kill = Max HP ของเป้าหมาย + Instant Damage Effect
 *
 * Phase 4.1: Basic Damage + Sound + Effects
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

        // 2,3. คำนวณความเสียหาย = HP สูงสุด (Instant Kill)
        float maxHealth = target.getMaxHealth();
        float overkillDamage = maxHealth * 5.0f;
        Megiddo.LOGGER.info("💀 Target HP: {}/{}", target.getHealth(), maxHealth);
        // 3. สร้างความเสียหายแบบ "Magic" (ทะลุเกราะ)
        target.hurtServer(world, world.damageSources().magic(), overkillDamage);

        // บังคับให้ตาย (สำหรับ Boss Mob ที่มี Healing พิเศษ)
        if (target.getHealth() > 0) {
            target.setHealth(0.0f);
        }

        Megiddo.LOGGER.info("💥 Dealt {} damage (5x overkill)", overkillDamage);

        // 4. เพิ่ม Effect: Instant Damage
        // ⚠️ สำคัญ: ใช้ MobEffects.INSTANT_DAMAGE
        // Instant Damage ใน Minecraft:
        // - Level 0 (I) = 3 hearts (6 HP)
        // - Level 1 (II) = 6 hearts (12 HP)
        // - Level 255 = ~128 hearts (256 HP)
        MobEffectInstance instantDamage = new MobEffectInstance(
                MobEffects.INSTANT_DAMAGE,
                INSTANT_DAMAGE_DURATION,                          // Duration (1 tick เพราะเป็น instant)
                INSTANT_DAMAGE_LEVEL,                        // Amplifier
                false,                      // Ambient (ไม่ใช่ effect จาก beacon/conduit)
                true,                       // Show particles (เห็น particles สีดำ-แดง)
                true                        // Show icon (แสดงไอคอนใน UI)
        );

        // ตัวเลือกเพิ่มเติม: Effect อื่นๆ ที่น่าสนใจ

        // Poison (พิษ - ไม่ทำงานกับ Undead)
         MobEffectInstance poison = new MobEffectInstance(
                 MobEffects.POISON,
                 POISON_DURATION,   // 5 วินาที
                 POISON_LEVEL,     // Level 3
                 false,
                 true,
                 true
         );
        // Slowness (ชะลอความเร็ว)
         MobEffectInstance slowness = new MobEffectInstance(
                 MobEffects.SLOWNESS,
                 SLOWNESS_DURATION,    // 3 วินาที
                 SLOWNESS_LEVEL,     // Level 5 (เกือบหยุดนิ่ง)
                 false,
                 true,
                 true
         );
        // Weakness (ลดความเสียหายที่ทำได้)
         MobEffectInstance weakness = new MobEffectInstance(
                 MobEffects.WEAKNESS,
                 WEAKNESS_DURATION,   // 5 วินาที
                 WEAKNESS_LEVEL,     // Level 3
                 false,
                 true,
                 true
         );



        // 5. เพิ่ม Wither Effect (ดูดเลือดแบบมืด - ทำงานกับ Undead ด้วย)
        // Wither แตกต่างจาก Poison ตรงที่:
        // - Poison ไม่ฆ่า (ลดเหลือ 0.5 hearts)
        // - Wither ฆ่าได้
        // - Wither ทำงานกับ Undead (Poison ไม่ทำงาน)
         MobEffectInstance wither = new MobEffectInstance(
                 MobEffects.WITHER,
                 WITHER_DURATION,    // 2 seconds
                 WITHER_LEVEL,     // Level 2
                 false,
                 true,
                 true
         );


        // 6. เพิ่ม Glowing Effect (เรืองแสง - เห็นผ่านกำแพง)
        MobEffectInstance glowing = new MobEffectInstance(
                MobEffects.GLOWING,
                GLOWING_DURATION,    // 3 seconds
                GLOWING_LEVEL,     // Level 1
                false,
                false, // ไม่แสดง particles
                true
        );

        target.addEffect(weakness);
        target.addEffect(slowness);
        target.addEffect(poison);
        target.addEffect(instantDamage);
        target.addEffect(wither);
        target.addEffect(glowing);


        // 7. เล่นเสียง: เสียงพุ่งเลเซอร์
        world.playSound(
                null, // ให้ทุกคนในบริเวณได้ยิน
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.FIRECHARGE_USE, // เสียงไฟพุ่ง
                SoundSource.HOSTILE,
                LASER_VOLUME, // Volume
                LASER_PITCH  // Pitch (สูงหน่อยให้ฟังดูเหมือนเลเซอร์)
        );

        // 8. เล่นเสียง: เสียง Impact
        world.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                EXPLOSION_VOLUME, // Volume ต่ำกว่า
                EXPLOSION_PITCH  // Pitch สูง
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