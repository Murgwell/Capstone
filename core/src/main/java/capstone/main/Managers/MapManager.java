package capstone.main.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class MapManager {
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer renderer;
    private final PhysicsManager physics;

    private float worldWidth, worldHeight;
    private final float scale = 1 / 32f; // 32px → 1 world unit
    private String currentMapPath;

    public MapManager(PhysicsManager physics) {
        this.physics = physics;
    }

    public void load(String mapPath) {
        try {
            Gdx.app.log("MapManager", "========================================");
            Gdx.app.log("MapManager", "LOADING MAP: " + mapPath);
            Gdx.app.log("MapManager", "========================================");

            // Step 1: Clear physics bodies first (most important!)
            clearStaticBodies();

            // Step 2: Dispose old resources
            if (renderer != null) {
                try {
                    // Don't dispose the renderer's batch - it may be shared
                    // Just null out the reference
                    Gdx.app.log("MapManager", "✓ Old renderer reference cleared (batch preserved)");
                } catch (Exception e) {
                    Gdx.app.log("MapManager", "Warning: renderer cleanup failed: " + e.getMessage());
                }
                renderer = null;
            }

            if (tiledMap != null) {
                try {
                    tiledMap.dispose();
                    Gdx.app.log("MapManager", "✓ Old map disposed");
                } catch (Exception e) {
                    Gdx.app.error("MapManager", "Warning: map disposal failed", e);
                }
                tiledMap = null;
            }

            // Small delay to ensure cleanup (helps with race conditions)
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}

            // Step 3: Load new map
            Gdx.app.log("MapManager", "Loading new map file...");
            tiledMap = new TmxMapLoader().load(mapPath);
            Gdx.app.log("MapManager", "✓ Map file loaded");

            // Step 4: Create renderer
            renderer = new OrthogonalTiledMapRenderer(tiledMap, scale);
            Gdx.app.log("MapManager", "✓ Renderer created");

            // Step 5: Calculate world dimensions
            TiledMapTileLayer ground = (TiledMapTileLayer) tiledMap.getLayers().get(0);
            worldWidth = ground.getWidth() * ground.getTileWidth() * scale;
            worldHeight = ground.getHeight() * ground.getTileHeight() * scale;
            Gdx.app.log("MapManager", "✓ Dimensions: " + worldWidth + " x " + worldHeight);

            // Step 6: Build collision for new map
            Gdx.app.log("MapManager", "Building collision...");
            CollisionLoader.buildCollision(physics.getWorld(), tiledMap, "collisionLayer", 32f, true);
            Gdx.app.log("MapManager", "✓ Collision layer built (blocks movement ONLY)");

            // Build wall collision (for blocking bullets/fireballs/punches ONLY, not movement)
            CollisionLoader.buildCollision(physics.getWorld(), tiledMap, "wallLayer", 32f, false);
            Gdx.app.log("MapManager", "✓ Wall layer built (blocks projectiles ONLY)");

            this.currentMapPath = mapPath;

            Gdx.app.log("MapManager", "========================================");
            Gdx.app.log("MapManager", "✓✓✓ MAP LOAD COMPLETE: " + mapPath);
            Gdx.app.log("MapManager", "========================================");

        } catch (Exception e) {
            Gdx.app.error("MapManager", "========================================");
            Gdx.app.error("MapManager", "CRITICAL ERROR LOADING MAP: " + mapPath);
            Gdx.app.error("MapManager", "========================================", e);
            throw new RuntimeException("Failed to load map: " + mapPath, e);
        }
    }

    /**
     * Clear all static physics bodies (map collision)
     * This MUST be done before loading a new map
     */
    private void clearStaticBodies() {
        try {
            // Wait for physics world to unlock
            int waitCount = 0;
            while (physics.getWorld().isLocked()) {
                Gdx.app.log("MapManager", "Waiting for physics unlock... (" + waitCount + ")");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {}
                waitCount++;
                if (waitCount > 100) {
                    Gdx.app.error("MapManager", "Physics world stuck locked!");
                    break;
                }
            }

            Array<Body> bodies = new Array<>();
            physics.getWorld().getBodies(bodies);

            int staticCount = 0;

            // Destroy only STATIC bodies (map collision)
            // Dynamic bodies (player, enemies) are handled by Game.java
            for (Body body : bodies) {
                if (body.getType() == BodyDef.BodyType.StaticBody) {
                    try {
                        physics.getWorld().destroyBody(body);
                        staticCount++;
                    } catch (Exception e) {
                        Gdx.app.error("MapManager", "Could not destroy body", e);
                    }
                }
            }

            Gdx.app.log("MapManager", "✓ Cleared " + staticCount + " static bodies");

        } catch (Exception e) {
            Gdx.app.error("MapManager", "Error clearing physics bodies", e);
        }
    }

    public OrthogonalTiledMapRenderer getRenderer() {
        return renderer;
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getWorldHeight() {
        return worldHeight;
    }

    public void dispose() {
        try {
            if (renderer != null) {
                renderer.dispose();
                renderer = null;
            }

            if (tiledMap != null) {
                tiledMap.dispose();
                tiledMap = null;
            }

            Gdx.app.log("MapManager", "MapManager disposed successfully");
        } catch (Exception e) {
            Gdx.app.error("MapManager", "Error disposing MapManager: " + e.getMessage());
        }
    }
}
