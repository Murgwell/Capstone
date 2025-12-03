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
        // Update weapon position & rotation based on player's aiming
        float weaponRad = player.getWeaponAimingRad();
        weaponSprite.setOrigin(weaponSprite.getWidth()/2f, weaponSprite.getHeight()*0.25f);
        weaponSprite.setRotation((float)Math.toDegrees(weaponRad));

        float gap = 0.1f; // adjust distance from player center
        float weaponCenterX = player.getSprite().getX() + player.getSprite().getWidth()/2f
            + (float)Math.cos(weaponRad) * gap;
        float weaponCenterY = player.getSprite().getY() + player.getSprite().getHeight()/2f
            + (float)Math.sin(weaponRad) * gap;

        if (player instanceof MannyPacquiao) {
            MannyPacquiao manny = (MannyPacquiao) player;
            Vector2 animOffSet = manny.getWeaponAnimationOffset();
            weaponCenterX += animOffSet.x;
            weaponCenterY += animOffSet.y;
        }
        weaponSprite.setPosition(weaponCenterX - weaponSprite.getOriginX(),
            weaponCenterY - weaponSprite.getOriginY());
    }

    public void render(SpriteBatch batch) {
        batch.begin();
        weaponSprite.draw(batch);
        batch.end();
    }
}
