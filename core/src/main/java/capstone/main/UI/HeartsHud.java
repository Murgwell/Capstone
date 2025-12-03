
package capstone.main.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import capstone.main.Characters.AbstractPlayer;

public class HeartsHud {
    private final Stage stage;
    private final AbstractPlayer player;
    private final Image[] hearts;
    private final Texture heartFullTex;
    private final Texture heartHalfTex;
    private final Texture heartEmptyTex;

    public HeartsHud(Viewport uiViewport, SpriteBatch batch, AbstractPlayer player) {
        this.stage = new Stage(uiViewport, batch);
        this.player = player;

        heartFullTex = new Texture(Gdx.files.internal("UI/Hud/Heart_Full.png"));
        heartHalfTex = new Texture(Gdx.files.internal("UI/Hud/Heart_Half.png"));
        heartEmptyTex = new Texture(Gdx.files.internal("UI/Hud/Heart_Empty.png"));

        int maxHearts = (int) Math.ceil(player.getMaxHp() / 10f);
        hearts = new Image[maxHearts];

        Table root = new Table();
        root.setFillParent(true);
        root.top().left();

        for (int i = 0; i < maxHearts; i++) {
            hearts[i] = new Image(heartFullTex);
            root.add(hearts[i]).pad(6f).size(32f, 32f); // scaled up
        }

        stage.addActor(root);
    }

    public void update(float delta) {
        int hp = (int) player.getHp();
        for (int i = 0; i < hearts.length; i++) {
            int heartHp = hp - (i * 10);
            if (heartHp >= 10) hearts[i].setDrawable(new TextureRegionDrawable(new TextureRegion(heartFullTex)));
            else if (heartHp >= 5) hearts[i].setDrawable(new TextureRegionDrawable(new TextureRegion(heartHalfTex)));
            else hearts[i].setDrawable(new TextureRegionDrawable(new TextureRegion(heartEmptyTex)));
        }
        stage.act(delta);
    }

    public void draw() {
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        heartFullTex.dispose();
        heartHalfTex.dispose();
        heartEmptyTex.dispose();
    }
}
