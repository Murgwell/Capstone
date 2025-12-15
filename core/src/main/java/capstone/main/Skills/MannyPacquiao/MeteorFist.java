package capstone.main.Skills.MannyPacquiao;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Managers.SoundManager;
import capstone.main.Skills.Skill;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class MeteorFist extends Skill {
    private static final float MIN_DAMAGE = 10f;
    private static final float MAX_DAMAGE = 13f;
    private static final float RANGE = 3f; // Attack range

    private final AbstractPlayer player;
    private final ArrayList<AbstractEnemy> enemies;
    private final ArrayList<DamageNumber> damageNumbers;
    private final BitmapFont damageFont;

    public MeteorFist(AbstractPlayer player, ArrayList<AbstractEnemy> enemies,
                      ArrayList<DamageNumber> damageNumbers, BitmapFont damageFont) {
        super("Meteor Fist", 5f);
        this.player = player;
        this.enemies = enemies;
        this.damageNumbers = damageNumbers;
        this.damageFont = damageFont;
    }

    @Override
    public void activate() {
        if (!canUse()) return;

        Vector2 playerPos = player.getPosition();

        // Multi-hit: line cleave forward, up to 3 targets
        // Build forward direction from player's current facing using getWeaponAimingRad if available
        float angle = player.getWeaponAimingRad();
        com.badlogic.gdx.math.Vector2 dir = new com.badlogic.gdx.math.Vector2((float)Math.cos(angle), (float)Math.sin(angle));
        float length = RANGE;
        float halfWidth = 0.5f;
        int hits = 0;
        float totalDamage = 0f;
        java.util.List<com.badlogic.gdx.math.Rectangle> walls = capstone.main.Managers.WallRegistry.getWalls();
        for (AbstractEnemy enemy : enemies) {
            if (enemy.isDead()) continue;
            com.badlogic.gdx.math.Vector2 p = enemy.getBody().getPosition();
            com.badlogic.gdx.math.Vector2 toP = new com.badlogic.gdx.math.Vector2(p).sub(playerPos);
            float proj = toP.dot(dir);
            if (proj < 0f || proj > length) continue;
            float perp = Math.abs(toP.crs(dir)) / dir.len();
            if (perp > halfWidth) continue;
            // Blocked by wall? Skip target if any wall intersects segment
            boolean blocked = false;
            if (walls != null) {
                for (com.badlogic.gdx.math.Rectangle r : walls) {
                    if (com.badlogic.gdx.math.Intersector.intersectSegmentRectangle(playerPos, p, r)) { blocked = true; break; }
                }
            }
            if (blocked) continue;
            // Calculate unique damage per enemy
            float damage = MathUtils.random(MIN_DAMAGE, MAX_DAMAGE);
            enemy.takeHit(damage);
            damageNumbers.add(new DamageNumber(String.format("%.0f", damage), p.x, p.y, damageFont, com.badlogic.gdx.graphics.Color.YELLOW));
            totalDamage += damage;
            hits++;
            if (hits >= 3) break;
        }
        
        // Always consume cooldown when activated
        startCooldown();
        
        if (hits > 0) {
            SoundManager.getInstance().playSound("manny_skill1");
            com.badlogic.gdx.Gdx.app.log("MeteorFist", "Hit " + hits + " enemies for total ~" + String.format("%.1f", totalDamage) + " damage!");
        }
    }

    @Override
    public void deactivate() {
        // Instant skill, no deactivation needed
    }
}
