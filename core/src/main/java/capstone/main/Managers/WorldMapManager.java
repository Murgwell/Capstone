package capstone.main.Managers;

import com.badlogic.gdx.Gdx;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages world maps, transitions, and spawn points
 */
public class WorldMapManager {

    // World map definitions
    public enum WorldMap {
        WORLD_1("Textures/World1.tmx"),
        WORLD_1_BOSS("Textures/World1_Boss.tmx"),
        WORLD_2("Textures/World2.tmx"),
        WORLD_2_BOSS("Textures/World2_Boss.tmx"),
        WORLD_3("Textures/World3.tmx"),
        WORLD_3_BOSS("Textures/World3_Boss.tmx");

        private final String filePath;

        WorldMap(String filePath) {
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }
    }

    // Spawn point data class
    public static class SpawnPoint {
        public final float x, y;
        public final String description;

        public SpawnPoint(float x, float y, String description) {
            this.x = x;
            this.y = y;
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("SpawnPoint(%.1f, %.1f) - %s", x, y, description);
        }
    }

    // Map spawn points - optimized coordinates for each world
    private static final Map<WorldMap, SpawnPoint> SPAWN_POINTS = new HashMap<>();
    static {
        // World 1 - Starting position (safe area away from bridge)
        SPAWN_POINTS.put(WorldMap.WORLD_1, new SpawnPoint(9f, 9f, "Starting area"));

        // World 1 Boss - Left bridge entrance (32x32 tiles at 32px each)
        // Bridge is on left side, spawn player safely on the bridge
        SPAWN_POINTS.put(WorldMap.WORLD_1_BOSS, new SpawnPoint(10f, 28f, "Left bridge entrance"));

        // World 2 - Bottom center, safe spawn
        SPAWN_POINTS.put(WorldMap.WORLD_2, new SpawnPoint(32f, 55f, "Bottom center area"));

        // World 2 Boss - Center spawn
        SPAWN_POINTS.put(WorldMap.WORLD_2_BOSS, new SpawnPoint(15.5f, 31.5f, "Bottom entrance area"));

        // World 3 - Bottom left spawn (corrected position)
        SPAWN_POINTS.put(WorldMap.WORLD_3, new SpawnPoint(1.5f, 57.3f, "Bottom entrance"));

        // World 3 Boss - Safe spawn at bottom entrance (player-tested coordinates)
        SPAWN_POINTS.put(WorldMap.WORLD_3_BOSS, new SpawnPoint(1.7f, 15f, "Boss arena entrance"));
    }

    // World progression mapping - World1_Boss is only accessible via bridge, not progression
    private static final Map<WorldMap, WorldMap> NEXT_WORLD = new HashMap<>();
    static {
        NEXT_WORLD.put(WorldMap.WORLD_1, WorldMap.WORLD_2);  // World1 progresses directly to World2
        NEXT_WORLD.put(WorldMap.WORLD_1_BOSS, WorldMap.WORLD_2); // Boss world leads to World2
        NEXT_WORLD.put(WorldMap.WORLD_2, WorldMap.WORLD_2_BOSS);
        NEXT_WORLD.put(WorldMap.WORLD_2_BOSS, WorldMap.WORLD_3);
        NEXT_WORLD.put(WorldMap.WORLD_3, WorldMap.WORLD_3_BOSS);
        // World 3 Boss could loop back or end game
        // Note: World1_Boss is only accessible via bridge teleport, not normal progression
    }

    // Enemy count per world
    private static final Map<WorldMap, Integer> ENEMY_COUNTS = new HashMap<>();
    static {
        // Higher preset, applied to all maps
        ENEMY_COUNTS.put(WorldMap.WORLD_1, 30);
        ENEMY_COUNTS.put(WorldMap.WORLD_1_BOSS, 15);
        ENEMY_COUNTS.put(WorldMap.WORLD_2, 40);
        ENEMY_COUNTS.put(WorldMap.WORLD_2_BOSS, 20);
        ENEMY_COUNTS.put(WorldMap.WORLD_3, 50);
        ENEMY_COUNTS.put(WorldMap.WORLD_3_BOSS, 25); // Boss arena gets more adds
    }

    private WorldMap currentWorld;

    public WorldMapManager() {
        this.currentWorld = WorldMap.WORLD_1; // Start in World 1
    }

    /**
     * Get spawn point for a specific world
     */
    public SpawnPoint getSpawnPoint(WorldMap world) {
        SpawnPoint spawn = SPAWN_POINTS.get(world);
        if (spawn == null) {
            Gdx.app.error("WorldMapManager", "No spawn point defined for " + world);
            return new SpawnPoint(32f, 32f, "Default center spawn");
        }
        return spawn;
    }

    /**
     * Get the next world in progression
     */
    public WorldMap getNextWorld(WorldMap currentWorld) {
        return NEXT_WORLD.get(currentWorld);
    }

    /**
     * Get enemy count for a world
     */
    public int getEnemyCount(WorldMap world) {
        return ENEMY_COUNTS.getOrDefault(world, 10);
    }

    /**
     * Get world by file path
     */
    public static WorldMap getWorldByPath(String filePath) {
        for (WorldMap world : WorldMap.values()) {
            if (world.getFilePath().equals(filePath)) {
                return world;
            }
        }
        return null;
    }

    /**
     * Check if current world matches given path
     */
    public boolean isCurrentWorld(String mapPath) {
        return currentWorld != null && currentWorld.getFilePath().equals(mapPath);
    }

    /**
     * Set current world
     */
    public void setCurrentWorld(WorldMap world) {
        this.currentWorld = world;
        Gdx.app.log("WorldMapManager", "Current world set to: " + world + " (" + world.getFilePath() + ")");
    }

    /**
     * Set current world by path
     */
    public void setCurrentWorld(String mapPath) {
        WorldMap world = getWorldByPath(mapPath);
        if (world != null) {
            setCurrentWorld(world);
        } else {
            Gdx.app.error("WorldMapManager", "Unknown world path: " + mapPath);
        }
    }

    /**
     * Get current world
     */
    public WorldMap getCurrentWorld() {
        return currentWorld;
    }

    /**
     * Get current world file path
     */
    public String getCurrentWorldPath() {
        return currentWorld != null ? currentWorld.getFilePath() : null;
    }

    /**
     * Transition to next world in progression
     */
    public WorldMap transitionToNext() {
        WorldMap nextWorld = getNextWorld(currentWorld);
        if (nextWorld != null) {
            setCurrentWorld(nextWorld);
            Gdx.app.log("WorldMapManager", "Transitioning from " + currentWorld + " to " + nextWorld);
        } else {
            Gdx.app.log("WorldMapManager", "No next world defined for " + currentWorld);
        }
        return nextWorld;
    }

    /**
     * Get transition data for moving to next world
     */
    public TransitionData getNextTransition() {
        WorldMap nextWorld = getNextWorld(currentWorld);
        if (nextWorld != null) {
            SpawnPoint spawn = getSpawnPoint(nextWorld);
            int enemyCount = getEnemyCount(nextWorld);
            return new TransitionData(nextWorld, spawn, enemyCount);
        }
        return null;
    }

    /**
     * Check if a world is bridge-only access (not accessible via normal progression)
     */
    public boolean isBridgeOnlyWorld(WorldMap world) {
        return world == WorldMap.WORLD_1_BOSS; // Currently only World1_Boss is bridge-only
    }

    /**
     * Check if transition from current world to target world is allowed via normal progression
     */
    public boolean isNormalProgressionAllowed(WorldMap from, WorldMap to) {
        // Bridge-only worlds cannot be accessed via normal progression
        if (isBridgeOnlyWorld(to)) {
            return false;
        }

        // Check if the transition follows normal progression
        WorldMap nextWorld = getNextWorld(from);
        return nextWorld != null && nextWorld.equals(to);
    }

    /**
     * Data class for world transitions
     */
    public static class TransitionData {
        public final WorldMap world;
        public final SpawnPoint spawnPoint;
        public final int enemyCount;

        public TransitionData(WorldMap world, SpawnPoint spawnPoint, int enemyCount) {
            this.world = world;
            this.spawnPoint = spawnPoint;
            this.enemyCount = enemyCount;
        }
    }
}
