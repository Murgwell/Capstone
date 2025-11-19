package capstone.main.Managers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class BoundaryManager {
    private final float worldWidth, worldHeight;
    private final World world;

    public BoundaryManager(float worldWidth, float worldHeight, World world) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.world = world;
        createWorldBounds();
    }

    private void createWorldBounds() {
        float thickness = 0.5f;

        // Bottom
        createWall(worldWidth / 2f, thickness / 2f, worldWidth, thickness);
        // Top
        createWall(worldWidth / 2f, worldHeight - thickness / 2f, worldWidth, thickness);
        // Left
        createWall(0f, worldHeight / 2f, thickness, worldHeight);
        // Right
        createWall(worldWidth, worldHeight / 2f, thickness, worldHeight);
    }

    private void createWall(float centerX, float centerY, float width, float height) {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.StaticBody;
        def.position.set(centerX, centerY);

        Body wall = world.createBody(def);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f, height / 2f);

        wall.createFixture(shape, 0);
        shape.dispose();
    }

    public Vector2 clamp(Vector2 pos, float halfWidth, float halfHeight) {
        float x = Math.max(halfWidth, Math.min(pos.x, worldWidth - halfWidth));
        float y = Math.max(halfHeight, Math.min(pos.y, worldHeight - halfHeight));
        return new Vector2(x, y);
    }
}
