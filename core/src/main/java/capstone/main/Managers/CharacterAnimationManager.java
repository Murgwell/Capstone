package capstone.main.Managers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CharacterAnimationManager {
    private TextureAtlas forwardAtlas;
    private TextureAtlas backwardAtlas;
    private TextureAtlas leftAtlas;
    private TextureAtlas rightAtlas;

    private Animation<TextureRegion> forwardAnim;
    private Animation<TextureRegion> backwardAnim;
    private Animation<TextureRegion> leftAnim;
    private Animation<TextureRegion> rightAnim;

    private Animation<TextureRegion> currentAnimation;
    private float stateTime = 0f;

    public enum Direction {
        FORWARD, BACKWARD, LEFT, RIGHT
    }

    private Direction currentDirection = Direction.FORWARD;

    public CharacterAnimationManager(String characterName) {
        // Load all 4 atlases
        forwardAtlas = new TextureAtlas("Textures/Characters/" + characterName + "/Run-Forward/" + characterName + "_Run-Forward.atlas");
        backwardAtlas = new TextureAtlas("Textures/Characters/" + characterName + "/Run-Backward/" + characterName + "_Run-Backward.atlas");
        leftAtlas = new TextureAtlas("Textures/Characters/" + characterName + "/Run-Left/" + characterName + "_Run-Left.atlas");
        rightAtlas = new TextureAtlas("Textures/Characters/" + characterName + "/Run-Right/" + characterName + "_Run-Right.atlas");

        // Create animations (adjust frame duration to your liking - 0.1f = 10 FPS)
        forwardAnim = new Animation<>(0.1f, forwardAtlas.getRegions(), Animation.PlayMode.LOOP);
        backwardAnim = new Animation<>(0.1f, backwardAtlas.getRegions(), Animation.PlayMode.LOOP);
        leftAnim = new Animation<>(0.1f, leftAtlas.getRegions(), Animation.PlayMode.LOOP);
        rightAnim = new Animation<>(0.1f, rightAtlas.getRegions(), Animation.PlayMode.LOOP);

        currentAnimation = forwardAnim;
    }

    public void update(
        float delta,
        float velocityX,
        float velocityY,
        boolean sprinting,
        boolean dodging,
        boolean isShooting,
        boolean aimingLeft
    ) {
        stateTime += delta;
        float V_TH = 0.05f;

        // Priority 1: Sprinting / Dodging -> movement takes over fully
        if (sprinting || dodging) {
            updateDirectionByVelocity(velocityX, velocityY, V_TH);
            return;
        }

        // Priority 2: Shooting -> facing strictly follows aim left/right
        if (isShooting) {
            setDirection(aimingLeft ? Direction.LEFT : Direction.RIGHT);
            return;
        }

        // Priority 3: Walking while aiming -> aim overrides walking,
        boolean verticalMovement = Math.abs(velocityY) > V_TH;
        boolean horizontalMovement = Math.abs(velocityX) > V_TH;

        // --- Pure vertical movement ---
        if (verticalMovement && !horizontalMovement) {
            if (velocityY > 0) setDirection(Direction.BACKWARD);
            else setDirection(Direction.FORWARD);
            return;
        }

        // --- Horizontal movement (including diagonal) ---
        if (horizontalMovement) {
            setDirection(velocityX < 0 || aimingLeft ? Direction.LEFT : Direction.RIGHT);
            return;
        }

        // --- Idle ---
        setDirection(aimingLeft ? Direction.LEFT : Direction.RIGHT);
    }

    private void updateDirectionByVelocity(float vx, float vy, float V_TH) {
        if (Math.abs(vy) > Math.abs(vx)) {
            if (vy > V_TH) setDirection(Direction.BACKWARD);
            else if (vy < -V_TH) setDirection(Direction.FORWARD);
        } else {
            if (vx < -V_TH) setDirection(Direction.LEFT);
            else if (vx > V_TH) setDirection(Direction.RIGHT);
        }
    }

    private void setDirection(Direction newDirection) {
        if (currentDirection != newDirection) {
            currentDirection = newDirection;
            stateTime = 0f; // Reset animation when changing direction

            switch (currentDirection) {
                case FORWARD:
                    currentAnimation = forwardAnim;
                    break;
                case BACKWARD:
                    currentAnimation = backwardAnim;
                    break;
                case LEFT:
                    currentAnimation = leftAnim;
                    break;
                case RIGHT:
                    currentAnimation = rightAnim;
                    break;
            }
        }
    }

    public TextureRegion getCurrentFrame() {
        return currentAnimation.getKeyFrame(stateTime);
    }

    public TextureRegion getIdleFrame() {
        // Return the first frame of current direction for idle state
        return currentAnimation.getKeyFrame(0);
    }

    public void dispose() {
        forwardAtlas.dispose();
        backwardAtlas.dispose();
        leftAtlas.dispose();
        rightAtlas.dispose();
    }
}
