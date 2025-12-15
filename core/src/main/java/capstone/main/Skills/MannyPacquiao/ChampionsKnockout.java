package capstone.main.Skills.MannyPacquiao;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Managers.SoundManager;
import capstone.main.Skills.Skill;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class ChampionsKnockout extends Skill {
    private static final float BASE_DAMAGE_MIN = 20f;
    private static final float BASE_DAMAGE_MAX = 40f;
    private static final float HP_PERCENT = 0.20f; // 20% of enemy's base HP
    private static final float RANGE = 3f;
    private static final int MAX_TARGETS = 5; // Maximum enemies hit
    private static final float SLOW_DURATION = 3f; // 3 seconds of slow
    private static final float SLOW_AMOUNT = 0.5f; // 50% speed reduction

    private final AbstractPlayer player;
    private final ArrayList<AbstractEnemy> enemies;
    private final ArrayList<DamageNumber> damageNumbers;
    private final BitmapFont damageFont;

    public ChampionsKnockout(AbstractPlayer player, ArrayList<AbstractEnemy> enemies,
                             ArrayList<DamageNumber> damageNumbers, BitmapFont damageFont) {
        super("Champion's Knockout", 15f);
        this.player = player;
        this.enemies = enemies;
        this.damageNumbers = damageNumbers;
        this.damageFont = damageFont;
    }

    @Override
    public void activate() {
        if (!canUse()) return;

        Vector2 playerPos = player.getPosition();

        // Multi-target ult: cleave line in front, apply damage to multiple, slow strongest (closest) target
        float angle = player.getWeaponAimingRad();
        com.badlogic.gdx.math.Vector2 dir = new com.badlogic.gdx.math.Vector2((float)Math.cos(angle), (float)Math.sin(angle));
        float length = RANGE;
        float halfWidth = 0.6f;
        int hits = 0;
        AbstractEnemy primary = null;
        float bestProj = -1f;
        java.util.List<com.badlogic.gdx.math.Rectangle> walls = capstone.main.Managers.WallRegistry.getWalls();
        
        for (AbstractEnemy enemy : enemies) {
            if (enemy.isDead()) continue;
            Vector2 p = enemy.getBody().getPosition();
            Vector2 toP = new Vector2(p).sub(playerPos);
            float proj = toP.dot(dir);
            if (proj < 0f || proj > length) continue;
            float perp = Math.abs(toP.crs(dir)) / dir.len();
            if (perp > halfWidth) continue;
            
            // Blocked by wall? Skip target if any wall intersects segment
            boolean blocked = false;
            if (walls != null) {
                for (com.badlogic.gdx.math.Rectangle r : walls) {
                    if (com.badlogic.gdx.math.Intersector.intersectSegmentRectangle(playerPos, p, r)) { 
                        blocked = true; 
                        break; 
                    }
                }
            }
            if (blocked) continue;
            
            float enemyMaxHP = enemy.getMaxHealth();
            float percentDamage = enemyMaxHP * HP_PERCENT;
            float baseDamage = MathUtils.random(BASE_DAMAGE_MIN, BASE_DAMAGE_MAX);
            float totalDamage = percentDamage + baseDamage;
            enemy.takeHit(totalDamage);
            damageNumbers.add(new DamageNumber(String.format("%.0f!", totalDamage), p.x, p.y, damageFont, Color.RED));
            if (proj > bestProj) { bestProj = proj; primary = enemy; }
            hits++;
            
            // Limit number of targets hit
            if (hits >= MAX_TARGETS) break;
        }
        
        // Always consume cooldown when activated
        startCooldown();
        
        if (hits > 0) {
            if (primary != null) {
                primary.applySlowEffect(SLOW_DURATION, SLOW_AMOUNT);
                Vector2 pos = primary.getBody().getPosition();
                damageNumbers.add(new DamageNumber("SLOWED!", pos.x, pos.y + 0.3f, damageFont, Color.CYAN));
            }
            SoundManager.getInstance().playSound("manny_ult");
            com.badlogic.gdx.Gdx.app.log("ChampionsKnockout", "KNOCKOUT! Hit " + hits + " enemies");
        }
    }

    @Override
    public void deactivate() {
        // Instant skill, no deactivation needed
    }
}
