package capstone.main.Sprites;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DamageNumber {
    private float x, y;
    private final float value;
    private float lifetime = 0.7f; // seconds
    private float timer = 0f;
    private final float riseSpeed = 0.5f; // units per second
    private final BitmapFont font;

    public boolean isAlive = true;

    public DamageNumber(float x, float y, float value, BitmapFont font) {
        this.x = x;
        this.y = y;
        this.value = value;
        this.font = font;
    }

    public void update(float delta) {
        timer += delta;
        y += riseSpeed * delta;
        if (timer >= lifetime) isAlive = false;
    }

    public void draw(SpriteBatch batch) {
        // Draw as integer and in red
        font.setColor(1f, 0f, 0f, 1f); // red
        font.draw(batch, String.valueOf(Math.round(value)), x, y);
    }

    public void updateAndDraw(SpriteBatch batch, float delta) {
        update(delta);
        draw(batch);
    }

}
