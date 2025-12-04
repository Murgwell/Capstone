package capstone.main.Render;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Characters.MannyPacquiao;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.Color;
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

        // Draw melee range indicator for Manny Pacquiao
        if (player instanceof MannyPacquiao) {
            MannyPacquiao manny = (MannyPacquiao) player;
            if (manny.shouldShowMeleeRange()) {
                spriteBatch.end(); // End sprite batch to draw shapes

                // Enable blending for transparency
                com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                com.badlogic.gdx.Gdx.gl.glBlendFunc(
                    com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                    com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA
                );

                shapeRenderer.setProjectionMatrix(camera.combined);
                shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);

                float playerCenterX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
                float playerCenterY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;
                float meleeRange = manny.getMeleeRange();

                // Draw circle outline for better visibility
                shapeRenderer.end();
                shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(Color.WHITE);
                shapeRenderer.circle(playerCenterX, playerCenterY, meleeRange, 50);

                shapeRenderer.end();
                com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

                spriteBatch.begin(); // Resume sprite batch
            }
        }

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
