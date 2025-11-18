package capstone.main.menus;

import capstone.main.Corrupted;
import capstone.main.Characters.*;
import capstone.main.Enemies.*;
import capstone.main.Managers.*;
import capstone.main.Logic.*;
import capstone.main.Render.*;
import capstone.main.Sprites.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import java.util.ArrayList;

public class Game implements Screen {

    private final Corrupted game;
    private SpriteBatch spriteBatch;
    private Viewport viewport;
    private Viewport uiViewport;
    private OrthographicCamera camera;

    private Texture weaponTexture;
    private Sprite weaponSprite;
    private Texture backBtnTexture;
    private Sprite backBtnSprite;

    private AbstractPlayer player;
    private InputManager inputManager;
    private InputMultiplexer gameplayInputs;
    private MovementManager movementManager;
    private EnemySpawner enemySpawner;

    private boolean isPaused = false;
    private Stage pauseStage;
    private Skin pauseSkin;
    private com.badlogic.gdx.InputProcessor gameInputProcessor;

    float mapWidth;
    float mapHeight;

    private ArrayList<DamageNumber> damageNumbers;
    private BitmapFont damageFont;

    private MapManager mapManager;

    OrthogonalTiledMapRenderer mapRenderer;
    ShaderProgram treeFadeShader;

    WorldRenderer worldRenderer;
    EntityRenderer entityRenderer;
    TreeRenderer treeRenderer;
    ShapeRenderer shapeRenderer;

    PlayerLogic playerLogic;
    BulletLogic bulletLogic;
    EnemyLogic enemyLogic;
    WeaponRenderer weaponRenderer;

    public Game(Corrupted game) {
        this.game = game;
    }

    @Override
    public void show() {

        // CREATE THESE FIRST, before using them
        inputManager = new InputManager();

        gameInputProcessor = new com.badlogic.gdx.InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    isPaused = !isPaused;
                    if (isPaused) {
                        Gdx.input.setInputProcessor(pauseStage);
                    } else {
                        Gdx.input.setInputProcessor(gameplayInputs);
                    }
                    return true;
                }

                // FULLSCREEN TOGGLE (F11)
                if (keycode == com.badlogic.gdx.Input.Keys.F11) {
                    if (Gdx.graphics.isFullscreen()) {
                        // Return to windowed mode
                        Gdx.graphics.setWindowedMode(1280, 720);   // Choose your window size
                    } else {
                        // Enter fullscreen using current monitor resolution
                        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                    }
                    return true;
                }

                return false;
            }

            @Override public boolean keyUp(int keycode) { return false; }
            @Override public boolean keyTyped(char character) { return false; }
            @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
            @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
            @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
            @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
            @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
            @Override public boolean scrolled(float amountX, float amountY) { return false; }
        };

        // NOW it's safe to use inputManager and gameInputProcessor.
        gameplayInputs = new InputMultiplexer();
        gameplayInputs.addProcessor(inputManager);
        gameplayInputs.addProcessor(gameInputProcessor);
        Gdx.input.setInputProcessor(gameplayInputs);

        // --- everything below stays the same ---
        mapManager = new MapManager();
        mapManager.load("Textures/World1.tmx");
        mapRenderer = mapManager.getRenderer();

        mapWidth = mapManager.getWorldWidth();
        mapHeight = mapManager.getWorldHeight();

        player = new VicoSotto(120,80, 5, 8,4,9f, 9f, 2f, 2f, new ArrayList<>(), mapWidth, mapHeight);

        weaponTexture = new Texture("gun.png");
        weaponSprite = new Sprite(weaponTexture);
        weaponSprite.setSize(.3f, .3f);
        weaponSprite.setOrigin(1f, -3f);

        movementManager = new MovementManager();
        damageNumbers = new ArrayList<>();
        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.1f);

        enemySpawner = new EnemySpawner(mapWidth, mapHeight);
        enemySpawner.spawnInitial(5);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        uiViewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        pauseStage = new Stage(uiViewport, spriteBatch);
        viewport = new ExtendViewport(20, 12);
        camera = (OrthographicCamera) viewport.getCamera();

        backBtnTexture = new Texture("ui/Menu/back_button.png");
        backBtnSprite = new Sprite(backBtnTexture);
        backBtnSprite.setSize(1.5f, 1.5f);
        backBtnSprite.setPosition(0.5f, viewport.getWorldHeight() - 2f);

        ShaderProgram.pedantic = false;
        treeFadeShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/treeFade.glsl")
        );

        bulletLogic = new BulletLogic((Ranged) player, enemySpawner.getEnemies(), damageNumbers, damageFont);
        playerLogic = new PlayerLogic(player, inputManager, viewport, movementManager, bulletLogic);
        enemyLogic = new EnemyLogic(enemySpawner, enemySpawner.getEnemies(), player);

        worldRenderer = new WorldRenderer(mapManager.getRenderer());
        entityRenderer = new EntityRenderer(spriteBatch, shapeRenderer, player, enemySpawner.getEnemies(), damageNumbers);
        treeRenderer = new TreeRenderer(spriteBatch, mapManager.getTiledMap(), player);
        weaponRenderer = new WeaponRenderer(player, weaponSprite);

        pauseSkin = new Skin(Gdx.files.internal("uiskin.json"));
    }


    @Override
    public void render(float delta) {
        if (!isPaused) {
            inputManager.update();
            playerLogic.update(delta);
            bulletLogic.update(delta);
            enemyLogic.update(delta);
        } else {
            if (pauseStage.getActors().size == 0) {
                createPauseMenu();
            }
            pauseStage.act(delta);
        }

        updateCamera();
        updateWeaponAiming();
        draw();

        if (isPaused) {
            pauseStage.draw();
        }

        // detect mouse click on back button
        if (Gdx.input.justTouched() && !isPaused) {
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.getCamera().unproject(mouse);

            if (backBtnSprite.getBoundingRectangle().contains(mouse.x, mouse.y)) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return;
            }
        }
    }

    private void updateWeaponAiming() {
        player.updateWeaponAiming(viewport);
        float weaponRad = player.getWeaponAimingRad();

        weaponSprite.setOrigin(weaponSprite.getWidth()/2f, weaponSprite.getHeight()*0.25f);
        weaponSprite.setRotation((float)Math.toDegrees(weaponRad));

        float gap = 0.1f;
        float weaponCenterX = player.getSprite().getX() + player.getSprite().getWidth()/2f
            + (float)Math.cos(weaponRad) * gap;
        float weaponCenterY = player.getSprite().getY() + player.getSprite().getHeight()/2f
            + (float)Math.sin(weaponRad) * gap;

        weaponSprite.setPosition(weaponCenterX - weaponSprite.getOriginX(),
            weaponCenterY - weaponSprite.getOriginY());
    }

    private void createPauseMenu() {
        pauseStage.clear();

        Table table = new Table();
        table.setFillParent(true);

        Label titleLabel = new Label("PAUSED", pauseSkin);
        table.add(titleLabel).padBottom(30).row();

        CheckBox musicCheckBox = new CheckBox("Music: ON", pauseSkin);
        table.add(musicCheckBox).padBottom(20).row();

        TextButton resumeButton = new TextButton("Resume", pauseSkin);
        resumeButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                isPaused = false;
                Gdx.input.setInputProcessor(gameplayInputs);
            }
        });
        table.add(resumeButton).width(150).height(40).padBottom(20).row();

        TextButton backButton = new TextButton("Back to Menu", pauseSkin);
        backButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        table.add(backButton).width(120).height(35);

        pauseStage.addActor(table);
        Gdx.input.setInputProcessor(pauseStage);
    }

    private void updateCamera() {
        float halfViewWidth = viewport.getWorldWidth()/2f;
        float halfViewHeight = viewport.getWorldHeight()/2f;

        float targetX = player.getSprite().getX() + player.getSprite().getWidth()/2f;
        float targetY = player.getSprite().getY() + player.getSprite().getHeight()/2f;

        Vector3 mouseWorld = viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float panFactor = 0.2f;
        targetX += (mouseWorld.x - targetX) * panFactor;
        targetY += (mouseWorld.y - targetY) * panFactor;

        float cameraX = MathUtils.clamp(targetX, halfViewWidth, mapWidth - halfViewWidth);
        float cameraY = MathUtils.clamp(targetY, halfViewHeight, mapHeight - halfViewHeight);

        camera.position.lerp(new Vector3(cameraX, cameraY, 0), 0.1f);
        camera.update();
    }

    private void draw() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        worldRenderer.renderGround(camera);
        entityRenderer.render(camera);
        //treeRenderer.render(camera);
        weaponRenderer.render(spriteBatch);

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        backBtnSprite.draw(spriteBatch);

        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height);
        uiViewport.update(width, height);

        float targetAspect = viewport.getWorldWidth()/viewport.getWorldHeight();
        float windowAspect = (float) width/height;

        camera.zoom = windowAspect > targetAspect ? windowAspect/targetAspect : targetAspect/windowAspect;
        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
