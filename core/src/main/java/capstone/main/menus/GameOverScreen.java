
package capstone.main.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import capstone.main.Corrupted;

public class GameOverScreen implements Screen {
    private final Corrupted game;
    private final int lastSelectedCharacterIndex;

    private Stage stage;
    private Skin skin;

    public GameOverScreen(Corrupted game, int lastSelectedCharacterIndex) {
        this.game = game;
        this.lastSelectedCharacterIndex = lastSelectedCharacterIndex;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table root = new Table();
        root.setFillParent(true);

        Label title = new Label("GAME OVER", skin);
        title.setFontScale(1.2f);

        TextButton retry = new TextButton("Retry", skin);
        retry.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new Game(game, lastSelectedCharacterIndex));
                dispose();
            }
        });

        TextButton menu = new TextButton("Main Menu", skin);
        menu.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        root.add(title).padBottom(30f).row();
        root.add(retry).width(160f).height(40f).padBottom(15f).row();
        root.add(menu).width(160f).height(40f);

        stage.addActor(root);
        Gdx.input.setInputProcessor(stage);
    }

    @Override public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { if (stage != null) stage.dispose(); if (skin != null) skin.dispose(); }
}
