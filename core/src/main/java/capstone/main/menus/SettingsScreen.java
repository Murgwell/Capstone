package capstone.main.menus;

import capstone.main.Corrupted;
import capstone.main.Managers.MusicManager;
import capstone.main.Managers.SoundManager;
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
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
    private Texture scrollbarTex, scrollboxTex;

    private MusicManager musicManager;
    private SoundManager soundManager;

    // UI elements that need updating
    private Label musicVolumeLabel;
    private Label soundVolumeLabel;

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
        // Match MainMenuScreen viewport setup
        camera = new OrthographicCamera(UI_W, UI_H);
        camera.position.set(UI_W / 2f, UI_H / 2f, 0f);
        camera.update();
        viewport = new FitViewport(UI_W, UI_H, camera);
        stage = new Stage(viewport, batch);

        // Route input: Stage first, then global keys
        inputMux = new InputMultiplexer(stage, (InputProcessorAdapter) (keycode -> {
            if (keycode == Input.Keys.F11) {
                boolean newFs = !VideoSettings.isFullscreen();
                VideoSettings.setFullscreen(newFs);
                VideoSettings.apply();
                showFloatingText(VideoSettings.isFullscreen() ? "Fullscreen" : "Windowed", 
                    UI_W / 2f, UI_H / 2f + 100f);
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
        soundManager = SoundManager.getInstance();

        // UI textures
        musicUpTex = new Texture("UI/Menu/music_button_normal.png");
        musicDownTex = new Texture("UI/Menu/music_button_pressed.png");
        fullscreenUpTex = new Texture("UI/Menu/fullscreen_button_normal.png");
        fullscreenDownTex = new Texture("UI/Menu/fullscreen_button_pressed.png");
        backUpTex = new Texture("UI/Menu/back_button_normal.png");
        backDownTex = new Texture("UI/Menu/back_button_pressed.png");
        
        // Slider textures
        scrollbarTex = new Texture("Textures/UI/Menu/Scrollbar.png");
        scrollboxTex = new Texture("Textures/UI/Menu/Scrollbar_Scrollbox.png");

        // Create custom slider style - fix the knobBefore issue
        SliderStyle sliderStyle = new SliderStyle();
        sliderStyle.background = new TextureRegionDrawable(new TextureRegion(scrollbarTex));
        sliderStyle.knob = new TextureRegionDrawable(new TextureRegion(scrollboxTex));
        // Don't set knobBefore to avoid the duplicate scrollbar issue
        sliderStyle.knobBefore = null;

        // Title
        Label titleLabel = new Label("SETTINGS", skin);
        titleLabel.setFontScale(3.5f);
        titleLabel.setColor(Color.WHITE);

        // Layout - restore original simple layout
        Table layout = new Table();
        layout.setFillParent(true);
        layout.center();

        layout.add(titleLabel).padBottom(60f).row();

        // Music toggle button
        ImageButton musicButton = createToggleButton(musicUpTex, musicDownTex, () -> {
            musicManager.setMusicEnabled(!musicManager.isMusicEnabled());
            String text = musicManager.isMusicEnabled() ? "Music is ON" : "Music is OFF";
            Color color = musicManager.isMusicEnabled() ? Color.GREEN : Color.RED;
            showFloatingText(text, UI_W / 2f, UI_H / 2f + 100f, color);
        });
        layout.add(musicButton).pad(12f).width(200f).height(60f).row();

        // Music volume slider
        Table musicVolumeRow = new Table();
        Label musicVolText = new Label("Music Volume:", skin);
        musicVolText.setFontScale(1.2f);
        musicVolText.setColor(Color.WHITE);
        musicVolumeRow.add(musicVolText).padRight(15f);
        
        Slider musicSlider = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        musicSlider.setValue(musicManager.getVolume());
        musicVolumeLabel = new Label(String.format("%.0f%%", musicManager.getVolume() * 100), skin);
        musicVolumeLabel.setFontScale(1.2f);
        musicVolumeLabel.setColor(Color.WHITE);
        
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = musicSlider.getValue();
                musicManager.setVolume(value);
                musicVolumeLabel.setText(String.format("%.0f%%", value * 100));
            }
        });
        
        musicVolumeRow.add(musicSlider).width(300f).padRight(15f);
        musicVolumeRow.add(musicVolumeLabel).width(60f);
        layout.add(musicVolumeRow).padBottom(20f).row();

        // Sound volume slider
        Table soundVolumeRow = new Table();
        Label soundVolText = new Label("Sound Volume:", skin);
        soundVolText.setFontScale(1.2f);
        soundVolText.setColor(Color.WHITE);
        soundVolumeRow.add(soundVolText).padRight(15f);
        
        Slider soundSlider = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        soundSlider.setValue(soundManager.getVolume());
        soundVolumeLabel = new Label(String.format("%.0f%%", soundManager.getVolume() * 100), skin);
        soundVolumeLabel.setFontScale(1.2f);
        soundVolumeLabel.setColor(Color.WHITE);
        
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = soundSlider.getValue();
                soundManager.setVolume(value);
                soundVolumeLabel.setText(String.format("%.0f%%", value * 100));
            }
        });
        
        soundVolumeRow.add(soundSlider).width(300f).padRight(15f);
        soundVolumeRow.add(soundVolumeLabel).width(60f);
        layout.add(soundVolumeRow).padBottom(20f).row();

        // Fullscreen toggle button
        ImageButton fullscreenButton = createToggleButton(fullscreenUpTex, fullscreenDownTex, () -> {
            boolean newFs = !VideoSettings.isFullscreen();
            VideoSettings.setFullscreen(newFs);
            VideoSettings.apply();
            Gdx.graphics.setResizable(!newFs);
            String text = newFs ? "Fullscreen" : "Windowed";
            showFloatingText(text, UI_W / 2f, UI_H / 2f + 100f);
        });
        layout.add(fullscreenButton).pad(12f).width(200f).height(60f).row();

        // Back button
        layout.add(createImageButton(backUpTex, backDownTex, () -> {
            VideoSettings.apply(); // keep mode consistent when leaving
            game.setScreen(new MainMenuScreen(game));
        })).pad(12f).width(200f).height(60f).row();

        stage.addActor(layout);

        // If you always want a fixed window size in windowed mode:
        Gdx.graphics.setResizable(!VideoSettings.isFullscreen());
    }

    private void showFloatingText(String text, float x, float y) {
        showFloatingText(text, x, y, Color.WHITE);
    }

    private void showFloatingText(String text, float x, float y, Color color) {
        Label floatingLabel = new Label(text, skin);
        floatingLabel.setFontScale(1.5f);
        floatingLabel.setColor(color);
        floatingLabel.setPosition(x - floatingLabel.getWidth() / 2f, y);
        floatingLabel.getColor().a = 1f;
        
        // Float up and fade out
        floatingLabel.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveBy(0f, 50f, 2f), // Move up 50 pixels over 2 seconds
                Actions.fadeOut(2f) // Fade out over 2 seconds
            ),
            Actions.removeActor() // Remove from stage when done
        ));
        
        stage.addActor(floatingLabel);
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
            showFloatingText(newFs ? "Fullscreen" : "Windowed", UI_W / 2f, UI_H / 2f + 100f);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
            camera.update();
        }
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
        if (scrollbarTex != null) scrollbarTex.dispose();
        if (scrollboxTex != null) scrollboxTex.dispose();

        stage = null;
        skin = null;
        batch = null;
        background = null;
        hoverSound = null;
        musicUpTex = musicDownTex = null;
        fullscreenUpTex = fullscreenDownTex = null;
        backUpTex = backDownTex = null;
        scrollbarTex = scrollboxTex = null;
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
