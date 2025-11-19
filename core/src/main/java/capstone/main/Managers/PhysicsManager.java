package capstone.main.Managers;

import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Sprites.Bullet;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class PhysicsManager {
    private final World world;

    public PhysicsManager() {
        world = new World(new Vector2(0, 0), true); // 0,0 gravity for top-down
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Object a = contact.getFixtureA().getUserData();
                Object b = contact.getFixtureB().getUserData();

                if (a instanceof Bullet && b instanceof AbstractEnemy) {
                    Bullet bullet = (Bullet) a;
                    AbstractEnemy enemy = (AbstractEnemy) b;

                    enemy.takeHit(bullet.getDamage());

                    // Apply knockback to enemy
                    Vector2 knockDir = bullet.getBody().getLinearVelocity().cpy().nor();
                    enemy.getBody().applyLinearImpulse(knockDir.scl(bullet.getKnockbackForce()),
                        enemy.getBody().getWorldCenter(), true);

                    bullet.setLifetime(0f); // mark bullet dead
                } else if (b instanceof Bullet && a instanceof AbstractEnemy) {
                    Bullet bullet = (Bullet) b;
                    AbstractEnemy enemy = (AbstractEnemy) a;

                    enemy.takeHit(bullet.getDamage());

                    // Apply knockback
                    Vector2 knockDir = bullet.getBody().getLinearVelocity().cpy().nor();
                    enemy.getBody().applyLinearImpulse(knockDir.scl(bullet.getKnockbackForce()),
                        enemy.getBody().getWorldCenter(), true);

                    bullet.setLifetime(0f);
                }
            }

            @Override
            public void endContact(Contact contact) {}
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });

    }

    public World getWorld() { return world; }

    public void step(float delta) {
        world.step(delta, 10, 5); // typical velocity/position iterations
    }

    public void dispose() { world.dispose(); }
}
