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
                // Spawn Greed immediately at specific location (Game.java already cleared enemies)
                spawner.spawnBossImmediately(Greed.class, 32.0f, 33.3f);
            }
        });

        // World2 boss: spawn Discaya at specific location + Security enemies
        POLICIES.put(WorldMap.WORLD_2_BOSS, new WorldSpawnPolicy() {
            @Override public boolean allowPeriodicSpawns() { return true; }
            @Override public List<Class<? extends AbstractEnemy>> getAllowedSpawns() {
                return Collections.singletonList(Security.class);
            }
            @Override public void onEnterWorld(EnemySpawner spawner, WorldMap world) {
                // Spawn Discaya immediately at specific location (Game.java already cleared enemies)
                spawner.spawnBossImmediately(Discaya.class, 15.7f, 10.9f);
            }
        });

        // World3 overworld: only Follower, periodic spawns
        POLICIES.put(WorldMap.WORLD_3, new WorldSpawnPolicy() {
            @Override public boolean allowPeriodicSpawns() { return true; }
            @Override public List<Class<? extends AbstractEnemy>> getAllowedSpawns() {
                return Collections.singletonList(Follower.class);
            }
            @Override public void onEnterWorld(EnemySpawner spawner, WorldMap world) {
                // No special action on enter
            }
        });

        // World3 boss: spawn QuiboloyBoss at specific location + Follower enemies
        POLICIES.put(WorldMap.WORLD_3_BOSS, new WorldSpawnPolicy() {
            @Override public boolean allowPeriodicSpawns() { return true; }
            @Override public List<Class<? extends AbstractEnemy>> getAllowedSpawns() {
                return Collections.singletonList(Follower.class);
            }
            @Override public void onEnterWorld(EnemySpawner spawner, WorldMap world) {
                // Spawn QuiboloyBoss immediately at center of boss arena (Game.java already cleared enemies)
                spawner.spawnBossImmediately(QuiboloyBoss.class, 16.0f, 16.0f);
            }
        });

        // Defaults for other worlds remain as-is; can be extended similarly.
    }
    
    /**
     * Helper method to apply spawn policy for a world.
     * Calls onEnterWorld if policy exists.
     */
    public static void applySpawnPolicy(WorldMap world, EnemySpawner spawner) {
        WorldSpawnPolicy policy = getPolicy(world);
        if (policy != null) {
            policy.onEnterWorld(spawner, world);
        }
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
