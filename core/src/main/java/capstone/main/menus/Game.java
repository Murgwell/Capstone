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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import java.util.ArrayList;

public class Game implements Screen {

    private final Corrupted game;
    private final int selectedCharacterIndex;
    private SpriteBatch spriteBatch;
    private Viewport viewport;      // world viewport
    private Viewport uiViewport;    // UI viewport for pause stage
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
    private PhysicsManager physicsManager;

    OrthogonalTiledMapRenderer mapRenderer;
    ShaderProgram treeFadeShader;

    WorldRenderer worldRenderer;
    EntityRenderer entityRenderer;
    TreeRenderer treeRenderer;
    ShapeRenderer shapeRenderer;
    WeaponRenderer weaponRenderer;
    CameraManager cameraManager;
    ScreenShake screenShake;

    PlayerLogic playerLogic;
    BulletLogic bulletLogic;
    EnemyLogic enemyLogic;

    public Game(Corrupted game, int selectedCharacterIndex) {
        this.game = game;
        this.selectedCharacterIndex = selectedCharacterIndex;
    }

    @Override
    public void show() {
        // --- Physics world (use physics-enabled map manager) ---
        physicsManager = new PhysicsManager(); // contains Box2D world
        mapManager = new MapManager(physicsManager);
        mapManager.load("Textures/World1.tmx");
        mapRenderer = mapManager.getRenderer();

        mapWidth = mapManager.getWorldWidth();
        mapHeight = mapManager.getWorldHeight();

        // Viewports & camera
        viewport = new ExtendViewport(20, 12);
        camera = (OrthographicCamera) viewport.getCamera();

        uiViewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Screen shake + camera manager
        screenShake = new ScreenShake();
        cameraManager = new CameraManager(camera, screenShake, mapWidth, mapHeight);

        // Create player with physics world (adjust constructor if needed)
        player = createPlayer();

        // Weapon sprite
        weaponTexture = new Texture("gun.png");
        weaponSprite = new Sprite(weaponTexture);
        weaponSprite.setSize(.3f, .3f);
        weaponSprite.setOrigin(1f, -3f);

        // Input setup
        inputManager = new InputManager();
        // gameInputProcessor handles ESC toggle and F11 fullscreen
        gameInputProcessor = new InputProcessor() {
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

                if (keycode == com.badlogic.gdx.Input.Keys.F11) {
                    if (Gdx.graphics.isFullscreen()) {
                        Gdx.graphics.setWindowedMode(1280, 720);
                    } else {
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
            @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
            @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
            @Override public boolean scrolled(float amountX, float amountY) { return false; }

            // REQUIRED BY YOUR LIBGDX VERSION
            @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
                return false;
            }
        };


        gameplayInputs = new InputMultiplexer();
        gameplayInputs.addProcessor(inputManager);
        gameplayInputs.addProcessor(gameInputProcessor);
        Gdx.input.setInputProcessor(gameplayInputs);

        // Movement and logic managers
        movementManager = new MovementManager(player); // physics-aware movement manager
        damageNumbers = new ArrayList<>();
        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.1f);

        enemySpawner = new EnemySpawner(mapWidth, mapHeight, screenShake, physicsManager);
        enemySpawner.spawnInitial(5);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Back button sprite (world coordinates)
        backBtnTexture = new Texture("ui/Menu/back_button.png");
        backBtnSprite = new Sprite(backBtnTexture);
        backBtnSprite.setSize(1.5f, 1.5f);
        backBtnSprite.setPosition(0.5f, viewport.getWorldHeight() - 2f);

        // Shaders
        ShaderProgram.pedantic = false;
        treeFadeShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/treeFade.glsl")
        );

        // Logic & renderers
        bulletLogic = new BulletLogic((Ranged) player, enemySpawner.getEnemies(), damageNumbers, damageFont, physicsManager);
        playerLogic = new PlayerLogic(player, inputManager, viewport, movementManager, bulletLogic);
        enemyLogic = new EnemyLogic(enemySpawner, enemySpawner.getEnemies(), player);

        worldRenderer = new WorldRenderer(mapManager.getRenderer());
        entityRenderer = new EntityRenderer(spriteBatch, shapeRenderer, player, enemySpawner.getEnemies(), damageNumbers);
        treeRenderer = new TreeRenderer(spriteBatch, mapManager.getTiledMap(), player);
        weaponRenderer = new WeaponRenderer(player, weaponSprite);

        // Pause UI stage and skin
        pauseSkin = new Skin(Gdx.files.internal("uiskin.json"));
        pauseStage = new Stage(uiViewport, spriteBatch);
    }

    @Override
    public void render(float delta) {
        // Cap delta to avoid large physics steps during debugging/resizes
        float stepDelta = Math.min(delta, 1f / 30f);

        if (!isPaused) {
            inputManager.update();
            // Step physics only when not paused
            physicsManager.step(stepDelta);

            playerLogic.update(delta);
            bulletLogic.update(delta);
            enemyLogic.update(delta);

            screenShake.update(delta);
        } else {
            // paused -> update pause UI only
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

        // detect mouse click on back button only when not paused
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
        // Use physics-aware aiming update (same logic from your physics version)
        player.updateWeaponAimingRad(viewport);
        float weaponRad = player.getWeaponAimingRad();

        // Player center
        float playerCenterX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float playerCenterY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        boolean lookingLeft = Math.cos(weaponRad) < 0;

        weaponSprite.setOrigin(weaponSprite.getWidth() / 2f, weaponSprite.getHeight() / 2f);
        weaponSprite.setFlip(lookingLeft, false);

        float angleDeg = (float)Math.toDegrees(weaponRad);
        if (lookingLeft) angleDeg += 180f;
        weaponSprite.setRotation(angleDeg);

        weaponSprite.setPosition(playerCenterX - weaponSprite.getOriginX(),
            playerCenterY - weaponSprite.getOriginY());
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
        // ensure pause stage receives input
        Gdx.input.setInputProcessor(pauseStage);
    }

    private void updateCamera() {
        float targetX = player.getSprite().getX() + player.getSprite().getWidth()/2f;
        float targetY = player.getSprite().getY() + player.getSprite().getHeight()/2f;

        Vector3 mouseWorld = viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float panFactor = 0.2f;
        targetX += (mouseWorld.x - targetX) * panFactor;
        targetY += (mouseWorld.y - targetY) * panFactor;

        // Use camera manager (handles clamp + shake)
        cameraManager.update(Gdx.graphics.getDeltaTime(), targetX, targetY, mouseWorld);
    }

    private void draw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        worldRenderer.renderGround(camera);
        entityRenderer.render(camera);
        //treeRenderer.render(camera);
        weaponRenderer.render(spriteBatch);

        // world-space UI: draw back button as part of world projection
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

        float targetAspect = viewport.getWorldWidth() / viewport.getWorldHeight();
        float windowAspect = (float) width / height;

        camera.zoom = windowAspect > targetAspect ? windowAspect / targetAspect : targetAspect / windowAspect;
        camera.update();
    }

    @Override
    public void pause() {
        // libGDX lifecycle pause (not same as isPaused toggle)
    }

    @Override
    public void resume() {
        // libGDX lifecycle resume
    }

    @Override
    public void hide() {
        // When hidden, ensure physics stopped
        isPaused = true;
        Gdx.input.setInputProcessor(pauseStage);
    }

    @Override
    public void dispose() {
        // Dispose resources used by this screen
        try { spriteBatch.dispose(); } catch (Exception ignored) {}
        try { weaponTexture.dispose(); } catch (Exception ignored) {}
        try { backBtnTexture.dispose(); } catch (Exception ignored) {}
        try { shapeRenderer.dispose(); } catch (Exception ignored) {}
        try { damageFont.dispose(); } catch (Exception ignored) {}
        try { pauseStage.dispose(); } catch (Exception ignored) {}
        try { pauseSkin.dispose(); } catch (Exception ignored) {}
        try { treeFadeShader.dispose(); } catch (Exception ignored) {}
        if (physicsManager != null) physicsManager.dispose(); // if you have cleanup
        if (mapManager != null) mapManager.dispose();

    }

    private AbstractPlayer createPlayer() {
        ArrayList<Bullet> bullets = new ArrayList<>();
        switch (selectedCharacterIndex) {
            case 1:
                return new MannyPacquiao(
                    120, 80, 6, 10, 11,
                    9f, 9f, 1f, 1f,
                    bullets,
                    mapWidth, mapHeight,
                    physicsManager.getWorld()
                );
            default:
                return new VicoSotto(
                    120, 80, 5, 8, 10,
                    9f, 9f, 1f, 1f,
                    bullets,
                    mapWidth, mapHeight,
                    physicsManager.getWorld()
                );
        }
    }
}
