package capstone.main.Managers;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class DirectionManager {

    private final Sprite sprite;
    private boolean facingLeft = false;

    public DirectionManager(Sprite sprite) {
        this.sprite = sprite;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public void setFacingLeft(boolean facingLeft) {
        this.facingLeft = facingLeft;
        // Flip sprite horizontally if necessary
        sprite.setFlip(facingLeft, false);
    }

    /**
     * Updates facing based on movement and aiming.
     *
     * @param velocity    Current velocity vector.
     * @param sprinting   Whether the entity is sprinting.
     * @param dodging     Whether the entity is dodging.
     * @param isShooting  Whether the entity is attacking/shooting.
     * @param aimingLeft  Whether the entity is aiming left.
     */
    public void updateFacing(Vector2 velocity, boolean sprinting, boolean dodging, boolean isShooting, boolean aimingLeft) {
        float V_TH = 0.05f;

        // Priority 1: Sprinting / Dodging
        if (sprinting || dodging) {
            if (velocity.x < -V_TH) setFacingLeft(true);
            else if (velocity.x > V_TH) setFacingLeft(false);
            return;
        }

        // Priority 2: Shooting overrides walking
        if (isShooting) {
            setFacingLeft(aimingLeft);
            return;
        }

        // Priority 3: Walking while aiming
        if (Math.abs(velocity.x) > V_TH) {
            setFacingLeft(aimingLeft); // Aim overrides walking
            return;
        }

        // Priority 4: Idle, just aim
        setFacingLeft(aimingLeft);
    }
}
