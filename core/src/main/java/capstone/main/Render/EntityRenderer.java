package capstone.main.Render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

import capstone.main.Characters.*;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Sprites.Bullet;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EntityRenderer {

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;
    private final AbstractPlayer player;
    private final ArrayList<AbstractEnemy> enemies;
    private final ArrayList<DamageNumber> dmgNumbers;

    public EntityRenderer(SpriteBatch batch,
                          ShapeRenderer shapeRenderer,
                          AbstractPlayer player,
                          ArrayList<AbstractEnemy> enemies,
                          ArrayList<DamageNumber> dmgNumbers) {

        this.batch = batch;
        this.shapeRenderer = shapeRenderer;
        this.player = player;
        this.enemies = enemies;
        this.dmgNumbers = dmgNumbers;
    }

    public void render(OrthographicCamera camera) {
        float delta = Gdx.graphics.getDeltaTime();

        // ensure GL_BLEND enabled
        Gdx.gl.glEnable(GL20.GL_BLEND);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Player
        player.getSprite().draw(batch);

        // Bullets
        if (player instanceof Ranged) {
            for (Bullet bullet : ((Ranged) player).getBullets()) {
                bullet.draw(batch);
            }
        }

        // Enemies: draw normal sprite first
        for (AbstractEnemy enemy : enemies) {
            if (!enemy.isDead()) {
                enemy.getSprite().draw(batch);
            }
        }

        // Damage numbers (they draw via batch too)
        for (int i = dmgNumbers.size() - 1; i >= 0; i--) {
            DamageNumber dn = dmgNumbers.get(i);
            dn.updateAndDraw(batch, delta);
            if (!dn.isAlive) dmgNumbers.remove(i);
        }

        for (AbstractEnemy enemy : enemies) {
            enemy.getSprite().draw(batch);

            float flashAlpha = enemy.getHitFlashAlpha();
            if (flashAlpha > 0f) {
                enemy.updateWhiteOverlay(); // sync position & flip
                Sprite overlay = enemy.getWhiteOverlaySprite();
                overlay.setColor(1f, 1f, 1f, flashAlpha);
                overlay.draw(batch);
                overlay.setColor(1f, 1f, 1f, 1f); // reset
            }
        }
        batch.end();

        // Healthbars with ShapeRenderer (keep as you had)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (AbstractEnemy enemy : enemies) {
            if (!enemy.isDead() && enemy.getHealthBar() != null) {
                enemy.getHealthBar().update(delta);
                enemy.getHealthBar().draw(shapeRenderer);
            }
        }
        shapeRenderer.end();

        // optional: disable blend (safe)
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

}


