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

public class BarrageCombo extends Skill {
    private static final float MIN_DAMAGE = 25f; // Significantly increased (5 hits total)
    private static final float MAX_DAMAGE = 35f; // Significantly increased (5 hits total)
    private static final float RANGE = 2.5f;
    private static final int HIT_COUNT = 5; // Number of rapid hits
    private static final float HIT_INTERVAL = 0.1f; // Time between hits

    private AbstractPlayer player;
    private ArrayList<AbstractEnemy> enemies;
    private ArrayList<DamageNumber> damageNumbers;
    private BitmapFont damageFont;

    private int hitsRemaining;
    private float hitTimer;
    private AbstractEnemy targetEnemy;

    public BarrageCombo(AbstractPlayer player, ArrayList<AbstractEnemy> enemies,
                        ArrayList<DamageNumber> damageNumbers, BitmapFont damageFont) {
        super("Barrage Combo", 10f);
        this.player = player;
        this.enemies = enemies;
        this.damageNumbers = damageNumbers;
        this.damageFont = damageFont;
        this.hitsRemaining = 0;
        this.hitTimer = 0f;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isActive) {
            hitTimer -= delta;

            if (hitTimer <= 0 && hitsRemaining > 0) {
                performHit();
                hitsRemaining--;
                hitTimer = HIT_INTERVAL;

                if (hitsRemaining <= 0) {
                    deactivate();
                }
            }
        }
    }

    @Override
    public void activate() {
        if (!canUse()) return;

        Vector2 playerPos = player.getPosition();

        // Multi-target barrage: perform short cleaves hitting up to 3 per tick
        isActive = true;
        hitsRemaining = HIT_COUNT;
        hitTimer = 0f;
        startCooldown();
        SoundManager.getInstance().playSound("manny_skill2");
        com.badlogic.gdx.Gdx.app.log("BarrageCombo", "Started barrage!");
    }

    private void performHit() {
        // Short cleave around facing direction; hits up to 3 targets
        float angle = player.getWeaponAimingRad();
        com.badlogic.gdx.math.Vector2 dir = new com.badlogic.gdx.math.Vector2((float)Math.cos(angle), (float)Math.sin(angle));
        com.badlogic.gdx.math.Vector2 playerPos = player.getPosition();
        float length = RANGE;
        float halfWidth = 0.45f;
        int hitsNow = 0;
        for (AbstractEnemy enemy : enemies) {
            if (enemy.isDead()) continue;
            com.badlogic.gdx.math.Vector2 p = enemy.getBody().getPosition();
            com.badlogic.gdx.math.Vector2 toP = new com.badlogic.gdx.math.Vector2(p).sub(playerPos);
            float proj = toP.dot(dir);
            if (proj < 0f || proj > length) continue;
            float perp = Math.abs(toP.crs(dir)) / dir.len();
            if (perp > halfWidth) continue;
            float damage = MathUtils.random(MIN_DAMAGE, MAX_DAMAGE);
            enemy.takeHit(damage);
            damageNumbers.add(new DamageNumber(String.format("%.0f", damage), p.x + MathUtils.random(-0.2f, 0.2f), p.y + MathUtils.random(-0.2f, 0.2f), damageFont, com.badlogic.gdx.graphics.Color.ORANGE));
            hitsNow++;
            if (hitsNow >= 3) break;
        }
        if (hitsNow == 0) {
            // If nothing hit this tick, we can end the barrage early
            deactivate();
        }
    }

    @Override
    public void deactivate() {
        isActive = false;
        targetEnemy = null;
        hitsRemaining = 0;
    }
}
