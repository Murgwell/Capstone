package capstone.main.Managers;

import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Enemies.EnemySpawner;
import capstone.main.Managers.WorldMapManager.WorldMap;

import java.util.List;

public interface WorldSpawnPolicy {
    boolean allowPeriodicSpawns();
    List<Class<? extends AbstractEnemy>> getAllowedSpawns();
    void onEnterWorld(EnemySpawner spawner, WorldMap world);
}
