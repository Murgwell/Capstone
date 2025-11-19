package capstone.main.Managers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class ScreenShake {

    private float duration = 0f;
    private float intensity = 0f;

    private final Vector2 offset = new Vector2();

    /** Start a shake effect */
    public void shake(float duration, float intensity) {
        this.duration = duration;
        this.intensity = intensity;
    }

    /** Call every frame to update the shake */
    public void update(float delta) {
        if (duration > 0f) {
            duration -= delta;
            // Smooth decay: intensity reduces as timer runs out
            float decay = duration > 0f ? duration / (duration + delta) : 0f;
            offset.set(
                MathUtils.random(-intensity, intensity) * decay,
                MathUtils.random(-intensity, intensity) * decay
            );
        } else {
            offset.set(0f, 0f);
        }
    }

    /** Returns the current offset to apply to the camera */
    public float getOffsetX() { return offset.x; }
    public float getOffsetY() { return offset.y; }
}
