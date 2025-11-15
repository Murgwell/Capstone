package capstone.main.menus;

import capstone.main.Corrupted;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class SettingsScreen implements Screen {
    private final Corrupted game;
    private SpriteBatch batch;
    private Stage stage;
    private Skin skin;
    private Texture background;
    private FitViewport viewport;
    private Sound hoverSound;

    private boolean isMusicOn = true;
    private boolean isFullscreen = false;

    public SettingsScreen(Corrupted game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture("mainMenuBG.png");
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        hoverSound = Gdx.audio.newSound(Gdx.files.internal("hover.wav"));

        viewport = new FitViewport(1280, 720);
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        // === Title ===
        Label titleLabel = new Label("SETTINGS", skin, "default");
        titleLabel.setFontScale(3.5f);
        titleLabel.setColor(Color.WHITE);

        // === Buttons ===
        TextButton musicButton = new TextButton("Music: ON", skin);
        TextButton fullscreenButton = new TextButton("Fullscreen: OFF", skin);
        TextButton backButton = new TextButton("Back", skin);

        styleButton(musicButton);
        styleButton(fullscreenButton);
        styleButton(backButton);

        addHoverEffect(musicButton);
        addHoverEffect(fullscreenButton);
        addHoverEffect(backButton);

        // === Music Toggle ===
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isMusicOn = !isMusicOn;
                musicButton.setText("Music: " + (isMusicOn ? "ON" : "OFF"));
            }
        });

        // === Fullscreen Toggle ===
        fullscreenButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isFullscreen = !isFullscreen;
                fullscreenButton.setText("Fullscreen: " + (isFullscreen ? "ON" : "OFF"));

                if (isFullscreen) {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                } else {
                    Gdx.graphics.setWindowedMode(1280, 720);
                }
            }
        });

        // === Back Button ===
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        // === Layout ===
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        table.add(titleLabel).padBottom(60f).row();
        table.add(musicButton).pad(15f).width(300f).height(80f).row();
        table.add(fullscreenButton).pad(15f).width(300f).height(80f).row();
        table.add(backButton).padTop(40f).width(300f).height(80f);

        stage.addActor(table);
    }

    private void styleButton(TextButton button) {
        button.getLabel().setFontScale(1.3f);
    }

    private void addHoverEffect(final TextButton button) {
        final Color originalColor = new Color(button.getColor());
        final float originalScaleX = button.getScaleX();
        final float originalScaleY = button.getScaleY();

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.addAction(Actions.parallel(
                    Actions.scaleTo(1.1f, 1.1f, 0.1f),
                    Actions.color(Color.LIGHT_GRAY, 0.1f)
                ));
                if (hoverSound != null) hoverSound.play(0.4f);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.addAction(Actions.parallel(
                    Actions.scaleTo(originalScaleX, originalScaleY, 0.1f),
                    Actions.color(originalColor, 0.1f)
                ));
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                button.addAction(Actions.sequence(
                    Actions.scaleTo(0.95f, 0.95f, 0.05f),
                    Actions.scaleTo(1.1f, 1.1f, 0.05f),
                    Actions.scaleTo(originalScaleX, originalScaleY, 0.05f)
                ));
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(background, 0, 0, 1280, 720);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        batch.dispose();
        hoverSound.dispose();
    }
}
