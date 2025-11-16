package capstone.main.Enemies;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Managers.DirectionManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class Dummy extends AbstractEnemy {

    private DirectionManager directionManager;

    public Dummy(float x, float y) {
        super(x, y, new Texture("enemyCharacter.png"), 0.5f, 0.5f, 100f); // adjust size
        directionManager = new DirectionManager(sprite);

        // Randomly spawn facing left or right
        boolean facingLeft = MathUtils.randomBoolean();
        directionManager.setFacingLeft(facingLeft);
    }

    @Override
    public void update(float delta, AbstractPlayer player) {
        if (isDead()) return;

        // Distance to player
        float dx = player.getSprite().getX() - sprite.getX();
        float dy = player.getSprite().getY() - sprite.getY();
        float distance = (float) Math.sqrt(dx*dx + dy*dy);

        float chaseRadius = isAggro ? aggroChaseDistance : defaultChaseDistance;

        if (distance <= chaseRadius || isAggro) {
            // move toward player
            float speed = 1.5f; // dummy speed
            sprite.setX(sprite.getX() + dx / distance * speed * delta);
            sprite.setY(sprite.getY() + dy / distance * speed * delta);

            // Flip sprite based on player position
            boolean aimingLeft = dx < 0;
            directionManager.setFacingLeft(aimingLeft);

            // If player moves too far from aggro, reset
            if (isAggro && distance > aggroChaseDistance) isAggro = false;
        }
        healthBar.update(delta);
    }
}
