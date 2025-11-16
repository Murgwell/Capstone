package capstone.main.Render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class WorldRenderer {

    private final OrthogonalTiledMapRenderer mapRenderer;

    public WorldRenderer(OrthogonalTiledMapRenderer renderer) {
        this.mapRenderer = renderer;
    }

    public void renderGround(OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render(new int[]{0}); // Ground layer
        mapRenderer.render(new int[]{1}); // Ground layer
        mapRenderer.render(new int[]{2}); // Ground layer
        mapRenderer.render(new int[]{3}); // Ground layer
        mapRenderer.render(new int[]{4}); // Ground layer
        mapRenderer.render(new int[]{5}); // Ground layer
        mapRenderer.render(new int[]{6}); // Ground layer
    }
}

