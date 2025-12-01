package capstone.main.Characters;

import capstone.main.Managers.ScreenShake;
import capstone.main.Skills.*;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Skills.MannyPacquiao.BarrageCombo;
import capstone.main.Skills.MannyPacquiao.ChampionsKnockout;
import capstone.main.Skills.MannyPacquiao.MeteorFist;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class MannyPacquiao extends AbstractPlayer implements Melee {

    private static final int SPRITE_COLUMNS = 13;
    private static final int SPRITE_ROWS = 2;

    private final ScreenShake screenShake;
    private final ArrayList<AbstractEnemy> enemies;
    private final ArrayList<DamageNumber> damageNumbers;
    private final BitmapFont damageFont;

    // Melee stats
    private static final float MELEE_RANGE = 1.5f;
    private static final float BASE_MELEE_DAMAGE = 8f;
    private static final float MAX_MELEE_DAMAGE = 12f;

    // Skills
    private MeteorFist meteorFist;
    private BarrageCombo barrageCombo;
    private ChampionsKnockout championsKnockout;

    public MannyPacquiao(float healthPoints, float manaPoints, float baseDamage, float maxDamage,
                         float attackSpeed, float x, float y, float width, float height,
                         ArrayList<AbstractEnemy> enemies, ArrayList<DamageNumber> damageNumbers,
                         BitmapFont damageFont, float worldWidth, float worldHeight,
                         World physicsWorld, ScreenShake screenShake) {
        super(
            healthPoints,
            manaPoints,
            baseDamage,
            maxDamage,
            attackSpeed,
            "Manny Pacquiao",
            x,
            y,
            width,
            height,
            worldWidth,
            worldHeight,
            physicsWorld
        );

        this.screenShake = screenShake;
        this.enemies = enemies;
        this.damageNumbers = damageNumbers;
        this.damageFont = damageFont;
    }

    // Initialize skills
    public void initializeSkills(ArrayList<AbstractEnemy> enemies,
                                 ArrayList<DamageNumber> damageNumbers,
                                 BitmapFont damageFont) {
        this.meteorFist = new MeteorFist(this, enemies, damageNumbers, damageFont);
        this.barrageCombo = new BarrageCombo(this, enemies, damageNumbers, damageFont);
        this.championsKnockout = new ChampionsKnockout(this, enemies, damageNumbers, damageFont);
    }

    public void updateSkills(float delta) {
        if (meteorFist != null) meteorFist.update(delta);
        if (barrageCombo != null) barrageCombo.update(delta);
        if (championsKnockout != null) championsKnockout.update(delta);
    }

    // Skill activation methods
    public void useMeteorFist() {
        if (meteorFist != null) {
            meteorFist.activate();
        }
    }

    public void useBarrageCombo() {
        if (barrageCombo != null) {
            barrageCombo.activate();
        }
    }

    public void useChampionsKnockout() {
        if (championsKnockout != null) {
            championsKnockout.activate();
        }
    }

    // Getters for UI display
    public Skill getMeteorFist() { return meteorFist; }
    public Skill getBarrageCombo() { return barrageCombo; }
    public Skill getChampionsKnockout() { return championsKnockout; }

    @Override
    public void performAttack(float delta, float weaponRotationRad) {
        if (!canAttack()) {
            return;
        }

        performMeleeAttack(delta, weaponRotationRad);
        onAttackPerformed();
    }

    @Override
    public void performMeleeAttack(float delta, float weaponRotationRad) {
        // Calculate attack direction based on weapon rotation
        float attackX = (float) Math.cos(weaponRotationRad) * MELEE_RANGE;
        float attackY = (float) Math.sin(weaponRotationRad) * MELEE_RANGE;

        Vector2 playerPos = getPosition();
        Vector2 attackPos = new Vector2(playerPos.x + attackX, playerPos.y + attackY);

        // Find enemies in melee range
        for (AbstractEnemy enemy : enemies) {
            if (enemy.isDead()) continue;

            Vector2 enemyPos = enemy.getBody().getPosition();
            float distance = attackPos.dst(enemyPos);

            // If enemy is within melee range
            if (distance <= MELEE_RANGE) {
                float damage = com.badlogic.gdx.math.MathUtils.random(BASE_MELEE_DAMAGE, MAX_MELEE_DAMAGE);
                enemy.takeHit(damage);

                // Create damage number
                damageNumbers.add(new DamageNumber(
                    String.format("%.0f", damage),
                    enemyPos.x,
                    enemyPos.y,
                    damageFont,
                    Color.WHITE
                ));

                // Small screen shake on hit
                screenShake.shake(0.15f, 0.05f);

                Gdx.app.log("MeleeAttack", "Manny punched enemy for " + damage + " damage!");

                // Only hit one enemy per attack
                break;
            }
        }
    }

    @Override
    public float getMeleeRange() {
        return MELEE_RANGE;
    }

    @Override
    public float getMeleeDamage() {
        return com.badlogic.gdx.math.MathUtils.random(BASE_MELEE_DAMAGE, MAX_MELEE_DAMAGE);
    }

    @Override
    protected void onDamaged(float delta) {
        super.onDamaged(delta);
        screenShake.shake(0.20f, 0.2f);
    }
}
