package capstone.main.Managers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import capstone.main.Characters.AbstractPlayer;

public class MovementManager {

    private final Body playerBody;

    private boolean sprinting = false;
    private boolean dodging = false;
    private Vector2 dodgeDir = new Vector2();
    private float dodgeTimer = 0f;
    private float dodgeCooldown = 0f;

    private boolean shiftHeld = false;
    private float shiftHoldTime = 0f;

    public float baseSpeed = 5f;
    public float sprintMultiplier = 1.8f;
    public float dodgeSpeed = baseSpeed * 3f;
    public float dodgeDuration = 0.15f;
    public float dodgeCooldownTime = 1f;
    public float dodgeThreshold = 0.2f;

    private final Vector2 lastMoveDir = new Vector2();
    private final Vector2 velocity = new Vector2();

    public MovementManager(AbstractPlayer player) {
        this.playerBody = player.getBody();

        // Optional: smooth stopping / slide
        playerBody.setLinearDamping(5f); // tweak 0..10 for more/less slide
    }

    /**
     * Call every frame.
     * @param inputDir Vector2 from input (-1..1)
     * @param delta delta time
     * @param shiftNow whether shift is held
     * @return current position
     */
    public Vector2 update(Vector2 inputDir, float delta, boolean shiftNow) {
        handleShift(inputDir, shiftNow, delta);

        sprinting = shiftHeld && shiftHoldTime > dodgeThreshold && !dodging;
        float speed = baseSpeed * (sprinting ? sprintMultiplier : 1f);

        // --- Dodge logic ---
        if (dodging) {
            dodgeTimer += delta;
            if (dodgeTimer < dodgeDuration) {
                // direct linear velocity for dash
                playerBody.setLinearVelocity(dodgeDir.x * dodgeSpeed, dodgeDir.y * dodgeSpeed);
                return playerBody.getPosition();
            } else {
                dodging = false;
                dodgeTimer = 0f;
            }
        }

        // --- Force-based movement for natural friction ---
        if (!inputDir.isZero()) {
            lastMoveDir.set(inputDir.cpy());
            Vector2 targetVel = inputDir.cpy().nor().scl(speed);
            playerBody.setLinearVelocity(targetVel);
            velocity.set(playerBody.getLinearVelocity());
        } else {
            velocity.set(playerBody.getLinearVelocity()); // naturally slows due to linear damping
        }

        // --- Dodge cooldown ---
        if (dodgeCooldown > 0f) dodgeCooldown -= delta;

        return playerBody.getPosition();
    }


    private void handleShift(Vector2 inputDir, boolean shiftNow, float delta) {
        if (shiftNow) {
            if (!shiftHeld) shiftHoldTime = 0f;
            shiftHoldTime += delta;
        } else {
            if (shiftHeld && shiftHoldTime <= dodgeThreshold && dodgeCooldown <= 0f && !inputDir.isZero()) {
                triggerDodge();
            }
            shiftHoldTime = 0f;
        }
        shiftHeld = shiftNow;
    }

    private void triggerDodge() {
        dodging = true;
        dodgeTimer = 0f;
        dodgeCooldown = dodgeCooldownTime;
        dodgeDir.set(lastMoveDir.isZero() ? new Vector2(1,0) : lastMoveDir.cpy());
    }

    public Vector2 getVelocity() { return velocity; }

    public boolean isDodging() { return dodging; }
    public boolean isSprinting() { return sprinting; }
}
