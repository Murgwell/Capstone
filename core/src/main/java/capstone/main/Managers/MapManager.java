package capstone.main.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class MapManager {
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer renderer;

    private float worldWidth;
    private float worldHeight;

    // Load and calculate world dimensions
    public void load(String mapPath) {
        if (tiledMap != null) {
            tiledMap.dispose(); // prevent memory leak if reloading
        }

        tiledMap = new TmxMapLoader().load(mapPath);
        renderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / 32f); // 32px → 1 world unit scale

        // Assuming layer 0 is the ground
        TiledMapTileLayer groundLayer = (TiledMapTileLayer) tiledMap.getLayers().get(0);
        worldWidth = groundLayer.getWidth() * groundLayer.getTileWidth() / 32f;
        worldHeight = groundLayer.getHeight() * groundLayer.getTileHeight() / 32f;

        Gdx.app.log("MapManager", "Loaded map: " + mapPath + " (" + worldWidth + "x" + worldHeight + ")");
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
}
