package capstone.main.Characters;

import capstone.main.Managers.ScreenShake;
import capstone.main.Sprites.Bullet;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class MannyPacquiao extends AbstractPlayer implements Ranged {

    private static final int SPRITE_COLUMNS = 13;
    private static final int SPRITE_ROWS = 2;

    private final ArrayList<Bullet> bullets;
    private final ScreenShake screenShake;

    // OPTION 1: Use animations (NEW)
    public MannyPacquiao(float healthPoints, float manaPoints, float baseDamage, float maxDamage,
                         float attackSpeed, float x, float y, float width, float height,
                         ArrayList<Bullet> bullets, float worldWidth, float worldHeight,
                         World physicsWorld, ScreenShake screenShake) {
        super(
            healthPoints,
            manaPoints,
            baseDamage,
            maxDamage,
            attackSpeed,
            "Manny Pacquiao", // Use character name for animations
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
        if (!canAttack()) {
            return;
        }
        onAttackPerformed();
        // TODO: Add Manny-specific attack behaviour (bullets, melee, etc.)
    }

    @Override
    protected void onDamaged(float delta) {
        super.onDamaged(delta);
        screenShake.shake(0.20f, 0.2f);
    }

    @Override
    public ArrayList<Bullet> getBullets() {
        return bullets;
    }
}
