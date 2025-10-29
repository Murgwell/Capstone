package capstone.main.Handlers;

import com.badlogic.gdx.math.Vector2;

public class BoundaryManager {

    private final float worldWidth;
    private final float worldHeight;
    private final float entityWidth;
    private final float entityHeight;

    public BoundaryManager(float worldWidth, float worldHeight, float entityWidth, float entityHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.entityWidth = entityWidth;
        this.entityHeight = entityHeight;
    }

    public Vector2 clamp(Vector2 position) {
        position.x = Math.max(0, Math.min(position.x, worldWidth - entityWidth));
        position.y = Math.max(0, Math.min(position.y, worldHeight - entityHeight));
        return position;
    }
}
