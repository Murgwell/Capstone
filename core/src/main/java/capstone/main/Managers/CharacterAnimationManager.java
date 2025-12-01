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

    public void update(float delta, float velocityX, float velocityY) {
        stateTime += delta;

        // Determine direction based on velocity
        // Prioritize vertical movement over horizontal
        if (Math.abs(velocityY) > Math.abs(velocityX)) {
            if (velocityY > 0) {
                setDirection(Direction.BACKWARD); // Moving up (W key)
            } else if (velocityY < 0) {
                setDirection(Direction.FORWARD); // Moving down (S key)
            }
        } else {
            if (velocityX < 0) {
                setDirection(Direction.LEFT); // Moving left (A key)
            } else if (velocityX > 0) {
                setDirection(Direction.RIGHT); // Moving right (D key)
            }
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
