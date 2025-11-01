package capstone.main.menus;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Characters.Ranged;
import capstone.main.Characters.VicoSotto;
import capstone.main.Corrupted;
import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Enemies.EnemySpawner;
import capstone.main.Handlers.InputManager;
import capstone.main.Handlers.MovementManager;
import capstone.main.Sprites.Bullet;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

public class World1 implements Screen {

    private final Corrupted game;
    private final float worldWidth = 32f;
    private final float worldHeight = 32f;
    TiledMap tiledMap;
    OrthogonalTiledMapRenderer mapRenderer;
    ShaderProgram treeFadeShader;
    private SpriteBatch spriteBatch;
    private Viewport viewport;
    private OrthographicCamera camera;
    private Texture weaponTexture;
    private Sprite weaponSprite;
    private float weaponAimingRad;
    private AbstractPlayer player;
    private InputManager inputManager;
    private MovementManager movementManager;
    private EnemySpawner enemySpawner;
    private ArrayList<DamageNumber> damageNumbers;
    private BitmapFont damageFont;

    public World1(Corrupted game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Weapon
        weaponTexture = new Texture("gun.png");
        weaponSprite = new Sprite(weaponTexture);
        weaponSprite.setSize(.2f, .2f);
        weaponSprite.setOrigin(1f, -3f);

        inputManager = new InputManager();
        movementManager = new MovementManager();

        damageNumbers = new ArrayList<>();
        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.1f);

        // Player
        player = new VicoSotto(5f, 3f, 1f, 1f, new ArrayList<>()); // bullets inside player

        // Enemy Spawner
        enemySpawner = new EnemySpawner(worldWidth, worldHeight);
        enemySpawner.spawnInitial(5); // initial 5 dummies

        tiledMap = new TmxMapLoader().load("Maps/Worlds/World 1 - Stage 1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / 32f); // scale to match world units

        spriteBatch = new SpriteBatch();
        viewport = new ExtendViewport(10, 6);
        camera = (OrthographicCamera) viewport.getCamera();

        ShaderProgram.pedantic = false;
        treeFadeShader = new ShaderProgram(
            Gdx.files.internal("shaders/default.vert"),
            Gdx.files.internal("shaders/treeFade.glsl")
        );
        if (!treeFadeShader.isCompiled()) {
            Gdx.app.error("Shader", treeFadeShader.getLog());
        }

        // Weapon initial position
        float centerX = (viewport.getWorldWidth() - weaponSprite.getWidth()) / 2f;
        float centerY = (viewport.getWorldHeight() - weaponSprite.getHeight()) / 2f;
        weaponSprite.setPosition(centerX, centerY);
    }

    @Override
    public void render(float delta) {
        inputManager.update();

        // Update player
        player.update(delta, inputManager, movementManager, viewport);

        // Update bullets and collisions
        if (player instanceof Ranged) {
            Ranged r = (Ranged) player;
            ArrayList<Bullet> bullets = r.getBullets();

            for (int i = bullets.size() - 1; i >= 0; i--) {
                Bullet b = bullets.get(i);
                b.update(delta);

                for (AbstractEnemy e : enemySpawner.getEnemies()) {
                    if (!e.isDead() && b.getBoundingBox().overlaps(e.getSprite().getBoundingRectangle())) {
                        e.takeHit(b.getDamage());
                        damageNumbers.add(new DamageNumber(
                            e.getSprite().getX() + e.getSprite().getWidth() / 2f,
                            e.getSprite().getY() + e.getSprite().getHeight(),
                            b.getDamage(),
                            damageFont
                        ));
                        bullets.remove(i);
                        break;
                    }
                }
            }

            // Handle attack input
            if (inputManager.isAttacking()) r.handleAttack(weaponAimingRad, delta, movementManager);
            r.updateBullets(delta);
        }

        // Update enemies
        enemySpawner.update(delta); // handles spawning
        for (AbstractEnemy e : enemySpawner.getEnemies()) {
            if (!e.isDead()) e.update(delta, player);
        }

        updateCamera();
        updateWeaponAiming();
        draw();
    }

    private void updateWeaponAiming() {
        Vector3 worldCoords = viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float charX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float charY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        weaponAimingRad = (float) Math.atan2(worldCoords.y - charY, worldCoords.x - charX);

        weaponSprite.setOrigin(weaponSprite.getWidth() / 2f, weaponSprite.getHeight() * 0.25f);
        weaponSprite.setRotation((float) Math.toDegrees(weaponAimingRad));

        float gap = 0.1f;
        float weaponCenterX = charX + (float) Math.cos(weaponAimingRad) * gap;
        float weaponCenterY = charY + (float) Math.sin(weaponAimingRad) * gap;

        weaponSprite.setPosition(weaponCenterX - weaponSprite.getOriginX(),
            weaponCenterY - weaponSprite.getOriginY());
    }

    private void updateCamera() {
        float halfViewWidth = viewport.getWorldWidth() / 2f;
        float halfViewHeight = viewport.getWorldHeight() / 2f;

        float targetX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float targetY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        Vector3 mouseWorld = viewport.getCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float panFactor = 0.2f;
        targetX += (mouseWorld.x - targetX) * panFactor;
        targetY += (mouseWorld.y - targetY) * panFactor;

        float cameraX = MathUtils.clamp(targetX, halfViewWidth, worldWidth - halfViewWidth);
        float cameraY = MathUtils.clamp(targetY, halfViewHeight, worldHeight - halfViewHeight);

        camera.position.lerp(new Vector3(cameraX, cameraY, 0), 0.1f);
        camera.update();
    }

    private void draw() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        // 1️⃣ Render ground layer
        mapRenderer.setView(camera);
        mapRenderer.render(new int[]{0});

        // 2️⃣ Draw entities (player, bullets, enemies, etc.)
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        player.getSprite().draw(spriteBatch);
        weaponSprite.draw(spriteBatch);

        if (player instanceof Ranged) {
            Ranged r = (Ranged) player;
            for (Bullet b : r.getBullets()) b.draw(spriteBatch);
        }

        for (AbstractEnemy e : enemySpawner.getEnemies()) {
            if (!e.isDead()) e.getSprite().draw(spriteBatch);
        }

        for (int i = damageNumbers.size() - 1; i >= 0; i--) {
            DamageNumber dn = damageNumbers.get(i);
            dn.update(Gdx.graphics.getDeltaTime());
            if (!dn.isAlive) damageNumbers.remove(i);
            else dn.draw(spriteBatch);
        }

        spriteBatch.end();

        // 3️⃣ Render trees layer with dynamic opacity
        renderTreesWithTransparency();
    }

    private void renderTreesWithTransparency() {
        // Everything in pixel units
        TiledMapTileLayer treesLayer = (TiledMapTileLayer) tiledMap.getLayers().get(1);
        if (treesLayer == null) return;

        float tileWidth = treesLayer.getTileWidth();   // 32
        float tileHeight = treesLayer.getTileHeight(); // 32

        // Player position in pixels
        float playerX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float playerY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        float fadeRadius = 96f;  // around 3 tiles
        float minAlpha = 0.4f;

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        for (int x = 0; x < treesLayer.getWidth(); x++) {
            for (int y = 0; y < treesLayer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = treesLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) continue;

                TextureRegion region = cell.getTile().getTextureRegion();

                // Tile position in world units
                float tileWorldX = x * tileWidth / 32f;  // match your 1/32f scaling
                float tileWorldY = y * tileHeight / 32f;

                // Player position also needs scaling to world units
                float scaledPlayerX = playerX;
                float scaledPlayerY = playerY;

                // If player uses world units (not pixels), uncomment this:
                // float scaledPlayerX = playerX / 32f;
                // float scaledPlayerY = playerY / 32f;

                float dist = Vector2.dst(scaledPlayerX, scaledPlayerY,
                    tileWorldX + (tileWidth / 64f),
                    tileWorldY + (tileHeight / 64f));

                float alpha = 1f;
                if (dist < fadeRadius / 32f) {
                    alpha = MathUtils.lerp(minAlpha, 1f, dist / (fadeRadius / 32f));
                }

                spriteBatch.setColor(1f, 1f, 1f, alpha);
                spriteBatch.draw(region, tileWorldX, tileWorldY, tileWidth / 32f, tileHeight / 32f);
            }
        }

        spriteBatch.setColor(1f, 1f, 1f, 1f);
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height);

        float targetAspect = viewport.getWorldWidth() / viewport.getWorldHeight();
        float windowAspect = (float) width / height;

        camera.zoom = windowAspect > targetAspect ? windowAspect / targetAspect : targetAspect / windowAspect;
        camera.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}
