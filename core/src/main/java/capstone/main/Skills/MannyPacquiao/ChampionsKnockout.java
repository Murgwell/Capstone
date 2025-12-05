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

        // Find the closest enemy in range
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
            // Calculate damage: 20% of enemy max HP + base damage
            float enemyMaxHP = closestEnemy.getMaxHealth();
            float percentDamage = enemyMaxHP * HP_PERCENT;
            float baseDamage = MathUtils.random(BASE_DAMAGE_MIN, BASE_DAMAGE_MAX);
            float totalDamage = percentDamage + baseDamage;

            closestEnemy.takeHit(totalDamage);

            // APPLY SLOW EFFECT
            closestEnemy.applySlowEffect(SLOW_DURATION, SLOW_AMOUNT);

            Vector2 enemyPos = closestEnemy.getBody().getPosition();
            damageNumbers.add(new DamageNumber(
                String.format("%.0f!", totalDamage),
                enemyPos.x,
                enemyPos.y,
                damageFont,
                Color.RED
            ));

            // Create "SLOWED" status text
            damageNumbers.add(new DamageNumber(
                "SLOWED!",
                enemyPos.x,
                enemyPos.y + 0.3f, // Slightly higher than damage number
                damageFont,
                Color.CYAN
            ));
            startCooldown();
            SoundManager.getInstance().playSound("manny_ult");
            com.badlogic.gdx.Gdx.app.log("ChampionsKnockout",
                "KNOCKOUT! Dealt " + totalDamage + " damage! (" + percentDamage + " from HP + " + baseDamage + " base)");
        } else {
            com.badlogic.gdx.Gdx.app.log("ChampionsKnockout", "No enemy in range!");
        }
    }

    @Override
    public void deactivate() {
        // Instant skill, no deactivation needed
    }
}
