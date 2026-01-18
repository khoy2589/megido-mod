package com.y3ll0w11508.megiddo.system;

import com.y3ll0w11508.megiddo.Megiddo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * ระบบ Visual Effects สำหรับ Megiddo
 *
 * Concept:
 * 1. Grid น้ำลอยฟ้า (25 blocks เหนือพื้น)
 * 2. จุดหักเหแสง (Water Droplet Lens) เหนือศัตรู 3-5 blocks
 * 3. เลเซอร์ 2 เส้น: ฟ้า -> จุดหักเห -> ศัตรู
 *
 * Phase 4.2: Visual Effects
 */
public class VisualSystem {

    private static final Random RANDOM = new Random();

    // ความสูงของ Grid น้ำเหนือพื้น
    private static final double GRID_HEIGHT = 25.0;

    // ความสูงของจุดหักเหเหนือเป้าหมาย (3-5 blocks สุ่ม)
    private static final double MIN_REFRACT_HEIGHT = 3.0;
    private static final double MAX_REFRACT_HEIGHT = 5.0;

    /**
     * สร้าง Grid น้ำบนท้องฟ้า (จานรับแสง)
     *
     * @param world Server World
     * @param center จุดศูนย์กลาง (ตำแหน่งผู้เล่น)
     * @param radius รัศมีของ Grid
     * @param density ความหนาแน่น (จำนวน Particle)
     */
    public static void spawnWaterGrid(ServerLevel world, Vec3 center, double radius, int density) {
        Megiddo.LOGGER.debug("🌊 Spawning water grid at {}, {}, {}",
                center.x, center.y, center.z);

        for (int i = 0; i < density; i++) {
            // สุ่มตำแหน่ง X, Z ภายในวงกลม
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double distance = RANDOM.nextDouble() * radius;

            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;
            double y = center.y + GRID_HEIGHT;

            // Particle หยดน้ำ
            world.sendParticles(
                    ParticleTypes.DRIPPING_WATER,
                    x, y, z,
                    1,    // Count
                    0.1,  // Delta X
                    0.0,  // Delta Y
                    0.1,  // Delta Z
                    0.0   // Speed
            );

            // เพิ่ม Particle SPLASH เป็นช่วงๆ เพื่อให้ดูเหมือนผิวน้ำ
            if (RANDOM.nextFloat() < 0.3f) {
                world.sendParticles(
                        ParticleTypes.SPLASH,
                        x, y, z,
                        3,
                        0.2, 0.1, 0.2,
                        0.0
                );
            }
        }
    }

    /**
     * สร้างจุดหักเหแสง (Water Droplet Lens)
     *
     * @param world Server World
     * @param position ตำแหน่งของจุดหักเห
     */
    public static void spawnRefractionPoint(ServerLevel world, Vec3 position) {
        // Particle หยดน้ำ (Lens)
        world.sendParticles(
                ParticleTypes.BUBBLE,
                position.x, position.y, position.z,
                8,    // Count
                0.15, // Delta X
                0.15, // Delta Y
                0.15, // Delta Z
                0.02  // Speed
        );

        // แสงวาบ
        world.sendParticles(
                ParticleTypes.END_ROD,
                position.x, position.y, position.z,
                2,
                0.1, 0.1, 0.1,
                0.01
        );

        // เพิ่ม Glow Effect
        world.sendParticles(
                ParticleTypes.GLOW,
                position.x, position.y, position.z,
                5,
                0.2, 0.2, 0.2,
                0.0
        );
    }

    /**
     * สร้างเลเซอร์ (Beam) ระหว่าง 2 จุด
     *
     * @param world Server World
     * @param start จุดเริ่มต้น
     * @param end จุดสิ้นสุด
     * @param particleDensity ระยะห่างระหว่าง Particle (ยิ่งต่ำยิ่งหนาแน่น)
     */
    public static void spawnLaserBeam(ServerLevel world, Vec3 start, Vec3 end,
                                      double particleDensity) {
        double distance = start.distanceTo(end);
        Vec3 direction = end.subtract(start).normalize();

        Megiddo.LOGGER.debug("⚡ Spawning laser beam ({}m)", String.format("%.1f", distance));

        // Loop สร้าง Particle ตามเส้นทาง
        for (double d = 0; d < distance; d += particleDensity) {
            Vec3 currentPos = start.add(direction.scale(d));

            // Particle หลัก: END_ROD (เลเซอร์สีขาว)
            world.sendParticles(
                    ParticleTypes.END_ROD,
                    currentPos.x, currentPos.y, currentPos.z,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );

            // เพิ่ม Particle รอง: ELECTRIC_SPARK (ทำให้ดูเหมือนพลังงาน)
            if (RANDOM.nextFloat() < 0.2f) {
                world.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        currentPos.x, currentPos.y, currentPos.z,
                        1,
                        0.05, 0.05, 0.05,
                        0.01
                );
            }
        }
    }

    /**
     * สร้าง Impact Effect ตรงจุดกระทบ
     */
    public static void spawnImpactEffect(ServerLevel world, Vec3 position) {
        // ระเบิด
        world.sendParticles(
                ParticleTypes.EXPLOSION,
                position.x, position.y, position.z,
                1,
                0.0, 0.0, 0.0,
                0.0
        );

        // ควันไฟ
        world.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                position.x, position.y, position.z,
                10,
                0.3, 0.5, 0.3,
                0.05
        );

        // เปลวไฟ
        world.sendParticles(
                ParticleTypes.FLAME,
                position.x, position.y, position.z,
                15,
                0.4, 0.3, 0.4,
                0.1
        );

        // แสงวาบ (Glow)
        world.sendParticles(
                ParticleTypes.GLOW,
                position.x, position.y, position.z,
                20,
                0.5, 0.5, 0.5,
                0.2
        );
    }

    /**
     * สร้าง Effect เต็มรูปแบบสำหรับการยิง Megiddo ครั้งเดียว
     *
     * @param world Server World
     * @param targetPos ตำแหน่งศัตรู
     * @param playerPos ตำแหน่งผู้เล่น (สำหรับคำนวณ Grid)
     */
    public static void spawnFullMegiddoEffect(ServerLevel world, Vec3 targetPos, Vec3 playerPos) {
        Megiddo.LOGGER.debug("✨ Spawning full Megiddo effect");

        // 1. คำนวณจุดกำเนิดแสง (บนฟ้า)
        // สุ่มตำแหน่งให้ไม่ตรงดิ่ง ทำให้ดูเป็นธรรมชาติ
        double offsetX = (RANDOM.nextDouble() - 0.5) * 8.0; // ±4 blocks
        double offsetZ = (RANDOM.nextDouble() - 0.5) * 8.0;
        Vec3 sourcePos = new Vec3(
                targetPos.x + offsetX,
                targetPos.y + GRID_HEIGHT,
                targetPos.z + offsetZ
        );

        // 2. คำนวณจุดหักเห (เหนือศัตรู 3-5 blocks)
        double refractHeight = MIN_REFRACT_HEIGHT +
                RANDOM.nextDouble() * (MAX_REFRACT_HEIGHT - MIN_REFRACT_HEIGHT);
        double refractOffsetX = (RANDOM.nextDouble() - 0.5) * 0.5;
        double refractOffsetZ = (RANDOM.nextDouble() - 0.5) * 0.5;
        Vec3 refractPos = new Vec3(
                targetPos.x + refractOffsetX,
                targetPos.y + refractHeight,
                targetPos.z + refractOffsetZ
        );

        // 3. สร้าง Visual ตามลำดับ

        // 3.1 จุดหักเห (Water Droplet)
        spawnRefractionPoint(world, refractPos);

        // 3.2 เส้นแสงที่ 1: ฟ้า -> จุดหักเห
        spawnLaserBeam(world, sourcePos, refractPos, 0.5);

        // 3.3 เส้นแสงที่ 2: จุดหักเห -> ศัตรู (หนาแน่นกว่า)
        spawnLaserBeam(world, refractPos, targetPos, 0.3);

        // 3.4 Impact Effect
        spawnImpactEffect(world, targetPos);
    }

    /**
     * สร้าง Effect แบบหักเหหลายครั้ง (สำหรับเป้าหมายในร่ม)
     *
     * @param world Server World
     * @param targetPos ตำแหน่งศัตรู
     * @param playerPos ตำแหน่งผู้เล่น
     * @param blockingBlockPos ตำแหน่ง Block ที่บัง
     */
    public static void spawnIndoorMegiddoEffect(ServerLevel world, Vec3 targetPos,
                                                Vec3 playerPos, net.minecraft.core.BlockPos blockingBlockPos) {
        Megiddo.LOGGER.debug("🏠 Spawning indoor Megiddo effect with multiple refractions");

        // 1. จุดกำเนิดแสง (บนฟ้า)
        double offsetX = (RANDOM.nextDouble() - 0.5) * 8.0;
        double offsetZ = (RANDOM.nextDouble() - 0.5) * 8.0;
        Vec3 sourcePos = new Vec3(
                targetPos.x + offsetX,
                targetPos.y + GRID_HEIGHT,
                targetPos.z + offsetZ
        );

        // 2. จุดหักเหที่ 1: ใต้ block ที่บัง (0.5 blocks ใต้ block)
        Vec3 refract1Pos = new Vec3(
                blockingBlockPos.getX() + 0.5 + (RANDOM.nextDouble() - 0.5) * 0.3,
                blockingBlockPos.getY() - 0.5,
                blockingBlockPos.getZ() + 0.5 + (RANDOM.nextDouble() - 0.5) * 0.3
        );

        // 3. จุดหักเหที่ 2: เหนือศัตรู (3-5 blocks)
        double refract2Height = MIN_REFRACT_HEIGHT +
                RANDOM.nextDouble() * (MAX_REFRACT_HEIGHT - MIN_REFRACT_HEIGHT);
        Vec3 refract2Pos = new Vec3(
                targetPos.x + (RANDOM.nextDouble() - 0.5) * 0.5,
                targetPos.y + refract2Height,
                targetPos.z + (RANDOM.nextDouble() - 0.5) * 0.5
        );

        // 4. สร้าง Visual

        // 4.1 จุดหักเหที่ 1 (ใต้ block)
        spawnRefractionPoint(world, refract1Pos);

        // 4.2 จุดหักเหที่ 2 (เหนือศัตรู)
        spawnRefractionPoint(world, refract2Pos);

        // 4.3 เส้นแสงที่ 1: ฟ้า -> จุดหักเหที่ 1 (ใต้ block)
        spawnLaserBeam(world, sourcePos, refract1Pos, 0.5);

        // 4.4 เส้นแสงที่ 2: จุดหักเหที่ 1 -> จุดหักเหที่ 2
        spawnLaserBeam(world, refract1Pos, refract2Pos, 0.4);

        // 4.5 เส้นแสงที่ 3: จุดหักเหที่ 2 -> ศัตรู (หนาแน่นที่สุด)
        spawnLaserBeam(world, refract2Pos, targetPos, 0.3);

        // 4.6 Impact Effect
        spawnImpactEffect(world, targetPos);

        // 4.7 Particle พิเศษที่จุดหักเหที่ 1 (ใต้ block)
        world.sendParticles(
                net.minecraft.core.particles.ParticleTypes.SPLASH,
                refract1Pos.x, refract1Pos.y, refract1Pos.z,
                10,
                0.3, 0.1, 0.3,
                0.05
        );
    }

    /**
     * คำนวณตำแหน่งจุดหักเหแสง (สำหรับใช้ภายนอก)
     *
     * @param targetPos ตำแหน่งเป้าหมาย
     * @return ตำแหน่งจุดหักเห
     */
    public static Vec3 calculateRefractionPoint(Vec3 targetPos) {
        double height = MIN_REFRACT_HEIGHT +
                RANDOM.nextDouble() * (MAX_REFRACT_HEIGHT - MIN_REFRACT_HEIGHT);
        double offsetX = (RANDOM.nextDouble() - 0.5) * 0.5;
        double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.5;

        return new Vec3(
                targetPos.x + offsetX,
                targetPos.y + height,
                targetPos.z + offsetZ
        );
    }
}