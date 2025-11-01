package capstone.main.Characters;

import capstone.main.Handlers.DirectionManager;
import capstone.main.Handlers.MovementManager;
import capstone.main.Sprites.Bullet;

import java.util.ArrayList;

public interface Ranged {
    ArrayList<Bullet> getBullets();

    void updateBullets(float delta);

    void modifyAttackSpeed(float multiplier);

    void resetAttackSpeed();

    void handleAttack(float arrowRotationRad, float delta, MovementManager movementManager);

    DirectionManager getDirectionManager();
}
