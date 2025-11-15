package capstone.main.Sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    private static final Texture DEFAULT_TEXTURE = new Texture("bullet.png"); // default bullet texture

    Sprite sprite;
    Vector2 velocity;
    float damage;
    float speed = 30f;   // bullet speed
    float lifetime = 3f;

    // Stretch based on distance traveled
    float baseWidth = 0.025f;
    float baseHeight = 0.4f;
    float maxStretch = 2f;
    float distanceTraveled = 0f;
    float stretchDistance = 0.5f;

    /** Constructor using default bullet texture */
    public Bullet(float x, float y, Vector2 direction, float damage) {
        sprite = new Sprite(DEFAULT_TEXTURE);

        sprite.setOrigin(baseWidth / 2f, 0f);
        sprite.setSize(baseWidth, baseHeight);
        sprite.setPosition(x - baseWidth / 2f, y);

        velocity = new Vector2(direction).nor().scl(speed);
        this.damage = damage;

        // Rotate to face travel direction
        float angleDeg = (float) Math.toDegrees((float) Math.atan2(direction.y, direction.x));
        sprite.setRotation(angleDeg - 90);
    }

    public boolean update(float delta) {
        // Move bullet
        float dx = velocity.x * delta;
        float dy = velocity.y * delta;
        sprite.translate(dx, dy);

        // Track distance traveled
        distanceTraveled += new Vector2(dx, dy).len();

        // Stretch based on distance
        float t = Math.min(distanceTraveled / stretchDistance, 1f);
        float currentHeight = baseHeight * (1 + t * (maxStretch - 1));
        sprite.setSize(baseWidth, currentHeight);

        lifetime -= delta;
        return lifetime > 0;
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public float getDamage() {
        return damage;
    }

    public Rectangle getBoundingBox() {
        return sprite.getBoundingRectangle();
    }
}
