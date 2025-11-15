package capstone.main.Characters;

import capstone.main.Managers.DirectionManager;
import capstone.main.Managers.MovementManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import capstone.main.Sprites.Bullet;

import java.util.ArrayList;

public class VicoSotto extends AbstractPlayer implements Ranged {

    private float health;
    private float mana;
    private float damage;
    Texture bulletTexture;
    ArrayList<Bullet> bullets;
    private float shootTimer = 0f; //Do not change
    private float baseAttackSpeed = 30f; // Attack Speed
    private float currentAttackSpeed = 1f / baseAttackSpeed; // delay in seconds
    DirectionManager directionManager;


    public VicoSotto(float x, float y, float width, float height,
                     ArrayList<Bullet> bullets, float worldWidth, float worldHeight) {
        super(120, 80, new Texture("character.png"), x, y, width, height, worldWidth, worldHeight);
        this.bullets = bullets;
        this.bulletTexture = new Texture("bullet.png");
        directionManager = new DirectionManager(sprite);
    }

    public void handleAttack(float weaponRotationRad, float delta, MovementManager movementManager) {
        shootTimer += delta;

        // Update generic post-movement timers
        updatePostMovementTimers(delta, movementManager);

        // Block shooting if still in dodge/sprint delay
        if (!canAttack()) return;

        // Normal shooting cooldown
        if (shootTimer >= currentAttackSpeed) {
            // --- Bullet dispersion setup ---
            float maxDispersionDeg = 1.5f;
            float maxDispersionRad = maxDispersionDeg * MathUtils.degreesToRadians;
            float dispersion = MathUtils.random(-maxDispersionRad, maxDispersionRad);
            float finalAngle = weaponRotationRad + dispersion;

            Vector2 dir = new Vector2(MathUtils.cos(finalAngle), MathUtils.sin(finalAngle));

            float startX = sprite.getX() + sprite.getWidth()/2f;
            float startY = sprite.getY() + sprite.getHeight()/2f;

            float damage = 10 + (float)Math.random() * 5;
            bullets.add(new Bullet(bulletTexture, startX, startY, dir, damage));

            shootTimer = 0f;
        }
    }

    @Override
    public ArrayList<Bullet> getBullets() {
        return bullets;
    }

    public DirectionManager getDirectionManager() {
        return directionManager;
    }
}
