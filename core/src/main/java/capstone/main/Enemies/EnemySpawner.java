package capstone.main.Enemies;

import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.ScreenShake;
import capstone.main.Pathfinding.NavMesh;
import capstone.main.Managers.CollisionLoader;
import capstone.main.Pathfinding.NavNode;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemySpawner {
    private ScreenShake screenShake;
    private PhysicsManager physics;
    private ArrayList<AbstractEnemy> enemies;
    private ArrayList<Rectangle> collisionRectangles; // Collision rectangles to avoid
    private float spawnTimer = 0f;
    private final float spawnInterval = 1f;
    private final int maxEnemies = 20; // MEMORY FIX: Limit total enemies
    private final float worldWidth;
    private final float worldHeight;
    private Random random;
    private NavMesh navMesh;
    private final Vector2 tmpVec = new Vector2();

    private float enemySpawnRadius = 2.0f; // Increase radius to avoid spawning near walls
    private float enemySpacingRadius = 5.0f; // Wider spacing for better scattering


    private String currentWorld = "World1"; // Default to World1

    public EnemySpawner(float worldWidth, float worldHeight, ScreenShake screenShake,
                        PhysicsManager physics, NavMesh navMesh) {
        this.enemies = new ArrayList<>();
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.screenShake = screenShake;
        this.physics = physics;
        this.random = new Random();
        this.navMesh = navMesh;
    }

    public void spawnInitial(int count) {
        for (int i = 0; i < count; i++) {
            spawnRandomEnemy();
        }
    }

    public void update(float delta) {
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            // MEMORY FIX: Only spawn if under limit
            if (enemies.size() < maxEnemies) {
                // TRY 20 SPAWN ATTEMPTS to find valid positions
                boolean spawnSuccessful = false;
                for (int attempt = 1; attempt <= 20; attempt++) {
                    System.out.println("SPAWN ATTEMPT " + attempt + "/20 - Current enemies: " + enemies.size() + "/" + maxEnemies);
                    
                    if (attemptSpawnRandomEnemy()) {
                        System.out.println("SPAWN SUCCESS on attempt " + attempt + " - Total: " + enemies.size() + "/" + maxEnemies);
                        spawnSuccessful = true;
                        break;
                    }
                }
                
                if (!spawnSuccessful) {
                    System.out.println("ALL 20 SPAWN ATTEMPTS FAILED - No valid positions found");
                }
            } else {
                System.out.println("SPAWN BLOCKED - Max enemies reached: " + enemies.size());
            }
            spawnTimer = 0f;
        }
    }

    // Spawn a random enemy avoiding collisions
    // NEW: Attempt to spawn without guarantees (returns success/failure)
    private boolean attemptSpawnRandomEnemy() {
        float x, y;
        int attempts = 0;
        int maxAttempts = 20; // Prevent infinite loop

        // Try to find a walkable spawn position using grid-based distribution
        do {
            // Use grid-based spawning to encourage better distribution across map
            int gridX = (int) (Math.random() * 12); // 12x6 grid for better distribution
            int gridY = (int) (Math.random() * 6);
            
            float gridCellWidth = (worldWidth - 4f) / 12f;
            float gridCellHeight = (worldHeight - 4f) / 6f;
            
            x = 2f + gridX * gridCellWidth + (float) Math.random() * gridCellWidth;
            y = 2f + gridY * gridCellHeight + (float) Math.random() * gridCellHeight;
            
            attempts++;
        } while (attempts < maxAttempts && !isWalkablePosition(x, y));

        // If we couldn't find a good position after 20 attempts, try validated fallback
        if (attempts >= maxAttempts) {
            System.out.println("WARNING: Primary spawn attempts failed, trying fallback positions...");
            
            // Try 25 fallback positions across ENTIRE map with more attempts
            boolean fallbackFound = false;
            for (int fallbackAttempts = 0; fallbackAttempts < 25; fallbackAttempts++) {
                x = enemySpawnRadius + (float) Math.random() * (worldWidth - enemySpawnRadius * 2f); // Entire width
                y = enemySpawnRadius + (float) Math.random() * (worldHeight - enemySpawnRadius * 2f); // Entire height
                
                if (isWalkablePosition(x, y)) {
                    System.out.println("FALLBACK SUCCESS: Found valid position at (" + x + ", " + y + ")");
                    fallbackFound = true;
                    break;
                }
            }
            
            if (!fallbackFound) {
                return false; // Failed to find any valid position
            }
        }

        // Spawn enemies based on current world
        spawnWorldSpecificEnemy(x, y);
        return true; // Successful spawn
    }

    // Keep original method for compatibility
    private void spawnRandomEnemy() {
        attemptSpawnRandomEnemy();
    }

    /**
     * Set the collision map for proper collision detection
     */
    public void setCollisionMap(TiledMap map) {
        // Get collision rectangles from the map's collision layer
        this.collisionRectangles = CollisionLoader.getCollisionRectangles(map, "collisionLayer", 1/32f);
    }

    /**
     * Check if a position is walkable - FIXED VERSION
     */
    private boolean isWalkablePosition(float x, float y) {
        // Basic bounds check
        if (x < enemySpawnRadius || x > worldWidth - enemySpawnRadius ||
            y < enemySpawnRadius || y > worldHeight - enemySpawnRadius) {
            return false;
        }

        // FIRST: Check collision rectangles (the ACTUAL collision layer)
        if (collisionRectangles != null) {
            Rectangle spawnRect = new Rectangle(x - enemySpawnRadius, y - enemySpawnRadius, 
                                               enemySpawnRadius * 2f, enemySpawnRadius * 2f);
            
            for (Rectangle collisionRect : collisionRectangles) {
                if (spawnRect.overlaps(collisionRect)) {
                    return false; // Blocked by actual collision geometry
                }
            }
        }

        // SECOND: Check NavMesh walkability 
        if (navMesh == null) return false;
        
        tmpVec.set(x, y);
        NavNode spawnNode = navMesh.getNearestNode(tmpVec);
        
        if (spawnNode == null || !spawnNode.walkable) {
            return false;
        }

        // THIRD: Simple mobility test with smaller radius
        int validDirections = 0;
        float testRadius = 0.2f; // Very small test radius for tight areas
        
        // Test 4 cardinal directions only (simpler and faster)
        float[] angles = {0f, 90f, 180f, 270f}; // North, East, South, West
        
        for (float angleDeg : angles) {
            float angleRad = (float) Math.toRadians(angleDeg);
            float testX = x + (float) Math.cos(angleRad) * testRadius;
            float testY = y + (float) Math.sin(angleRad) * testRadius;
            
            // Keep within bounds
            testX = Math.max(1f, Math.min(worldWidth - 1f, testX));
            testY = Math.max(1f, Math.min(worldHeight - 1f, testY));
            
            // Check if this direction is walkable
            tmpVec.set(testX, testY);
            NavNode testNode = navMesh.getNearestNode(tmpVec);
            
            if (testNode != null && testNode.walkable) {
                validDirections++;
            }
        }
        
        // Need ALL 4 directions to be walkable (perfect mobility, no stuck enemies)
        boolean isValidSpawn = validDirections >= 4;
        
        // FOURTH: Check spacing from existing enemies (with flexible spacing)
        if (isValidSpawn) {
            float minDistance = enemySpacingRadius;
            
            // Reduce minimum distance if we have many enemies (adaptive spacing)
            if (enemies.size() > 15) {
                minDistance = 3.5f; // Still decent spacing when map gets crowded
            } else if (enemies.size() > 10) {
                minDistance = 4.0f; // Good spacing
            }
            
            for (AbstractEnemy existingEnemy : enemies) {
                Vector2 enemyPos = existingEnemy.getBody().getPosition();
                float distance = Vector2.dst(x, y, enemyPos.x, enemyPos.y);
                
                if (distance < minDistance) {
                    System.out.println("SPAWN DEBUG: Position (" + x + ", " + y + ") REJECTED - too close to enemy at (" + 
                                     enemyPos.x + ", " + enemyPos.y + ") distance: " + String.format("%.2f", distance) + 
                                     " (min: " + String.format("%.1f", minDistance) + ")");
                    return false;
                }
            }
        }
        
        // DEBUG: Show WHY positions are rejected
        if (!isValidSpawn) {
            if (spawnNode == null) {
                System.out.println("SPAWN DEBUG: Position (" + x + ", " + y + ") REJECTED - NavNode is null");
            } else if (!spawnNode.walkable) {
                System.out.println("SPAWN DEBUG: Position (" + x + ", " + y + ") REJECTED - NavNode not walkable");
            } else {
                System.out.println("SPAWN DEBUG: Position (" + x + ", " + y + ") REJECTED - poor mobility (" + validDirections + "/4 directions)");
            }
        } else {
            System.out.println("SPAWN DEBUG: Position (" + x + ", " + y + ") ACCEPTED (" + validDirections + "/4 directions walkable)");
        }
        
        // EXTRA DEBUG: Show collision check results
        if (collisionRectangles != null) {
            Rectangle spawnRect = new Rectangle(x - enemySpawnRadius, y - enemySpawnRadius, 
                                               enemySpawnRadius * 2f, enemySpawnRadius * 2f);
            boolean hasCollision = false;
            for (Rectangle collisionRect : collisionRectangles) {
                if (spawnRect.overlaps(collisionRect)) {
                    hasCollision = true;
                    System.out.println("COLLISION DEBUG: Position (" + x + ", " + y + ") blocked by collision at " + 
                                     collisionRect.x + "," + collisionRect.y + " size " + collisionRect.width + "x" + collisionRect.height);
                    break;
                }
            }
            if (!hasCollision && validDirections > 0) {
                System.out.println("COLLISION DEBUG: Position (" + x + ", " + y + ") passed collision check");
            }
        }
        
        return isValidSpawn;
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
                        enemies.add(new Survivor(x, y, screenShake, physics, navMesh));
                        break;
                    case 1:
                        enemies.add(new Greed(x, y, screenShake, physics, navMesh));
                        break;
                }
                break;

            case "World2":
                // World2 enemies: Security and Discaya
                int world2EnemyType = random.nextInt(2);
                switch (world2EnemyType) {
                    case 0:
                        enemies.add(new Security(x, y, screenShake, physics, navMesh));
                        break;
                    case 1:
                        enemies.add(new Discaya(x, y, screenShake, physics, navMesh));
                        break;
                }
                break;

            case "World3":
                // World3 enemies: Follower and QuiboloyBoss (rare)
                int world3EnemyType = random.nextInt(10); // 0-9
                if (world3EnemyType < 8) {
                    // 80% chance for Followers
                    enemies.add(new Follower(x, y, screenShake, physics, navMesh));
                } else {
                    // 20% chance for QuiboloyBoss
                    enemies.add(new QuiboloyBoss(x, y, screenShake, physics, navMesh));
                }
                break;

            default:
                // Fallback to World1 enemies if unknown world
                enemies.add(new Survivor(x, y, screenShake, physics, navMesh));
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

        enemies.add(new Survivor(x, y, screenShake, physics, navMesh));
    }

    public void spawnSecurity() {
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

        enemies.add(new Security(x, y, screenShake, physics, navMesh));
    }

    public void spawnDiscaya() {
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

        enemies.add(new Discaya(x, y, screenShake, physics, navMesh));
    }

    public void spawnFollower() {
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

        enemies.add(new Follower(x, y, screenShake, physics, navMesh));
    }

    public void spawnQuiboloyBoss() {
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

        enemies.add(new QuiboloyBoss(x, y, screenShake, physics, navMesh));
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
