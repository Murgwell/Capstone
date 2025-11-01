package capstone.main.Enemies;

import java.util.ArrayList;

public class EnemySpawner {
    private final float spawnInterval = 1f;
    private final float worldWidth;
    private final float worldHeight;
    private final ArrayList<AbstractEnemy> enemies;
    private float spawnTimer = 0f;

    public EnemySpawner(float worldWidth, float worldHeight) {
        enemies = new ArrayList<>();
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public void spawnInitial(int count) {
        for (int i = 0; i < count; i++) spawnDummy();
    }

    public void update(float delta) {
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnDummy();
            spawnTimer = 0f;
        }
    }

    private void spawnDummy() {
        float x = (float) Math.random() * worldWidth;
        float y = (float) Math.random() * worldHeight;
        enemies.add(new Dummy(x, y));
    }

    public ArrayList<AbstractEnemy> getEnemies() {
        return enemies;
    }
}
