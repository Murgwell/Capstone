package capstone.main.Logic;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Sprites.Bullet;
import capstone.main.Characters.Ranged;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class BulletLogic {
    private final Ranged player;
    private final ArrayList<AbstractEnemy> enemies;
    private final ArrayList<DamageNumber> damageNumbers;
    private final BitmapFont damageFont;

    public BulletLogic(Ranged player, ArrayList<AbstractEnemy> enemies,
                       ArrayList<DamageNumber> damageNumbers, BitmapFont damageFont) {
        this.player = player;
        this.enemies = enemies;
        this.damageNumbers = damageNumbers;
        this.damageFont = damageFont;
    }

    public void update(float delta) {
        ArrayList<Bullet> bullets = player.getBullets();
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta);

            for (AbstractEnemy e : enemies) {
                if (!e.isDead() && Intersector.overlaps(new com.badlogic.gdx.math.Circle(
                        b.getBoundingBox().x + b.getBoundingBox().width / 2f,
                        b.getBoundingBox().y + b.getBoundingBox().height / 2f,
                        Math.min(b.getBoundingBox().width, b.getBoundingBox().height) / 2f),
                    e.getHitbox())) {
                    e.takeHit(b.getDamage());
                    damageNumbers.add(new DamageNumber(
                        e.getSprite().getX() + e.getSprite().getWidth()/2f,
                        e.getSprite().getY() + e.getSprite().getHeight(),
                        b.getDamage(),
                        damageFont
                    ));
                    bullets.remove(i);
                    break;
                }
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

        bullets.add(new Bullet(startX, startY, dir, p, p.getDamage()));
    }

}
