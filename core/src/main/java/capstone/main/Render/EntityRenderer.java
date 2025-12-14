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
    private final SpriteBatch spriteBatch;
    private final ShapeRenderer shapeRenderer;
    private final AbstractPlayer player;
    private final ArrayList<AbstractEnemy> enemies;
    private final ArrayList<DamageNumber> damageNumbers;

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

            // Draw status text above enemy
            if (enemy.isSlowed() && !enemy.getStatusText().isEmpty()) {
                float textX = enemy.getSprite().getX() + enemy.getSprite().getWidth() / 2f;
                float textY = enemy.getSprite().getY() + enemy.getSprite().getHeight() + 0.6f;

                // Draw with pulsing effect
                float pulseAlpha = 0.7f + 0.3f * (float)Math.sin(enemy.getSlowTimer() * 5f);
                com.badlogic.gdx.graphics.Color originalColor = enemy.getStatusFont().getColor().cpy();

                // Shadow
                enemy.getStatusFont().setColor(0, 0, 0, pulseAlpha);
                enemy.getStatusFont().draw(spriteBatch, enemy.getStatusText(), textX + 0.02f, textY - 0.02f);

                // Text
                enemy.getStatusFont().setColor(0.3f, 0.8f, 1f, pulseAlpha); // Cyan color

                // Center the text
                com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                    new com.badlogic.gdx.graphics.g2d.GlyphLayout(enemy.getStatusFont(), enemy.getStatusText());
                float centeredX = textX - layout.width / 2f;

                enemy.getStatusFont().draw(spriteBatch, enemy.getStatusText(), centeredX, textY);

                // Restore original color
                enemy.getStatusFont().setColor(originalColor);
            }
        }

        // Boss telegraph AoE indicators (bosses only)
        if (!enemies.isEmpty()) {
            boolean drewAnyTelegraph = false;
            for (AbstractEnemy enemy : enemies) {
                if (enemy instanceof capstone.main.Enemies.BossEntity && enemy instanceof capstone.main.Enemies.TelegraphProvider) {
                    capstone.main.Enemies.TelegraphProvider boss = (capstone.main.Enemies.TelegraphProvider) enemy;
                    if (boss.isTelegraphing()) {
                        if (!drewAnyTelegraph) {
                            drewAnyTelegraph = true;
                            spriteBatch.end();
                            // Enable blending for transparency
                            com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                            com.badlogic.gdx.Gdx.gl.glBlendFunc(
                                com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA
                            );
                            shapeRenderer.setProjectionMatrix(camera.combined);
                        }

                        // World-space center (use physics body position for accuracy)
                        float cx = boss.getTelegraphOriginX();
                        float cy = boss.getTelegraphOriginY();

                        String skill = boss.getTelegraphSkill();
                        if ("SLAM".equals(skill)) {
                            float r = capstone.main.Enemies.GreedConfig.SLAM_RANGE;
                            drawCircleTelegraph(cx, cy, r, new Color(1f, 0f, 0f, 0.09f), new Color(1f, 0.2f, 0.2f, 0.5f));
                        } else if ("RING WAVE".equals(skill)) {
                            float r = capstone.main.Enemies.GreedConfig.RING_RANGE;
                            drawCircleTelegraph(cx, cy, r, new Color(1f, 0f, 0f, 0.06f), new Color(1f, 0.2f, 0.2f, 0.5f));
                        } else if ("CONE BARRAGE".equals(skill)) {
                            float r = capstone.main.Enemies.GreedConfig.BARRAGE_RANGE;
                            float half = capstone.main.Enemies.GreedConfig.BARRAGE_CONE_HALF_ANGLE;
                            float facing = boss.getTelegraphAngleDegrees();
                            drawSectorTelegraph(cx, cy, r, facing - half, facing + half, 
                                new Color(1f, 0f, 0f, 0.06f), new Color(1f, 0.2f, 0.2f, 0.5f));
                        }
                    }
                }
            }
            if (drewAnyTelegraph) {
                // Disable blending and resume sprite batch
                com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                spriteBatch.begin();
            }
        }

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

    // --- Helpers to draw telegraph shapes ---
    private void drawCircleTelegraph(float cx, float cy, float radius, Color fill, Color outline) {
        // Filled circle
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(fill);
        shapeRenderer.circle(cx, cy, radius, 60);
        shapeRenderer.end();
        // Outline
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(outline);
        shapeRenderer.circle(cx, cy, radius, 60);
        shapeRenderer.end();
    }

    private void drawSectorTelegraph(float cx, float cy, float radius, float startDeg, float endDeg, Color fill, Color outline) {
        int segments = 60;
        float start = (float) Math.toRadians(startDeg);
        float end = (float) Math.toRadians(endDeg);
        float step = (end - start) / segments;
        // Filled fan
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(fill);
        float prevX = cx + (float) Math.cos(start) * radius;
        float prevY = cy + (float) Math.sin(start) * radius;
        for (int i = 1; i <= segments; i++) {
            float a = start + step * i;
            float x = cx + (float) Math.cos(a) * radius;
            float y = cy + (float) Math.sin(a) * radius;
            shapeRenderer.triangle(cx, cy, prevX, prevY, x, y);
            prevX = x; prevY = y;
        }
        shapeRenderer.end();
        // Outline arc
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(outline);
        float lastX = cx + (float) Math.cos(start) * radius;
        float lastY = cy + (float) Math.sin(start) * radius;
        for (int i = 1; i <= segments; i++) {
            float a = start + step * i;
            float x = cx + (float) Math.cos(a) * radius;
            float y = cy + (float) Math.sin(a) * radius;
            shapeRenderer.line(lastX, lastY, x, y);
            lastX = x; lastY = y;
        }
        // Outline the two sides from center
        float sx = cx + (float) Math.cos(start) * radius;
        float sy = cy + (float) Math.sin(start) * radius;
        float ex = cx + (float) Math.cos(end) * radius;
        float ey = cy + (float) Math.sin(end) * radius;
        shapeRenderer.line(cx, cy, sx, sy);
        shapeRenderer.line(cx, cy, ex, ey);
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
