package capstone.main.Render;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Characters.MannyPacquiao;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class WeaponRenderer {
    private final AbstractPlayer player;
    private final Sprite weaponSprite;

    public WeaponRenderer(AbstractPlayer player, Sprite weaponSprite) {
        this.player = player;
        this.weaponSprite = weaponSprite;
    }

    public void update() {
        // Get aiming angle
        float weaponRad = player.getWeaponAimingRad();
        float angleDeg = (float) Math.toDegrees(weaponRad);

        // Determine flip based on aiming direction
        boolean lookingLeft = Math.cos(weaponRad) < 0;
        weaponSprite.setFlip(lookingLeft, false);
        if (lookingLeft) angleDeg += 180f;

        // Set origin at the center
        weaponSprite.setOrigin(weaponSprite.getWidth() / 2f, weaponSprite.getHeight() / 2f);

        // Base weapon position at player's center + small gap
        float gap = 0.1f;
        float weaponCenterX = player.getSprite().getX() + player.getSprite().getWidth() / 2f
            + (float) Math.cos(weaponRad) * gap;
        float weaponCenterY = player.getSprite().getY() + player.getSprite().getHeight() / 2f
            + (float) Math.sin(weaponRad) * gap;

        if (player instanceof MannyPacquiao) {
            Vector2 animOffset = ((MannyPacquiao) player).getWeaponAnimationOffset();
            weaponCenterX += animOffset.x;
            weaponCenterY += animOffset.y;
        }

        // Set final position and rotation
        weaponSprite.setPosition(weaponCenterX - weaponSprite.getOriginX(),
            weaponCenterY - weaponSprite.getOriginY());
        weaponSprite.setRotation(angleDeg);
    }

    public void render(SpriteBatch batch) {
        batch.begin();
        weaponSprite.draw(batch);
        batch.end();
    }
}
