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
    private OrthographicCamera camera;

    private Texture weaponTexture;
    private Sprite weaponSprite;

    private AbstractPlayer player;
    private InputManager inputManager;
    private MovementManager movementManager;
    private EnemySpawner enemySpawner;

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
        mapManager = new MapManager();
        //mapManager.load("World 1/World1_Stage1.tmx");
        mapManager.load("Maps/Worlds/World 1 - Stage 1.tmx");
        mapRenderer = mapManager.getRenderer();

        mapWidth = mapManager.getWorldWidth();
        mapHeight = mapManager.getWorldHeight();

        player = new VicoSotto(120,80, 5, 8,10,5f, 3f, 1f, 1f, new ArrayList<>(), mapWidth, mapHeight);

        weaponTexture = new Texture("gun.png");
        weaponSprite = new Sprite(weaponTexture);
        weaponSprite.setSize(.2f, .2f);
        weaponSprite.setOrigin(1f, -3f);

        inputManager = new InputManager();
        movementManager = new MovementManager();

        damageNumbers = new ArrayList<>();
        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.1f);

        enemySpawner = new EnemySpawner(mapWidth, mapHeight);
        enemySpawner.spawnInitial(5);

        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        viewport = new ExtendViewport(10, 6);
        camera = (OrthographicCamera) viewport.getCamera();

        // Shader setup
        ShaderProgram.pedantic = false;
        treeFadeShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/treeFade.glsl")
        );
        if (!treeFadeShader.isCompiled()) {
            Gdx.app.error("Shader", treeFadeShader.getLog());
        }

        bulletLogic = new BulletLogic((Ranged) player, enemySpawner.getEnemies(), damageNumbers, damageFont);
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

        playerLogic.update(delta);
        bulletLogic.update(delta);
        enemyLogic.update(delta);

        updateCamera();
        updateWeaponAiming();
        draw();
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
        treeRenderer.render(camera);
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
