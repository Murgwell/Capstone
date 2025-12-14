package capstone.main.Managers;

import capstone.main.Enemies.*;
import capstone.main.Managers.WorldMapManager.WorldMap;

import java.util.*;

public class SpawnPolicies {
    private static final Map<WorldMap, WorldSpawnPolicy> POLICIES = new EnumMap<>(WorldMap.class);

    static {
        // World1 overworld: only Survivor, periodic spawns
        POLICIES.put(WorldMap.WORLD_1, new WorldSpawnPolicy() {
            @Override public boolean allowPeriodicSpawns() { return true; }
            @Override public List<Class<? extends AbstractEnemy>> getAllowedSpawns() {
                return Collections.singletonList(Survivor.class);
            }
            @Override public void onEnterWorld(EnemySpawner spawner, WorldMap world) {
                // No special action on enter
            }
        });

        // World1 boss: spawn Greed once at center, no periodic spawns
        POLICIES.put(WorldMap.WORLD_1_BOSS, new WorldSpawnPolicy() {
            @Override public boolean allowPeriodicSpawns() { return false; }
            @Override public List<Class<? extends AbstractEnemy>> getAllowedSpawns() {
                return Collections.emptyList();
            }
            @Override public void onEnterWorld(EnemySpawner spawner, WorldMap world) {
                float cx = spawner.getWorldWidth() / 2f;
                float cy = spawner.getWorldHeight() / 2f;
                spawner.clearEnemies();
                // Defer Greed spawn: trigger when player comes within radius of center
                spawner.scheduleBossSpawn(Greed.class, cx, cy, 6f);
            }
        });

        // Defaults for other worlds remain as-is; can be extended similarly.
    }

    public static WorldSpawnPolicy getPolicy(WorldMap world) {
        return POLICIES.getOrDefault(world, new WorldSpawnPolicy() {
            @Override public boolean allowPeriodicSpawns() { return true; }
            @Override public List<Class<? extends AbstractEnemy>> getAllowedSpawns() {
                return Collections.singletonList(Survivor.class);
            }
            @Override public void onEnterWorld(EnemySpawner spawner, WorldMap world) { }
        });
    }
}
