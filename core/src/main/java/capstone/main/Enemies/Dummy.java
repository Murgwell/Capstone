package capstone.main.Enemies;

import capstone.main.Characters.AbstractPlayer;
import com.badlogic.gdx.graphics.Texture;

public class Dummy extends AbstractEnemy {

    public Dummy(float x, float y) {
        super(x, y, new Texture("character.png"), 1f, 1f); // adjust size
        health = 100f; // example
    }

    @Override
    public void update(float delta, AbstractPlayer player) {
        if (isDead()) return;

        // Distance to player
        float dx = player.getSprite().getX() - sprite.getX();
        float dy = player.getSprite().getY() - sprite.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        float chaseRadius = isAggro ? aggroChaseDistance : defaultChaseDistance;

        if (distance <= chaseRadius || isAggro) {
            // move toward player
            float speed = 2f; // dummy speed
            sprite.setX(sprite.getX() + dx / distance * speed * delta);
            sprite.setY(sprite.getY() + dy / distance * speed * delta);

            // If player moves too far from aggro, reset
            if (isAggro && distance > aggroChaseDistance) isAggro = false;
        }
    }
}
