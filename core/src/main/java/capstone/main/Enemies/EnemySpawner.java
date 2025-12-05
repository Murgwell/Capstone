package capstone.main.Enemies;

import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.ScreenShake;
import capstone.main.Managers.CollisionLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;

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
    private String currentWorld = "World1"; // Default to World1
    private ArrayList<Rectangle> collisionRectangles;

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
        float x, y;
        int attempts = 0;
        int maxAttempts = 20; // Prevent infinite loop
        
        // Try to find a walkable spawn position
        do {
            x = 2f + (float) Math.random() * (worldWidth - 4f); // Keep away from edges
            y = 2f + (float) Math.random() * (worldHeight - 4f);
            attempts++;
        } while (attempts < maxAttempts && !isWalkablePosition(x, y));
        
        // If we couldn't find a good position after 20 attempts, use safe fallback
        if (attempts >= maxAttempts) {
            x = worldWidth * 0.25f + (float) Math.random() * (worldWidth * 0.5f); // Middle 50% of map
            y = worldHeight * 0.25f + (float) Math.random() * (worldHeight * 0.5f);
        }

        // Spawn enemies based on current world
        spawnWorldSpecificEnemy(x, y);
    }
    
    /**
     * Set the collision map for proper collision detection
     */
    public void setCollisionMap(TiledMap map) {
        // Get collision rectangles from the map's collision layer
        this.collisionRectangles = CollisionLoader.getCollisionRectangles(map, "collisionLayer", 1/32f);
    }
    
    /**
     * Check if a position is walkable (not on collision layer)
     */
    private boolean isWalkablePosition(float x, float y) {
        // Avoid map edges
        if (x < 1f || x > worldWidth - 1f || y < 1f || y > worldHeight - 1f) {
            return false;
        }
        
        // Check if position overlaps with any collision rectangle
        if (collisionRectangles != null) {
            // Create a small rectangle around the spawn point to check for collision
            Rectangle spawnRect = new Rectangle(x - 0.5f, y - 0.5f, 1f, 1f);
            
            for (Rectangle collisionRect : collisionRectangles) {
                if (spawnRect.overlaps(collisionRect)) {
                    return false; // Position overlaps with collision (water/wall)
                }
            }
        }
        
        return true;
    }
    
    /**
     * Set the current world for world-specific enemy spawning
     */
    public void setCurrentWorld(String worldPath) {
        if (worldPath.contains("World1_Boss") || worldPath.contains("World1")) {
            this.currentWorld = "World1";
        } else if (worldPath.contains("World2_Boss") || worldPath.contains("World2")) {
            this.currentWorld = "World2";
        } else if (worldPath.contains("World3_Boss") || worldPath.contains("World3")) {
            this.currentWorld = "World3";
        }
    }
    
    /**
     * Spawn enemies specific to the current world
     */
    private void spawnWorldSpecificEnemy(float x, float y) {
        switch (currentWorld) {
            case "World1":
                // World1 enemies: Survivor and Greed
                int world1EnemyType = random.nextInt(2);
                switch (world1EnemyType) {
                    case 0:
                        enemies.add(new Survivor(x, y, screenShake, physics));
                        break;
                    case 1:
                        enemies.add(new Greed(x, y, screenShake, physics));
                        break;
                }
                break;
                
            case "World2":
                // World2 enemies: Security and Discaya
                int world2EnemyType = random.nextInt(2);
                switch (world2EnemyType) {
                    case 0:
                        enemies.add(new Security(x, y, screenShake, physics));
                        break;
                    case 1:
                        enemies.add(new Discaya(x, y, screenShake, physics));
                        break;
                }
                break;
                
            case "World3":
                // World3 enemies: Follower and QuiboloyBoss (rare)
                int world3EnemyType = random.nextInt(10); // 0-9
                if (world3EnemyType < 8) {
                    // 80% chance for Followers
                    enemies.add(new Follower(x, y, screenShake, physics));
                } else {
                    // 20% chance for QuiboloyBoss
                    enemies.add(new QuiboloyBoss(x, y, screenShake, physics));
                }
                break;
                
            default:
                // Fallback to World1 enemies if unknown world
                enemies.add(new Survivor(x, y, screenShake, physics));
                break;
        }
    }

    // Spawn specific enemy types (useful for testing or special spawns)
    public void spawnSurvivor() {
        float x, y;
        int attempts = 0;
        int maxAttempts = 20;
        
        do {
            x = 2f + (float) Math.random() * (worldWidth - 4f);
            y = 2f + (float) Math.random() * (worldHeight - 4f);
            attempts++;
        } while (attempts < maxAttempts && !isWalkablePosition(x, y));
        
        if (attempts >= maxAttempts) {
            x = worldWidth * 0.25f + (float) Math.random() * (worldWidth * 0.5f);
            y = worldHeight * 0.25f + (float) Math.random() * (worldHeight * 0.5f);
        }
        
        enemies.add(new Survivor(x, y, screenShake, physics));
    }

    public void spawnGreed() {
        float x, y;
        int attempts = 0;
        int maxAttempts = 20;
        
        do {
            x = 2f + (float) Math.random() * (worldWidth - 4f);
            y = 2f + (float) Math.random() * (worldHeight - 4f);
            attempts++;
        } while (attempts < maxAttempts && !isWalkablePosition(x, y));
        
        if (attempts >= maxAttempts) {
            x = worldWidth * 0.25f + (float) Math.random() * (worldWidth * 0.5f);
            y = worldHeight * 0.25f + (float) Math.random() * (worldHeight * 0.5f);
        }
        
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
