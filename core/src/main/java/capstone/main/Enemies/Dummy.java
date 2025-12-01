package capstone.main.Enemies;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.ScreenShake;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class Dummy extends AbstractEnemy {


    public Dummy(float x, float y, ScreenShake screenShake, PhysicsManager physics) {
        super(x, y, new Texture("enemyCharacter.png"), 0.5f, 0.5f, 100, screenShake, physics);

        // random initial facing
        boolean facingLeft = MathUtils.randomBoolean();
        directionManager.setFacingLeft(facingLeft);

        this.speed = 1.5f; // optional per-enemy speed
    }

    @Override
    public void update(float delta, AbstractPlayer player) {
        if (isDead()) {
            body.setLinearVelocity(0, 0);
            return;
        }

        // call the default aggro + movement behavior
        updateHitFlash(delta);
        defaultChaseBehavior(delta, player);
    }
}
