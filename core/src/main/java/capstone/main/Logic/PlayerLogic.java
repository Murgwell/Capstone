package capstone.main.Logic;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Characters.Ranged;
import capstone.main.Managers.InputManager;
import capstone.main.Managers.MovementManager;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PlayerLogic {
    private final AbstractPlayer player;
    private final InputManager input;
    private final Viewport viewport;
    private final MovementManager movementManager;

    public PlayerLogic(AbstractPlayer player, InputManager input, Viewport viewport, MovementManager movementManager) {
        this.player = player;
        this.input = input;
        this.viewport = viewport;
        this.movementManager = movementManager;
    }

    public void update(float delta) {
        player.update(delta, input, movementManager, viewport); // movementManager can be injected if needed
        if (player instanceof Ranged && input.isAttacking()) {
            Ranged r = (Ranged) player;
            r.handleAttack(player.getWeaponAimingRad(), delta, movementManager);
        }

    }
}
