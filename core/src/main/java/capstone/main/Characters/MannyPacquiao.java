
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
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class MannyPacquiao extends AbstractPlayer implements Melee {
    private final ScreenShake screenShake;
    private final ArrayList<AbstractEnemy> enemies;
    private final ArrayList<DamageNumber> damageNumbers;
    private final BitmapFont damageFont;

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

    // --- Melee attack delegation (like Quiboloy) ---
    @Override
    public void performAttack(float delta, float weaponRotationRad) {
        if (!canAttack()) return;

        // Consume attack & play "air punch" upfront (character-level, just like Quiboloy)
        onAttackPerformed();
        capstone.main.Managers.SoundManager.getInstance().playSound("manny_airpunch");

        // Delegate the actual punch to PunchLogic (external)
        punchLogic.performPunch(weaponRotationRad);
    }

    // Satisfy the Melee interface (delegate as well)
    @Override
    public void performMeleeAttack(float delta, float weaponRotationRad) {
        performAttack(delta, weaponRotationRad);
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
