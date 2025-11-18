package capstone.main.menus;

import capstone.main.Corrupted;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {
    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;

    private final Corrupted game;
    private SpriteBatch batch;
    private Texture background;
    private OrthographicCamera camera;
    private FitViewport viewport;

    private Stage stage;
    private Stage uiStage;
    private Skin skin;
    private Label titleLabel;
    private float glitchTimer = 0f;
    private float glitchInterval = 0.2f; // how often it flickers

    private Sound hoverSound;

    public MainMenuScreen(Corrupted game) {
        this.game = game;
    }

    @Override
    public void show() {

        Gdx.graphics.setWindowedMode(1280, 720);
        Gdx.graphics.setResizable(false);

        batch = new SpriteBatch();
        background = new Texture("mainMenuBG.png");

        skin = new Skin(Gdx.files.internal("uiskin.json"));
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("hover.wav"));

        camera = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        uiStage = new Stage(new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera));
        Gdx.input.setInputProcessor(uiStage);

        // Load background texture
        background = new Texture("mainMenuBG.png");

        // Main stage for world elements
        stage = new Stage(viewport, batch);

        // UI overlay stage
        uiStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(uiStage);




        // === Title ===
        titleLabel = new Label("CORRUPTED", skin, "default");
        titleLabel.setFontScale(3.5f);
        titleLabel.setColor(Color.WHITE);
        titleLabel.setPosition(Gdx.graphics.getWidth() / 2f - titleLabel.getPrefWidth() / 2f, Gdx.graphics.getHeight() - 150f);
        uiStage.addActor(titleLabel);

        // === Play Button ===
        Texture playNormal = new Texture("ui/Menu/Main Menu/play_normal.png");
        Texture playPressed = new Texture("ui/Menu/Main Menu/play_pressed.png");

        ImageButton.ImageButtonStyle playStyle = new ImageButton.ImageButtonStyle();
        playStyle.up = new TextureRegionDrawable(new TextureRegion(playNormal));
        playStyle.down = new TextureRegionDrawable(new TextureRegion(playPressed));

        ImageButton playButton = new ImageButton(playStyle);

        // === Settings Button ===
        Texture settingsNormal = new Texture("ui/Menu/Main Menu/settings_normal.png");
        Texture settingsPressed = new Texture("ui/Menu/Main Menu/settings_pressed.png");

        ImageButton.ImageButtonStyle settingsStyle = new ImageButton.ImageButtonStyle();
        settingsStyle.up = new TextureRegionDrawable(new TextureRegion(settingsNormal));
        settingsStyle.down = new TextureRegionDrawable(new TextureRegion(settingsPressed));

        ImageButton settingsButton = new ImageButton(settingsStyle);


        // === Exit Button ===
        Texture exitNormal = new Texture("ui/Menu/Main Menu/quit_normal.png");
        Texture exitPressed = new Texture("ui/Menu/Main Menu/quit_pressed.png");

        ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
        exitStyle.up = new TextureRegionDrawable(new TextureRegion(exitNormal));
        exitStyle.down = new TextureRegionDrawable(new TextureRegion(exitPressed));

        ImageButton exitButton = new ImageButton(exitStyle);

        addHoverEffect(playButton);
        addHoverEffect(settingsButton);
        addHoverEffect(exitButton);

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.padTop(140f); // pushes buttons a bit lower

        table.add(playButton).pad(12).width(200).height(60);
        table.row();
        table.add(settingsButton).pad(12).width(200).height(60);
        table.row();
        table.add(exitButton).pad(12).width(200).height(60);

        uiStage.addActor(table);

        // === Button actions ===
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
    }

    private void addHoverEffect(final ImageButton button) {
        final Color originalColor = new Color(button.getColor());
        final float originalScaleX = button.getScaleX();
        final float originalScaleY = button.getScaleY();

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
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        batch.end();

        // Glitch flicker for title
        glitchTimer += delta;
        if (glitchTimer > glitchInterval) {
            glitchTimer = 0f;
            titleLabel.setColor(
                MathUtils.random(0.7f, 1f),
                MathUtils.random(0.7f, 1f),
                MathUtils.random(0.7f, 1f),
                1f
            );
            titleLabel.setPosition(
                (Gdx.graphics.getWidth() / 2f - titleLabel.getPrefWidth() / 2f) + MathUtils.random(-3f, 3f),
                (Gdx.graphics.getHeight() - 150f) + MathUtils.random(-2f, 2f)
            );
        }

        // Draw stages
        stage.act(delta);
        stage.draw();

        uiStage.act(delta);
        uiStage.draw();
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        background.dispose();
        stage.dispose();
        skin.dispose();
    }
}
