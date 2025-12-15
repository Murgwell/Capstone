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
                        
                        // Greed's skills (red color scheme)
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
                        
                        // Discaya's skills (purple color scheme)
                        else if ("POISON CLOUD".equals(skill)) {
                            float r = capstone.main.Enemies.DiscayaConfig.POISON_RANGE;
                            // Purple poison cloud with pulsing effect
                            drawCircleTelegraph(cx, cy, r, new Color(0.5f, 0f, 0.8f, 0.12f), new Color(0.7f, 0.2f, 1f, 0.6f));
                        } else if ("SHADOW DASH".equals(skill)) {
                            float length = capstone.main.Enemies.DiscayaConfig.DASH_RANGE;
                            float width = capstone.main.Enemies.DiscayaConfig.DASH_WIDTH;
                            float facing = boss.getTelegraphAngleDegrees();
                            // Draw a rectangle showing the dash path
                            drawDashTelegraph(cx, cy, length, width, facing, 
                                new Color(0.3f, 0f, 0.5f, 0.15f), new Color(0.6f, 0.1f, 0.9f, 0.7f));
                        } else if ("CORRUPTION PULSE".equals(skill)) {
                            float innerR = capstone.main.Enemies.DiscayaConfig.PULSE_INNER_RANGE;
                            float outerR = capstone.main.Enemies.DiscayaConfig.PULSE_OUTER_RANGE;
                            // Draw donut shape (ring between two circles)
                            drawDonutTelegraph(cx, cy, innerR, outerR, 
                                new Color(0.4f, 0f, 0.7f, 0.1f), new Color(0.7f, 0.2f, 1f, 0.6f));
                        }
                        
                        // QuiboloyBoss's skills (golden/divine color scheme)
                        else if ("DIVINE JUDGMENT".equals(skill)) {
                            // Draw three concentric rings showing damage zones
                            float innerR = capstone.main.Enemies.QuiboloyConfig.DIVINE_INNER_RANGE;
                            float middleR = capstone.main.Enemies.QuiboloyConfig.DIVINE_MIDDLE_RANGE;
                            float outerR = capstone.main.Enemies.QuiboloyConfig.DIVINE_OUTER_RANGE;
                            // Inner ring (most dangerous) - bright gold
                            drawCircleTelegraph(cx, cy, innerR, new Color(1f, 0.8f, 0f, 0.2f), new Color(1f, 0.9f, 0.2f, 0.8f));
                            // Middle ring - medium gold
                            drawDonutTelegraph(cx, cy, innerR, middleR, 
                                new Color(1f, 0.7f, 0f, 0.15f), new Color(1f, 0.85f, 0.3f, 0.7f));
                            // Outer ring - light gold
                            drawDonutTelegraph(cx, cy, middleR, outerR, 
                                new Color(1f, 0.6f, 0f, 0.1f), new Color(1f, 0.8f, 0.4f, 0.6f));
                        } else if ("FIREBALL BARRAGE".equals(skill)) {
                            float r = 15f; // Fireball range
                            float half = capstone.main.Enemies.QuiboloyConfig.BARRAGE_SPREAD_ANGLE / 2f;
                            float facing = boss.getTelegraphAngleDegrees();
                            // Orange-red cone for fireball spread
                            drawSectorTelegraph(cx, cy, r, facing - half, facing + half, 
                                new Color(1f, 0.3f, 0f, 0.12f), new Color(1f, 0.5f, 0.1f, 0.7f));
                        } else if ("TELEPORT STRIKE".equals(skill)) {
                            float strikeR = capstone.main.Enemies.QuiboloyConfig.TELEPORT_STRIKE_RANGE;
                            // Purple circle for teleport destination (approximate)
                            // Show at player location since that's where boss will teleport near
                            float playerX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
                            float playerY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;
                            drawCircleTelegraph(playerX, playerY, strikeR, 
                                new Color(0.5f, 0f, 1f, 0.15f), new Color(0.8f, 0.3f, 1f, 0.7f));
                        } else if ("CORRUPTION ZONE".equals(skill)) {
                            float r = capstone.main.Enemies.QuiboloyConfig.CORRUPTION_RANGE;
                            // Dark purple zone
                            drawCircleTelegraph(cx, cy, r, new Color(0.3f, 0f, 0.4f, 0.15f), new Color(0.6f, 0.1f, 0.7f, 0.7f));
                        } else if ("SUMMON FOLLOWERS".equals(skill)) {
                            float summonR = capstone.main.Enemies.QuiboloyConfig.SUMMON_RADIUS;
                            // Blue circle for summoning area
                            drawCircleTelegraph(cx, cy, summonR, new Color(0.2f, 0.3f, 1f, 0.12f), new Color(0.4f, 0.6f, 1f, 0.6f));
                        }
                    }
                }
            }
            if (drewAnyTelegraph) {
                // Disable blending and resume sprite batch
                com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                spriteBatch.begin();
            }
            
            // Draw lingering poison clouds from Discaya
            boolean drewPoisonClouds = false;
            for (AbstractEnemy enemy : enemies) {
                if (enemy instanceof capstone.main.Enemies.Discaya) {
                    capstone.main.Enemies.Discaya discaya = (capstone.main.Enemies.Discaya) enemy;
                    java.util.List<capstone.main.Enemies.Discaya.PoisonCloud> clouds = discaya.getActivePoisonClouds();
                    
                    if (!clouds.isEmpty()) {
                        if (!drewPoisonClouds) {
                            drewPoisonClouds = true;
                            spriteBatch.end();
                            com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                            com.badlogic.gdx.Gdx.gl.glBlendFunc(
                                com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA
                            );
                            shapeRenderer.setProjectionMatrix(camera.combined);
                        }
                        
                        // Draw each poison cloud with pulsing and fading effect
                        for (capstone.main.Enemies.Discaya.PoisonCloud cloud : clouds) {
                            float alpha = cloud.getAlpha() * 0.15f; // Base transparency
                            float pulse = (float)Math.sin(System.currentTimeMillis() * 0.003) * 0.05f + 0.1f;
                            alpha = Math.max(0f, alpha + pulse);
                            
                            // Draw filled cloud with green-purple color
                            Color fillColor = new Color(0.4f, 0.8f, 0.3f, alpha);
                            Color outlineColor = new Color(0.5f, 1.0f, 0.4f, cloud.getAlpha() * 0.4f);
                            
                            drawCircleTelegraph(cloud.x, cloud.y, cloud.radius, fillColor, outlineColor);
                        }
                    }
                }
            }
            
            if (drewPoisonClouds) {
                com.badlogic.gdx.Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                spriteBatch.begin();
            }
            
            // Draw lingering corruption zones from QuiboloyBoss
            boolean drewCorruptionZones = false;
            for (AbstractEnemy enemy : enemies) {
                if (enemy instanceof capstone.main.Enemies.QuiboloyBoss) {
                    capstone.main.Enemies.QuiboloyBoss quiboloy = (capstone.main.Enemies.QuiboloyBoss) enemy;
                    java.util.List<capstone.main.Enemies.QuiboloyBoss.CorruptionZone> zones = quiboloy.getActiveCorruptionZones();
                    
                    if (!zones.isEmpty()) {
                        if (!drewCorruptionZones) {
                            drewCorruptionZones = true;
                            spriteBatch.end();
                            com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                            com.badlogic.gdx.Gdx.gl.glBlendFunc(
                                com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA
                            );
                            shapeRenderer.setProjectionMatrix(camera.combined);
                        }
                        
                        // Draw each corruption zone with pulsing and fading effect
                        for (capstone.main.Enemies.QuiboloyBoss.CorruptionZone zone : zones) {
                            float alpha = zone.getAlpha() * 0.2f; // Base transparency (darker than poison)
                            float pulse = (float)Math.sin(System.currentTimeMillis() * 0.004) * 0.08f + 0.12f;
                            alpha = Math.max(0f, alpha + pulse);
                            
                            // Draw filled zone with dark purple color
                            Color fillColor = new Color(0.3f, 0.1f, 0.4f, alpha);
                            Color outlineColor = new Color(0.6f, 0.2f, 0.8f, zone.getAlpha() * 0.5f);
                            
                            drawCircleTelegraph(zone.x, zone.y, zone.radius, fillColor, outlineColor);
                        }
                    }
                }
            }
            
            if (drewCorruptionZones) {
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

    // Draw a rectangular dash telegraph
    private void drawDashTelegraph(float cx, float cy, float length, float width, float angleDeg, Color fill, Color outline) {
        float angleRad = (float) Math.toRadians(angleDeg);
        float halfWidth = width / 2f;
        
        // Calculate the four corners of the rectangle
        float cosA = (float) Math.cos(angleRad);
        float sinA = (float) Math.sin(angleRad);
        float perpCosA = (float) Math.cos(angleRad + Math.PI / 2);
        float perpSinA = (float) Math.sin(angleRad + Math.PI / 2);
        
        // Start at center, extend forward by length
        float endX = cx + cosA * length;
        float endY = cy + sinA * length;
        
        // Four corners of the rectangle
        float x1 = cx + perpCosA * halfWidth;
        float y1 = cy + perpSinA * halfWidth;
        float x2 = cx - perpCosA * halfWidth;
        float y2 = cy - perpSinA * halfWidth;
        float x3 = endX - perpCosA * halfWidth;
        float y3 = endY - perpSinA * halfWidth;
        float x4 = endX + perpCosA * halfWidth;
        float y4 = endY + perpSinA * halfWidth;
        
        // Draw filled rectangle
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(fill);
        shapeRenderer.triangle(x1, y1, x2, y2, x3, y3);
        shapeRenderer.triangle(x1, y1, x3, y3, x4, y4);
        shapeRenderer.end();
        
        // Draw outline
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(outline);
        shapeRenderer.line(x1, y1, x2, y2);
        shapeRenderer.line(x2, y2, x3, y3);
        shapeRenderer.line(x3, y3, x4, y4);
        shapeRenderer.line(x4, y4, x1, y1);
        shapeRenderer.end();
    }
    
    // Draw a donut-shaped telegraph (ring between two circles)
    private void drawDonutTelegraph(float cx, float cy, float innerRadius, float outerRadius, Color fill, Color outline) {
        int segments = 60;
        
        // Draw filled donut using triangles
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(fill);
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);
            
            float innerX1 = cx + (float) Math.cos(angle1) * innerRadius;
            float innerY1 = cy + (float) Math.sin(angle1) * innerRadius;
            float innerX2 = cx + (float) Math.cos(angle2) * innerRadius;
            float innerY2 = cy + (float) Math.sin(angle2) * innerRadius;
            
            float outerX1 = cx + (float) Math.cos(angle1) * outerRadius;
            float outerY1 = cy + (float) Math.sin(angle1) * outerRadius;
            float outerX2 = cx + (float) Math.cos(angle2) * outerRadius;
            float outerY2 = cy + (float) Math.sin(angle2) * outerRadius;
            
            // Two triangles to form the ring segment
            shapeRenderer.triangle(innerX1, innerY1, outerX1, outerY1, innerX2, innerY2);
            shapeRenderer.triangle(innerX2, innerY2, outerX1, outerY1, outerX2, outerY2);
        }
        shapeRenderer.end();
        
        // Draw outlines for both circles
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(outline);
        shapeRenderer.circle(cx, cy, innerRadius, segments);
        shapeRenderer.circle(cx, cy, outerRadius, segments);
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
