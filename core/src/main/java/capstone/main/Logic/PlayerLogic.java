package capstone.main.Logic;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Characters.Melee;
import capstone.main.Characters.Ranged;
import capstone.main.Characters.MagicRanged;
import capstone.main.Managers.InputManager;
import capstone.main.Managers.MovementManager;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PlayerLogic {
    private final AbstractPlayer player;
    private final InputManager input;
    private final Viewport viewport;
    private final MovementManager movementManager;
    private final BulletLogic bulletLogic;
    private final FireballLogic fireballLogic;

    public PlayerLogic(AbstractPlayer player, InputManager input, Viewport viewport,
                       MovementManager movementManager, BulletLogic bulletLogic,
                       FireballLogic fireballLogic) {
        this.player = player;
        this.input = input;
        this.viewport = viewport;
        this.movementManager = movementManager;
        this.bulletLogic = bulletLogic;
        this.fireballLogic = fireballLogic;
    }


    public void update(float delta) {
        player.update(delta, input, movementManager, viewport);
        player.updatePostMovementTimers(delta, movementManager); // always runs

        if (input.isAttacking() && player.canAttack()) {
            if (player instanceof MagicRanged && fireballLogic != null) {
                fireballLogic.spawnFireball((MagicRanged) player, player.getWeaponAimingRad());
                player.performAttack(delta, player.getWeaponAimingRad());
            } else if (player instanceof Ranged && bulletLogic != null) {
                bulletLogic.spawnBullet((Ranged) player, player.getWeaponAimingRad());
                player.performAttack(delta, player.getWeaponAimingRad());
            } else if (player instanceof Melee) {
                player.performAttack(delta, player.getWeaponAimingRad());
            }
        }
    }
}
