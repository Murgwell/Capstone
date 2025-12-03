
package capstone.main.menus;

import capstone.main.Corrupted;
import capstone.main.Managers.MusicManager;
import capstone.main.Managers.VideoSettings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Settings screen: toggles music and fullscreen, returns to main menu.
 * Uses VideoSettings to persist and apply window mode consistently.
 */
public class SettingsScreen implements Screen {
    private static final float UI_W = 1280f;
    private static final float UI_H = 720f;

    private final Corrupted game;

    private SpriteBatch batch;
    private Stage stage;
    private Skin skin;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private Sound hoverSound;

    // Textures (owned by this screen)
    private Texture background;
    private Texture musicUpTex, musicDownTex;
    private Texture fullscreenUpTex, fullscreenDownTex;
    private Texture backUpTex, backDownTex;

    private MusicManager musicManager;

    // Input multiplexing so Stage + global keys (F11) both work
    private InputMultiplexer inputMux;

    // Small cooldown to avoid hover sound spam
    private long lastHoverMs = 0L;
    private static final long HOVER_COOLDOWN_MS = 120L;

    public SettingsScreen(Corrupted game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Honor last saved mode (windowed/fullscreen)
        VideoSettings.apply();

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(UI_W, UI_H, camera);
        stage = new Stage(viewport, batch);

        // Route input: Stage first, then global keys
        inputMux = new InputMultiplexer(stage, (InputProcessorAdapter) (keycode -> {
            if (keycode == Input.Keys.F11) {
                boolean newFs = !VideoSettings.isFullscreen();
                VideoSettings.setFullscreen(newFs);
                VideoSettings.apply();
                return true;
            }
            return false;
        }));
        Gdx.input.setInputProcessor(inputMux);

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/hover.wav"));
        background = new Texture("mainMenuBG.png");

        // Managers
        musicManager = MusicManager.getInstance();

        // UI textures
        musicUpTex = new Texture("ui/Menu/music_button_normal.png");
        musicDownTex = new Texture("ui/Menu/music_button_pressed.png");
        fullscreenUpTex = new Texture("ui/Menu/fullscreen_button_normal.png");
        fullscreenDownTex = new Texture("ui/Menu/fullscreen_button_pressed.png");
        backUpTex = new Texture("ui/Menu/back_button_normal.png");
        backDownTex = new Texture("ui/Menu/back_button_pressed.png");

        // Title
        Label titleLabel = new Label("SETTINGS", skin);
        titleLabel.setFontScale(3.5f);
        titleLabel.setColor(Color.WHITE);

        // Layout
        Table layout = new Table();
        layout.setFillParent(true);
        layout.center();

        layout.add(titleLabel).padBottom(60f).row();

        // Music toggle
        layout.add(createToggleButton(musicUpTex, musicDownTex, () -> {
            musicManager.setMusicEnabled(!musicManager.isMusicEnabled());
            // Optionally: musicManager.ensurePlaying() / stop() depending on state
        })).pad(12f).width(200f).height(60f).row();

        // Fullscreen toggle via VideoSettings
        layout.add(createToggleButton(fullscreenUpTex, fullscreenDownTex, () -> {
            boolean newFs = !VideoSettings.isFullscreen();
            VideoSettings.setFullscreen(newFs); // persist preference
            VideoSettings.apply();              // apply immediately
            // Optional: lock resizing while fullscreen
            Gdx.graphics.setResizable(!newFs);
        })).pad(12f).width(200f).height(60f).row();

        // Back button
        layout.add(createImageButton(backUpTex, backDownTex, () -> {
            VideoSettings.apply(); // keep mode consistent when leaving
            game.setScreen(new MainMenuScreen(game));
        })).pad(12f).width(200f).height(60f).row();

        stage.addActor(layout);

        // If you always want a fixed window size in windowed mode:
        Gdx.graphics.setResizable(!VideoSettings.isFullscreen());
    }

    private ImageButton createToggleButton(Texture upTex, Texture downTex, Runnable onToggle) {
        TextureRegion up = new TextureRegion(upTex);
        TextureRegion down = new TextureRegion(downTex);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(up);
        style.down = new TextureRegionDrawable(down);

        ImageButton button = new ImageButton(style);
        button.setSize(300f, 80f);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onToggle.run();
                // Play click sound softly
                if (hoverSound != null) hoverSound.play(0.4f);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.clearActions(); // prevent stacking
                button.addAction(Actions.parallel(
                    Actions.scaleTo(1.05f, 1.05f, 0.10f),
                    Actions.color(new Color(1f, 1f, 1f, 0.95f), 0.10f)
                ));
                long now = TimeUtils.millis();
                if (hoverSound != null && now - lastHoverMs > HOVER_COOLDOWN_MS) {
                    hoverSound.play(0.25f);
                    lastHoverMs = now;
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.clearActions();
                button.addAction(Actions.parallel(
                    Actions.scaleTo(1f, 1f, 0.10f),
                    Actions.color(Color.WHITE, 0.10f)
                ));
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
        button.setSize(300f, 80f);

        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onClick.run();
                if (hoverSound != null) hoverSound.play(0.4f);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.clearActions();
                button.addAction(Actions.parallel(
                    Actions.scaleTo(1.05f, 1.05f, 0.10f),
                    Actions.color(new Color(1f, 1f, 1f, 0.95f), 0.10f)
                ));
                long now = TimeUtils.millis();
                if (hoverSound != null && now - lastHoverMs > HOVER_COOLDOWN_MS) {
                    hoverSound.play(0.25f);
                    lastHoverMs = now;
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.clearActions();
                button.addAction(Actions.parallel(
                    Actions.scaleTo(1f, 1f, 0.10f),
                    Actions.color(Color.WHITE, 0.10f)
                ));
            }
        });

        return button;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Background (fit to UI viewport)
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(background, 0f, 0f, UI_W, UI_H);
        batch.end();

        stage.act(delta);
        stage.draw();

        // Global F11 toggle (in case input mux misses it)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            boolean newFs = !VideoSettings.isFullscreen();
            VideoSettings.setFullscreen(newFs);
            VideoSettings.apply();
            Gdx.graphics.setResizable(!newFs);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        // Optional: clear input when leaving
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        // Dispose owned resources safely
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (batch != null) batch.dispose();
        if (background != null) background.dispose();
        if (hoverSound != null) hoverSound.dispose();

        if (musicUpTex != null) musicUpTex.dispose();
        if (musicDownTex != null) musicDownTex.dispose();
        if (fullscreenUpTex != null) fullscreenUpTex.dispose();
        if (fullscreenDownTex != null) fullscreenDownTex.dispose();
        if (backUpTex != null) backUpTex.dispose();
        if (backDownTex != null) backDownTex.dispose();

        stage = null;
        skin = null;
        batch = null;
        background = null;
        hoverSound = null;
        musicUpTex = musicDownTex = null;
        fullscreenUpTex = fullscreenDownTex = null;
        backUpTex = backDownTex = null;
    }

    // --- Small adapter so we can inline a one-method InputProcessor in the multiplexer ---
    @FunctionalInterface
    private interface InputProcessorAdapter extends com.badlogic.gdx.InputProcessor {
        boolean handleKeyDown(int keycode);

        @Override
        default boolean keyDown(int keycode) {
            return handleKeyDown(keycode);
        }

        @Override
        default boolean keyUp(int keycode) {
            return false;
        }

        @Override
        default boolean keyTyped(char character) {
            return false;
        }

        @Override
        default boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        default boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        default boolean touchDragged(int screenX, int screenY, int pointer) {
            return false;
        }

        @Override
        default boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        default boolean scrolled(float amountX, float amountY) {
            return false;
        }

        @Override
        default boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            return false;
        }
    }
}
