package capstone.main.Characters;

import capstone.main.Managers.MovementManager;
import capstone.main.Sprites.Bullet;
import java.util.ArrayList;

public interface Ranged {
    ArrayList<Bullet> getBullets();
    void handleAttack(float arrowRotationRad, float delta, MovementManager movementManager);
}
