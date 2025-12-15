package capstone.main.Managers;

import capstone.main.Enemies.BossEntity;
import capstone.main.UI.BossHudActor;
import capstone.main.UI.VictoryOverlay;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Manages all UI elements including boss HUD, toast notifications, and victory overlay.
 * 
 * <p>Features:
 * <ul>
 *   <li>Boss health bar and telegraph cast bar</li>
 *   <li>Toast notification system</li>
 *   <li>Victory overlay management</li>
 *   <li>Scene2D stage coordination</li>
 * </ul>
 * 
 * <p>This class centralizes UI rendering and state management,
 * providing a clean interface for game logic to display information to the player.
 * 
 * @author Capstone Team
 * @version 1.0
 */
public class UIManager {
    private final BossManager bossManager = new BossManager();
    private final Stage uiStage;
    private final BossHudActor bossActor;
    private VictoryOverlay victoryOverlay;
    public BossManager getBossManager() { return bossManager; }

    // Show a short toast message
    public void showToast(String text, float duration) {
        toastLabel.setText(text);
        toastGroup.clearActions();
        toastGroup.getColor().a = 1f;
        toastGroup.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.visible(true),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(duration),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.35f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.visible(false)
        ));
    }
    private final com.badlogic.gdx.scenes.scene2d.ui.ProgressBar castBar;

    // Toast UI
    private final com.badlogic.gdx.scenes.scene2d.ui.Label toastLabel;
    private final com.badlogic.gdx.scenes.scene2d.Group toastGroup;
    private final com.badlogic.gdx.scenes.scene2d.ui.Image toastBg;

    public UIManager(Viewport uiViewport, SpriteBatch batch, BitmapFont font, VictoryOverlay.VictoryCallback victoryCallback) {
        this.uiStage = new Stage(uiViewport, batch);
        this.bossActor = new BossHudActor(font);
        this.uiStage.addActor(bossActor);
        
        // Create victory overlay
        this.victoryOverlay = new VictoryOverlay(uiStage, font, victoryCallback);

        // Simple cast bar centered under boss HP
        com.badlogic.gdx.scenes.scene2d.ui.Skin skin = new com.badlogic.gdx.scenes.scene2d.ui.Skin();

        // Toast elements: background panel + label in a group
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle toastStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE);
        this.toastLabel = new com.badlogic.gdx.scenes.scene2d.ui.Label("", toastStyle);
        // Make toast readable: larger font and centered (moderate scale)
        this.toastLabel.setFontScale(1.4f);
        this.toastLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        this.toastLabel.setWrap(false);
        // Use a proper 9-patch drawable for scalable background
        this.toastBg = new com.badlogic.gdx.scenes.scene2d.ui.Image(
            new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(
                new com.badlogic.gdx.graphics.g2d.NinePatch(
                    new com.badlogic.gdx.graphics.Texture("ui/Menu/Scrollbar.png"), 2, 2, 2, 2
                )
            )
        );
        this.toastBg.setColor(0, 0, 0, 0.7f); // semi-transparent black
        this.toastGroup = new com.badlogic.gdx.scenes.scene2d.Group();
        this.toastGroup.addActor(toastBg);
        this.toastGroup.addActor(toastLabel);
        this.toastGroup.setVisible(false);
        this.uiStage.addActor(this.toastGroup);
        com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle style = new com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle();
        style.background = new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(new com.badlogic.gdx.graphics.g2d.NinePatch(new com.badlogic.gdx.graphics.Texture("ui/Menu/Scrollbar.png"), 2,2,2,2));
        style.knobBefore = new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(new com.badlogic.gdx.graphics.g2d.NinePatch(new com.badlogic.gdx.graphics.Texture("ui/Hud/HP-Bar.png"), 2,2,2,2));
        this.castBar = new com.badlogic.gdx.scenes.scene2d.ui.ProgressBar(0f, 1f, 0.001f, false, style);
        this.castBar.setSize(300f, 12f);
        this.castBar.setVisible(false);
        this.uiStage.addActor(this.castBar);
    }

    public void updateBossReference(java.util.List<? extends Object> enemies) {
        BossEntity boss = null;
        for (Object obj : enemies) {
            if (obj instanceof BossEntity) { boss = (BossEntity) obj; break; }
        }
        bossManager.setBoss(boss);
        bossActor.setBoss(boss);
    }

    public void actAndDraw(float delta) {
        // Position toast at top center if visible
        if (toastGroup.isVisible()) {
            float w = uiStage.getViewport().getWorldWidth();
            float y = uiStage.getViewport().getWorldHeight() - 64f;
            float padX = 22f, padY = 12f;
            // Compute scaled label size so background fits correctly
            float scaleX = toastLabel.getFontScaleX();
            float scaleY = toastLabel.getFontScaleY();
            // Force a layout update before measuring
            toastLabel.invalidateHierarchy();
            float labelW = toastLabel.getPrefWidth() * scaleX;
            float labelH = toastLabel.getPrefHeight() * scaleY;
            // Size the background to fit the scaled label with padding
            float bgW = labelW + padX * 2f;
            float bgH = labelH + padY * 2f;
            toastBg.setSize(bgW, bgH);
            // Position background centered at top
            float bgX = (w - bgW) / 2f;
            toastBg.setPosition(bgX, y);
            // Center the label within the background box
            float labelX = bgX + (bgW - labelW) / 2f;
            float labelY = y + (bgH - labelH) / 2f;
            toastLabel.setPosition(labelX, labelY);
            // Optionally set the label's size to its scaled bounds (helps alignment on some platforms)
            toastLabel.setSize(labelW, labelH);
        }
        // Update cast bar based on boss telegraphing
        BossEntity boss = bossManager.getCurrentBoss();
        boolean showCast = false;
        float progress = 0f;
        if (boss instanceof capstone.main.Enemies.TelegraphProvider) {
            capstone.main.Enemies.TelegraphProvider telegraph = (capstone.main.Enemies.TelegraphProvider) boss;
            
            // Check if any boss is telegraphing
            if (telegraph.isTelegraphing()) {
                String skill = telegraph.getTelegraphSkill();
                
                // Show cast bar for specific skills
                if (boss instanceof capstone.main.Enemies.Greed) {
                    capstone.main.Enemies.Greed greed = (capstone.main.Enemies.Greed) boss;
                    if ("CONE BARRAGE".equals(skill)) {
                        showCast = true;
                        progress = greed.getCastProgress();
                    }
                } else if (boss instanceof capstone.main.Enemies.Discaya) {
                    capstone.main.Enemies.Discaya discaya = (capstone.main.Enemies.Discaya) boss;
                    // Show cast bar for all Discaya skills
                    if ("SHADOW DASH".equals(skill) || "POISON CLOUD".equals(skill) || "CORRUPTION PULSE".equals(skill)) {
                        showCast = true;
                        progress = discaya.getCastProgress();
                    }
                }
            }
        }
        castBar.setVisible(showCast);
        if (showCast) {
            castBar.setValue(progress);
            // Position under top boss bar
            float screenWidth = uiStage.getViewport().getWorldWidth();
            float barY = uiStage.getViewport().getWorldHeight() - 60f;
            castBar.setPosition((screenWidth - castBar.getWidth()) / 2f, barY);
        } else {
            castBar.setValue(0f);
        }

        uiStage.act(delta);
        uiStage.draw();
    }

    public void resize(int width, int height) {
        if (uiStage.getViewport() != null) uiStage.getViewport().update(width, height, true);
    }

    public Stage getStage() { return uiStage; }

    public void showVictoryOverlay() {
        if (victoryOverlay != null) {
            victoryOverlay.show();
        }
    }
    
    public void hideVictoryOverlay() {
        if (victoryOverlay != null) {
            victoryOverlay.hide();
        }
    }
    
    public boolean isVictoryOverlayVisible() {
        return victoryOverlay != null && victoryOverlay.isVisible();
    }
    
    public void dispose() {
        if (uiStage != null) uiStage.dispose();
        if (bossActor != null) bossActor.dispose();
        if (victoryOverlay != null) victoryOverlay.dispose();
    }
}
