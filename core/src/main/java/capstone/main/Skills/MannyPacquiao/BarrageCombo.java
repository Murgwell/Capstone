package capstone.main.Skills.MannyPacquiao;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Skills.Skill;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class BarrageCombo extends Skill {
    private static final float MIN_DAMAGE = 5f;
    private static final float MAX_DAMAGE = 9f;
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

        // Find closest enemy in range
        AbstractEnemy closestEnemy = null;
        float closestDist = Float.MAX_VALUE;

        for (AbstractEnemy enemy : enemies) {
            if (enemy.isDead()) continue;

            Vector2 enemyPos = enemy.getBody().getPosition();
            float dist = playerPos.dst(enemyPos);

            if (dist <= RANGE && dist < closestDist) {
                closestDist = dist;
                closestEnemy = enemy;
            }
        }

        if (closestEnemy != null) {
            targetEnemy = closestEnemy;
            isActive = true;
            hitsRemaining = HIT_COUNT;
            hitTimer = 0f;
            startCooldown();

            com.badlogic.gdx.Gdx.app.log("BarrageCombo", "Started barrage on enemy!");
        } else {
            com.badlogic.gdx.Gdx.app.log("BarrageCombo", "No enemy in range!");
        }
    }

    private void performHit() {
        if (targetEnemy == null || targetEnemy.isDead()) {
            deactivate();
            return;
        }

        float damage = MathUtils.random(MIN_DAMAGE, MAX_DAMAGE);
        targetEnemy.takeHit(damage);

        // Create damage number
        Vector2 enemyPos = targetEnemy.getBody().getPosition();
        damageNumbers.add(new DamageNumber(
            String.format("%.0f", damage),
            enemyPos.x + MathUtils.random(-0.2f, 0.2f), // Slight offset for visual variety
            enemyPos.y + MathUtils.random(-0.2f, 0.2f),
            damageFont,
            com.badlogic.gdx.graphics.Color.ORANGE
        ));
    }

    @Override
    public void deactivate() {
        isActive = false;
        targetEnemy = null;
        hitsRemaining = 0;
    }
}
