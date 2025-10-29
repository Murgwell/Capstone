package capstone.main.Characters;

import capstone.main.Handlers.DirectionManager;
import capstone.main.Handlers.MovementManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import capstone.main.Sprites.Bullet;

import java.util.ArrayList;

public class VicoSotto extends AbstractPlayer implements Ranged {

    Texture bulletTexture;
    ArrayList<Bullet> bullets;
    private float shootTimer = 0f;
    // attackSpeed: number of shots per second (SPS)
    private float baseAttackSpeed = 30f; // 10 shots per second
    private float currentAttackSpeed = 1f / baseAttackSpeed; // delay in seconds
    float postDodgeDelay = 0.5f; // delay after dodge before shooting
    private float postDodgeTimer = postDodgeDelay;   // tracks time since dodge ended

    DirectionManager directionManager;


    public VicoSotto(float x, float y, float width, float height, ArrayList<Bullet> bullets) {
        super(
            120,
            80,
            new Texture("character.png"),          // normal facing right
            new Texture("characterReversed.png"),  // reversed facing left
            x,
            y,
            width,
            height
        );

        this.bullets = bullets;
        this.bulletTexture = new Texture("bullet.png");

        Texture normalTexture = new Texture("character.png");
        Texture reversedTexture = new Texture("characterReversed.png");

        directionManager = new DirectionManager(sprite);
    }


    public void handleAttack(float arrowRotationRad, float delta, MovementManager movementManager) {
        shootTimer += delta;

        // Block shooting while dodging
        if (movementManager.isDodging()) {
            postDodgeTimer = 0f; // reset timer after dodge
            return;
        }

        // Block shooting while sprinting
        if (movementManager.isSprinting()) return;

        // Post-dodge delay only counts if a dodge just ended
        postDodgeTimer += delta;
        if (postDodgeTimer < postDodgeDelay) return;

        // Normal shooting cooldown
        if (shootTimer >= currentAttackSpeed) {
            Vector2 dir = new Vector2((float)Math.cos(arrowRotationRad), (float)Math.sin(arrowRotationRad));
            float startX = sprite.getX() + sprite.getWidth()/2f;
            float startY = sprite.getY() + sprite.getHeight()/2f;
            bullets.add(new Bullet(bulletTexture, startX, startY, dir));
            shootTimer = 0f;
        }
    }

    @Override
    public ArrayList<Bullet> getBullets() {
        return bullets;
    }

    @Override
    public void updateBullets(float delta) {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            if (!b.update(delta)) bullets.remove(i);
        }
    }

    @Override
    public void modifyAttackSpeed(float multiplier) {
        baseAttackSpeed *= multiplier; // increase SPS
        currentAttackSpeed = 1f / baseAttackSpeed; // recalc delay
    }

    @Override
    public void resetAttackSpeed() {
        baseAttackSpeed = 3f;
        currentAttackSpeed = 1f / baseAttackSpeed;
    }

    public DirectionManager getDirectionManager() {
        return directionManager;
    }
}
