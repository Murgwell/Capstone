package capstone.main.Enemies;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.CollisionBits;
import capstone.main.Managers.ScreenShake;
import capstone.main.Managers.SoundManager;
import capstone.main.UI.HealthBar;
import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.DirectionManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public abstract class AbstractEnemy {

    ScreenShake screenShake;
    protected boolean pendingRemoval = false;
    protected Sprite sprite;
    protected Body body;
    protected float health;
    protected float maxHealth;
    protected HealthBar healthBar;

    protected boolean isAggro = false;
    protected boolean enteredClose = false;
    protected float defaultChaseDistance = 3f;
    protected float aggroChaseDistance = 6f;
    protected float speed = 1.5f;
    protected float hitboxRadius;

    private float hitFlashTimer = 0f;   // in seconds
    private final float hitFlashDuration = 0.1f; // how long it stays white
    protected Sprite whiteOverlaySprite;
    protected DirectionManager directionManager;

    // Add these fields near the top with other fields
    private float attackCooldown = 0f;
    private static final float ATTACK_COOLDOWN_TIME = 1.0f; // 1 second between attacks
    private static final float MELEE_RANGE = 0.8f; // How close to attack
    private static final float MELEE_DAMAGE = 5f; // 5 HP = half a heart

    public AbstractEnemy(float x, float y, Texture texture, float width, float height, float maxHealth, ScreenShake screenShake, PhysicsManager physics) {
        this.sprite = new Sprite(texture);
        this.sprite.setPosition(x, y);
        this.sprite.setSize(width, height);

        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.screenShake = screenShake;

        this.hitboxRadius = Math.min(width, height) / 2f;

        this.healthBar = new HealthBar(sprite, maxHealth, width, 3f / 32f, 0.05f);

        this.directionManager = new DirectionManager(sprite);

        // --- Box2D body ---
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(x + width/2f, y + height/2f);
        bd.fixedRotation = true;

        body = physics.getWorld().createBody(bd);

        CircleShape shape = new CircleShape();
        shape.setRadius(Math.min(width, height)/2f);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.isSensor = false;
        fd.filter.categoryBits = CollisionBits.ENEMY;
        fd.filter.maskBits = CollisionBits.BULLET | CollisionBits.WALL;

        Fixture fixture = body.createFixture(fd);
        fixture.setUserData(this);

        shape.dispose();

        whiteOverlaySprite = new Sprite(new Texture("enemyCharacterWhiteOverlay.png"));
        whiteOverlaySprite.setSize(width, height);
    }

    public abstract void update(float delta, AbstractPlayer player);

    public float getMaxHealth() {
        return maxHealth;
    }

    protected void defaultChaseBehavior(float delta, AbstractPlayer player) {
        if (isDead()) {
            body.setLinearVelocity(0, 0);
            return;
        }

        // Update attack cooldown
        updateAttackCooldown(delta);

        float px = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float py = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        Vector2 enemyPos = body.getPosition();
        float dx = px - enemyPos.x;
        float dy = py - enemyPos.y;
        float dist = (float)Math.sqrt(dx*dx + dy*dy);
        if (dist < 0.0001f) dist = 0.0001f;

        Vector2 velocity = new Vector2(0, 0);

        if (isAggro) {
            velocity.set(dx / dist * speed, dy / dist * speed);
            if (!enteredClose && dist <= aggroChaseDistance) enteredClose = true;
            if (enteredClose && dist > aggroChaseDistance) {
                isAggro = false;
                enteredClose = false;
                velocity.set(0,0);
            }
        } else {
            if (dist <= defaultChaseDistance) velocity.set(dx / dist * speed, dy / dist * speed);
        }

        body.setLinearVelocity(velocity);
        sprite.setPosition(body.getPosition().x - sprite.getWidth()/2f,
            body.getPosition().y - sprite.getHeight()/2f);

        directionManager.setFacingLeft(velocity.x < 0);

        if (healthBar != null) healthBar.update(delta);
    }

    public void takeHit(float damage) {
        health -= damage;
        if (healthBar != null) healthBar.setHealth(health);
        isAggro = true;
        enteredClose = false;

        // Trigger hit flash
        hitFlashTimer = hitFlashDuration;

        screenShake.shake(0.25f, 0.05f);

        // PLAY ENEMY HIT SOUND
        SoundManager.getInstance().playSound("enemy_hit");

        if (health <= 0 && !pendingRemoval)
            pendingRemoval = true;
    }

    // Call every frame in update
    protected void updateHitFlash(float delta) {
        if (hitFlashTimer > 0f) {
            hitFlashTimer -= delta;
            if (hitFlashTimer < 0f) hitFlashTimer = 0f;
        }
    }

    // Add this method
    public void updateAttackCooldown(float delta) {
        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }
    }

    public boolean canAttack() {
        return attackCooldown <= 0;
    }

    public void resetAttackCooldown() {
        attackCooldown = ATTACK_COOLDOWN_TIME;
    }

    public float getMeleeRange() {
        return MELEE_RANGE;
    }

    public float getMeleeDamage() {
        return MELEE_DAMAGE;
    }

    public boolean isDead() { return health <= 0; }
    public Sprite getSprite() { return sprite; }
    public HealthBar getHealthBar() { return healthBar; }
    public boolean isPendingRemoval() { return pendingRemoval; }
    public Body getBody() { return body; }

    public float getHitFlashAlpha() {
        // returns 0..1
        return Math.max(0f, Math.min(1f, hitFlashTimer / hitFlashDuration));
    }

    public Sprite getWhiteOverlaySprite() {
        return whiteOverlaySprite;
    }

    public void updateWhiteOverlay() {
        whiteOverlaySprite.setPosition(sprite.getX(), sprite.getY());
        whiteOverlaySprite.setSize(sprite.getWidth(), sprite.getHeight());

        // Flip according to facing direction
        whiteOverlaySprite.setFlip(directionManager.isFacingLeft(), false);

        // Keep origin same as sprite
        whiteOverlaySprite.setOrigin(sprite.getOriginX(), sprite.getOriginY());
    }
}
