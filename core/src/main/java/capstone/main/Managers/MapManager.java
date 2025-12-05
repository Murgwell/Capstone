package capstone.main.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
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

    public MapManager(PhysicsManager physics) {
        this.physics = physics;
    }

    public void load(String mapPath) {
        if (tiledMap != null) tiledMap.dispose();

        tiledMap = new TmxMapLoader().load(mapPath);
        renderer = new OrthogonalTiledMapRenderer(tiledMap, scale);

        // Calculate world size from first tile layer
        TiledMapTileLayer ground = (TiledMapTileLayer) tiledMap.getLayers().get(0);
        worldWidth = ground.getWidth() * ground.getTileWidth() * scale;
        worldHeight = ground.getHeight() * ground.getTileHeight() * scale;

        CollisionLoader.buildCollision(physics.getWorld(), tiledMap, "collisionLayer", 32f);

        Gdx.app.log("MapManager", "Loaded map: " + mapPath + " (" + worldWidth + "x" + worldHeight + ")");
    }

    public OrthogonalTiledMapRenderer getRenderer() { return renderer; }
    public TiledMap getTiledMap() { return tiledMap; }
    public float getWorldWidth() { return worldWidth; }
    public float getWorldHeight() { return worldHeight; }

    public void dispose() {
        try {
            if (renderer != null) renderer.dispose();
            if (tiledMap != null) tiledMap.dispose();
        } catch (Exception e) {
            Gdx.app.error("MapManager", "Error disposing map resources: " + e.getMessage());
        }
    }
}
