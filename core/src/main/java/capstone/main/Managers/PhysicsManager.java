package capstone.main.Managers;

import capstone.main.Characters.VicoSotto;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Sprites.Bullet;
import capstone.main.Sprites.Fireball;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import java.util.ArrayList;

public class PhysicsManager {
   private final World world;
   private ArrayList<DamageNumber> damageNumbers;
   private BitmapFont damageFont;
   // Track enemies for AOE processing
   private ArrayList<AbstractEnemy> enemies;
   // Configurable AOE radius for Fireball explosions
   private float fireballAoeRadius = 2.75f;

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
     * Handles collision between a projectile (Bullet/Fireball) and an enemy or wall.
     */
    private void handleCollision(Object projObj, Object targetObj) {
        if (targetObj instanceof AbstractEnemy) {
            AbstractEnemy enemy = (AbstractEnemy) targetObj;

            if (projObj instanceof Bullet) {
                Bullet bullet = (Bullet) projObj;
                // Vico Sotto bullets: 50% chance to penetrate enemies
                if (bullet.getOwner() instanceof VicoSotto) {
                    if (!bullet.hasHit(enemy)) {
                        applyDamageAndKnockback(enemy, bullet.getDamage(), bullet.getBody(), bullet.getKnockbackForce());
                        bullet.markHit(enemy);
                        // Roll 50% chance to continue ONLY on first time hitting this enemy
                        if (MathUtils.randomBoolean(0.5f)) {
                            // penetrate: keep bullet alive
                        } else {
                            bullet.setLifetime(0f); // bullet stops here
                        }
                    }
                    // If already hit this enemy, ignore further contacts without stopping the bullet
                } else {
                    // Default: remove bullet on hit
                    applyDamageAndKnockback(enemy, bullet.getDamage(), bullet.getBody(), bullet.getKnockbackForce());
                    bullet.setLifetime(0f); // remove bullet
                }
            } else if (projObj instanceof Fireball) {
                Fireball fireball = (Fireball) projObj;
                // Always ensure the directly hit enemy takes damage
                applyDamageAndKnockback(enemy, fireball.getDamage(), fireball.getBody(), fireball.getKnockbackForce());
                // On fireball impact with an enemy, also apply AOE damage to nearby enemies
                Vector2 center = fireball.getBody().getPosition().cpy();
                float radius = fireballAoeRadius; // AOE radius (configurable)
                applyAOEDamage(center, radius, fireball.getDamage(), fireball.getKnockbackForce());
                fireball.setLifetime(0f); // remove fireball
            }
        } else if (targetObj instanceof String && "solid".equals(targetObj)) {
            // Projectile hit a wall
            if (projObj instanceof Bullet) {
                Bullet bullet = (Bullet) projObj;
                bullet.setLifetime(0f); // remove bullet
            } else if (projObj instanceof Fireball) {
                Fireball fireball = (Fireball) projObj;
                // Also explode on walls
                Vector2 center = fireball.getBody().getPosition().cpy();
                float radius = fireballAoeRadius; // AOE radius (configurable)
                applyAOEDamage(center, radius, fireball.getDamage(), fireball.getKnockbackForce());
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

        // Create damage number if damage number system is available
        if (damageNumbers != null && damageFont != null) {
            Vector2 enemyPos = enemy.getBody().getPosition();
            damageNumbers.add(new DamageNumber(
                String.format("%.0f", damage),
                enemyPos.x,
                enemyPos.y,
                damageFont,
                Color.WHITE
            ));
        }
    }

    /**
     * Set the damage number system for displaying projectile damage
     */
    public void setDamageNumberSystem(ArrayList<DamageNumber> damageNumbers, BitmapFont damageFont) {
        this.damageNumbers = damageNumbers;
        this.damageFont = damageFont;
    }

    public World getWorld() {
        return world;
    }

    /**
     * Provide the current enemy list for AOE processing
     */
    public void setEnemies(ArrayList<AbstractEnemy> enemies) {
        this.enemies = enemies;
    }

    public void setFireballAoeRadius(float radius) {
        this.fireballAoeRadius = Math.max(0f, radius);
    }

    /**
     * Apply radial AOE damage centered at position
     */
    private void applyAOEDamage(Vector2 center, float radius, float damage, float knockback) {
        if (enemies == null) return;
        float r2 = radius * radius;
        for (int i = 0; i < enemies.size(); i++) {
            AbstractEnemy e = enemies.get(i);
            if (e == null || e.isDead()) continue;
            Vector2 epos = e.getBody().getPosition();
            float dx = epos.x - center.x;
            float dy = epos.y - center.y;
            float d2 = dx*dx + dy*dy;
            if (d2 <= r2) {
                // Deal damage
                e.takeHit(damage);
                // Radial knockback away from center
                Vector2 dir = new Vector2(dx, dy);
                if (dir.len2() > 1e-6f) dir.nor();
                e.getBody().applyLinearImpulse(dir.scl(knockback), e.getBody().getWorldCenter(), true);

                // Damage number
                if (damageNumbers != null && damageFont != null) {
                    damageNumbers.add(new DamageNumber(
                        String.format("%.0f", damage),
                        epos.x,
                        epos.y,
                        damageFont,
                        Color.WHITE
                    ));
                }
            }
        }
    }

    public void step(float delta) {
        world.step(delta, 10, 5); // typical velocity/position iterations
    }

    public void dispose() {
        world.dispose();
    }
}
