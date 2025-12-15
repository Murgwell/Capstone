package capstone.main.Characters;

import capstone.main.Managers.ScreenShake;
import capstone.main.Managers.SoundManager;
import capstone.main.Sprites.Fireball;
import com.badlogic.gdx.physics.box2d.World;
import capstone.main.Skills.Quiboloy.DivineHealing;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.g2d.BitmapFont;


import java.util.ArrayList;

/**
 * Quiboloy character class - A magic ranged fighter who attacks with fireballs.
 * 
 * <p>Character Stats:
 * <ul>
 *   <li>Type: Magic Ranged</li>
 *   <li>Range: Medium-distance</li>
 *   <li>Damage: 18-25 per fireball</li>
 *   <li>Mana: 120 (highest mana pool)</li>
 * </ul>
 * 
 * <p>Features:
 * <ul>
 *   <li>Fireball-based magic attacks</li>
 *   <li>High mana capacity for sustained combat</li>
 *   <li>Magical projectile management</li>
 * </ul>
 * 
 * @author Capstone Team
 * @version 1.0
 */
public class Quiboloy extends AbstractPlayer implements MagicRanged {

    private final ArrayList<Fireball> fireballs;
    private final ScreenShake screenShake;
    private DivineHealing divineHealing;


    // Animation-based constructor
    public Quiboloy(float healthPoints, float manaPoints, float baseDamage, float maxDamage,
                    float attackSpeed, float x, float y, float width, float height,
                    ArrayList<Fireball> fireballs, float worldWidth, float worldHeight,
                    World physicsWorld, ScreenShake screenShake) {
        super(
            healthPoints,
            manaPoints,
            baseDamage,
            maxDamage,
            attackSpeed,
            "Quiboloy", // Character name for animations
            x,
            y,
            width,
            height,
            worldWidth,
            worldHeight,
            physicsWorld
        );

        this.fireballs = fireballs;
        this.screenShake = screenShake;
    }

    @Override
    public void performAttack(float delta, float weaponRotationRad) {
        if (!canAttack()) return;
        onAttackPerformed();
        // Fireball spawning should be handled by FireballLogic
        SoundManager.getInstance().playSound("quiboloy_fireball");
    }

    @Override
    protected void onDamaged(float delta) {
        super.onDamaged(delta);
        screenShake.shake(0.20f, 0.2f);
        // PLAY SOUND
        SoundManager.getInstance().playSound("player_damage");
    }

    // New getter for FireballLogic
    public ArrayList<Fireball> getFireballs() {
        return fireballs;
    }

    public void initializeSkills(ArrayList<DamageNumber> damageNumbers,
                                 BitmapFont damageFont) {
        this.divineHealing = new DivineHealing(this, damageNumbers, damageFont);
    }

    public void updateSkills(float delta) {
        if (divineHealing != null) {
            divineHealing.update(delta);
        }
    }

    public void useDivineHealing() {
        if (divineHealing != null) {
            divineHealing.activate();
        }
    }
}
