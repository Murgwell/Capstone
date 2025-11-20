package capstone.main.menus;

import capstone.main.Corrupted;
import capstone.main.Managers.MusicManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class SettingsScreen implements Screen {
    private final Corrupted game;
    private SpriteBatch batch;
    private Stage stage;
    private Skin skin;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private Sound hoverSound;

    private Texture background;
    private Texture musicUpTex, musicDownTex;
    private Texture fullscreenUpTex, fullscreenDownTex;
    private Texture backUpTex, backDownTex;

    private MusicManager musicManager;
    private boolean isFullscreen = false;

    public SettingsScreen(Corrupted game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280f, 720f, camera);
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("hover.wav"));
        background = new Texture("mainMenuBG.png");

        // Get music manager instance
        musicManager = MusicManager.getInstance();

        musicUpTex = new Texture("ui/Menu/music_button_normal.png");
        musicDownTex = new Texture("ui/Menu/music_button_pressed.png");
        fullscreenUpTex = new Texture("ui/Menu/fullscreen_button_normal.png");
        fullscreenDownTex = new Texture("ui/Menu/fullscreen_button_pressed.png");
        backUpTex = new Texture("ui/Menu/back_button_normal.png");
        backDownTex = new Texture("ui/Menu/back_button_pressed.png");

        Label titleLabel = new Label("SETTINGS", skin);
        titleLabel.setFontScale(3.5f);
        titleLabel.setColor(Color.WHITE);

        Table layout = new Table();
        layout.setFillParent(true);
        layout.center();

        layout.add(titleLabel).padBottom(60f).row();
        layout.add(createToggleButton(musicUpTex, musicDownTex, () -> {
            musicManager.setMusicEnabled(!musicManager.isMusicEnabled());
        })).pad(12).width(200).height(60).row();

        layout.add(createToggleButton(fullscreenUpTex, fullscreenDownTex, () -> {
            isFullscreen = !isFullscreen;
            if (isFullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            else Gdx.graphics.setWindowedMode(1280, 720);
        })).pad(12).width(200).height(60).row();

        layout.add(createImageButton(backUpTex, backDownTex, () -> game.setScreen(new MainMenuScreen(game)))
        ).pad(12).width(200).height(60).row();

        stage.addActor(layout);
    }

    private ImageButton createToggleButton(Texture upTex, Texture downTex, Runnable onToggle) {
        TextureRegion up = new TextureRegion(upTex);
        TextureRegion down = new TextureRegion(downTex);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(up);
        style.down = new TextureRegionDrawable(down);

        ImageButton button = new ImageButton(style);
        button.setSize(300, 80);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onToggle.run();
                hoverSound.play(0.4f);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.addAction(Actions.scaleTo(1f, 1f, 0.1f));
            }
        });

        return button;
    }

    private ImageButton createImageButton(Texture upTex, Texture downTex, Runnable onClick) {
        TextureRegion up = new TextureRegion(upTex);
        TextureRegion down = new TextureRegion(downTex);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(up);
        style.down = new TextureRegionDrawable(down);

        ImageButton button = new ImageButton(style);
        button.setSize(300, 80);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.run();
                hoverSound.play(0.4f);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.addAction(Actions.scaleTo(1f, 1f, 0.1f));
            }
        });

        return button;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0, 0, 1280, 720);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        batch.dispose();
        background.dispose();
        hoverSound.dispose();
        musicUpTex.dispose();
        musicDownTex.dispose();
        fullscreenUpTex.dispose();
        fullscreenDownTex.dispose();
        backUpTex.dispose();
        backDownTex.dispose();
    }
}
