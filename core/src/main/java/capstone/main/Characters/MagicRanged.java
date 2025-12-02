package capstone.main.Characters;

import capstone.main.Sprites.Fireball;
import java.util.ArrayList;

public interface MagicRanged {
    ArrayList<Fireball> getFireballs();
    void performAttack(float delta, float weaponRotationRad);
}
