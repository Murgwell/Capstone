package capstone.main.Enemies;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.UI.HealthBar;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.graphics.Texture;

public abstract class AbstractEnemy {
    protected Sprite sprite;
    protected float health;
    protected float maxHealth;
    protected HealthBar healthBar;
    protected float defaultChaseDistance = 3f;
    protected float aggroChaseDistance = 6f;
    protected boolean isAggro = false;

    protected float hitboxRadius;

    public AbstractEnemy(float x, float y, Texture texture, float width, float height, float maxHealth) {
        this.sprite = new Sprite(texture);
        this.sprite.setPosition(x, y);
        this.sprite.setSize(width, height);

        this.maxHealth = maxHealth;
        this.health = maxHealth;

        this.hitboxRadius = Math.min(width, height) / 2f;

        // Health bar automatically initialized
        this.healthBar = new HealthBar(sprite, maxHealth, width, 3f / 32f, 0.05f);
    }

    public abstract void update(float delta, AbstractPlayer player);

    public void takeHit(float damage) {
        health -= damage;
        isAggro = true;
        healthBar.setHealth(health); // keep health bar in sync
    }

    public boolean isDead() { return health <= 0; }

    public Sprite getSprite() { return sprite; }

    public Circle getHitbox() {
        float cx = sprite.getX() + sprite.getWidth() / 2f;
        float cy = sprite.getY() + sprite.getHeight() / 2f;
        return new Circle(cx, cy, hitboxRadius);
    }

    public HealthBar getHealthBar() { return healthBar; }
}
