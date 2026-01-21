package com.y3ll0w11508.megiddo.system;

import net.minecraft.world.entity.EntityType;
import java.util.List;

/**
 * ไฟล์เก็บค่าตั้งต่างๆ ของ Megiddo
 * แก้ไขตรงนี้แทนที่จะแก้ในโค้ดหลัก
 */
public class MegiddoConfig {

    // ========== ความเสียหาย ==========

    /** ความเสียหายเป็นกี่เท่าของ HP (5.0 = ตายแน่นอน) */
    public static final float DAMAGE_MULTIPLIER = 5.0f;

    // ========== Effect Durations (หน่วย: ticks, 20 ticks = 1 วินาที) ==========

    /** Instant Damage - ไม่ต้องยาวเพราะเป็น instant */
    public static final int INSTANT_DAMAGE_DURATION = 1;

    /** Wither Effect - ดูดเลือด (40 ticks = 2 วินาที) */
    public static final int WITHER_DURATION = 40;

    /** Glowing Effect - เรืองแสง (60 ticks = 3 วินาที) */
    public static final int GLOWING_DURATION = 60;

    /** Poison Effect - พิษ (100 ticks = 5 วินาที) */
    public static final int POISON_DURATION = 100;

    /** Slowness Effect - ชะลอ (60 ticks = 3 วินาที) */
    public static final int SLOWNESS_DURATION = 60;

    /** Weakness Effect - อ่อนแอ (100 ticks = 5 วินาที) */
    public static final int WEAKNESS_DURATION = 100;

    // ========== Effect Levels (0 = Level I, 1 = Level II, ...) ==========

    /** Instant Damage Level (255 = ความเสียหายสูงสุด) */
    public static final int INSTANT_DAMAGE_LEVEL = 255;

    /** Wither Level (1 = Level II) */
    public static final int WITHER_LEVEL = 1;

    /** Glowing Level (0 = Level I) */
    public static final int GLOWING_LEVEL = 0;

    /** Poison Level (2 = Level III) */
    public static final int POISON_LEVEL = 2;

    /** Slowness Level (4 = Level V - เกือบหยุดนิ่ง) */
    public static final int SLOWNESS_LEVEL = 4;

    /** Weakness Level (2 = Level III) */
    public static final int WEAKNESS_LEVEL = 2;

    // ========== Visual Effects ==========

    /** ความสูงของ Grid น้ำเหนือพื้น (blocks) */
    public static final double GRID_HEIGHT = 25.0;

    /** ความสูงต่ำสุดของจุดหักเหเหนือเป้าหมาย (blocks) */
    public static final double MIN_REFRACT_HEIGHT = 3.0;

    /** ความสูงสูงสุดของจุดหักเหเหนือเป้าหมาย (blocks) */
    public static final double MAX_REFRACT_HEIGHT = 5.0;

    /** ความหนาแน่นของ Particle (ตัวเลขยิ่งมากยิ่งหนาแน่น) */
    public static final int WATER_GRID_DENSITY = 30;

    /** ระยะห่างระหว่าง Particle ของ Laser (ยิ่งต่ำยิ่งหนา) */
    public static final double LASER_PARTICLE_DENSITY = 0.3;

    // ========== Targeting ==========

    /** ระยะต่ำสุดในการยิง (blocks) */
    public static final double DEFAULT_MIN_RANGE = 5.0;

    /** ระยะสูงสุดในการยิง (blocks) */
    public static final double DEFAULT_MAX_RANGE = 60.0;

    /** Mob ที่ห้ามโจมตี (Whitelist) */
    public static final List<EntityType<?>> WHITELIST = List.of(
            EntityType.CAT,
            EntityType.WOLF
            // เพิ่มได้ตรงนี้ เช่น:
            // EntityType.VILLAGER,
            // EntityType.IRON_GOLEM
    );

    /** ป้องกัน Mob ที่มี Custom Name (nametag) หรือไม่ */
    public static final boolean PROTECT_NAMED_MOBS = false;

    // ========== Sounds ==========

    /** ระดับเสียง Laser (0.0 - 1.0) */
    public static final float LASER_VOLUME = 1.0f;

    /** ระดับเสียง Explosion (0.0 - 1.0) */
    public static final float EXPLOSION_VOLUME = 0.5f;

    /** Pitch เสียง Laser (สูงกว่า = เสียงสูงขึ้น) */
    public static final float LASER_PITCH = 1.5f;

    /** Pitch เสียง Explosion */
    public static final float EXPLOSION_PITCH = 2.0f;
}