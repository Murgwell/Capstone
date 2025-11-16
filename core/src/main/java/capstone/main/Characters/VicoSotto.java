package capstone.main.Characters;

import com.badlogic.gdx.graphics.Texture;
import capstone.main.Sprites.Bullet;

import java.util.ArrayList;

public class VicoSotto extends AbstractPlayer implements Ranged {

    ArrayList<Bullet> bullets;

    public VicoSotto(float healthPoints, float manaPoints, float baseDamage, float maxDamage, float attackSpeed,float x, float y, float width, float height,
                     ArrayList<Bullet> bullets, float worldWidth, float worldHeight) {
        super(healthPoints, manaPoints, baseDamage, maxDamage, attackSpeed, new Texture("character.png"), x, y, width, height, worldWidth, worldHeight);
        this.bullets = bullets;
    }

    public void performAttack(float delta, float weaponRotationRad) {
        if (!canAttack()) return;
        onAttackPerformed();
    }

    @Override
    public ArrayList<Bullet> getBullets() {
        return bullets;
    }
}
