package capstone.main.Managers;

import capstone.main.Characters.Quiboloy;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Sprites.Bullet;
import capstone.main.Sprites.Fireball;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class PhysicsManager {
    private final World world;

    public PhysicsManager() {
        world = new World(new Vector2(0, 0), true); // 0,0 gravity for top-down

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                handleCollision(contact.getFixtureA().getUserData(),
                    contact.getFixtureB().getUserData());
                handleCollision(contact.getFixtureB().getUserData(),
                    contact.getFixtureA().getUserData());
            }

            @Override
            public void endContact(Contact contact) {}
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    /**
     * Handles collision between a projectile (Bullet/Fireball) and an enemy.
     */
    private void handleCollision(Object projObj, Object targetObj) {
        if (targetObj instanceof AbstractEnemy) {
            AbstractEnemy enemy = (AbstractEnemy) targetObj;

            if (projObj instanceof Bullet) {
                Bullet bullet = (Bullet) projObj;
                applyDamageAndKnockback(enemy, bullet.getDamage(), bullet.getBody(), bullet.getKnockbackForce());
                bullet.setLifetime(0f); // remove bullet
            } else if (projObj instanceof Fireball) {
                Fireball fireball = (Fireball) projObj;
                applyDamageAndKnockback(enemy, fireball.getDamage(), fireball.getBody(), fireball.getKnockbackForce());
                fireball.setLifetime(0f); // remove fireball
            }
        }
    }

    /**
     * Apply damage and knockback to an enemy from a projectile body
     */
    private void applyDamageAndKnockback(AbstractEnemy enemy, float damage, Body projectileBody, float knockback) {
        enemy.takeHit(damage);
        Vector2 knockDir = projectileBody.getLinearVelocity().cpy().nor();
        enemy.getBody().applyLinearImpulse(knockDir.scl(knockback), enemy.getBody().getWorldCenter(), true);
    }

    public World getWorld() {
        return world;
    }

    public void step(float delta) {
        world.step(delta, 10, 5); // typical velocity/position iterations
    }

    public void dispose() {
        world.dispose();
    }
}
