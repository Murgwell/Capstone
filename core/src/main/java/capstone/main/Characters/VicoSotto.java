package capstone.main.Characters;

import capstone.main.Managers.DirectionManager;
import com.badlogic.gdx.graphics.Texture;
import capstone.main.Sprites.Bullet;

import java.util.ArrayList;

public class VicoSotto extends AbstractPlayer implements Ranged {

    private float health;
    private float mana;
    private float damage;
    ArrayList<Bullet> bullets;
    private static float AttackSpeed = 100f; // Attack Speed
    DirectionManager directionManager;


    public VicoSotto(float x, float y, float width, float height,
                     ArrayList<Bullet> bullets, float worldWidth, float worldHeight) {
        super(120, 80, AttackSpeed, new Texture("character.png"), x, y, width, height, worldWidth, worldHeight);
        this.bullets = bullets;
        directionManager = new DirectionManager(sprite);
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
