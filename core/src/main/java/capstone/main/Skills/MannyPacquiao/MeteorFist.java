package capstone.main.Skills.MannyPacquiao;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Enemies.AbstractEnemy;
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

    private AbstractPlayer player;
    private ArrayList<AbstractEnemy> enemies;
    private ArrayList<DamageNumber> damageNumbers;
    private BitmapFont damageFont;

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

        float damage = MathUtils.random(MIN_DAMAGE, MAX_DAMAGE);
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
            closestEnemy.takeHit(damage);

            // Create damage number
            Vector2 enemyPos = closestEnemy.getBody().getPosition();
            damageNumbers.add(new DamageNumber(
                String.format("%.0f", damage),
                enemyPos.x,
                enemyPos.y,
                damageFont,
                com.badlogic.gdx.graphics.Color.YELLOW // Special color for skill damage
            ));

            com.badlogic.gdx.Gdx.app.log("MeteorFist", "Hit enemy for " + damage + " damage!");
        } else {
            com.badlogic.gdx.Gdx.app.log("MeteorFist", "No enemy in range!");
        }

        startCooldown();
    }

    @Override
    public void deactivate() {
        // Instant skill, no deactivation needed
    }
}
