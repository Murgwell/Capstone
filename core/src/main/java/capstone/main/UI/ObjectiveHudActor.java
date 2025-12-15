package capstone.main.UI;

import capstone.main.Managers.ObjectiveManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;

/**
 * Simple UI widget showing current objective icon and text, e.g., "Survivor: (12/25)"
 */
public class ObjectiveHudActor extends Group {
    private static final float TOP_MARGIN = 60f; // push down to avoid Hearts HUD overlap
    private final Label headerLabel; // "Objectives:" header
    private final Label label;
    private final Image icon;
    private Texture currentIconTex; // keep reference so we can dispose when replaced

    public ObjectiveHudActor(BitmapFont font) {
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);

        // Header label "Objectives:"
        headerLabel = new Label("Objectives:", style);
        headerLabel.setFontScale(1.2f); // Slightly larger
        headerLabel.setAlignment(Align.left);
        headerLabel.setColor(Color.YELLOW); // Make it stand out

        label = new Label("", style);
        label.setFontScale(1.0f);
        label.setAlignment(Align.left);

        icon = new Image();
        addActor(headerLabel);
        addActor(icon);
        addActor(label);

        setVisible(false);
    }

    public void setObjective(ObjectiveManager.Objective obj) {
        if (obj == null) {
            setVisible(false);
            return;
        }
        setVisible(true);
        // Update text (supports multi-line for composite objectives)
        label.setText(obj.getDisplayText());
        label.setWrap(true); // Enable text wrapping for multi-line objectives
        // Update icon
        try {
            String path = obj.getIconPath();
            if (path != null && !path.isEmpty()) {
                // Use internal file handle to avoid native IO issues and cache via AssetManager if needed
                com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.internal(path);
                if (fh.exists()) {
                    // dispose previous icon texture if any to avoid leaks
                    if (currentIconTex != null) { try { currentIconTex.dispose(); } catch (Exception ignored) {} }
                    currentIconTex = new Texture(fh);
                    icon.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(currentIconTex)));
                    icon.setVisible(true);
                } else {
                    icon.setVisible(false);
                }
            } else {
                icon.setVisible(false);
            }
        } catch (Exception ignored) { icon.setVisible(false); }
        // Layout: place header at top, then icon and label below
        float iconSize = 32f; // world units in UI stage coordinates
        icon.setSize(iconSize, iconSize);
        float yTop = getStage() != null ? getStage().getViewport().getWorldHeight() : 0f;

        // Position header label above icon
        headerLabel.invalidateHierarchy();
        float headerY = yTop - TOP_MARGIN;
        headerLabel.setPosition(10f, Math.max(10f, headerY));

        // Position icon below header with more spacing
        float iconY = headerY - headerLabel.getPrefHeight() - 20f; // 20px spacing for visibility
        icon.setPosition(10f, Math.max(10f, iconY));

        // Size label based on pref size, position to right of icon
        label.invalidateHierarchy();
        label.setWidth(400f); // Set width for multi-line text wrapping
        float labelX = icon.getX() + icon.getWidth() + 8f;
        // Align label top with icon top for multi-line objectives
        float labelY = icon.getY() + icon.getHeight() - label.getPrefHeight();
        label.setPosition(labelX, labelY);
    }

    public void updateTextOnly(ObjectiveManager.Objective obj) {
        if (obj == null) return;
        label.setText(obj.getDisplayText());
        label.invalidateHierarchy();
        // Align label top with icon top for multi-line objectives
        float labelY = icon.getY() + icon.getHeight() - label.getPrefHeight();
        label.setPosition(label.getX(), labelY);
    }

    public void dispose() {
        // Note: Textures loaded here are not disposed to avoid disposing shared assets.
    }
}
