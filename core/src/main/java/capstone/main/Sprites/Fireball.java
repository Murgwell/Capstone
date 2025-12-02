package capstone.main.Sprites;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.CollisionBits;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Fireball {

    private static final Texture FIREBALL_TEXTURE =
        new Texture("Textures/UI/Fireball/Fireball.png");

    private final AbstractPlayer owner;

    public Sprite sprite;
    public Body body;

    private float damage;
    private float lifetime = 3f;

    private float speed = 50f;         // slower & heavier than bullets
    private float knockbackForce = 15f; // stronger knockback


    private final float baseWidth = 2f;
    private final float baseHeight = 2f;
    private final float maxStretch = 0.5f;
    private final float stretchDistance = 0.25f;

    private float distanceTraveled = 0f;
    private Vector2 lastPosition;

    public Fireball(float x, float y, Vector2 direction, AbstractPlayer owner,
                    float damage, World world) {

        this.owner = owner;
        this.damage = damage;

        // ========== SPRITE ==========
        sprite = new Sprite(FIREBALL_TEXTURE);
        sprite.setSize(baseWidth, baseHeight);
        sprite.setOrigin(baseWidth / 2f, baseHeight / 2f);
        sprite.setPosition(x - baseWidth / 2f, y - baseHeight / 2f);

        lastPosition = new Vector2(x, y);

        // ========== BODY ==========
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(x, y);
        bd.bullet = true;
        bd.fixedRotation = false;

        body = world.createBody(bd);

        CircleShape shape = new CircleShape();
        shape.setRadius(baseWidth / 2f);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        fd.isSensor = false;
        fd.density = 1f;
        fd.restitution = 0f;

        fd.filter.categoryBits = CollisionBits.BULLET;
        fd.filter.maskBits = CollisionBits.ENEMY | CollisionBits.WALL;

        body.createFixture(fd).setUserData(this);
        shape.dispose();

        body.setLinearVelocity(direction.nor().scl(speed));
    }

    public void update(float delta) {
        lifetime -= delta;

        // stretch calculation (same as Bullet)
        Vector2 currentPos = body.getPosition();
        float frameDist = currentPos.cpy().sub(lastPosition).len();
        distanceTraveled += frameDist;
        lastPosition.set(currentPos);

        float t = Math.min(distanceTraveled / stretchDistance, 1f);
        float newHeight = baseHeight * (1 + t * (maxStretch - 1));
        sprite.setSize(baseWidth, newHeight);

        sprite.setPosition(
            body.getPosition().x - sprite.getWidth() / 2f,
            body.getPosition().y - sprite.getHeight() / 2f
        );

        // rotate fireball to its velocity
        Vector2 vel = body.getLinearVelocity();
        float angleDeg = (float) Math.toDegrees(Math.atan2(vel.y, vel.x));
        sprite.setRotation(angleDeg);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public float getDamage() {
        return owner.getDamage();
    }

    public float getLifetime() {
        return lifetime;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public float getKnockbackForce() {
        return knockbackForce;
    }

    public Body getBody() {
        return body;
    }
}
