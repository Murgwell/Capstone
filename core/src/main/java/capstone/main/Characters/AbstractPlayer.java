package capstone.main.Characters;

import capstone.main.Managers.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Abstract base class for all playable characters in the game
 * Provides common functionality like health, movement, animations, and Box2D physics
 */
public abstract class AbstractPlayer {
    private static final float PPM = 32f;
    protected Body body;

    // --- HEALTH ---
    protected float healthPoints;
    protected float maxHealthPoints;

    protected float manaPoints;
    protected float baseDamage;
    protected float maxDamage;
    protected float baseAttackSpeed;
    protected float attackSpeedMultiplier = 1f;
    protected float width;
    protected float height;
    protected DirectionManager directionManager;
    protected BoundaryManager boundaryManager;
    protected Sprite sprite;
    protected float weaponAimingRad;

    protected float attackTimer = getAttackDelay();
    protected float postDodgeDelay = 0.5f;
    protected float postSprintDelay = 0.3f;
    protected float postDodgeTimer = postDodgeDelay;
    protected float postSprintTimer = postSprintDelay;

    // --- ANIMATION ---
    protected CharacterAnimationManager animationManager;
    protected TextureRegion currentFrame;
    protected boolean useAnimations = false; // flag to enable/disable animations

    // Constructor for animated characters (NEW)
    public AbstractPlayer(float healthPoints, float manaPoints, float baseDamage, float maxDamage,
                          float baseAttackSpeed, String characterName, float x, float y,
                          float width, float height, float worldWidth, float worldHeight,
                          World physicsWorld) {
        this.maxHealthPoints = Math.max(1f, healthPoints);
        this.healthPoints = Math.min(Math.max(0f, healthPoints), maxHealthPoints);
        this.manaPoints = manaPoints;
        this.baseDamage = baseDamage;
        this.maxDamage = maxDamage;
        this.baseAttackSpeed = baseAttackSpeed;
        this.width = width;
        this.height = height;

        // Initialize animation manager
        animationManager = new CharacterAnimationManager(characterName);
        currentFrame = animationManager.getCurrentFrame();
        useAnimations = true;

        // Create sprite from first frame
        sprite = new Sprite(currentFrame);
        sprite.setSize(width, height);

        initializeCommon(x, y, worldWidth, worldHeight, physicsWorld);
    }

    // Constructor for static texture characters (EXISTING - for compatibility)
    public AbstractPlayer(float healthPoints, float manaPoints, float baseDamage, float maxDamage,
                          float baseAttackSpeed, Texture texture, float x, float y,
                          float width, float height, float worldWidth, float worldHeight,
                          World physicsWorld) {
        this.maxHealthPoints = Math.max(1f, healthPoints);
        this.healthPoints = Math.min(Math.max(0f, healthPoints), maxHealthPoints);
        this.manaPoints = manaPoints;
        this.baseDamage = baseDamage;
        this.maxDamage = maxDamage;
        this.baseAttackSpeed = baseAttackSpeed;
        this.width = width;
        this.height = height;

        sprite = new Sprite(texture);
        sprite.setSize(width, height);
        useAnimations = false;

        initializeCommon(x, y, worldWidth, worldHeight, physicsWorld);
    }

    // Common initialization code
    private void initializeCommon(float x, float y, float worldWidth, float worldHeight, World physicsWorld) {
        boundaryManager = new BoundaryManager(worldWidth, worldHeight, physicsWorld);
        directionManager = new DirectionManager(sprite);

        // --- Create Box2D body ---
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x + width/2f, y + height/2f);
        bodyDef.fixedRotation = true;
        body = physicsWorld.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.5f, 0.5f);
        body.createFixture(shape, 1f);
        shape.dispose();
    }

    // Health API
    public int getHp() {
        return Math.round(healthPoints);
    }

    public int getMaxHp() {
        return Math.round(maxHealthPoints);
    }

    public boolean isDead() {
        return healthPoints <= 0f;
    }

    public void heal(float amount) {
        if (amount <= 0f) return;
        float old = healthPoints;
        healthPoints = Math.min(healthPoints + amount, maxHealthPoints);
        onHealed(healthPoints - old);
    }

    public void damage(float amount) {
        if (amount <= 0f) return;
        float old = healthPoints;
        healthPoints = Math.max(healthPoints - amount, 0f);
        onDamaged(old - healthPoints);
    }

    protected void onHealed(float delta) {}
    protected void onDamaged(float delta) {}

    public void update(float delta, InputManager input, MovementManager movementManager, Viewport viewport) {
        // --- Compute mouse aiming ---
        Vector3 worldMouse = viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        boolean aimingLeft = worldMouse.x < body.getPosition().x;
        boolean isShooting = input.isAttacking();

        Vector2 inputDir = input.getMovement();
        boolean shiftHeld = input.isShiftHeld();

        // --- Apply movement ---
        movementManager.update(inputDir, delta, shiftHeld);
        Vector2 velocity = movementManager.getVelocity();

        // --- Update facing based on movement and aiming ---
        directionManager.updateFacing(
            velocity,
            movementManager.isSprinting(),
            movementManager.isDodging(),
            isShooting,
            aimingLeft
        );

        // --- Update animation if enabled ---
        if (useAnimations && animationManager != null) {
            if (velocity.len() > 0.1f) {
                // Moving: play running animation
                animationManager.update(
                    delta,
                    velocity.x,
                    velocity.y,
                    movementManager.isSprinting(),
                    movementManager.isDodging(),
                    isShooting,
                    aimingLeft
                );
                currentFrame = animationManager.getCurrentFrame();
            } else {
                // Idle: force direction again to ensure proper flip
                animationManager.update(
                    delta,
                    0f,
                    0f,
                    movementManager.isSprinting(),
                    movementManager.isDodging(),
                    isShooting,
                    aimingLeft
                );
                currentFrame = animationManager.getIdleFrame();
            }
            sprite.setRegion(currentFrame);
        }

        // --- Update sprite position ---
        sprite.setPosition(
            body.getPosition().x - width / 2f,
            body.getPosition().y - height / 2f
        );
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void updateWeaponAimingRad(Viewport viewport) {
        Vector3 worldCoords = viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float charX = body.getPosition().x;
        float charY = body.getPosition().y;
        weaponAimingRad = (float)Math.atan2(worldCoords.y - charY, worldCoords.x - charX);
    }

    public float getWeaponAimingRad() { return weaponAimingRad; }

    public boolean canAttack() {
        float cooldown = 1f / (baseAttackSpeed * attackSpeedMultiplier);
        return postDodgeTimer >= postDodgeDelay &&
            postSprintTimer >= postSprintDelay &&
            attackTimer >= cooldown;
    }

    public void updateAttackTimer(float delta) { attackTimer += delta; }
    public void onAttackPerformed() { attackTimer = 0f; }
    public float getAttackDelay() { return 1f / baseAttackSpeed; }
    public void modifyAttackSpeed(float multiplier) { attackSpeedMultiplier = multiplier; }
    public void resetAttackSpeed() { attackSpeedMultiplier = 1f; }

    public void updatePostMovementTimers(float delta, MovementManager movementManager) {
        postDodgeTimer = movementManager.isDodging() ? 0f : postDodgeTimer + delta;
        postSprintTimer = movementManager.isSprinting() ? 0f : postSprintTimer + delta;
        updateAttackTimer(delta);
    }

    public abstract void performAttack(float delta, float weaponRotationRad);

    public float getDamage() {
        return baseDamage + MathUtils.random(0f, maxDamage - baseDamage);
    }

    public float getHeight() { return height; }
    public float getWidth() { return width; }

    public Vector2 getPosition() { return body.getPosition(); }
    public Body getBody() { return body; }

    public void dispose() {
        if (useAnimations && animationManager != null) {
            animationManager.dispose();
        }
    }
}
