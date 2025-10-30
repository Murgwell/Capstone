package capstone.main.Characters;

import capstone.main.Handlers.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;


public abstract class AbstractPlayer {

    protected float healthPoints;
    protected float manaPoints;

    protected MovementManager movementManager = new MovementManager();
    protected DirectionManager directionManager; // initialize in constructor
    protected BoundaryManager boundaryManager;
    protected Vector2 position = new Vector2();
    protected Sprite sprite;

    public AbstractPlayer(float healthPoints, float manaPoints, Texture texture, float x, float y, float width, float height) {
        this.healthPoints = healthPoints;
        this.manaPoints = manaPoints;
        position.set(x, y);
        sprite = new Sprite(texture);
        sprite.setSize(width, height);
        boundaryManager = new BoundaryManager(20f, 12f, width, height); // World bounds

        // Initialize direction manager
        directionManager = new DirectionManager(sprite);
    }

    public void update(float delta,
                       InputManager input,
                       MovementManager movementManager,
                       Viewport viewport) {

        // --- compute mouse aiming ---
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        Vector3 worldMouse = viewport.getCamera().unproject(new Vector3(mouseX, mouseY, 0));
        float charCenterX = sprite.getX() + sprite.getWidth() / 2f;

        boolean aimingLeft = worldMouse.x < charCenterX;
        boolean isShooting = input.isAttacking();

        position = movementManager.update(position, input, delta);
        position = boundaryManager.clamp(position);
        sprite.setPosition(position.x, position.y);

        updateDirection(movementManager, aimingLeft, isShooting);
    }

    public Sprite getSprite() {
        return sprite;
    }

    private boolean lastFacingLeft = false;

    private void updateDirection(MovementManager movementManager, boolean aimingLeft, boolean isShooting) {
        Vector2 velocity = movementManager.getVelocity();
        boolean sprinting = movementManager.isSprinting();
        boolean dodging  = movementManager.isDodging();

        float V_TH = 0.05f;

        // ---- Priority 1: Sprinting / Dodging overrides everything ----
        if (sprinting || dodging) {
            if (velocity.x < -V_TH) directionManager.setFacingLeft(true);
            else if (velocity.x > V_TH) directionManager.setFacingLeft(false);
            return;
        }

        // ---- Priority 2: Shooting overrides walking ----
        if (isShooting) {
            directionManager.setFacingLeft(aimingLeft);
            return;
        }

        // ---- Priority 3: Walking while aiming ----
        if (Math.abs(velocity.x) > V_TH) {
            directionManager.setFacingLeft(aimingLeft); // Aim overrides walking
            return;
        }

        // ---- Priority 4: Idle ----
        directionManager.setFacingLeft(aimingLeft);
    }


    public DirectionManager getDirectionManager() {
        return directionManager;
    }

}

