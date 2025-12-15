package capstone.main.Enemies;

import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.ScreenShake;
import capstone.main.Pathfinding.NavMesh;
import capstone.main.Managers.CollisionLoader;
import capstone.main.Pathfinding.NavNode;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemySpawner {
    // Deferred boss spawn
    private Class<? extends AbstractEnemy> scheduledBossType;
    private float scheduledBossX;
    private float scheduledBossY;
    private float scheduledBossRadius;

    public void scheduleBossSpawn(Class<? extends AbstractEnemy> type, float x, float y, float radius) {
        this.scheduledBossType = type;
        this.scheduledBossX = x;
        this.scheduledBossY = y;
        this.scheduledBossRadius = radius;
        Gdx.app.log("EnemySpawner", "Scheduled boss spawn: " + type.getSimpleName() + " at (" + x + "," + y + ") radius=" + radius);
    }
    
    /**
     * Spawn a boss immediately at the specified location (no deferred trigger).
     */
    public void spawnBossImmediately(Class<? extends AbstractEnemy> type, float x, float y) {
        Gdx.app.log("EnemySpawner", "Spawning boss immediately: " + type.getSimpleName() + " at (" + x + "," + y + ")");
        spawnSpecific(type, x, y);
        Gdx.app.log("EnemySpawner", "Boss spawned successfully: " + type.getSimpleName());
        Gdx.app.log("EnemySpawner", "Enemies list size after spawn: " + enemies.size());
    }

    public boolean hasScheduledBoss() { return scheduledBossType != null; }
    public float getScheduledBossX() { return scheduledBossX; }
    public float getScheduledBossY() { return scheduledBossY; }
    public float getScheduledBossRadius() { return scheduledBossRadius; }

    public void executeScheduledBoss() {
        if (scheduledBossType != null) {
            float sx = scheduledBossX;
            float sy = scheduledBossY;
            // If scheduled point isn't walkable, search nearby
            if (!isWalkablePosition(sx, sy)) {
                final int maxAttempts = 24;
                final float step = 1.0f; // world units per ring
                boolean found = false;
                for (int ring = 1; ring <= 6 && !found; ring++) {
                    float r = ring * step;
                    // sample 8 directions per ring
                    for (int k = 0; k < 8; k++) {
                        float angle = (float)(k * Math.PI / 4.0);
                        float tx = sx + (float)Math.cos(angle) * r;
                        float ty = sy + (float)Math.sin(angle) * r;
                        if (isWalkablePosition(tx, ty)) {
                            sx = tx; sy = ty; found = true; break;
                        }
                    }
                }
                if (!found) {
                    // fallback random within bounds
                    float x, y; int attempts = 0;
                    do {
                        x = 2f + (float)Math.random() * (worldWidth - 4f);
                        y = 2f + (float)Math.random() * (worldHeight - 4f);
                        attempts++;
                    } while (attempts < maxAttempts && !isWalkablePosition(x, y));
                    sx = x; sy = y;
                }
            }
            spawnSpecific(scheduledBossType, sx, sy);
            Gdx.app.log("EnemySpawner", "Executed scheduled boss spawn: " + scheduledBossType.getSimpleName() +
                    " at (" + sx + "," + sy + ")");
            Gdx.app.log("EnemySpawner", "Enemies list size after spawn: " + enemies.size());
            scheduledBossType = null;
        }
    }
    private ScreenShake screenShake;
    private PhysicsManager physics;
    private ArrayList<AbstractEnemy> enemies;
    private ArrayList<Rectangle> collisionRectangles; // Collision rectangles to avoid
    private float spawnTimer = 0f;
    private float spawnInterval = 0.6f; // Balanced density default
    private int maxEnemies = 50; // Lower cap to reduce CPU load
    private float worldWidth;
    private float worldHeight;
    private Random random;
    private NavMesh navMesh;
    private final Vector2 tmpVec = new Vector2();

    public void setNavMesh(NavMesh navMesh) { this.navMesh = navMesh; }
    public void clearEnemies() {
        if (enemies == null) return;
        for (int i = enemies.size() - 1; i >= 0; i--) {
            try {
                AbstractEnemy e = enemies.get(i);
                if (e != null) e.dispose();
            } catch (Exception ignored) {}
        }
        enemies.clear();
    }
    public void setWorldSize(float width, float height) { this.worldWidth = width; this.worldHeight = height; }
    public float getWorldWidth() { return worldWidth; }
    public float getWorldHeight() { return worldHeight; }
    public void setPolicy(capstone.main.Managers.WorldSpawnPolicy policy) {
        this.spawnPolicy = policy;
        this.periodicEnabled = policy == null || policy.allowPeriodicSpawns();
    }
    public void spawnSpecific(Class<? extends AbstractEnemy> type, float x, float y) {
        try {
            AbstractEnemy e = null;
            if (type == Survivor.class) e = new Survivor(x, y, screenShake, physics, navMesh);
            else if (type == Greed.class) e = new Greed(x, y, screenShake, physics, navMesh);
            else if (type == Security.class) e = new Security(x, y, screenShake, physics, navMesh);
            else if (type == Discaya.class) e = new Discaya(x, y, screenShake, physics, navMesh);
            else if (type == Follower.class) e = new Follower(x, y, screenShake, physics, navMesh);
            else if (type == QuiboloyBoss.class) e = new QuiboloyBoss(x, y, screenShake, physics, navMesh, this);
            if (e != null) enemies.add(e);
        } catch (Exception ex) {
            System.err.println("Failed to spawn entity: " + type + " " + ex.getMessage());
        }
    }

    private float enemySpawnRadius = 2.0f; // Increase radius to avoid spawning near walls
    private float enemySpacingRadius = 3.0f; // Less spacing to allow denser packs


    private String currentWorld = "World1"; // Default to World1
    private capstone.main.Managers.WorldSpawnPolicy spawnPolicy;
    private boolean periodicEnabled = true;

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
        int safe = Math.max(0, maxEnemies - enemies.size());
        int toSpawn = Math.min(count, safe);
        for (int i = 0; i < toSpawn; i++) {
            spawnRandomEnemy();
        }
    }

    public void update(float delta) {
        if (!periodicEnabled) return;
        // Balanced FPS guard: throttle spawns when FPS drops below 50
        float fps = Gdx.graphics.getFramesPerSecond();
        if (fps > 0 && fps < 58f) {
            // Increase interval and cap down to ease pressure aggressively
            spawnInterval = 0.9f;
            maxEnemies = Math.min(maxEnemies, 50);
        } else {
            // Restore target interval when FPS is healthy
            spawnInterval = 0.6f;
        }
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            // MEMORY FIX: Only spawn if under limit
            if (enemies.size() < maxEnemies) {
                // TRY 20 SPAWN ATTEMPTS to find valid positions
                boolean spawnSuccessful = false;
                for (int attempt = 1; attempt <= 20; attempt++) {
                    // Debug disabled: spawn attempt header removed

                    if (attemptSpawnRandomEnemy()) {
                        // Debug disabled: spawn success log removed
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
            // Debug disabled: primary spawn attempts warning removed

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

        // Debug: Log how many collision rectangles were found
        if (collisionRectangles != null) {
            Gdx.app.log("EnemySpawner", "Found " + collisionRectangles.size() + " collision rectangles");
            // Log first few rectangles to see what they contain
            for (int i = 0; i < Math.min(5, collisionRectangles.size()); i++) {
                Rectangle rect = collisionRectangles.get(i);
                Gdx.app.log("EnemySpawner", "Collision " + i + ": x=" + rect.x + ", y=" + rect.y + ", w=" + rect.width + ", h=" + rect.height);
            }
        } else {
            Gdx.app.log("EnemySpawner", "No collision rectangles found!");
        }
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
        // Slightly relax walkability: allow nodes that are near-walkable
        // by checking nearest of 5 candidates and accepting if any walkable
        NavNode primary = navMesh.getNearestNode(tmpVec);
        if ((primary == null || !primary.walkable)) {
            boolean nearOk = false;
            float[] offsets = {-0.2f, 0f, 0.2f};
            for (float ox : offsets) for (float oy : offsets) {
                tmpVec.set(x + ox, y + oy);
                NavNode nn = navMesh.getNearestNode(tmpVec);
                if (nn != null && nn.walkable) { nearOk = true; break; }
            }
            if (!nearOk) return false;
        }

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
                    // Debug disabled: spawn rejection - too close to enemy
                                     // (debug line removed)
                    return false;
                }
            }
        }

        // DEBUG: Show WHY positions are rejected
        if (!isValidSpawn) {
            if (spawnNode == null) {
                // Debug disabled: spawn rejection - NavNode is null
            } else if (!spawnNode.walkable) {
                // Debug disabled: spawn rejection - NavNode not walkable
            } else {
                // Debug disabled: spawn rejection - poor mobility
            }
        } else {
            // Debug disabled: spawn accepted
        }

        // EXTRA DEBUG: Show collision check results
        if (collisionRectangles != null) {
            Rectangle spawnRect = new Rectangle(x - enemySpawnRadius, y - enemySpawnRadius,
                                               enemySpawnRadius * 2f, enemySpawnRadius * 2f);
            boolean hasCollision = false;
            for (Rectangle collisionRect : collisionRectangles) {
                if (spawnRect.overlaps(collisionRect)) {
                    hasCollision = true;
                    // Debug disabled: collision blocked at position
                                     // (debug line removed)
                    break;
                }
            }
            if (!hasCollision && validDirections > 0) {
                // Debug disabled: collision passed at position
            }
        }

        return isValidSpawn;
    }


    /**
     * Set the current world for world-specific enemy spawning
     */
    public void setCurrentWorld(String worldPath) {
        if (worldPath.contains("World1_Boss")) {
            this.currentWorld = "World1";
        } else if (worldPath.contains("World1")) {
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
        java.util.List<Class<? extends AbstractEnemy>> allowed = null;
        if (spawnPolicy != null) {
            allowed = spawnPolicy.getAllowedSpawns();
        }
        switch (currentWorld) {
            case "World1":
                if (allowed != null && !allowed.isEmpty()) {
                    Class<? extends AbstractEnemy> t = allowed.get(random.nextInt(allowed.size()));
                    spawnSpecific(t, x, y);
                } else {
                    enemies.add(new Survivor(x, y, screenShake, physics, navMesh));
                }
                break;

            case "World2":
                // World2 enemies: Security only (remove Discaya)
                enemies.add(new Security(x, y, screenShake, physics, navMesh));
                break;

            case "World3":
                // World3 enemies: Only Followers (QuiboloyBoss is only spawned as boss)
                enemies.add(new Follower(x, y, screenShake, physics, navMesh));
                break;

            default:
                // Fallback to World1 enemies if unknown world
                enemies.add(new Survivor(x, y, screenShake, physics, navMesh));
                break;
        }
    }

    /**
     * Finds a valid walkable spawn position using collision detection.
     * 
     * @return A Vector2 with valid x,y coordinates
     */
    private Vector2 findValidSpawnPosition() {
        float x, y;
        int attempts = 0;
        int maxAttempts = 20;

        // Try to find walkable position
        do {
            x = 2f + (float) Math.random() * (worldWidth - 4f);
            y = 2f + (float) Math.random() * (worldHeight - 4f);
            attempts++;
        } while (attempts < maxAttempts && !isWalkablePosition(x, y));

        // Fallback to center area if no position found
        if (attempts >= maxAttempts) {
            x = worldWidth * 0.25f + (float) Math.random() * (worldWidth * 0.5f);
            y = worldHeight * 0.25f + (float) Math.random() * (worldHeight * 0.5f);
        }

        return new Vector2(x, y);
    }

    // Spawn specific enemy types (useful for testing or special spawns)
    
    /**
     * Spawns a Survivor enemy at a valid position.
     */
    public void spawnSurvivor() {
        Vector2 pos = findValidSpawnPosition();
        enemies.add(new Survivor(pos.x, pos.y, screenShake, physics, navMesh));
    }

    /**
     * Spawns a Security enemy at a valid position.
     */
    public void spawnSecurity() {
        Vector2 pos = findValidSpawnPosition();
        enemies.add(new Security(pos.x, pos.y, screenShake, physics, navMesh));
    }

    /**
     * Spawns a Discaya enemy at a valid position.
     */
    public void spawnDiscaya() {
        Vector2 pos = findValidSpawnPosition();
        enemies.add(new Discaya(pos.x, pos.y, screenShake, physics, navMesh));
    }

    /**
     * Spawns a Follower enemy at a valid position.
     */
    public void spawnFollower() {
        Vector2 pos = findValidSpawnPosition();
        enemies.add(new Follower(pos.x, pos.y, screenShake, physics, navMesh));
    }

    /**
     * Spawns the QuiboloyBoss at a valid position.
     */
    public void spawnQuiboloyBoss() {
        Vector2 pos = findValidSpawnPosition();
        enemies.add(new QuiboloyBoss(pos.x, pos.y, screenShake, physics, navMesh, this));
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
