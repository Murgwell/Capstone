package capstone.main.Characters;

import capstone.main.Managers.ScreenShake;
import capstone.main.Managers.SoundManager;
import capstone.main.Sprites.Bullet;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

/**
 * Vico Sotto character class - A ranged fighter specializing in rapid-fire bullet attacks.
 * 
 * <p>Character Stats:
 * <ul>
 *   <li>Type: Ranged Gunner</li>
 *   <li>Range: Long-distance</li>
 *   <li>Damage: 15-22 per bullet</li>
 *   <li>Attack Speed: 1.8 (high fire rate)</li>
 * </ul>
 * 
 * <p>Features:
 * <ul>
 *   <li>Fast-paced ranged combat</li>
 *   <li>Bullet projectile management</li>
 *   <li>Screen shake feedback on damage</li>
 * </ul>
 * 
 * @author Capstone Team
 * @version 1.0
 */
public class VicoSotto extends AbstractPlayer implements Ranged {

    private final ArrayList<Bullet> bullets;
    private final ScreenShake screenShake;

    public VicoSotto(float healthPoints, float manaPoints, float baseDamage, float maxDamage,
                     float attackSpeed, float x, float y, float width, float height,
                     ArrayList<Bullet> bullets, float worldWidth, float worldHeight,
                     World physicsWorld, ScreenShake screenShake) {
        super(
            healthPoints,
            manaPoints,
            baseDamage,
            maxDamage,
            attackSpeed,
            "Vico Sotto", // Use character name for animations
            x,
            y,
            width,
            height,
            worldWidth,
            worldHeight,
            physicsWorld
        );

        this.bullets = bullets;
        this.screenShake = screenShake;
    }

    @Override
    public void performAttack(float delta, float weaponRotationRad) {
        if (!canAttack()) return;
        onAttackPerformed();
    }

    @Override
    protected void onDamaged(float delta) {
        super.onDamaged(delta);
        screenShake.shake(0.20f, 0.2f);
        // PLAY SOUND
        SoundManager.getInstance().playSound("player_damage");
    }

    @Override
    public ArrayList<Bullet> getBullets() {
        return bullets;
    }
}
