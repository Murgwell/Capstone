package capstone.main.Enemies;

/**
 * Parameterized configuration for Discaya boss.
 * Discaya is a World 2 boss with unique skill set focused on area denial and debuffs.
 */

public final class DiscayaConfig {
    private DiscayaConfig() {}

    // Core stats
    public static float MAX_HP = 1500f; // Higher HP than Greed for World 2

    // Global cadence
    public static float COOLDOWN_MIN = 2.8f;
    public static float COOLDOWN_MAX = 4.5f;

    // POISON CLOUD - Creates a lingering AoE damage zone
    public static float POISON_TELEGRAPH = 1.0f; // seconds
    public static float POISON_RANGE = 5.0f;     // world units (radius)
    public static int   POISON_DAMAGE = 15;      // damage per tick

    // SHADOW DASH - Quick dash that damages in a line
    public static float DASH_TELEGRAPH = 0.7f;   // seconds (faster telegraph)
    public static float DASH_RANGE = 10.0f;      // world units (dash distance)
    public static float DASH_WIDTH = 2.0f;       // world units (width of damage zone)
    public static int   DASH_DAMAGE = 25;

    // CORRUPTION PULSE - Expanding ring that slows and damages
    public static float PULSE_TELEGRAPH = 1.3f;  // seconds
    public static float PULSE_INNER_RANGE = 3.0f; // inner radius (safe zone)
    public static float PULSE_OUTER_RANGE = 8.0f; // outer radius
    public static int   PULSE_DAMAGE = 20;
}
