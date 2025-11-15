package capstone.main.Handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class InputManager {

    Vector2 movement = new Vector2();
    private boolean shiftHeld;

    public void update() {
        movement.set(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) movement.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) movement.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) movement.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) movement.x += 1;

        if (!movement.isZero()) movement.nor();

        shiftHeld = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    public Vector2 getMovement() {
        return movement;
    }

    public boolean isShiftHeld() {
        return shiftHeld;
    }

    public boolean isAttacking() {
        return Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    }

    public boolean isSprinting() {
        return shiftHeld && !movement.isZero(); // sprinting only if moving + shift held
    }
}
