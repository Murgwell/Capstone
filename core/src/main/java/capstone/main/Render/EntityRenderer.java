package capstone.main.Render;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

public class EntityRenderer {
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private AbstractPlayer player;
    private ArrayList<AbstractEnemy> enemies;
    private ArrayList<DamageNumber> damageNumbers;

    public EntityRenderer(SpriteBatch spriteBatch, ShapeRenderer shapeRenderer,
                          AbstractPlayer player, ArrayList<AbstractEnemy> enemies,
                          ArrayList<DamageNumber> damageNumbers) {
        this.spriteBatch = spriteBatch;
        this.shapeRenderer = shapeRenderer;
        this.player = player;
        this.enemies = enemies;
        this.damageNumbers = damageNumbers;
    }

    public void render(OrthographicCamera camera) {
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // Draw player
        player.getSprite().draw(spriteBatch);

        // Draw enemies
        for (AbstractEnemy enemy : enemies) {
            enemy.getSprite().draw(spriteBatch);

            // Draw white overlay if hit
            if (enemy.getHitFlashAlpha() > 0f) {
                enemy.updateWhiteOverlay();
                enemy.getWhiteOverlaySprite().setAlpha(enemy.getHitFlashAlpha());
                enemy.getWhiteOverlaySprite().draw(spriteBatch);
            }
        }

        // Draw damage numbers
        for (DamageNumber dn : damageNumbers) {
            dn.draw(spriteBatch);
        }

        spriteBatch.end();

        // Draw health bars
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (AbstractEnemy enemy : enemies) {
            if (enemy.getHealthBar() != null) {
                enemy.getHealthBar().draw(shapeRenderer);
            }
        }
        shapeRenderer.end();
    }

    public void update(float delta) {
        // Update damage numbers
        for (int i = damageNumbers.size() - 1; i >= 0; i--) {
            DamageNumber dn = damageNumbers.get(i);
            dn.update(delta);

            if (dn.isExpired()) {
                damageNumbers.remove(i);
            }
        }
    }
}
