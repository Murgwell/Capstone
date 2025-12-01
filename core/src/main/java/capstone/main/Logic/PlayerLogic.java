package capstone.main.Logic;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Characters.Melee;
import capstone.main.Characters.Ranged;
import capstone.main.Managers.InputManager;
import capstone.main.Managers.MovementManager;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PlayerLogic {
    private final AbstractPlayer player;
    private final InputManager input;
    private final Viewport viewport;
    private final MovementManager movementManager;
    private final BulletLogic bulletLogic;

    public PlayerLogic(AbstractPlayer player, InputManager input, Viewport viewport, MovementManager movementManager, BulletLogic bulletLogic) {
        this.player = player;
        this.input = input;
        this.viewport = viewport;
        this.movementManager = movementManager;
        this.bulletLogic = bulletLogic;
    }

    public void update(float delta) {
        player.update(delta, input, movementManager, viewport);
        player.updatePostMovementTimers(delta, movementManager); // <-- always runs

        // In the update method, modify the attack section:
        if (input.isAttacking() && player.canAttack()) {
            if (player instanceof Ranged && bulletLogic != null) {
                // Ranged attack
                bulletLogic.spawnBullet((Ranged) player, player.getWeaponAimingRad());
            } else if (player instanceof Melee) {
                // Melee attack
                player.performAttack(delta, player.getWeaponAimingRad());
            }
        }
    }
}
