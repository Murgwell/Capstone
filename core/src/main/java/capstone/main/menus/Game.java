
package capstone.main.menus;

import capstone.main.UI.HeartsHud;
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
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.ArrayList;

public class Game implements Screen {
    private final Corrupted game;
    private final int selectedCharacterIndex;
    private boolean isGameOver = false;

    // Rendering
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport; // world viewport
    private Viewport uiViewport; // UI viewport
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

    // UI (game-over)
    private Stage gameOverStage;
    private Skin gameOverSkin;
    private boolean gameOverUIBuilt = false;

    // Input
    private InputManager inputManager;
    private InputMultiplexer gameplayInputs;
    private InputProcessor globalInputProcessor;

    // FX & HUD
    private ArrayList<DamageNumber> damageNumbers;
    private BitmapFont damageFont;

    // HUD elements
    private HeartsHud heartsHud;
    private Texture hpFrameTex;
    private Texture hpFillTex;

    // Shaders
    private ShaderProgram treeFadeShader;

    // Back button (world-space)
    private Texture backBtnTexture;
    private Sprite backBtnSprite;

    // Weapon
    private Texture weaponTexture;
    private Sprite weaponSprite;

    // State
    private boolean isPaused = false;

    public Game(Corrupted game, int selectedCharacterIndex) {
        this.game = game;
        this.selectedCharacterIndex = selectedCharacterIndex;
    }

    @Override
    public void show() {
        // --- Physics & Map ---
        physicsManager = new PhysicsManager();
        mapManager = new MapManager(physicsManager);
        mapManager.load("Textures/World1.tmx");
        mapRenderer = mapManager.getRenderer();
        mapWidth = mapManager.getWorldWidth();
        mapHeight = mapManager.getWorldHeight();

        // --- Viewports & Camera ---
        viewport = new ExtendViewport(20f, 12f);
        camera = (OrthographicCamera) viewport.getCamera();
        uiViewport = new ScreenViewport();

        // --- Camera helpers ---
        screenShake = new ScreenShake();
        cameraManager = new CameraManager(camera, screenShake, mapWidth, mapHeight);

        // --- Initialize damage system first ---
        damageNumbers = new ArrayList<>();
        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.1f);

        // --- Create enemy spawner ---
        enemySpawner = new EnemySpawner(mapWidth, mapHeight, screenShake, physicsManager);
        enemySpawner.spawnInitial(5);

        // --- NOW create player (with enemies available for Manny) ---
        player = createPlayer();

        weaponTexture = new Texture("gun.png");
        weaponSprite = new Sprite(weaponTexture);
        weaponSprite.setSize(0.3f, 0.3f);
        weaponSprite.setOrigin(weaponSprite.getWidth() / 2f, weaponSprite.getHeight() / 2f);

        // --- Inputs ---
        inputManager = new InputManager();
        globalInputProcessor = new InputProcessor() {
            @Override public boolean keyDown(int keycode) {
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

                // Skill keybinds
                if (!isPaused && player instanceof MannyPacquiao) {
                    MannyPacquiao manny = (MannyPacquiao) player;
                    if (keycode == com.badlogic.gdx.Input.Keys.Q) {
                        manny.useMeteorFist();
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.E) {
                        manny.useBarrageCombo();
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.R) {
                        manny.useChampionsKnockout();
                        return true;
                    }
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

        // --- Initialize skills for Manny ---
        if (player instanceof MannyPacquiao) {
            ((MannyPacquiao) player).initializeSkills(
                enemySpawner.getEnemies(),
                damageNumbers,
                damageFont
            );
        }

        // --- Movement/logic ---
        movementManager = new MovementManager(player);

        // --- Batches & renderers ---
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        backBtnTexture = new Texture("ui/Menu/back_button.png");
        backBtnSprite = new Sprite(backBtnTexture);
        backBtnSprite.setSize(1.5f, 1.5f);
        backBtnSprite.setPosition(0.5f, viewport.getWorldHeight() - 2f);

        ShaderProgram.pedantic = false;
        treeFadeShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/treeFade.glsl")
        );

        // For Vico (ranged), initialize bullet logic
        if (player instanceof Ranged) {
            bulletLogic = new BulletLogic((Ranged) player, enemySpawner.getEnemies(), damageNumbers, damageFont, physicsManager);
        }

        playerLogic = new PlayerLogic(player, inputManager, viewport, movementManager, bulletLogic);
        enemyLogic = new EnemyLogic(enemySpawner, enemySpawner.getEnemies(), player);
        worldRenderer = new WorldRenderer(mapManager.getRenderer());
        entityRenderer = new EntityRenderer(spriteBatch, shapeRenderer, player, enemySpawner.getEnemies(), damageNumbers);
        treeRenderer = new TreeRenderer(spriteBatch, mapManager.getTiledMap(), player);
        weaponRenderer = new WeaponRenderer(player, weaponSprite);

        // --- Pause UI ---
        pauseSkin = new Skin(Gdx.files.internal("uiskin.json"));
        pauseStage = new Stage(uiViewport, spriteBatch);

        // --- HUD ---
        heartsHud = new HeartsHud(uiViewport, spriteBatch, player);
    }

    @Override
    public void render(float delta) {
        float stepDelta = Math.min(delta, 1f / 30f);
        if (!isPaused) {
            inputManager.update();
            physicsManager.step(stepDelta);
            playerLogic.update(delta);

            // Only update bullet logic if it exists (for ranged characters)
            if (bulletLogic != null) {
                bulletLogic.update(delta);
            }

            enemyLogic.update(delta);
            screenShake.update(delta);
            entityRenderer.update(delta);

            // Update skills if Manny
            if (player instanceof MannyPacquiao) {
                ((MannyPacquiao) player).updateSkills(delta);
            }
        } else {
            if (pauseStage.getActors().size == 0) createPauseMenu();
            pauseStage.act(delta);
        }

        updateCamera();
        updateWeaponAiming();

        // --- World rendering ---
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        worldRenderer.renderGround(camera);
        entityRenderer.render(camera);
        weaponRenderer.render(spriteBatch);

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        backBtnSprite.draw(spriteBatch);
        spriteBatch.end();

        // --- UI HUD ---
        heartsHud.update(delta);
        heartsHud.draw();

        if (isPaused) pauseStage.draw();

        // Back button click
        if (Gdx.input.justTouched() && !isPaused) {
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f);
            viewport.getCamera().unproject(mouse);
            if (backBtnSprite.getBoundingRectangle().contains(mouse.x, mouse.y)) {
                VideoSettings.apply();
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return;
            }
        }
    }

    private void updateWeaponAiming() {
        player.updateWeaponAimingRad(viewport);
        float weaponRad = player.getWeaponAimingRad();
        float playerCenterX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float playerCenterY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;
        boolean lookingLeft = Math.cos(weaponRad) < 0;
        weaponSprite.setFlip(lookingLeft, false);
        float angleDeg = (float) Math.toDegrees(weaponRad);
        if (lookingLeft) angleDeg += 180f;
        weaponSprite.setRotation(angleDeg);
        weaponSprite.setPosition(playerCenterX - weaponSprite.getOriginX(), playerCenterY - weaponSprite.getOriginY());
    }

    private void updateCamera() {
        float targetX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float targetY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;
        Vector3 mouseWorld = viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f));
        float panFactor = 0.2f;
        targetX += (mouseWorld.x - targetX) * panFactor;
        targetY += (mouseWorld.y - targetY) * panFactor;
        cameraManager.update(Gdx.graphics.getDeltaTime(), targetX, targetY, mouseWorld);
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
            @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                isPaused = false;
                Gdx.input.setInputProcessor(gameplayInputs);
            }
        });
        table.add(resumeButton).width(150f).height(40f).padBottom(20f).row();
        TextButton backButton = new TextButton("Back to Menu", pauseSkin);
        backButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                VideoSettings.apply();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        table.add(backButton).width(140f).height(40f);
        pauseStage.addActor(table);
        Gdx.input.setInputProcessor(pauseStage);
    }

    private void buildGameOverOverlay() {
        gameOverStage.clear();

        // Dim background
        Image dim = new Image(gameOverSkin.newDrawable("white", 0f, 0f, 0f, 0.6f));
        dim.setFillParent(true);
        gameOverStage.addActor(dim);

        Table root = new Table();
        root.setFillParent(true);

        Table panel = new Table(gameOverSkin);
        panel.setBackground("default-round");
        panel.pad(30f);

        Label title = new Label("GAME OVER", gameOverSkin);
        title.setFontScale(1.2f);
        Label subtitle = new Label("You have fallen.", gameOverSkin);

        TextButton retryBtn = new TextButton("Retry", gameOverSkin);
        retryBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                game.setScreen(new Game(game, selectedCharacterIndex));
                dispose();
            }
        });

        TextButton menuBtn = new TextButton("Back to Menu", gameOverSkin);
        menuBtn.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                VideoSettings.apply();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        panel.add(title).padBottom(12f).row();
        panel.add(subtitle).padBottom(18f).row();
        panel.add(retryBtn).width(180f).height(45f).padBottom(10f).row();
        panel.add(menuBtn).width(180f).height(45f).row();

        root.add(panel).center();
        gameOverStage.addActor(root);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);
        heartsHud.resize(width, height);
        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        isPaused = true;
        Gdx.input.setInputProcessor(pauseStage);
    }

    @Override
    public void dispose() {
        try { if (spriteBatch != null) spriteBatch.dispose(); } catch (Exception ignored) {}
        try { if (shapeRenderer != null) shapeRenderer.dispose(); } catch (Exception ignored) {}
        try { if (weaponTexture != null) weaponTexture.dispose(); } catch (Exception ignored) {}
        try { if (backBtnTexture != null) backBtnTexture.dispose(); } catch (Exception ignored) {}
        try { if (damageFont != null) damageFont.dispose(); } catch (Exception ignored) {}
        try { if (pauseStage != null) pauseStage.dispose(); } catch (Exception ignored) {}
        try { if (pauseSkin != null) pauseSkin.dispose(); } catch (Exception ignored) {}
        try { if (treeFadeShader != null) treeFadeShader.dispose(); } catch (Exception ignored) {}
        if (physicsManager != null) physicsManager.dispose();
        if (mapManager != null) mapManager.dispose();
        if (heartsHud != null) heartsHud.dispose();
        if (player != null) player.dispose();
    }


    private AbstractPlayer createPlayer() {
        ArrayList<Bullet> bullets = new ArrayList<>();
        switch (selectedCharacterIndex) {
            case 1:
                // Manny Pacquiao - Melee fighter
                return new MannyPacquiao(120,80, 8, 12, 1.5f,9f, 9f, 2f, 2f, enemySpawner.getEnemies(), damageNumbers, damageFont, mapWidth, mapHeight, physicsManager.getWorld(), screenShake
                );
            default:
                return new VicoSotto(120, 80, 5, 8, 10, 9f, 9f, 2f, 2f, bullets, mapWidth, mapHeight, physicsManager.getWorld(), screenShake);
        }
    }
}
