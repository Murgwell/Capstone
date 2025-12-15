package capstone.main.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * A visual health bar that appears above entities when they take damage.
 * 
 * <p>Features:
 * <ul>
 *   <li>Automatic fade-in/fade-out animation</li>
 *   <li>Color-coded by health percentage (green > yellow > red)</li>
 *   <li>Follows target sprite position</li>
 *   <li>Timed visibility (3 seconds, then fades)</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>{@code
 * HealthBar bar = new HealthBar(sprite, 100f, 2f, 0.2f, 0.5f);
 * bar.setHealth(currentHealth); // Shows bar and updates
 * bar.update(delta);
 * bar.draw(shapeRenderer);
 * }</pre>
 * 
 * @author Capstone Team
 * @version 1.0
 */
public class HealthBar {
    private final Sprite target;
    private float maxHealth;
    private float currentHealth;

    private boolean visible = false;

    private float visibleTimer = 0f;
    private static final float VISIBLE_DURATION = 3f; // stay fully visible for 3 seconds
    private static final float FADE_SPEED = 1f;       // fade-out speed per second

    private float alpha = 0f; // start invisible

    private float width;
    private float height;
    private float yOffset;

    public HealthBar(Sprite target, float maxHealth, float width, float height, float yOffset) {
        this.target = target;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.width = width;
        this.height = height;
        this.yOffset = yOffset;
    }

    // Called whenever the enemy takes damage
    public void setHealth(float health) {
        this.currentHealth = Math.max(0, Math.min(health, maxHealth));

        show(); // make bar visible immediately upon damage
    }

    // Show the bar immediately at full opacity
    public void show() {
        visible = true;
        visibleTimer = VISIBLE_DURATION;
        alpha = 1f;
    }

    // Update fading logic
    public void update(float delta) {
        if (!visible) return;

        if (visibleTimer > 0) {
            visibleTimer -= delta;
        } else {
            alpha -= FADE_SPEED * delta;

            if (alpha <= 0f) {
                alpha = 0f;
                visible = false;
            }
        }
    }

    public void draw(ShapeRenderer shapeRenderer) {
        if (!visible || alpha <= 0f) return;

        float x = target.getX();
        float y = target.getY() + target.getHeight() + yOffset;
        float healthPercent = currentHealth / maxHealth;

        // Background bar
        shapeRenderer.setColor(0, 0, 0, alpha);
        shapeRenderer.rect(x, y, width, height);

        // Foreground (health)
        Color barColor =
            healthPercent > 0.5f ? Color.GREEN :
                healthPercent > 0.2f ? Color.YELLOW :
                    Color.RED;

        shapeRenderer.setColor(barColor.r, barColor.g, barColor.b, alpha);
        shapeRenderer.rect(x, y, width * healthPercent, height);
    }
}
