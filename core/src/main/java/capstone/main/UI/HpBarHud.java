
package capstone.main.UI;

import java.util.function.IntSupplier;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Heads-up display for player HP using a bar (icon + frame + fill).
 * Assets:
 *  - UI/HP/HP.png      (icon)
 *  - UI/HP/HP-Bar.png  (frame/background)
 */
public class HpBarHud {

    private final Stage stage;
    private final Viewport uiViewport;

    private Texture hpIconTex;
    private Texture hpFrameTex;
    private Texture barFill1x1;

    private Image hpIcon;
    private ProgressBar hpBar;
    private Table root;

    // Health suppliers (flexible: plug any getters)
    private final IntSupplier currentHpSupplier;
    private final IntSupplier maxHpSupplier;

    // Sizes (tweak to match your art)
    private float iconW = 24f, iconH = 24f;
    private float barW  = 180f, barH  = 16f;

    /**
     * Prefer this constructor if your player has non-standard method names.
     */
    public HpBarHud(Viewport uiViewport, SpriteBatch sharedBatch,
                    IntSupplier currentHpSupplier, IntSupplier maxHpSupplier) {
        this.uiViewport       = uiViewport;
        this.stage            = new Stage(uiViewport, sharedBatch);
        this.currentHpSupplier = currentHpSupplier;
        this.maxHpSupplier     = maxHpSupplier;
        loadAssets();
        buildUI();
    }

    /**
     * Convenience constructor for common names (player.getHp / player.getMaxHp).
     * Replace the getters inside if your methods differ.
     */
    public HpBarHud(Viewport uiViewport, SpriteBatch sharedBatch, Object player) {
        this(uiViewport, sharedBatch,
            () -> tryGetInt(player, "getHp", 10),
            () -> tryGetInt(player, "getMaxHp", 10)
        );
    }

    // --- reflection helper to avoid tight coupling ---
    private static int tryGetInt(Object obj, String method, int fallback) {
        try {
            return (int) obj.getClass().getMethod(method).invoke(obj);
        } catch (Throwable t) {
            Gdx.app.log("HpBarHud", "Missing " + method + "(), defaulting to " + fallback);
            return fallback;
        }
    }

    private void loadAssets() {
        hpIconTex  = new Texture("UI/Hud/HP.png");
        hpFrameTex = new Texture("UI/Hud/HP-Bar.png");

        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.RED);
        px.fill();
        barFill1x1 = new Texture(px);
        px.dispose();

        hpIconTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        hpFrameTex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        barFill1x1.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private void buildUI() {
        root = new Table();
        root.setFillParent(true);
        root.top().left().pad(12f);  // position top-left; change to bottom().left() if you prefer

        // Icon
        hpIcon = new Image(new TextureRegionDrawable(new TextureRegion(hpIconTex)));
        hpIcon.setSize(iconW, iconH);

        // ProgressBar style: frame as background + red fill as knobBefore
        TextureRegionDrawable bg         = new TextureRegionDrawable(new TextureRegion(hpFrameTex));
        TextureRegionDrawable knob       = new TextureRegionDrawable(new TextureRegion(barFill1x1)); // minimal knob
        TextureRegionDrawable knobBefore = new TextureRegionDrawable(new TextureRegion(barFill1x1)); // the fill

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        style.background = bg;
        style.knob = knob;
        style.knobBefore = knobBefore;

        hpBar = new ProgressBar(0f, maxHpSupplier.getAsInt(), 1f, false, style);
        hpBar.setSize(barW, barH);
        hpBar.setValue(currentHpSupplier.getAsInt());
        hpBar.setAnimateDuration(0.08f);

        Table row = new Table();
        row.add(hpIcon).size(iconW, iconH).padRight(8f);
        row.add(hpBar).size(barW, barH);

        root.add(row).row();
        stage.addActor(root);
    }

    /** Update HP value and animate fill. Call each frame. */
    public void update(float delta) {
        hpBar.setRange(0f, maxHpSupplier.getAsInt());
        hpBar.setValue(currentHpSupplier.getAsInt());
        stage.act(delta);
    }

    /** Draw the HUD. Call after world rendering. */
    public void draw() {
        stage.draw();
    }

    /** Forward window resizes to keep the HUD anchored. */
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    /** Free resources owned by this HUD. */
    public void dispose() {
        stage.dispose();
        if (hpIconTex  != null) hpIconTex.dispose();
        if (hpFrameTex != null) hpFrameTex.dispose();
        if (barFill1x1 != null) barFill1x1.dispose();
    }

    // Optional tweaks:
    public void setIconSize(float w, float h) { this.iconW = w; this.iconH = h; hpIcon.setSize(w, h); }
    public void setBarSize(float w, float h)  { this.barW = w;  this.barH = h;  hpBar.setSize(w, h); }
}
