package capstone.main.Handlers;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class DirectionManager {

    private final Sprite sprite;
    private boolean facingRight = true;
    private boolean isShooting = false;
    private boolean isSprinting = false;
    private boolean isWalking = false;

    public DirectionManager(Sprite sprite) {
        this.sprite = sprite;
    }

    // Called every frame by movement logic
    public void updateMovementDirection(Vector2 velocity) {
        if (velocity.x < 0) setFacingLeft(true);
        else if (velocity.x > 0) setFacingLeft(false);
    }

    // Called when shooting starts (with aim direction)
    public void startShooting(boolean aimingRight) {
        isShooting = true;
        // While shooting, aim direction defines facing
        setFacingRight(aimingRight);
    }

    // Called when shooting stops
    public void stopShooting() {
        isShooting = false;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    private void setFacingRight(boolean right) {
        // FIX: remove the logical NOT to make left/right consistent
        if (facingRight != right) {
            sprite.setFlip(right, false);
            facingRight = right;
        }
    }

    public boolean isFacingLeft() {
        return !facingRight;
    }

    // flip sprite so that it faces left when left==true
    public void setFacingLeft(boolean left) {
        boolean wantRight = !left;
        if (wantRight != facingRight) {
            // flip horizontally: sprite.setFlip(xFlip, yFlip) expects booleans
            sprite.setFlip(!wantRight, false); // note how setFlip expects flippedX
            facingRight = wantRight;
        }
    }

    public void applyDirectionToSprite(Sprite sprite) {
        sprite.setFlip(!facingRight, false);
    }

    public void resetStates() {
        isShooting = false;
        isSprinting = false;
        isWalking = false;
    }
}
