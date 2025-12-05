package capstone.main.Render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class WorldRenderer {

    private final OrthogonalTiledMapRenderer mapRenderer;
    private final TiledMap map;
    private final int[] layers;

    /**
     * Constructs a WorldRenderer that automatically renders all layers in the map.
     *
     * @param renderer The TiledMap renderer
     * @param map      The TiledMap to render
     */
    public WorldRenderer(OrthogonalTiledMapRenderer renderer, TiledMap map) {
        this.mapRenderer = renderer;
        this.map = map;

        // Dynamically detect all layers
        int layerCount = map.getLayers().getCount();
        this.layers = new int[layerCount];
        for (int i = 0; i < layerCount; i++) {
            layers[i] = i;
        }
    }

    /**
     * Renders all layers using the given camera.
     *
     * @param camera The camera to set the view
     */
    public void render(OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render(layers);
    }

    /** Returns the map */
    public TiledMap getMap() {
        return map;
    }
}
