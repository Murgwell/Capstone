package capstone.main.Characters;

import com.badlogic.gdx.graphics.Texture;
import capstone.main.Sprites.Bullet;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class VicoSotto extends AbstractPlayer implements Ranged {
    ArrayList<Bullet> bullets;

    public VicoSotto(float healthPoints, float manaPoints, float baseDamage, float maxDamage,
                     float attackSpeed, float x, float y, float width, float height,
                     ArrayList<Bullet> bullets, float worldWidth, float worldHeight, World physicsWorld) {
        super(healthPoints, manaPoints, baseDamage, maxDamage, attackSpeed,
            new Texture("character.png"), x, y, width, height, worldWidth, worldHeight, physicsWorld);
        this.bullets = bullets;
    }

    @Override
    public void performAttack(float delta, float weaponRotationRad) {
        if (!canAttack()) return;
        onAttackPerformed();
        // Spawn bullet logic can be added here
    }

    @Override
    public ArrayList<Bullet> getBullets() {
        return bullets;
    }
}
