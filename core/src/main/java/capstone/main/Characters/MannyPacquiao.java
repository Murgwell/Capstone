
package capstone.main.Characters;

import capstone.main.Managers.ScreenShake;
import capstone.main.Managers.SoundManager;
import capstone.main.Skills.*;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Skills.MannyPacquiao.BarrageCombo;
import capstone.main.Skills.MannyPacquiao.ChampionsKnockout;
import capstone.main.Skills.MannyPacquiao.MeteorFist;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class MannyPacquiao extends AbstractPlayer implements Melee {
    private final ScreenShake screenShake;
    private final ArrayList<AbstractEnemy> enemies;
    private final ArrayList<DamageNumber> damageNumbers;
    private final BitmapFont damageFont;

    // Punch animation fields
    private float punchAnimationTimer = 0f;
    private final float punchAnimationDuration = 0.2f;
    private boolean isPunching = false;
    private Vector2 originalWeaponOffset = new Vector2(0, 0);
    private Vector2 punchWeaponOffset = new Vector2(0, 0);

    // Skills
    private MeteorFist meteorFist;
    private BarrageCombo barrageCombo;
    private ChampionsKnockout championsKnockout;

    // External melee logic (new)
    private capstone.main.Logic.PunchLogic punchLogic;

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
            x, y, width, height,
            worldWidth, worldHeight,
            physicsWorld
        );
        this.screenShake = screenShake;
        this.enemies = enemies;
        this.damageNumbers = damageNumbers;
        this.damageFont = damageFont;

        // Initialize external PunchLogic with Manny as both AbstractPlayer and Melee
        this.punchLogic = new capstone.main.Logic.PunchLogic(this, this, enemies, damageNumbers, damageFont, screenShake);
    }

    // Skills setup (unchanged from your design)
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

    public void useMeteorFist() {
        if (meteorFist != null) meteorFist.activate();
    }

    public void useBarrageCombo() {
        if (barrageCombo != null) barrageCombo.activate();
    }

    public void useChampionsKnockout() {
        if (championsKnockout != null) championsKnockout.activate();
    }

    public Skill getMeteorFist() {
        return meteorFist;
    }

    public Skill getBarrageCombo() {
        return barrageCombo;
    }

    public Skill getChampionsKnockout() {
        return championsKnockout;
    }

    @Override
    public void performAttack(float delta, float weaponRotationRad) {
        if (!canAttack()) return;

        onAttackPerformed();

        // Start punch animation
        isPunching = true;
        punchAnimationTimer = 0f;

        // Calculate punch direction offset
        float punchDistance = getMeleeRange();
        punchWeaponOffset.set(
            (float)Math.cos(weaponRotationRad) * punchDistance,
            (float)Math.sin(weaponRotationRad) * punchDistance
        );

        // Delegate actual damage to PunchLogic
        punchLogic.performPunch(weaponRotationRad);
    }

    public void updatePunchAnimation(float delta) {
        if (isPunching) {
            float oldProgress = punchAnimationTimer / punchAnimationDuration;
            punchAnimationTimer += delta;
            float newProgress = punchAnimationTimer / punchAnimationDuration;

            // Play punch sound when animation reaches peak (30% progress)
            if (oldProgress < 0.3f && newProgress >= 0.3f) {
                SoundManager.getInstance().playSound("manny_airpunch");
            }

            if (punchAnimationTimer >= punchAnimationDuration) {
                isPunching = false;
                punchAnimationTimer = 0f;
            }
        }
    }

    // Satisfy the Melee interface (delegate as well)
    @Override
    public void performMeleeAttack(float delta, float weaponRotationRad) {
        performAttack(delta, weaponRotationRad);
    }

    public Vector2 getWeaponAnimationOffset() {
        if (!isPunching) return originalWeaponOffset;

        float progress = punchAnimationTimer / punchAnimationDuration;

        // Quick punch forward, slower return
        float animationCurve;
        if (progress < 0.3f) {
            // Fast forward punch
            animationCurve = progress / 0.3f;
        } else {
            // Slower return
            animationCurve = 1f - ((progress - 0.3f) / 0.7f);
        }

        return new Vector2(
            punchWeaponOffset.x * animationCurve,
            punchWeaponOffset.y * animationCurve
        );
    }

    // Manny's melee stats used by PunchLogic
    @Override
    public float getMeleeRange() {
        return 1.5f;
    }

    @Override
    public float getMeleeDamage() {
        return com.badlogic.gdx.math.MathUtils.random(8f, 12f);
    }

    @Override
    protected void onDamaged(float delta) {
        super.onDamaged(delta);
        screenShake.shake(0.20f, 0.2f);
        SoundManager.getInstance().playSound("player_damage");
    }
}
