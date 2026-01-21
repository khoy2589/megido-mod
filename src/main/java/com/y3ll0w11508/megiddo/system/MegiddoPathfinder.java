package com.y3ll0w11508.megiddo.system;

import com.y3ll0w11508.megiddo.Megiddo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ระบบหาเส้นทาง Laser สำหรับ Megiddo
 *
 * Features:
 * - ทะลุ Block ได้สูงสุด 10 ชั้น
 * - ถ้าเกิน 10 ชั้นไม่เจออากาศ → Laser หาย
 * - ถ้าเจออากาศ → สร้างจุดหักเห แล้วยิงต่อได้อีก 1 เส้น
 */
public class MegiddoPathfinder {

    private static final Random RANDOM = new Random();

    // Constants
    private static final int MAX_SOLID_BLOCKS = 10;  // ทะลุได้สูงสุด 10 ชั้น
    private static final double MIN_REFRACT_DISTANCE = 5.0;   // ระยะห่างต่ำสุด
    private static final double MAX_REFRACT_DISTANCE = 15.0;  // ระยะห่างสูงสุด
    private static final double GRID_HEIGHT = 25.0;           // ความสูงของ Grid

    /**
     * คำนวณเส้นทาง Laser จากฟ้าไปยังเป้าหมาย
     *
     * @return List ของจุดหักเห (ไม่รวมจุดเริ่มต้นและจุดสุดท้าย)
     *         หรือ null ถ้าหาเส้นทางไม่ได้
     */
    public static List<Vec3> calculatePath(Level level, Vec3 targetPos) {
        List<Vec3> refractionPoints = new ArrayList<>();

        // จุดเริ่มต้น: บนฟ้า
        Vec3 currentPos = new Vec3(
                targetPos.x + (RANDOM.nextDouble() - 0.5) * 8.0,
                targetPos.y + GRID_HEIGHT,
                targetPos.z + (RANDOM.nextDouble() - 0.5) * 8.0
        );

        Megiddo.LOGGER.debug("🔍 Pathfinding from Y={} to Y={}",
                currentPos.y, targetPos.y);

        int iterations = 0;
        int maxIterations = 20; // ป้องกัน infinite loop

        while (currentPos.y > targetPos.y + 2.0 && iterations < maxIterations) {
            iterations++;

            // คำนวณระยะทางที่เหลือ
            double remainingDistance = currentPos.distanceTo(targetPos);

            if (remainingDistance < 3.0) {
                // ใกล้เป้าหมายแล้ว ไม่ต้องสร้างจุดหักเหเพิ่ม
                break;
            }

            // หาจุดหักเหถัดไป
            Vec3 nextRefractPoint = findNextRefractionPoint(
                    level, currentPos, targetPos, remainingDistance
            );

            if (nextRefractPoint == null) {
                // หาเส้นทางไม่ได้ (Block ทึบเกินไป)
                Megiddo.LOGGER.warn("⚠️ Laser blocked: Cannot penetrate more than {} solid blocks",
                        MAX_SOLID_BLOCKS);
                return null;
            }

            refractionPoints.add(nextRefractPoint);
            currentPos = nextRefractPoint;

            Megiddo.LOGGER.debug("  ✓ Refraction point #{} at Y={}",
                    iterations, nextRefractPoint.y);
        }

        Megiddo.LOGGER.info("✅ Path found with {} refraction points",
                refractionPoints.size());

        return refractionPoints;
    }

    /**
     * หาจุดหักเหถัดไป
     */
    private static Vec3 findNextRefractionPoint(Level level, Vec3 currentPos,
                                                Vec3 targetPos, double remainingDistance) {
        // คำนวณทิศทางไปยังเป้าหมาย
        Vec3 direction = targetPos.subtract(currentPos).normalize();

        double stepDistance = MIN_REFRACT_DISTANCE +
                RANDOM.nextDouble() * (MAX_REFRACT_DISTANCE - MIN_REFRACT_DISTANCE);

        // ถ้าเหลือระยะน้อย ให้ใช้ระยะที่เหลือ
        stepDistance = Math.min(stepDistance, remainingDistance * 0.5);

        // จุดที่จะไปถึง (Ideal position)
        Vec3 idealPos = currentPos.add(direction.scale(stepDistance));

        // หา Air Pocket ใกล้ๆ จุดนี้
        Vec3 airPocket = findNearbyAirPocket(level, idealPos, 3);

        if (airPocket == null) {
            // ไม่เจอ Air Pocket → ลองเดินลงต่อ
            Megiddo.LOGGER.debug("  ⏩ No air pocket, moving down...");

            // เดินลงไปอีก 5 blocks
            idealPos = idealPos.add(0, -5, 0);
            airPocket = findNearbyAirPocket(level, idealPos, 5);
        }

        return airPocket; // อาจ null ถ้าหาไม่เจอ
    }

    /**
     * หา Air Pocket (ช่องว่าง) ใกล้ๆ ตำแหน่งที่กำหนด
     */
    private static Vec3 findNearbyAirPocket(Level level, Vec3 position, int searchRadius) {
        BlockPos centerPos = BlockPos.containing(position);

        // นับ Block ทึบที่ทะลุผ่านมา
        int solidBlockCount = 0;

        // ค้นหาจากบนลงล่าง
        for (int y = 0; y <= searchRadius * 2; y++) {
            BlockPos checkPos = centerPos.below(y);
            BlockState state = level.getBlockState(checkPos);

            if (isPassable(state)) {
                // เจอช่องว่าง!
                Vec3 airPos = new Vec3(
                        checkPos.getX() + 0.5 + (RANDOM.nextDouble() - 0.5) * 0.3,
                        checkPos.getY() + 0.5,
                        checkPos.getZ() + 0.5 + (RANDOM.nextDouble() - 0.5) * 0.3
                );

                Megiddo.LOGGER.debug("    💨 Found air pocket at Y={} (passed {} solid blocks)",
                        checkPos.getY(), solidBlockCount);

                return airPos;
            } else {
                solidBlockCount++;

                if (solidBlockCount > MAX_SOLID_BLOCKS) {
                    // ทะลุ Block ทึบเกิน 10 ชั้นแล้ว → Laser หายไป
                    Megiddo.LOGGER.debug("    🚫 Too many solid blocks ({} > {})",
                            solidBlockCount, MAX_SOLID_BLOCKS);
                    return null;
                }
            }
        }

        // ไม่เจอช่องว่างในรัศมีค้นหา
        return null;
    }

    /**
     * เช็คว่า Block นี้ผ่านได้ไหม (Air, Water, Lava, etc.)
     */
    private static boolean isPassable(BlockState state) {
        // Air
        if (state.isAir()) {
            return true;
        }

        // Liquid (Water, Lava)
        if (state.liquid()) {
            return true;
        }

        // Glass-like (โปร่งแสง)
        // if (!state.canOcclude()) {
        //     return true;
        // }

        return false;
    }

    /**
     * สร้างเส้นทางสำหรับเป้าหมายกลางแจ้ง (Outdoor)
     */
    public static List<Vec3> createOutdoorPath(Vec3 targetPos) {
        List<Vec3> path = new ArrayList<>();

        // จุดหักเหเดียว (เหนือเป้าหมาย 3-5 blocks)
        double refractHeight = 3.0 + RANDOM.nextDouble() * 2.0;
        Vec3 refractPos = new Vec3(
                targetPos.x + (RANDOM.nextDouble() - 0.5) * 0.5,
                targetPos.y + refractHeight,
                targetPos.z + (RANDOM.nextDouble() - 0.5) * 0.5
        );

        path.add(refractPos);

        return path;
    }
}