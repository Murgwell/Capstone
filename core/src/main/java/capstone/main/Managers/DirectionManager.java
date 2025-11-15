package capstone.main.Managers;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class DirectionManager {

    private final Sprite sprite;
    private boolean facingRight = true;
    public DirectionManager(Sprite sprite) {
        this.sprite = sprite;
    }
    public void setFacingLeft(boolean left) {
        boolean wantRight = !left;
        if (wantRight != facingRight) {
            // flip horizontally: sprite.setFlip(xFlip, yFlip) expects booleans
            sprite.setFlip(!wantRight, false); // note how setFlip expects flippedX
            facingRight = wantRight;
        }
    }
}
