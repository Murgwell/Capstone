package capstone.main.Characters;

import capstone.main.Managers.ScreenShake;
import capstone.main.Sprites.Fireball;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class Quiboloy extends AbstractPlayer implements MagicRanged {

    private final ArrayList<Fireball> fireballs;
    private final ScreenShake screenShake;

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
    }

    @Override
    protected void onDamaged(float delta) {
        super.onDamaged(delta);
        screenShake.shake(0.20f, 0.2f);
    }

    // New getter for FireballLogic
    public ArrayList<Fireball> getFireballs() {
        return fireballs;
    }
}
