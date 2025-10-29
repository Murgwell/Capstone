package capstone.main.Characters;

import capstone.main.Handlers.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public abstract class AbstractPlayer {

    protected float healthPoints;
    protected float manaPoints;

    protected MovementManager movementManager = new MovementManager();
    protected DirectionManager directionManager; // initialize in constructor
    protected BoundaryManager boundaryManager;
    protected Vector2 position = new Vector2();
    protected Sprite sprite;

    public AbstractPlayer(float healthPoints, float manaPoints, Texture texture, Texture reversedTexture, float x, float y, float width, float height) {
        this.healthPoints = healthPoints;
        this.manaPoints = manaPoints;
        position.set(x, y);
        sprite = new Sprite(texture);
        sprite.setSize(width, height);
        boundaryManager = new BoundaryManager(20f, 12f, width, height); // World bounds

        // Initialize direction manager
        directionManager = new DirectionManager(sprite, texture, reversedTexture);
    }

    public void update(float delta, InputManager input, MovementManager movementManager) {
        position = movementManager.update(position, input, delta);
        position = boundaryManager.clamp(position);

        sprite.setPosition(position.x, position.y);

        // Update facing based on movement
        directionManager.updateDirection(movementManager.getVelocity());
    }

    public Sprite getSprite() {
        return sprite;
    }
}

