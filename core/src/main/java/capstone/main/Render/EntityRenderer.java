package capstone.main.Render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

        // Enemies
        for (AbstractEnemy enemy : enemies) {
            if (!enemy.isDead()){
                enemy.getSprite().draw(batch);
            }
        }

        // Damage numbers
        for (int i = dmgNumbers.size() - 1; i >= 0; i--) {
            DamageNumber dn = dmgNumbers.get(i);
            dn.updateAndDraw(batch, Gdx.graphics.getDeltaTime());
            if (!dn.isAlive) dmgNumbers.remove(i);
        }
        batch.end();
        // Draw HealthBars with ShapeRenderer
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (AbstractEnemy enemy : enemies) {
            if (!enemy.isDead() && enemy.getHealthBar() != null) {
                enemy.getHealthBar().draw(shapeRenderer);
            }
        }
        shapeRenderer.end();
    }
}


