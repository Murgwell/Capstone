package capstone.main.Enemies;

import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.ScreenShake;

import java.util.ArrayList;
import java.util.Random;

public class EnemySpawner {
    private ScreenShake screenShake;
    private PhysicsManager physics;
    private ArrayList<AbstractEnemy> enemies;
    private float spawnTimer = 0f;
    private final float spawnInterval = 1f;
    private final float worldWidth;
    private final float worldHeight;
    private Random random;

    public EnemySpawner(float worldWidth, float worldHeight, ScreenShake screenShake, PhysicsManager physics) {
        enemies = new ArrayList<>();
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.screenShake = screenShake;
        this.physics = physics;
        this.random = new Random();
    }

    public void spawnInitial(int count) {
        for (int i = 0; i < count; i++) {
            spawnRandomEnemy();
        }
    }

    public void update(float delta) {
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnRandomEnemy();
            spawnTimer = 0f;
        }
    }

    private void spawnRandomEnemy() {
        float x = (float) Math.random() * worldWidth;
        float y = (float) Math.random() * worldHeight;

        // Randomly choose between Survivor and Greed
        int enemyType = random.nextInt(6); // 0 or 1

        switch (enemyType) {
            case 0:
                enemies.add(new Survivor(x, y, screenShake, physics));
                break;
            case 1:
                enemies.add(new Greed(x, y, screenShake, physics));
                break;
            case 2:
                enemies.add(new Security(x, y, screenShake, physics));
                break;
            case 3:
                enemies.add(new Discaya(x, y, screenShake, physics));
                break;
            case 4:
                enemies.add(new Follower(x, y, screenShake, physics));
                break;
            case 5:
                enemies.add(new QuiboloyBoss(x, y, screenShake, physics));
                break;
        }
    }

    // Spawn specific enemy types (useful for testing or special spawns)
    public void spawnSurvivor() {
        float x = (float) Math.random() * worldWidth;
        float y = (float) Math.random() * worldHeight;
        enemies.add(new Survivor(x, y, screenShake, physics));
    }

    public void spawnGreed() {
        float x = (float) Math.random() * worldWidth;
        float y = (float) Math.random() * worldHeight;
        enemies.add(new Greed(x, y, screenShake, physics));
    }

    public void spawnSecurity() {
        float x = (float) Math.random() * worldWidth;
        float y = (float) Math.random() * worldHeight;
        enemies.add(new Security(x, y, screenShake, physics));
    }

    public void spawnDiscaya() {
        float x = (float) Math.random() * worldWidth;
        float y = (float) Math.random() * worldHeight;
        enemies.add(new Discaya(x, y, screenShake, physics));
    }

    public void spawnFollower() {
        float x = (float) Math.random() * worldWidth;
        float y = (float) Math.random() * worldHeight;
        enemies.add(new Follower(x, y, screenShake, physics));
    }

    public void spawnQuiboloyBoss() {
        float x = (float) Math.random() * worldWidth;
        float y = (float) Math.random() * worldHeight;
        enemies.add(new QuiboloyBoss(x, y, screenShake, physics));
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
