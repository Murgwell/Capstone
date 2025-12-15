
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

    public void update(float delta, float weaponRotationRad) {
        // Optional: punch state/animation bookkeeping
    }

    /**
     * Execute the punch — range check, damage, hit sound, shake, knockback.
     */
    public void performPunch(float weaponRotationRad) {
        final java.util.List<com.badlogic.gdx.math.Rectangle> walls = capstone.main.Managers.WallRegistry.getWalls();
        // Compute player center from sprite (provided by AbstractPlayer)
        Vector2 playerCenter = new Vector2(
            playerBase.getSprite().getX() + playerBase.getSprite().getWidth() / 2f,
            playerBase.getSprite().getY() + playerBase.getSprite().getHeight() / 2f
        );

        // Cleave: short line in facing direction; hit up to 3 targets
        float length = melee.getMeleeRange() + 1.2f; // extend slightly beyond melee range
        float halfWidth = 1.2f; // cleave strip half-width (increased from 0.4f for easier hits)
        Vector2 dir = new Vector2((float) Math.cos(weaponRotationRad), (float) Math.sin(weaponRotationRad));

        int hits = 0;
        for (AbstractEnemy enemy : enemies) {
            if (enemy.isDead()) continue;
            Vector2 p = enemy.getBody().getPosition();
            // Vector from player to enemy
            Vector2 toP = new Vector2(p).sub(playerCenter);
            float proj = toP.dot(dir); // along the cleave
            if (proj < 0f || proj > length) continue; // outside segment
            // perpendicular distance to cleave centerline
            float perp = Math.abs(toP.crs(dir)) / dir.len();
            if (perp > halfWidth) continue;

            // Blocked by wall? Skip if any wall intersects line of sight
            if (segmentHitsAnyWall(playerCenter, p, walls)) continue;

            float damage = melee.getMeleeDamage();
            enemy.takeHit(damage);
            SoundManager.getInstance().playSound("manny_punch");
            damageNumbers.add(new DamageNumber(
                String.format("%.0f", damage),
                p.x,
                p.y,
                damageFont,
                Color.WHITE
            ));
            screenShake.shake(0.15f, 0.05f);
            if (enemy.getBody() != null) {
                Vector2 knockback = new Vector2(dir).scl(KNOCKBACK_FORCE);
                enemy.getBody().applyLinearImpulse(knockback, enemy.getBody().getWorldCenter(), true);
            }
            hits++;
            if (hits >= 3) break; // cap at 3 targets
        }

       }

private boolean segmentHitsAnyWall(Vector2 a, Vector2 b, java.util.List<com.badlogic.gdx.math.Rectangle> walls) {
           if (walls == null || walls.isEmpty()) return false;
           float minX = Math.min(a.x, b.x), minY = Math.min(a.y, b.y);
           float maxX = Math.max(a.x, b.x), maxY = Math.max(a.y, b.y);
           for (com.badlogic.gdx.math.Rectangle r : walls) {
               if (r.x > maxX || r.x + r.width < minX || r.y > maxY || r.y + r.height < minY) continue;
               if (com.badlogic.gdx.math.Intersector.intersectSegmentRectangle(a, b, r)) return true;
           }
           return false;
    }
}
