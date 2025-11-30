
package capstone.main.menus;


// at top:
import capstone.main.UI.Hud;
import capstone.main.UI.HpBarHud;

import capstone.main.Corrupted;
import capstone.main.Characters.*;
import capstone.main.Enemies.*;
import capstone.main.Managers.*;
import capstone.main.Logic.*;
import capstone.main.Render.*;
import capstone.main.Sprites.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import com.badlogic.gdx.math.Vector3;

import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;

/**
 * Main game screen (world + gameplay + pause UI).
 * World rendering uses physics-aware managers; UI is rendered in a separate stage.
 */
public class Game implements Screen {

    private final Corrupted game;
    private final int selectedCharacterIndex;

    // Rendering
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;    // world viewport
    private Viewport uiViewport;  // UI viewport
    private OrthogonalTiledMapRenderer mapRenderer;

    // World/map
    private MapManager mapManager;
    private PhysicsManager physicsManager;
    private float mapWidth;
    private float mapHeight;

    // Player & logic
    private AbstractPlayer player;
    private MovementManager movementManager;
    private EnemySpawner enemySpawner;
    private PlayerLogic playerLogic;
    private BulletLogic bulletLogic;
    private EnemyLogic enemyLogic;

    // Renderers
    private WorldRenderer worldRenderer;
    private EntityRenderer entityRenderer;
    private TreeRenderer treeRenderer;
    private WeaponRenderer weaponRenderer;

    // Camera helpers
    private CameraManager cameraManager;
    private ScreenShake screenShake;

    // UI (pause)
    private Stage pauseStage;
    private Skin pauseSkin;

    // Input
    private InputManager inputManager;
    private InputMultiplexer gameplayInputs;
    private InputProcessor globalInputProcessor;

    // FX & HUD
    private ArrayList<DamageNumber> damageNumbers;
    private BitmapFont damageFont;
    private HpBarHud hpHud;

    // Shaders
    private ShaderProgram treeFadeShader;

    // Back button (world-space)
    private Texture backBtnTexture;
    private Sprite  backBtnSprite;

    // Weapon
    private Texture weaponTexture;
    private Sprite  weaponSprite;

    // State
    private boolean isPaused = false;

    public Game(Corrupted game, int selectedCharacterIndex) {
        this.game = game;
        this.selectedCharacterIndex = selectedCharacterIndex;
    }

    @Override
    public void show() {
        // --- Physics & Map ---
        physicsManager = new PhysicsManager();                // Box2D world
        mapManager     = new MapManager(physicsManager);
        mapManager.load("Textures/World1.tmx");

        mapRenderer = mapManager.getRenderer();
        mapWidth    = mapManager.getWorldWidth();
        mapHeight   = mapManager.getWorldHeight();

        // --- Viewports & Camera ---
        // World units (meters): choose a comfortable scale for your gameplay area
        viewport = new ExtendViewport(20f, 12f);
        camera   = (OrthographicCamera) viewport.getCamera();

        // UI uses screen-space scaling, independent of world units
        uiViewport = new ScreenViewport();

        // --- Camera helpers ---
        screenShake   = new ScreenShake();
        cameraManager = new CameraManager(camera, screenShake, mapWidth, mapHeight);

        // --- Player & weapon ---
        player = createPlayer();
        weaponTexture = new Texture("gun.png");
        weaponSprite  = new Sprite(weaponTexture);
        weaponSprite.setSize(0.3f, 0.3f);
        weaponSprite.setOrigin(weaponSprite.getWidth() / 2f, weaponSprite.getHeight() / 2f);

        // --- Inputs ---
        inputManager = new InputManager();

        // Global input handler foHerer Pause (ESC) + Fullscreen toggle (F11 via VideoSettings)
        globalInputProcessor = new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    isPaused = !isPaused;
                    Gdx.input.setInputProcessor(isPaused ? pauseStage : gameplayInputs);
                    return true;
                }
                if (keycode == com.badlogic.gdx.Input.Keys.F11) {
                    boolean newFs = !VideoSettings.isFullscreen();
                    VideoSettings.setFullscreen(newFs);
                    VideoSettings.apply();
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
            @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
        };

        gameplayInputs = new InputMultiplexer();
        gameplayInputs.addProcessor(inputManager);
        gameplayInputs.addProcessor(globalInputProcessor);
        Gdx.input.setInputProcessor(gameplayInputs);

        // --- Movement/logic ---
        movementManager = new MovementManager(player);
        damageNumbers   = new ArrayList<>();
        damageFont      = new BitmapFont();
        damageFont.getData().setScale(0.1f);

        enemySpawner = new EnemySpawner(mapWidth, mapHeight, screenShake, physicsManager);
        enemySpawner.spawnInitial(5);

        // --- Batches & renderers ---
        spriteBatch    = new SpriteBatch();
        shapeRenderer  = new ShapeRenderer();

        backBtnTexture = new Texture("ui/Menu/back_button.png");
        backBtnSprite  = new Sprite(backBtnTexture);
        backBtnSprite.setSize(1.5f, 1.5f);
        backBtnSprite.setPosition(0.5f, viewport.getWorldHeight() - 2f);

        ShaderProgram.pedantic = false;
        treeFadeShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/treeFade.glsl")
        );

        bulletLogic   = new BulletLogic((Ranged) player, enemySpawner.getEnemies(), damageNumbers, damageFont, physicsManager);
        playerLogic   = new PlayerLogic(player, inputManager, viewport, movementManager, bulletLogic);
        enemyLogic    = new EnemyLogic(enemySpawner, enemySpawner.getEnemies(), player);

        worldRenderer  = new WorldRenderer(mapManager.getRenderer());
        entityRenderer = new EntityRenderer(spriteBatch, shapeRenderer, player, enemySpawner.getEnemies(), damageNumbers);
        treeRenderer   = new TreeRenderer(spriteBatch, mapManager.getTiledMap(), player);
        weaponRenderer = new WeaponRenderer(player, weaponSprite);

        // --- Pause UI ---
        pauseSkin  = new Skin(Gdx.files.internal("uiskin.json"));
        pauseStage = new Stage(uiViewport, spriteBatch);

        hpHud = new HpBarHud(uiViewport, spriteBatch, player);  // <-- correct order & argument
    }

    @Override
    public void render(float delta) {
        // Cap delta to avoid large physics steps during debugging/resizes
        float stepDelta = Math.min(delta, 1f / 30f);

        if (!isPaused) {
            inputManager.update();
            physicsManager.step(stepDelta);   // Step physics only when not paused
            playerLogic.update(delta);
            bulletLogic.update(delta);
            enemyLogic.update(delta);
            screenShake.update(delta);
        } else {
            // Update (lazy-create) pause UI only when paused
            if (pauseStage.getActors().size == 0) {
                createPauseMenu();
            }
            pauseStage.act(delta);
        }

        updateCamera();
        updateWeaponAiming();
        draw();

        // Draw pause UI on top if paused
        if (isPaused) {
            pauseStage.draw();
        }

        // World-space back button click (only when not paused)
        if (Gdx.input.justTouched() && !isPaused) {
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            viewport.getCamera().unproject(mouse); // to world units
            if (backBtnSprite.getBoundingRectangle().contains(mouse.x, mouse.y)) {
                // Honor current video setting when returning
                VideoSettings.apply();
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return;
            }
        }

        hpHud.update(delta);
        hpHud.draw();
        if (isPaused) {
            pauseStage.draw(); // pause menu overlays HUD
        }

    }

    private void updateWeaponAiming() {
        player.updateWeaponAimingRad(viewport);
        float weaponRad = player.getWeaponAimingRad();

        // Player center (world units)
        float playerCenterX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float playerCenterY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        boolean lookingLeft = Math.cos(weaponRad) < 0;
        weaponSprite.setFlip(lookingLeft, false);

        float angleDeg = (float) Math.toDegrees(weaponRad);
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
        table.add(titleLabel).padBottom(30f).row();

        CheckBox musicCheckBox = new CheckBox("Music: ON", pauseSkin);
        table.add(musicCheckBox).padBottom(20f).row();

        TextButton resumeButton = new TextButton("Resume", pauseSkin);
        resumeButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                isPaused = false;
                Gdx.input.setInputProcessor(gameplayInputs);
            }
        });
        table.add(resumeButton).width(150f).height(40f).padBottom(20f).row();

        TextButton backButton = new TextButton("Back to Menu", pauseSkin);
        backButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                VideoSettings.apply(); // keep fullscreen/windowed on return
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        table.add(backButton).width(140f).height(40f);

        pauseStage.addActor(table);

        // Ensure pause stage receives input
        Gdx.input.setInputProcessor(pauseStage);
    }

    private void updateCamera() {
        // Target: player center with subtle mouse pan
        float targetX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float targetY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        Vector3 mouseWorld = viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f));
        float panFactor = 0.2f;
        targetX += (mouseWorld.x - targetX) * panFactor;
        targetY += (mouseWorld.y - targetY) * panFactor;

        // Camera manager handles clamping + shake
        cameraManager.update(Gdx.graphics.getDeltaTime(), targetX, targetY, mouseWorld);
    }

    private void draw() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        // Ground/tile layers
        worldRenderer.renderGround(camera);

        // Entities (player, enemies, damage numbers)
        entityRenderer.render(camera);

        // Optional trees
        // treeRenderer.render(camera);

        // Weapon
        weaponRenderer.render(spriteBatch);

        // World-space UI: back button
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        backBtnSprite.draw(spriteBatch);
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Guard against invalid sizes on certain platforms
        if (width <= 0 || height <= 0) return;

        viewport.update(width, height, true);
        uiViewport.update(width, height, true);

        if (hpHud != null) hpHud.resize(width, height);
        // No manual camera.zoom juggling:
        // ExtendViewport keeps aspect and adds gutters; ScreenViewport scales UI.
        camera.update();
    }

    @Override public void pause()  { /* app lifecycle pause */ }
    @Override public void resume() { /* app lifecycle resume */ }

    @Override
    public void hide() {
        // When hidden, ensure pause input isn't active
        isPaused = true;
        Gdx.input.setInputProcessor(pauseStage);
    }

    @Override
    public void dispose() {
        // Dispose resources used by this screen (defensive try-catch to avoid cascading errors)
        try { if (spriteBatch   != null) spriteBatch.dispose(); }   catch (Exception ignored) {}
        try { if (shapeRenderer != null) shapeRenderer.dispose(); } catch (Exception ignored) {}
        try { if (weaponTexture != null) weaponTexture.dispose(); } catch (Exception ignored) {}
        try { if (backBtnTexture!= null) backBtnTexture.dispose(); }catch (Exception ignored) {}
        try { if (damageFont    != null) damageFont.dispose(); }    catch (Exception ignored) {}
        try { if (pauseStage    != null) pauseStage.dispose(); }    catch (Exception ignored) {}
        try { if (pauseSkin     != null) pauseSkin.dispose(); }     catch (Exception ignored) {}
        try { if (treeFadeShader!= null) treeFadeShader.dispose(); }catch (Exception ignored) {}

        // Managers that own heavy resources
        if (physicsManager != null) physicsManager.dispose();
        if (mapManager     != null) mapManager.dispose();

        if (hpHud != null) hpHud.dispose();

        // Null references to help GC in desktop runs
        spriteBatch = null;
        shapeRenderer = null;
        weaponTexture = null;
        backBtnTexture = null;
        damageFont = null;
        pauseStage = null;
        pauseSkin = null;
        treeFadeShader = null;
        physicsManager = null;
        mapManager = null;
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
