package capstone.main.Characters;

import capstone.main.Sprites.Bullet;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

/**
 * Manny Pacquiao playable character.
 * Currently mirrors Vico's behaviour but uses the Manny sprite sheet for visuals so that
 * he can be selected independently inside the game.
 */

public class MannyPacquiao extends AbstractPlayer implements Ranged {

    private static final int SPRITE_COLUMNS = 13;
    private static final int SPRITE_ROWS = 2;

    private final ArrayList<Bullet> bullets;

    public MannyPacquiao(float healthPoints, float manaPoints, float baseDamage, float maxDamage,
                         float attackSpeed, float x, float y, float width, float height,
                         ArrayList<Bullet> bullets, float worldWidth, float worldHeight,
                         World physicsWorld) {
        super(
            healthPoints,
            manaPoints,
            baseDamage,
            maxDamage,
            attackSpeed,
            new Texture("Textures/Characters/packed/manny_pacquiao_animations.png"),
            x,
            y,
            width,
            height,
            worldWidth,
            worldHeight,
            physicsWorld
        );

        this.bullets = bullets;

        // Use the first frame from the sprite sheet so the in-game sprite is cropped correctly.
        int frameWidth = sprite.getTexture().getWidth() / SPRITE_COLUMNS;
        int frameHeight = sprite.getTexture().getHeight() / SPRITE_ROWS;
        sprite.setRegion(0, 0, frameWidth, frameHeight);
        sprite.setSize(width, height);
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
    public ArrayList<Bullet> getBullets() {
        return bullets;
    }
}

