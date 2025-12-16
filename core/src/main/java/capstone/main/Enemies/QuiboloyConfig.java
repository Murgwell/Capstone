package capstone.main.Enemies;

/**
 * Parameterized configuration for QuiboloyBoss - The Final Boss.
 * Features overpowered but telegraphed skills with longer cast times for fairness.
 */

public final class QuiboloyConfig {
    private QuiboloyConfig() {}

    // Core stats - Highest HP boss as the final challenge
    public static float MAX_HP = 2000f;

    // Global cadence - Slightly longer cooldowns for more powerful skills
    public static float COOLDOWN_MIN = 3.0f;
    public static float COOLDOWN_MAX = 5.0f;

    // DIVINE JUDGMENT - Massive AOE attack that hits the entire arena in rings
    public static float DIVINE_TELEGRAPH = 2.0f;  // Long telegraph for fairness
    public static float DIVINE_INNER_RANGE = 4.0f; // Inner ring radius
    public static float DIVINE_MIDDLE_RANGE = 8.0f; // Middle ring radius
    public static float DIVINE_OUTER_RANGE = 12.0f; // Outer ring radius (massive!)
    public static int   DIVINE_INNER_DAMAGE = 40;   // Close range = most damage (reduced by 10)
    public static int   DIVINE_MIDDLE_DAMAGE = 25;  // Middle range (reduced by 10)
    public static int   DIVINE_OUTER_DAMAGE = 10;   // Far range = least damage (reduced by 10)

    // FIREBALL BARRAGE - Shoots 5 fireballs in a spread pattern
    public static float BARRAGE_TELEGRAPH = 1.5f;  // seconds
    public static int   BARRAGE_FIREBALL_COUNT = 5; // number of fireballs
    public static float BARRAGE_SPREAD_ANGLE = 60f; // degrees total spread
    public static int   BARRAGE_DAMAGE = 15;        // per fireball hit (reduced by 10)
    public static float BARRAGE_SPEED = 20f;        // fireball speed

    // TELEPORT STRIKE - Teleports near player and strikes with AOE
    public static float TELEPORT_TELEGRAPH = 1.2f;  // seconds
    public static float TELEPORT_DISTANCE = 3.0f;   // units behind player
    public static float TELEPORT_STRIKE_RANGE = 4.0f; // AOE radius after teleport
    public static int   TELEPORT_DAMAGE = 30;       // reduced by 10

    // CORRUPTION ZONE - Creates a lingering damage field (like Discaya's poison but stronger)
    public static float CORRUPTION_TELEGRAPH = 1.3f; // seconds
    public static float CORRUPTION_RANGE = 6.0f;     // radius
    public static float CORRUPTION_DURATION = 8.0f;  // how long it lasts
    public static int   CORRUPTION_DAMAGE = 10;      // damage per tick (reduced by 10)
    public static float CORRUPTION_TICK_RATE = 0.4f; // faster ticks than poison

    // FOLLOWER SUMMON - Summons multiple Follower minions
    public static float SUMMON_TELEGRAPH = 1.8f;     // seconds
    public static int   SUMMON_COUNT = 3;            // number of followers to summon
    public static float SUMMON_RADIUS = 5.0f;        // spawn radius around boss
}
