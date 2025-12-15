package capstone.main.Sprites;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.CollisionBits;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import java.util.HashSet;

public class Bullet {
    private static final Texture DEFAULT_TEXTURE = new Texture("Textures/UI/Bullet Indicators/Pistol-Bullet.png");
    private final AbstractPlayer owner;
    public Sprite sprite;
    public Body body;
    private float lifetime = 1f;
    private final float speed = 75f; // faster speed
    private final float knockbackForce = 10f;

    // Penetration support: track enemies already hit (to avoid repeated damage)
    private final HashSet<AbstractEnemy> hitEnemies = new HashSet<>();

    private final float baseWidth = 0.07f;
    private final float baseHeight = 0.5f;
    private final float maxStretch = 1f;
    private final float stretchDistance = 0.25f;
    private float distanceTraveled = 0f;
    private Vector2 lastPosition;

    public Bullet(float x, float y, Vector2 direction, AbstractPlayer owner, World world) {
        this.owner = owner;

        // --- Sprite ---
        sprite = new Sprite(DEFAULT_TEXTURE);
        sprite.setSize(baseWidth, baseHeight);
        sprite.setOrigin(baseWidth / 2f, 0);
        sprite.setPosition(x - baseWidth / 2f, y);

        lastPosition = new Vector2(x, y);

        // --- Box2D body ---
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(x, y);
        bd.bullet = true; // CCD enabled
        bd.fixedRotation = true;

        body = world.createBody(bd);

        PolygonShape shape = new PolygonShape();
        // Set the fixture to start at the bottom of the bullet
        shape.setAsBox(baseWidth / 2f, baseHeight / 2f, new Vector2(0, baseHeight / 2f), 0f);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        // Use sensor so bullets can pass through bodies (we manually handle damage/knockback)
        fd.isSensor = true;
        fd.density = 1f;
        fd.restitution = 0f;

        // Collision filtering: ignore owner
        fd.filter.categoryBits = CollisionBits.BULLET;
        fd.filter.maskBits = CollisionBits.ENEMY | CollisionBits.WALL;

        body.createFixture(fd).setUserData(this);
        shape.dispose();

        body.setLinearVelocity(direction.nor().scl(speed));

        // Rotate sprite to face travel direction
        float angleDeg = (float) Math.toDegrees(Math.atan2(direction.y, direction.x));
        sprite.setRotation(angleDeg - 90);
    }

    public void update(float delta) {
        lifetime -= delta;

        // Calculate distance traveled this frame
        Vector2 currentPos = body.getPosition();
        float frameDistance = currentPos.cpy().sub(lastPosition).len();
        distanceTraveled += frameDistance;
        lastPosition.set(currentPos);

        // Stretch sprite based on distance
        float t = Math.min(distanceTraveled / stretchDistance, 1f);
        float newHeight = baseHeight * (1 + t * (maxStretch - 1));
        sprite.setSize(baseWidth, newHeight);

        // Sync sprite position with body (origin remains at bottom center)
        sprite.setPosition(
            body.getPosition().x - baseWidth / 2f,
            body.getPosition().y // keep origin at bottom
        );

        // Optional: rotate sprite to match velocity direction
        Vector2 vel = body.getLinearVelocity();
        float angleDeg = (float) Math.toDegrees(Math.atan2(vel.y, vel.x));
        sprite.setRotation(angleDeg - 90);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public float getDamage() {
        return owner.getDamage();
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public float getLifetime() {
        return lifetime;
    }

    public Body getBody() {
        return body;
    }

    public float getKnockbackForce() {
        return knockbackForce;
    }

    public AbstractPlayer getOwner() {
        return owner;
    }

    public boolean hasHit(AbstractEnemy enemy) {
        return hitEnemies.contains(enemy);
    }

    public void markHit(AbstractEnemy enemy) {
        hitEnemies.add(enemy);
    }
}
