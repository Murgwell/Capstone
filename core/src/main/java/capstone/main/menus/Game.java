package capstone.main.menus;

import capstone.main.Corrupted;
import capstone.main.Characters.*;
import capstone.main.Enemies.*;
import capstone.main.Managers.*;
import capstone.main.Logic.*;
import capstone.main.Render.*;
import capstone.main.Sprites.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
    private SpriteBatch spriteBatch;
    private Viewport viewport;
    private OrthographicCamera camera;

    private Texture weaponTexture;
    private Sprite weaponSprite;

    private AbstractPlayer player;
    private InputManager inputManager;
    private MovementManager movementManager;
    private EnemySpawner enemySpawner;
    private PhysicsManager physicsManager;

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
    WeaponRenderer weaponRenderer;
    CameraManager cameraManager;
    ScreenShake screenShake;

    PlayerLogic playerLogic;
    BulletLogic bulletLogic;
    EnemyLogic enemyLogic;

    public Game(Corrupted game) {
        this.game = game;
    }

    @Override
    public void show() {
        // --- Physics world ---
        physicsManager = new PhysicsManager(); // contains Box2D world
        mapManager = new MapManager(physicsManager);
        mapManager.load("Textures/World1.tmx");
        mapRenderer = mapManager.getRenderer();

        mapWidth = mapManager.getWorldWidth();
        mapHeight = mapManager.getWorldHeight();

        viewport = new ExtendViewport(  20, 12);
        camera = (OrthographicCamera) viewport.getCamera();
        screenShake = new ScreenShake();
        cameraManager = new CameraManager(camera, screenShake, mapWidth, mapHeight);

        // Pass physicsManager.getWorld() to player
        player = new VicoSotto(
            120, 80, 5, 8, 10, 9f, 9f, 1f, 1f, new ArrayList<>(),
            mapWidth, mapHeight,
            physicsManager.getWorld()
        );

        weaponTexture = new Texture("gun.png");
        weaponSprite = new Sprite(weaponTexture);
        weaponSprite.setSize(.3f, .3f);
        weaponSprite.setOrigin(1f, -3f);

        inputManager = new InputManager();
        movementManager = new MovementManager(player); // already uses Box2D body

        damageNumbers = new ArrayList<>();
        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.1f);

        enemySpawner = new EnemySpawner(mapWidth, mapHeight, screenShake, physicsManager);
        enemySpawner.spawnInitial(5);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();


        // Shader setup
        ShaderProgram.pedantic = false;
        treeFadeShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/treeFade.glsl")
        );
        if (!treeFadeShader.isCompiled()) {
            Gdx.app.error("Shader", treeFadeShader.getLog());
        }

        bulletLogic = new BulletLogic((Ranged) player, enemySpawner.getEnemies(), damageNumbers, damageFont, physicsManager);
        playerLogic = new PlayerLogic(player, inputManager, viewport, movementManager, bulletLogic);
        enemyLogic = new EnemyLogic(enemySpawner, enemySpawner.getEnemies(), player);

        worldRenderer = new WorldRenderer(mapManager.getRenderer());
        entityRenderer = new EntityRenderer(spriteBatch, shapeRenderer, player, enemySpawner.getEnemies(), damageNumbers);
        treeRenderer = new TreeRenderer(spriteBatch, mapManager.getTiledMap(), player);
        weaponRenderer = new WeaponRenderer(player, weaponSprite);
    }

    @Override
    public void render(float delta) {
        inputManager.update();

        physicsManager.step(delta);

        playerLogic.update(delta);
        bulletLogic.update(delta);
        enemyLogic.update(delta);

        screenShake.update(delta);
        updateCamera();
        updateWeaponAiming();
        draw();
    }

    private void updateWeaponAiming() {
        // Update player's aiming angle internally
        player.updateWeaponAimingRad(viewport);
        float weaponRad = player.getWeaponAimingRad();

        // Player center
        float playerCenterX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float playerCenterY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        // Determine if looking left
        boolean lookingLeft = Math.cos(weaponRad) < 0;

        // Set origin at pivot
        weaponSprite.setOrigin(weaponSprite.getWidth()/2f, weaponSprite.getHeight()/2f);

        // Flip horizontally if looking left
        weaponSprite.setFlip(lookingLeft, false);

        // Convert rotation to degrees
        float angleDeg = (float)Math.toDegrees(weaponRad);

        // If flipped, invert rotation around Y axis
        if (lookingLeft) angleDeg += 180f;

        weaponSprite.setRotation(angleDeg);

        // Position the weapon at player center (you can add small gap if desired)
        weaponSprite.setPosition(playerCenterX - weaponSprite.getOriginX(),
            playerCenterY - weaponSprite.getOriginY());
    }

    private void updateCamera() {
        Vector3 mouseWorld = viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float targetX = player.getSprite().getX() + player.getSprite().getWidth()/2f;
        float targetY = player.getSprite().getY() + player.getSprite().getHeight()/2f;

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
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height);

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
