package capstone.main.Enemies;

/**
 * Parameterized configuration for Greed boss.
 * Centralized so tuning requires no changes to behavior code.
 */

public final class GreedConfig {
    private GreedConfig() {}

    // Core stats
    public static float MAX_HP = 1200f;

    // Global cadence
    public static float COOLDOWN_MIN = 2.5f;
    public static float COOLDOWN_MAX = 4.0f;

    // SLAM
    public static float SLAM_TELEGRAPH = 0.9f; // seconds
    public static float SLAM_RANGE = 3.2f;     // world units
    public static int   SLAM_DAMAGE = 30;

    // CONE BARRAGE
    public static float BARRAGE_TELEGRAPH = 1.1f; // seconds
    public static float BARRAGE_CONE_HALF_ANGLE = 40f; // degrees
    public static float BARRAGE_RANGE = 8.5f;         // world units
    public static int   BARRAGE_DAMAGE = 18;          // single-hit abstraction

    // RING WAVE
    public static float RING_TELEGRAPH = 1.2f; // seconds
    public static float RING_RANGE = 6.5f;     // world units
    public static int   RING_DAMAGE = 12;
}
