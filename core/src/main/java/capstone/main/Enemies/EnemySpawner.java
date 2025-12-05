package capstone.main.Enemies;

import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.ScreenShake;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Random;

public class EnemySpawner {
    private ScreenShake screenShake;
    private PhysicsManager physics;
    private ArrayList<AbstractEnemy> enemies;
    private ArrayList<Rectangle> collisionRects; // Collision rectangles to avoid
    private float spawnTimer = 0f;
    private final float spawnInterval = 1f;
    private final float worldWidth;
    private final float worldHeight;
    private Random random;

    public EnemySpawner(float worldWidth, float worldHeight, ScreenShake screenShake,
                        PhysicsManager physics, ArrayList<Rectangle> collisionRects) {
        this.enemies = new ArrayList<>();
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.screenShake = screenShake;
        this.physics = physics;
        this.random = new Random();
        this.collisionRects = collisionRects;
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

    // Spawn a random enemy avoiding collisions
    private void spawnRandomEnemy() {
        float[] pos = getValidPosition();
        if (pos == null) return; // No valid position found

        int enemyType = random.nextInt(2); // 0 or 1
        switch (enemyType) {
            case 0:
                enemies.add(new Survivor(pos[0], pos[1], screenShake, physics));
                break;
            case 1:
                enemies.add(new Greed(pos[0], pos[1], screenShake, physics));
                break;
        }
    }

    public void spawnSurvivor() {
        float[] pos = getValidPosition();
        if (pos != null) {
            enemies.add(new Survivor(pos[0], pos[1], screenShake, physics));
        }
    }

    public void spawnGreed() {
        float[] pos = getValidPosition();
        if (pos != null) {
            enemies.add(new Greed(pos[0], pos[1], screenShake, physics));
        }
    }

    // Returns a valid (x, y) that is not inside a collision rectangle
    private float[] getValidPosition() {
        float x, y;
        int attempts = 0;

        do {
            x = random.nextFloat() * worldWidth;
            y = random.nextFloat() * worldHeight;
            attempts++;
            if (attempts > 100) return null; // fail-safe: couldn't find a valid spot
        } while (isBlocked(x, y));

        return new float[]{x, y};
    }

    // Check if the position is inside any collision rectangle
    private boolean isBlocked(float x, float y) {
        for (Rectangle rect : collisionRects) {
            if (rect.contains(x, y)) return true;
        }
        return false;
    }

    public ArrayList<AbstractEnemy> getEnemies() {
        return enemies;
    }

    // Optional: Resolve collisions between enemies (existing code)
    public static void resolveEnemyCollisions(ArrayList<AbstractEnemy> enemies, float delta) {
        float repulsionDistance = 0.5f; // adjust based on enemy size
        float repulsionStrength = 2f;

        for (int i = 0; i < enemies.size(); i++) {
            AbstractEnemy e1 = enemies.get(i);
            for (int j = i + 1; j < enemies.size(); j++) {
                AbstractEnemy e2 = enemies.get(j);

                float dx = e2.getSprite().getX() - e1.getSprite().getX();
                float dy = e2.getSprite().getY() - e1.getSprite().getY();
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < repulsionDistance && distance > 0f) {
                    float overlap = repulsionDistance - distance;
                    float pushX = (dx / distance) * overlap * repulsionStrength * delta;
                    float pushY = (dy / distance) * overlap * repulsionStrength * delta;

                    e1.getSprite().setPosition(e1.getSprite().getX() - pushX / 2,
                        e1.getSprite().getY() - pushY / 2);
                    e2.getSprite().setPosition(e2.getSprite().getX() + pushX / 2,
                        e2.getSprite().getY() + pushY / 2);
                }
            }
        }
    }
}
