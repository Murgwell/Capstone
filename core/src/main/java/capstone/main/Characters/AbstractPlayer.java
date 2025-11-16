package capstone.main.Characters;

import capstone.main.Managers.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;


public abstract class AbstractPlayer {

    protected float healthPoints;
    protected float manaPoints;
    protected float baseDamage;
    protected float maxDamage;
    protected float baseAttackSpeed;
    protected float attackSpeedMultiplier = 1f;
    protected DirectionManager directionManager; // initialize in constructor
    protected BoundaryManager boundaryManager;
    protected Vector2 position = new Vector2();
    protected Sprite sprite;
    protected float weaponAimingRad; // current aiming angle

    protected float attackTimer = getAttackDelay();
    protected float postDodgeDelay = 0.5f;
    protected float postSprintDelay = 0.3f;
    protected float postDodgeTimer = postDodgeDelay;
    protected float postSprintTimer = postSprintDelay;

    public AbstractPlayer(float healthPoints, float manaPoints, float baseDamage, float maxDamage, float baseAttackSpeed,Texture texture,
                          float x, float y, float width, float height,
                          float worldWidth, float worldHeight) {
        this.healthPoints = healthPoints;
        this.manaPoints = manaPoints;
        this.baseDamage = baseDamage;
        this.maxDamage = maxDamage;
        this.baseAttackSpeed = baseAttackSpeed;
        position.set(x, y);
        sprite = new Sprite(texture);
        sprite.setSize(width, height);

        boundaryManager = new BoundaryManager(worldWidth, worldHeight, width, height);
        directionManager = new DirectionManager(sprite);
    }

    public void update(float delta,
                       InputManager input,
                       MovementManager movementManager,
                       Viewport viewport) {

        // --- compute mouse aiming ---
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        Vector3 worldMouse = viewport.getCamera().unproject(new Vector3(mouseX, mouseY, 0));
        float charCenterX = sprite.getX() + sprite.getWidth() / 2f;

        boolean aimingLeft = worldMouse.x < charCenterX;
        boolean isShooting = input.isAttacking();

        position = movementManager.update(position, input, delta);
        position = boundaryManager.clamp(position);
        sprite.setPosition(position.x, position.y);

        Vector2 velocity = movementManager.getVelocity();
        boolean sprinting = movementManager.isSprinting();
        boolean dodging = movementManager.isDodging();
        directionManager.updateFacing(velocity, sprinting, dodging, isShooting, aimingLeft);
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void updateWeaponAiming(Viewport viewport) {
        Vector3 worldCoords = viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float charX = sprite.getX() + sprite.getWidth() / 2f;
        float charY = sprite.getY() + sprite.getHeight() / 2f;

        weaponAimingRad = (float) Math.atan2(worldCoords.y - charY, worldCoords.x - charX);
    }

    public float getWeaponAimingRad() {
        return weaponAimingRad;
    }

    public boolean canAttack() {
        float cooldown = 1f / (baseAttackSpeed * attackSpeedMultiplier);
        return postDodgeTimer >= postDodgeDelay &&
            postSprintTimer >= postSprintDelay &&
            attackTimer >= cooldown;
    }

    public void updateAttackTimer(float delta) {
        attackTimer += delta;
    }
    public void onAttackPerformed() {
        attackTimer = 0f;
    }

    public float getAttackDelay() {
        return 1f / baseAttackSpeed;
    }

    public void modifyAttackSpeed(float multiplier) {
        attackSpeedMultiplier = multiplier;
    }

    public void resetAttackSpeed() {
        attackSpeedMultiplier = 1f;
    }

    // --- Movement timers ---
    public void updatePostMovementTimers(float delta, MovementManager movementManager) {
        postDodgeTimer = movementManager.isDodging() ? 0f : postDodgeTimer + delta;
        postSprintTimer = movementManager.isSprinting() ? 0f : postSprintTimer + delta;
        updateAttackTimer(delta);
    }

    // --- Abstract attack method ---
    public abstract void performAttack(float delta, float weaponRotationRad);

    public float getDamage(){
        return baseDamage + MathUtils.random(0f, maxDamage - baseDamage); // gives float between 0 and n inclusive;
    }
}

