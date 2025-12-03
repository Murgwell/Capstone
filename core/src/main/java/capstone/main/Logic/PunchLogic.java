
package capstone.main.Logic;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Characters.Melee;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Managers.ScreenShake;
import capstone.main.Managers.SoundManager;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class PunchLogic {
    // Use AbstractPlayer for sprite/position, and Melee for stats (range/damage).
    private final AbstractPlayer playerBase;
    private final Melee melee;

    private final ArrayList<AbstractEnemy> enemies;
    private final ArrayList<DamageNumber> damageNumbers;
    private final BitmapFont damageFont;
    private final ScreenShake screenShake;

    // Knockback tuning
    private static final float KNOCKBACK_FORCE = 5f;

    public PunchLogic(AbstractPlayer playerBase,
                      Melee melee,
                      ArrayList<AbstractEnemy> enemies,
                      ArrayList<DamageNumber> damageNumbers,
                      BitmapFont damageFont,
                      ScreenShake screenShake) {
        this.playerBase = playerBase;
        this.melee = melee;
        this.enemies = enemies;
        this.damageNumbers = damageNumbers;
        this.damageFont = damageFont;
        this.screenShake = screenShake;
    }

    public void update(float delta) {
        // Optional: punch state/animation bookkeeping
    }

    /**
     * Execute the punch — range check, damage, hit sound, shake, knockback.
     */
    public void performPunch(float weaponRotationRad) {
        // Compute player center from sprite (provided by AbstractPlayer)
        Vector2 playerCenter = new Vector2(
            playerBase.getSprite().getX() + playerBase.getSprite().getWidth() / 2f,
            playerBase.getSprite().getY() + playerBase.getSprite().getHeight() / 2f
        );

        float range = melee.getMeleeRange();
        Vector2 dir = new Vector2((float) Math.cos(weaponRotationRad), (float) Math.sin(weaponRotationRad));
        Vector2 punchCenter = playerCenter.cpy().add(dir.scl(range));

        // Check enemies in range and apply effects
        for (AbstractEnemy enemy : enemies) {
            if (enemy.isDead()) continue;

            Vector2 enemyPos = enemy.getBody().getPosition();
            if (punchCenter.dst(enemyPos) <= range) {
                float damage = melee.getMeleeDamage();

                // Apply damage on enemy
                enemy.takeHit(damage);

                // Play hit sound (only when a punch connects)
                SoundManager.getInstance().playSound("manny_punch");

                // Damage number
                damageNumbers.add(new DamageNumber(
                    String.format("%.0f", damage),
                    enemyPos.x,
                    enemyPos.y,
                    damageFont,
                    Color.WHITE
                ));

                // Impact feedback
                screenShake.shake(0.15f, 0.05f);

                // Box2D knockback
                if (enemy.getBody() != null) {
                    Vector2 knockback = new Vector2(dir).scl(KNOCKBACK_FORCE);
                    enemy.getBody().applyLinearImpulse(knockback, enemy.getBody().getWorldCenter(), true);
                }
                // Hit only one target per punch
                break;
            }
        }
    }
}
