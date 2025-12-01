package capstone.main.Sprites;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DamageNumber {
    private String text;
    private float x;
    private float y;
    private BitmapFont font;
    private Color color;

    private float lifetime;
    private float maxLifetime = 1.5f; // How long the number stays visible (seconds)
    private float velocityY = 0.5f;   // How fast it floats upward
    private float alpha = 1f;         // Opacity

    public DamageNumber(String text, float x, float y, BitmapFont font, Color color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.font = font;
        this.color = color.cpy(); // Copy to avoid shared reference
        this.lifetime = 0f;
    }

    public void update(float delta) {
        lifetime += delta;

        // Float upward
        y += velocityY * delta;

        // Fade out over time
        float progress = lifetime / maxLifetime;
        alpha = 1f - progress;

        if (alpha < 0) alpha = 0;
    }

    public void draw(SpriteBatch batch) {
        Color oldColor = font.getColor().cpy();

        // Set color with alpha
        font.setColor(color.r, color.g, color.b, alpha);

        // Draw the damage text
        font.draw(batch, text, x, y);

        // Restore original color
        font.setColor(oldColor);
    }

    public boolean isExpired() {
        return lifetime >= maxLifetime;
    }
}
