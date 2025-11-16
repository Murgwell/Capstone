package capstone.main.Sprites;

import capstone.main.Characters.AbstractPlayer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    //private static final Texture DEFAULT_TEXTURE = new Texture("bullet.png");
    private static final Texture DEFAULT_TEXTURE = new Texture("Textures/UI/Bullet Indicators/Pistol-Bullet.png");
    private final AbstractPlayer owner;

    Sprite sprite;
    Vector2 velocity;
    float damage;
    float speed = 50f;
    float lifetime = 3f;

    float baseWidth = 0.05f;
    float baseHeight = 0.3f;
    float maxStretch = 5f;
    float distanceTraveled = 0f;
    float stretchDistance = 0.25f;

    /** Constructor using default bullet texture */
    public Bullet(float x, float y, Vector2 direction, AbstractPlayer owner, float damage) {
        this.owner = owner;
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
        return owner.getDamage();
    }

    public Rectangle getBoundingBox() {
        return sprite.getBoundingRectangle();
    }
}
