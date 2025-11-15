package capstone.main.Handlers;

import com.badlogic.gdx.math.Vector2;

public class MovementManager {

    // Configurable movement parameters
    public float baseSpeed = 4f;
    public float sprintMultiplier = 1.8f;
    public float acceleration = 20f;
    public float friction = 10f;
    public float dodgeSpeed = 10f;
    public float dodgeDuration = 0.15f;
    public float dodgeCooldown = 0f;
    public float dodgeCooldownTime = 1f;
    public float dodgeThreshold = 0.2f;
    Vector2 velocity = new Vector2();
    Vector2 lastMoveDirection = new Vector2();
    Vector2 dodgeDirection = new Vector2();
    private boolean shiftHeld = false;
    private float shiftHoldTime = 0f;
    private boolean sprinting = false;
    private boolean dodging = false;
    private float dodgeTimer = 0f;

    public Vector2 update(Vector2 position, InputManager input, float delta) {
        Vector2 inputDir = input.getMovement();

        // Save last move direction
        if (!dodging && !inputDir.isZero()) lastMoveDirection.set(inputDir.cpy());

        // Handle shift/dodge input
        boolean shiftNow = input.isShiftHeld();
        if (shiftNow) {
            if (!shiftHeld) {
                // Shift just pressed
                shiftHeld = true;
                shiftHoldTime = 0f;
            } else {
                // Shift held
                shiftHoldTime += delta;
            }
        } else {
            // Shift released
            if (shiftHeld && shiftHoldTime <= dodgeThreshold && dodgeCooldown <= 0f) {
                // Quick tap: trigger dodge
                triggerDodge();
            }
            shiftHeld = false;
            shiftHoldTime = 0f;
        }

        // Sprinting state (persistent while holding shift past threshold)
        sprinting = shiftHeld && shiftHoldTime > dodgeThreshold && !dodging;
        float maxSpeed = baseSpeed * (sprinting ? sprintMultiplier : 1f);

        // Dodge movement
        if (dodging) {
            dodgeTimer += delta;
            if (dodgeTimer < dodgeDuration) {
                position.x += dodgeDirection.x * dodgeSpeed * delta;
                position.y += dodgeDirection.y * dodgeSpeed * delta;
                return position; // ignore normal movement
            } else {
                dodging = false;
                dodgeTimer = 0f;
            }
        }

        // Normal movement
        if (!inputDir.isZero()) {
            Vector2 desiredVelocity = new Vector2(inputDir).scl(maxSpeed);
            velocity.lerp(desiredVelocity, acceleration * delta);
        } else {
            float speed = velocity.len();
            if (speed > 0) {
                float decel = friction * delta;
                speed = Math.max(speed - decel, 0);
                velocity.setLength(speed);
            }
        }

        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        if (dodgeCooldown > 0f) dodgeCooldown -= delta;

        return position;
    }

    private void triggerDodge() {
        dodging = true;
        dodgeTimer = 0f;
        dodgeCooldown = dodgeCooldownTime;
        dodgeDirection.set(lastMoveDirection.isZero() ? new Vector2(1, 0) : lastMoveDirection.cpy());
    }

    public boolean isDodging() {
        return dodging;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public Vector2 getVelocity() {
        return velocity.cpy(); // return a copy to avoid external modification
    }
}
