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
        mapRenderer.render(new int[]{0,1,2,3,4,5,6,7});
    }
}
