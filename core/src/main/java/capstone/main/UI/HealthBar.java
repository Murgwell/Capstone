package capstone.main.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class HealthBar {
    private final Sprite target; // the entity this bar follows
    private float maxHealth;
    private float currentHealth;

    private float width;  // width of the bar in world units
    private float height; // height of the bar
    private float yOffset; // vertical offset from sprite

    public HealthBar(Sprite target, float maxHealth, float width, float height, float yOffset) {
        this.target = target;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.width = width;
        this.height = height;
        this.yOffset = yOffset;
    }

    public void setHealth(float health) {
        this.currentHealth = Math.max(0, Math.min(health, maxHealth));
    }

    public void draw(ShapeRenderer shapeRenderer) {
        float x = target.getX();
        float y = target.getY() + target.getHeight() + yOffset;
        float healthPercent = currentHealth / maxHealth;

        // Background bar
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        // Foreground bar (health)
        Color barColor = healthPercent > 0.5f ? Color.GREEN
            : healthPercent > 0.2f ? Color.YELLOW
            : Color.RED;
        shapeRenderer.setColor(barColor);
        shapeRenderer.rect(x, y, width * healthPercent, height);
    }
}
