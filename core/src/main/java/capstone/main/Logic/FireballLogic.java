package capstone.main.Logic;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Characters.MagicRanged;
import capstone.main.Characters.Quiboloy;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Managers.SoundManager;
import capstone.main.Sprites.DamageNumber;
import capstone.main.Sprites.Fireball;
import capstone.main.Managers.PhysicsManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

public class FireballLogic {

    private final MagicRanged player;
    private final PhysicsManager physicsManager;

    public FireballLogic(Quiboloy player, PhysicsManager physicsManager) {
        this.player = player;
        this.physicsManager = physicsManager;
    }

    /** Update all fireballs (position, lifetime, physics) and cooldown */
    public void update(float delta) {
        ArrayList<Fireball> fireballs = player.getFireballs();
        for (int i = fireballs.size() - 1; i >= 0; i--) {
            Fireball f = fireballs.get(i);
            f.update(delta);

            if (f.getLifetime() <= 0) {
                if (f.getBody() != null) {
                    f.getBody().getWorld().destroyBody(f.getBody());
                }
                fireballs.remove(i);
            }
        }
    }
    /** Spawn a new fireball from player */
    public void spawnFireball(MagicRanged player, float weaponRotationRad) {

        AbstractPlayer p = (AbstractPlayer) player;
        ArrayList<Fireball> fireballs = player.getFireballs();

        // --- Dispersion ---
        float maxDispersionDeg = 1.5f;
        float maxDispersionRad = maxDispersionDeg * MathUtils.degreesToRadians;
        float dispersion = MathUtils.random(-maxDispersionRad, maxDispersionRad);
        float finalAngle = weaponRotationRad + dispersion;

        // Shooting direction
        Vector2 dir = new Vector2(MathUtils.cos(finalAngle), MathUtils.sin(finalAngle));

        // --- BACKWARD OFFSET (only horizontal) ---
        float backwardOffset = 0.5f;
        Vector2 offset = new Vector2(-Math.signum(dir.x) * backwardOffset, 0f);

        // Player center
        float startX = p.getSprite().getX() + p.getSprite().getWidth() / 2f + offset.x;
        float startY = p.getSprite().getY() + p.getSprite().getHeight() / 2f; // always centered vertically

        // Create fireball
        fireballs.add(new Fireball(startX, startY, dir, p, p.getDamage(), physicsManager.getWorld()));

        // Play sound
        SoundManager.getInstance().playSound("quiboloy_fireball");
    }

    public void render(com.badlogic.gdx.graphics.g2d.SpriteBatch batch, com.badlogic.gdx.graphics.OrthographicCamera camera) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Fireball f : player.getFireballs()) {
            f.draw(batch);
        }
        batch.end();
    }
}
