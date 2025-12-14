
package capstone.main.UI;

import capstone.main.Characters.AbstractPlayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import com.badlogic.gdx.utils.viewport.Viewport;
import capstone.main.Characters.AbstractPlayer;

/**
 * Heads-up display for player HP.
 * Supports two modes:
 *  - Hearts (full/half/empty)
 *  - HP bar (icon + frame + fill)
 */
public class Hud {

    public enum Mode { HEARTS, BAR }

    private final AbstractPlayer player;
    private final Stage stage;
    private final SpriteBatch batch;
    private final Viewport uiViewport;

    private final Mode mode;

    // --- Hearts textures ---
    private Texture heartFullTex, heartHalfTex, heartEmptyTex;

    // --- Bar textures ---
    private Texture hpIconTex, hpFrameTex, barFill1x1;

    // --- Scene2D ---
    private Table root;
    private ProgressBar hpBar;
    private Image hpIcon;

    // Hearts row
    private Image[] heartImages;
    private int heartsCount;
    private int HEART_UNIT = 2; // each heart represents 2 HP (full=2, half=1). Change if your design differs.

    /**
     * @param player      player to read HP from
     * @param uiViewport  shared ScreenViewport from your Game UI
     * @param batch       shared SpriteBatch from Game
     * @param mode        HEARTS or BAR
     */
    public Hud(AbstractPlayer player, Viewport uiViewport, SpriteBatch batch, Mode mode) {
        this.player = player;
        this.uiViewport = uiViewport;
        this.batch = batch;
        this.mode = mode;

        this.stage = new Stage(uiViewport, batch);
        loadAssets();
        buildUI();
    }

    private void loadAssets() {
        // NOTE: adjust paths if your assets are in a different folder
        // HEARTS
        heartFullTex  = new Texture("UI/HP/Heart_Full.png");
        heartHalfTex  = new Texture("UI/HP/Heart_Half.png");
        heartEmptyTex = new Texture("UI/HP/Heart_Empty.png");

        // BAR
        hpIconTex  = new Texture("UI/HP/HP.png");       // small HP icon
        hpFrameTex = new Texture("UI/HP/HP-Bar.png");   // bar frame

        // Create 1×1 red fill texture for the progress bar
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.RED);
        px.fill();
        barFill1x1 = new Texture(px);
        px.dispose();

        // Optional: force crisp pixel art
        heartFullTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        heartHalfTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        heartEmptyTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        hpIconTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        hpFrameTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        barFill1x1.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private void buildUI() {
        root = new Table();
        root.setFillParent(true);

        // Top-left corner HUD
        float pad = 12f;
        root.top().left().pad(pad);

        if (mode == Mode.HEARTS) {
            buildHeartsRow();
        } else {
            buildHpBar();
        }

        stage.addActor(root);
    }

    // ------- HEARTS UI -------
    private void buildHeartsRow() {
        int maxHp = getMaxHp();
        heartsCount = (int) Math.ceil(maxHp / (float) HEART_UNIT);
        heartImages = new Image[heartsCount];

        Table heartsTable = new Table();
        float heartW = 32f; // adjust to your image
        float heartH = 32f;

        for (int i = 0; i < heartsCount; i++) {
            Image img = new Image(new TextureRegionDrawable(new TextureRegion(heartEmptyTex)));
            img.setSize(heartW, heartH);
            heartImages[i] = img;
            heartsTable.add(img).size(heartW, heartH).padRight(4f);
        }

        root.add(heartsTable).row();
        updateHearts(); // initialize visuals
    }

    private void updateHearts() {
        int maxHp = getMaxHp();
        int curHp = getHp();

        // Recompute hearts if max changed
        int newHearts = (int) Math.ceil(maxHp / (float) HEART_UNIT);
        if (newHearts != heartsCount) {
            root.clearChildren();
            buildHeartsRow();
            return;
        }

        // Fill hearts from left to right
        int hpRemaining = curHp;
        for (int i = 0; i < heartsCount; i++) {
            if (hpRemaining >= HEART_UNIT) {
                heartImages[i].setDrawable(new TextureRegionDrawable(new TextureRegion(heartFullTex)));
                hpRemaining -= HEART_UNIT;
            } else if (hpRemaining > 0) {
                heartImages[i].setDrawable(new TextureRegionDrawable(new TextureRegion(heartHalfTex)));
                hpRemaining = 0;
            } else {
                heartImages[i].setDrawable(new TextureRegionDrawable(new TextureRegion(heartEmptyTex)));
            }
        }
    }

    // ------- BAR UI -------
    private void buildHpBar() {
        // Icon
        hpIcon = new Image(new TextureRegionDrawable(new TextureRegion(hpIconTex)));
        float iconW = 24f, iconH = 24f;
        hpIcon.setSize(iconW, iconH);

        // Build progress bar style
        TextureRegionDrawable barBg = new TextureRegionDrawable(new TextureRegion(hpFrameTex));
        TextureRegionDrawable barKnob = new TextureRegionDrawable(new TextureRegion(barFill1x1));
        TextureRegionDrawable barKnobBefore = new TextureRegionDrawable(new TextureRegion(barFill1x1));

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = barBg;        // the frame image
        style.knob = barKnob;            // tiny "knob" (we'll make it 0 width)
        style.knobBefore = barKnobBefore;// the fill (left side)

        hpBar = new ProgressBar(0f, getMaxHp(), 1f, false, style);
        hpBar.setSize(180f, 16f);        // adjust to match your frame proportion
        hpBar.setValue(getHp());
        hpBar.setAnimateDuration(0.08f); // smooth fill

        Table barRow = new Table();
        barRow.add(hpIcon).size(iconW, iconH).padRight(8f);
        barRow.add(hpBar).size(180f, 16f);

        root.add(barRow).row();
    }

    // ------- Public API -------
    public void update(float delta) {
        if (mode == Mode.HEARTS) {
            updateHearts();
        } else {
            hpBar.setRange(0f, getMaxHp());
            hpBar.setValue(getHp());
        }
        stage.act(delta);
    }

    public void draw() {
        stage.draw();
    }

    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        if (heartFullTex != null) heartFullTex.dispose();
        if (heartHalfTex != null) heartHalfTex.dispose();
        if (heartEmptyTex != null) heartEmptyTex.dispose();
        if (hpIconTex != null) hpIconTex.dispose();
        if (hpFrameTex != null) hpFrameTex.dispose();
        if (barFill1x1 != null) barFill1x1.dispose();
    }

    // --- Utility: AbstractPlayer API integration ---
    private int getHp() {
        return player.getHp();
    }

    private int getMaxHp() {
        return player.getMaxHp();
    }

    /** Optional: change how many HP each heart represents at runtime. */
    public void setHeartUnit(int unit) {
        this.HEART_UNIT = Math.max(1, unit);
        buildHeartsRow();
    }
}
