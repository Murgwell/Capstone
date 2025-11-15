package capstone.main.Characters;

import capstone.main.Sprites.Bullet;
import java.util.ArrayList;

public interface Ranged {
    ArrayList<Bullet> getBullets();
    void performAttack(float delta, float weaponRotationRad);
}
