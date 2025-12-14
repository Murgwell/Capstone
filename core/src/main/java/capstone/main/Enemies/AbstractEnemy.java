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
import capstone.main.Pathfinding.NavMesh;
import capstone.main.Pathfinding.NavNode;
import capstone.main.Pathfinding.PathfindingCache;

import java.util.ArrayList;
import java.util.List;

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
    protected float defaultChaseDistance = 5f;
    protected float aggroChaseDistance = 10f;

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
    protected BitmapFont statusFont = new BitmapFont();
    ;
    protected String statusText = "";
    protected List<NavNode> currentPath = new ArrayList<NavNode>();
    protected int pathIndex = 0;

    protected NavMesh navMesh;
    private final Vector2 tmpVelocity = new Vector2();
    private final Vector2 tmpDirection = new Vector2();
    private final Vector2 tmpNextPos = new Vector2();
    private final Vector2 tmpPlayerPos = new Vector2();


    private float attackCooldown = 0f;
    private static final float ATTACK_COOLDOWN_TIME = 1.0f; // 1 second between attacks
    private static final float MELEE_RANGE = 0.8f; // How close to attack
    private static final float MELEE_DAMAGE = 5f; // 5 HP = half a heart

    private float pathUpdateTimer = 0f;
    private final float PATH_UPDATE_INTERVAL = 0.25f; // recalc path every 0.25s

    public AbstractEnemy(float x, float y, Texture texture, float width, float height, float maxHealth, ScreenShake screenShake, PhysicsManager physics, NavMesh navMesh) {
        this.sprite = new Sprite(texture);
        this.sprite.setPosition(x, y);
        this.sprite.setSize(width, height);
        this.baseSpeed = speed;

        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.screenShake = screenShake;
        this.navMesh = navMesh;

        this.hitboxRadius = Math.min(width, height) / 2f;

        this.healthBar = new HealthBar(sprite, maxHealth, width, 3f / 32f, 0.05f);

        this.directionManager = new DirectionManager(sprite);

        this.statusFont.getData().setScale(0.08f);
        this.statusFont.setColor(com.badlogic.gdx.graphics.Color.CYAN);

        // --- Box2D body ---
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(x + width / 2f, y + height / 2f);
        bd.fixedRotation = true;

        body = physics.getWorld().createBody(bd);

        CircleShape shape = new CircleShape();
        shape.setRadius(Math.min(width, height) / 4f);

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

    protected void pathfindingChaseBehavior(float delta, AbstractPlayer player) {
        if (isDead()) {
            body.setLinearVelocity(0, 0);
            return;
        }

        updateStatusEffects(delta);
        updateAttackCooldown(delta);

        // --- Get positions ---
        tmpPlayerPos.set(
            player.getSprite().getX() + player.getSprite().getWidth() / 2f,
            player.getSprite().getY() + player.getSprite().getHeight() / 2f
        );

        Vector2 enemyPos = body.getPosition();
        float distanceToPlayer = enemyPos.dst(tmpPlayerPos);

        // --- AGGRO LOGIC ---
        if (isAggro) {
            if (!enteredClose && distanceToPlayer <= aggroChaseDistance)
                enteredClose = true;

            if (enteredClose && distanceToPlayer > aggroChaseDistance) {
                isAggro = false;
                enteredClose = false;
                body.setLinearVelocity(0, 0);
                return;
            }
        } else {
            if (distanceToPlayer <= defaultChaseDistance) {
                // Enter aggro when player is within default chase distance
                isAggro = true;
                // Track if we've been inside the close radius at least once
                enteredClose = (distanceToPlayer <= aggroChaseDistance);
            } else {
                // Too far to aggro; stay idle
                body.setLinearVelocity(0, 0);
                return;
            }
        }

        // --- OPTIMIZED PATHFINDING ---
        if (navMesh == null) {
            Gdx.app.log("EnemyPF", getClass().getSimpleName() + ": navMesh is NULL");
        }
        pathUpdateTimer += delta;
        if (pathUpdateTimer >= PATH_UPDATE_INTERVAL) {
            pathUpdateTimer = 0f;

            NavNode startNode = getNearestNode(enemyPos);
            NavNode targetNode = getNearestNode(tmpPlayerPos);
            if (startNode == null || targetNode == null) {
                Gdx.app.log("EnemyPF", getClass().getSimpleName() + ": startNode=" + (startNode==null?"null":startNode.x+","+startNode.y) +
                        " targetNode=" + (targetNode==null?"null":targetNode.x+","+targetNode.y));
            }

            // Only recalculate if target changed significantly or path is empty
            boolean shouldRecalculate = false;

            if (currentPath.isEmpty()) {
                shouldRecalculate = true;
            } else if (targetNode != null) {
                NavNode lastNode = currentPath.get(currentPath.size() - 1);
                if (lastNode == null ||
                    Math.abs(targetNode.x - lastNode.x) > 1 ||
                    Math.abs(targetNode.y - lastNode.y) > 1) {
                    shouldRecalculate = true;
                }
            }

            if (shouldRecalculate && startNode != null && targetNode != null) {
                Gdx.app.log("EnemyPF", getClass().getSimpleName() + ": recalculating path. start=" + startNode.x+","+startNode.y + " target=" + targetNode.x+","+targetNode.y);
                // MEMORY FIX: Clear old path before getting new one
                currentPath.clear();

                // Use cached pathfinding to avoid recalculating same paths
                currentPath = PathfindingCache.getCachedPath(navMesh, startNode, targetNode);
                pathIndex = 0;
                Gdx.app.log("EnemyPF", getClass().getSimpleName() + ": path size=" + (currentPath==null?0:currentPath.size()));
            }
        }

        // --- MOVE ALONG PATH ---
        tmpVelocity.setZero(); // More efficient than set(0, 0)
        float nodeThreshold = Math.max(navMesh.getNodeSize() * 0.25f, hitboxRadius * 0.8f);

        if (!currentPath.isEmpty() && pathIndex < currentPath.size()) {
            NavNode nextNode = currentPath.get(pathIndex);

            tmpNextPos.set(nextNode.worldPos); // use precomputed world position
            tmpDirection.set(tmpNextPos).sub(enemyPos);

            if (tmpDirection.len2() < nodeThreshold * nodeThreshold) { // Use len2() for performance
                pathIndex++;
            } else {
                tmpVelocity.set(tmpDirection.nor().scl(speed));
                if (isSlowed) tmpVelocity.scl(slowMultiplier);

                // prevent overshoot
                float velocityLen = tmpVelocity.len();
                float directionLen = tmpDirection.len();
                if (velocityLen * delta > directionLen) {
                    tmpVelocity.set(tmpDirection.scl(1f / delta));
                }
                Gdx.app.log("EnemyPF", getClass().getSimpleName() + ": moving toward node velLen=" + tmpVelocity.len());
            }
        }


        // --- APPLY VELOCITY ---
        // Fallback: if no path movement, steer directly toward player to avoid navmesh gaps
if (tmpVelocity.isZero()) {
    tmpDirection.set(tmpPlayerPos).sub(enemyPos);
    if (tmpDirection.len2() > 1e-6f) {
        tmpVelocity.set(tmpDirection.nor().scl(speed));
        if (isSlowed) tmpVelocity.scl(slowMultiplier);
    }
}

body.setLinearVelocity(tmpVelocity);
body.setAwake(true);

        sprite.setPosition(
            body.getPosition().x - sprite.getWidth() / 2f,
            body.getPosition().y - sprite.getHeight() / 2f
        );

        directionManager.setFacingLeft(tmpVelocity.x < 0);

        if (healthBar != null)
            healthBar.update(delta);
    }


    private NavNode getNearestNode(Vector2 pos) {
        int x = (int) Math.floor(pos.x / navMesh.getNodeSize());
        int y = (int) Math.floor(pos.y / navMesh.getNodeSize());
        return navMesh.getNode(x, y);
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

        if (health <= 0 && !pendingRemoval) {
            pendingRemoval = true;
            System.out.println("ENEMY MARKED FOR DEATH - Health: " + health + " (Memory Debug)");
        }
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

    public boolean isDead() {
        return health <= 0;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public HealthBar getHealthBar() {
        return healthBar;
    }

    public boolean isPendingRemoval() {
        return pendingRemoval;
    }

    public Body getBody() {
        return body;
    }

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

    // MEMORY FIX: Add cleanup method to call when enemy is destroyed
    public void dispose() {
        // Clear pathfinding data to prevent memory leaks
        if (currentPath != null) {
            currentPath.clear();
            currentPath = null;
        }

        // Dispose of physics body
        if (body != null && body.getWorld() != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }

        // Clean up sprites (DO NOT dispose shared textures!)
        // Textures are shared between enemies and managed by LibGDX AssetManager
        sprite = null;
        whiteOverlaySprite = null;

        // Clean up other resources
        if (healthBar != null) {
            healthBar = null;
        }
        if (statusFont != null) {
            statusFont.dispose();
            statusFont = null;
        }
    }
}
