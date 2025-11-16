package capstone.main.Enemies;

import java.util.ArrayList;

public class EnemySpawner {
    private ArrayList<AbstractEnemy> enemies;
    private float spawnTimer = 0f;
    private final float spawnInterval = 1f;
    private final float worldWidth;
    private final float worldHeight;

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

    public static void resolveEnemyCollisions(ArrayList<AbstractEnemy> enemies, float delta) {
        float repulsionDistance = 0.5f; // adjust based on enemy size
        float repulsionStrength = 2f;

        for (int i = 0; i < enemies.size(); i++) {
            AbstractEnemy e1 = enemies.get(i);
            for (int j = i + 1; j < enemies.size(); j++) {
                AbstractEnemy e2 = enemies.get(j);

                float dx = e2.getSprite().getX() - e1.getSprite().getX();
                float dy = e2.getSprite().getY() - e1.getSprite().getY();
                float distance = (float) Math.sqrt(dx*dx + dy*dy);

                if (distance < repulsionDistance && distance > 0f) {
                    float overlap = repulsionDistance - distance;
                    float pushX = (dx / distance) * overlap * repulsionStrength * delta;
                    float pushY = (dy / distance) * overlap * repulsionStrength * delta;

                    e1.getSprite().setPosition(e1.getSprite().getX() - pushX / 2, e1.getSprite().getY() - pushY / 2);
                    e2.getSprite().setPosition(e2.getSprite().getX() + pushX / 2, e2.getSprite().getY() + pushY / 2);
                }
            }
        }
    }
}
