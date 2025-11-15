package capstone.main.Managers;

import com.badlogic.gdx.math.Vector2;

public class BoundaryManager {
    private float worldWidth;
    private float worldHeight;
    private float entityWidth;
    private float entityHeight;

    public BoundaryManager(float worldWidth, float worldHeight, float entityWidth, float entityHeight) {
        setBounds(worldWidth, worldHeight, entityWidth, entityHeight);
    }

    public void setBounds(float worldWidth, float worldHeight, float entityWidth, float entityHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.entityWidth = entityWidth;
        this.entityHeight = entityHeight;
    }

    public Vector2 clamp(Vector2 pos) {
        pos.x = Math.max(0, Math.min(pos.x, worldWidth - entityWidth));
        pos.y = Math.max(0, Math.min(pos.y, worldHeight - entityHeight));
        return pos;
    }
}
