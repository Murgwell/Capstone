
package capstone.main.menus;

import capstone.main.Corrupted;
import capstone.main.Managers.MusicManager;
import capstone.main.Managers.VideoSettings;

//import image
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.graphics.Texture;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {
    private static final float WORLD_WIDTH  = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    private final Corrupted game;

    private SpriteBatch batch;
    private Texture background;

    private OrthographicCamera camera;
    private FitViewport viewport;

    private Stage stage;    // world stage (background, etc.)
    private Stage uiStage;  // UI stage (title, buttons)
    private Skin skin;


    private Sound hoverSound;

    // Glitch effect state
    private float glitchTimer    = 0f;
    private float glitchInterval = 0.2f;

    public MainMenuScreen(Corrupted game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Apply the user's preferred video mode (fullscreen/windowed)
        VideoSettings.apply();

        // Camera + world viewport
        if (camera == null) {
            camera = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        }
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        if (viewport == null) {
            viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        }

        // Do NOT force windowed here; honor VideoSettings
        boolean isFullscreen = VideoSettings.isFullscreen();
        Gdx.graphics.setResizable(!isFullscreen); // optional: lock resizing only in windowed mode

        // Resources
        if (batch == null)      batch = new SpriteBatch();
        if (background == null) background = new Texture("mainMenuBG.png");
        if (skin == null)       skin = new Skin(Gdx.files.internal("uiskin.json"));
        if (hoverSound == null) hoverSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/hover.wav"));

        // Music
        MusicManager.getInstance().ensurePlaying();

        // UI stage (ScreenViewport makes UI scale to the window/screen)
        if (uiStage == null) {
            uiStage = new Stage(new ScreenViewport());
        } else {
            uiStage.clear();
        }
        Gdx.input.setInputProcessor(uiStage);

        // World stage
        if (stage == null) {
            stage = new Stage(viewport, batch);
        } else {
            stage.clear();
        }

        // === Title ===
        Texture titleTexture = new Texture("Corrupted.png");
        Image titleImage = new Image(titleTexture);

        titleImage.setSize(400, 300); // adjust as needed
        titleImage.setPosition(stage.getWidth() / 2f - titleImage.getWidth() / 2f,
            stage.getHeight() * 0.55f);

        stage.addActor(titleImage);




        // Use UI viewport world size for stable positioning across modes
        float uiW = uiStage.getViewport().getWorldWidth();
        float uiH = uiStage.getViewport().getWorldHeight();


        // === Buttons ===
        Texture playNormal      = new Texture("ui/Menu/Main Menu/play_normal.png");
        Texture playPressed     = new Texture("ui/Menu/Main Menu/play_pressed.png");
        Texture settingsNormal  = new Texture("ui/Menu/Main Menu/settings_normal.png");
        Texture settingsPressed = new Texture("ui/Menu/Main Menu/settings_pressed.png");
        Texture exitNormal      = new Texture("ui/Menu/Main Menu/quit_normal.png");
        Texture exitPressed     = new Texture("ui/Menu/Main Menu/quit_pressed.png");

        ImageButton.ImageButtonStyle playStyle = new ImageButton.ImageButtonStyle();
        playStyle.up   = new TextureRegionDrawable(new TextureRegion(playNormal));
        playStyle.down = new TextureRegionDrawable(new TextureRegion(playPressed));
        ImageButton playButton = new ImageButton(playStyle);

        ImageButton.ImageButtonStyle settingsStyle = new ImageButton.ImageButtonStyle();
        settingsStyle.up   = new TextureRegionDrawable(new TextureRegion(settingsNormal));
        settingsStyle.down = new TextureRegionDrawable(new TextureRegion(settingsPressed));
        ImageButton settingsButton = new ImageButton(settingsStyle);

        ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
        exitStyle.up   = new TextureRegionDrawable(new TextureRegion(exitNormal));
        exitStyle.down = new TextureRegionDrawable(new TextureRegion(exitPressed));
        ImageButton exitButton = new ImageButton(exitStyle);

        addHoverEffect(playButton);
        addHoverEffect(settingsButton);
        addHoverEffect(exitButton);

        // Layout
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.padTop(140f);

        table.add(playButton).pad(12f).width(200f).height(60f);
        table.row();
        table.add(settingsButton).pad(12f).width(200f).height(60f);
        table.row();
        table.add(exitButton).pad(12f).width(200f).height(60f);

        uiStage.addActor(table);

        // Actions
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new CharacterSelectionScreen(game));
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SettingsScreen(game));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // Switch back to menu music
        MusicManager musicManager = MusicManager.getInstance();
        musicManager.switchMusic("Music/bg_music.mp3");
    }

    private void addHoverEffect(final ImageButton button) {
        final Color originalColor   = new Color(button.getColor());
        final float originalScaleX  = button.getScaleX();
        final float originalScaleY  = button.getScaleY();

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.clearActions(); // prevents animation stacking
                button.addAction(Actions.parallel(
                    Actions.scaleTo(1.15f, 1.15f, 0.08f),
                    Actions.color(new Color(1f, 1f, 1f, 0.9f), 0.08f) // soft glow
                ));
                if (hoverSound != null) hoverSound.play(0.4f);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.clearActions();
                button.addAction(Actions.parallel(
                    Actions.scaleTo(originalScaleX, originalScaleY, 0.08f),
                    Actions.color(originalColor, 0.08f)
                ));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                button.addAction(Actions.sequence(
                    Actions.scaleTo(0.92f, 0.92f, 0.05f),
                    Actions.scaleTo(1.15f, 1.15f, 0.05f),
                    Actions.scaleTo(originalScaleX, originalScaleY, 0.05f)
                ));
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (camera != null && batch != null) {
            camera.update();
            batch.setProjectionMatrix(camera.combined);

            if (background != null) {
                batch.begin();
                batch.draw(background, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
                batch.end();
            }
        }


        // Toggle fullscreen with F11
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            boolean newFs = !VideoSettings.isFullscreen();
            capstone.main.Managers.VideoSettings.setFullscreen(newFs);
            capstone.main.Managers.VideoSettings.apply();
        }

        // Draw stages
        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
        if (uiStage != null) {
            uiStage.act(delta);
            uiStage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
        if (uiStage != null && uiStage.getViewport() != null) {
            uiStage.getViewport().update(width, height, true);
        }
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (background != null) { background.dispose(); background = null; }
        if (stage != null)      { stage.dispose();      stage      = null; }
        if (uiStage != null)    { uiStage.dispose();    uiStage    = null; }
        if (skin != null)       { skin.dispose();       skin       = null; }
        if (hoverSound != null) { hoverSound.dispose(); hoverSound = null; }
        if (batch != null)      { batch.dispose();      batch      = null; }
    }
}
