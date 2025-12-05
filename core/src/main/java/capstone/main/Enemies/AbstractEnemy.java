package capstone.main.Enemies;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.CollisionBits;
import capstone.main.Managers.ScreenShake;
import capstone.main.Managers.SoundManager;
import capstone.main.UI.HealthBar;
import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.DirectionManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
    protected float baseSpeed = 1.5f;
    protected float hitboxRadius;

    private float hitFlashTimer = 0f;   // in seconds
    private final float hitFlashDuration = 0.1f; // how long it stays white
    protected Sprite whiteOverlaySprite;
    protected DirectionManager directionManager;

    // Status effects
    protected boolean isSlowed = false;
    protected float slowTimer = 0f;
    protected float slowMultiplier = 0.5f; // 50% speed when slowed
    protected BitmapFont statusFont;
    protected String statusText = "";

    private float attackCooldown = 0f;
    private static final float ATTACK_COOLDOWN_TIME = 1.0f; // 1 second between attacks
    private static final float MELEE_RANGE = 0.8f; // How close to attack
    private static final float MELEE_DAMAGE = 5f; // 5 HP = half a heart

    public AbstractEnemy(float x, float y, Texture texture, float width, float height, float maxHealth, ScreenShake screenShake, PhysicsManager physics) {
        this.sprite = new Sprite(texture);
        this.sprite.setPosition(x, y);
        this.sprite.setSize(width, height);
        this.baseSpeed = speed;

        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.screenShake = screenShake;

        this.hitboxRadius = Math.min(width, height) / 2f;

        this.healthBar = new HealthBar(sprite, maxHealth, width, 3f / 32f, 0.05f);

        this.directionManager = new DirectionManager(sprite);

        this.statusFont = new BitmapFont();
        this.statusFont.getData().setScale(0.08f);
        this.statusFont.setColor(com.badlogic.gdx.graphics.Color.CYAN);

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

        whiteOverlaySprite = new Sprite(new Texture("Textures/Enemies/World1/Greed/Run-Forward/orc1_walk_full-0.png"));
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

        // Update status effects
        updateStatusEffects(delta);

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

        // Use speed (which is already modified by slow effect)
        // REMOVED: float currentSpeed = speed; - not needed, just use 'speed' directly

        if (isAggro) {
            velocity.set(dx / dist * speed, dy / dist * speed); // Changed from currentSpeed to speed
            if (!enteredClose && dist <= aggroChaseDistance) enteredClose = true;
            if (enteredClose && dist > aggroChaseDistance) {
                isAggro = false;
                enteredClose = false;
                velocity.set(0,0);
            }
        } else {
            if (dist <= defaultChaseDistance) velocity.set(dx / dist * speed, dy / dist * speed); // Changed
        }

        body.setLinearVelocity(velocity);
        sprite.setPosition(body.getPosition().x - sprite.getWidth()/2f,
            body.getPosition().y - sprite.getHeight()/2f);

        directionManager.setFacingLeft(velocity.x < 0);

        if (healthBar != null) healthBar.update(delta);
    }

    protected void updateStatusEffects(float delta) {
        if (isSlowed) {
            slowTimer -= delta;
            if (slowTimer <= 0) {
                removeSlowEffect();
            }
        }
    }

    // Attack cooldown methods
    public void updateAttackCooldown(float delta) {
        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }
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

    // Slow effect methods
    public void applySlowEffect(float duration, float slowAmount) {
        if (!isSlowed) {
            isSlowed = true;
            slowMultiplier = slowAmount;
            speed = baseSpeed * slowMultiplier;
            statusText = "";

            // Change sprite tint to indicate slow
            sprite.setColor(0.5f, 0.5f, 1f, 1f); // Blue tint

            Gdx.app.log("StatusEffect", "Enemy slowed! Speed: " + speed);
        }
        slowTimer = duration; // Refresh duration
    }

    public void removeSlowEffect() {
        isSlowed = false;
        speed = baseSpeed;
        statusText = "";
        sprite.setColor(1f, 1f, 1f, 1f); // Reset color

        Gdx.app.log("StatusEffect", "Slow effect removed. Speed: " + speed);
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

    public boolean isSlowed() {
        return isSlowed;
    }

    public String getStatusText() {
        return statusText;
    }

    public BitmapFont getStatusFont() {
        return statusFont;
    }

    public float getSlowTimer() {
        return slowTimer;
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
