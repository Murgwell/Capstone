package capstone.main.Logic;

import capstone.main.Sprites.Bullet;
import capstone.main.Characters.Ranged;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

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
                if (!e.isDead() && b.getBoundingBox().overlaps(e.getSprite().getBoundingRectangle())) {
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
}
