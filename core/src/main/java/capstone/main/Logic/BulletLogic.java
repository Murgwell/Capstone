package capstone.main.Logic;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.SoundManager;
import capstone.main.Sprites.Bullet;
import capstone.main.Characters.Ranged;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

public class BulletLogic {
    private final Ranged player;
    private final PhysicsManager physicsManager; // add this

    public BulletLogic(Ranged player, PhysicsManager physicsManager) {
        this.player = player;
        this.physicsManager = physicsManager; // store reference
    }

    public void update(float delta) {
        ArrayList<Bullet> bullets = player.getBullets();
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            // Remove bullets that expired naturally or via collision
            if (b.getLifetime() <= 0) {
                // Also destroy the Box2D body if you created one
                if (b.getBody() != null) {
                    b.getBody().getWorld().destroyBody(b.getBody());
                }
                bullets.remove(i);
            }
        }
    }


    public void spawnBullet(Ranged player, float weaponRotationRad) {

        AbstractPlayer p = (AbstractPlayer) player;
        ArrayList<Bullet> bullets = player.getBullets();

        float maxDispersionDeg = 1.5f;
        float maxDispersionRad = maxDispersionDeg * MathUtils.degreesToRadians;
        float dispersion = MathUtils.random(-maxDispersionRad, maxDispersionRad);
        float finalAngle = weaponRotationRad + dispersion;

        // Shooting direction
        Vector2 dir = new Vector2(MathUtils.cos(finalAngle), MathUtils.sin(finalAngle));

        // BACKWARD OFFSET
        float backwardOffset = 0.5f;  // tweak this number
        Vector2 offset = new Vector2(dir).scl(-backwardOffset);

        // Player center
        float startX = p.getSprite().getX() + p.getSprite().getWidth() / 2f;
        float startY = p.getSprite().getY() + p.getSprite().getHeight() / 2f;

        // Apply offset
        startX += offset.x;
        startY += offset.y;

        bullets.add(new Bullet(startX, startY, dir, p, physicsManager.getWorld()));

        SoundManager.getInstance().playSound("vico_shoot");
    }

    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, com.badlogic.gdx.graphics.OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Bullet b : player.getBullets()) {
            b.draw(batch);
        }
        batch.end();
    }
}
