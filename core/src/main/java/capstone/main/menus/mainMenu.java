package capstone.main.menus;

import capstone.main.Characters.*;
import capstone.main.Handlers.DirectionManager;
import capstone.main.Handlers.MovementManager;
import capstone.main.Sprites.Bullet;
import capstone.main.Corrupted;
import capstone.main.Handlers.InputManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

public class mainMenu implements Screen {

    Corrupted game;
    private SpriteBatch spriteBatch;
    private Viewport viewport;
    private OrthographicCamera camera;

    private Texture backgroundTexture;
    TextureRegion backgroundRegion;

    Texture weaponTexture;
    private Sprite weaponSprite;
    private float weaponAimingRad;

    ArrayList<Bullet> bullets;

    private AbstractPlayer player;
    private InputManager inputManager;
    private MovementManager movementManager;

    private final float worldWidth = 20f;
    private final float worldHeight = 12f;

    public mainMenu(Corrupted game) {
        this.game = game;
    }

    @Override
    public void show() {
        backgroundTexture = new Texture("checkeredBackground.png");
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        backgroundRegion = new TextureRegion(backgroundTexture, 0, 0,
            backgroundTexture.getWidth() * 2, backgroundTexture.getHeight() * 2);

        weaponTexture = new Texture("gun.png");
        weaponSprite = new Sprite(weaponTexture);
        // Set sprite size first
        weaponSprite.setSize(.2f, 0.2f);  // example size
        weaponSprite.setOrigin(1f, -3f);

        bullets = new ArrayList<>();

        inputManager = new InputManager();
        movementManager = new MovementManager();

        // Default character
        player = new VicoSotto(5f, 3f, 1f, 1f, bullets);

        // Uncomment to switch characters
        // player = new Warrior(5f, 3f, 1f, 1f, bullets);
        // player = new SimoneQuiboloy(5f, 3f, 1f, 1f, bullets);

        spriteBatch = new SpriteBatch();
        viewport = new ExtendViewport(10, 6);
        camera = (OrthographicCamera) viewport.getCamera();

        float centerX = (viewport.getWorldWidth() - weaponSprite.getWidth()) / 2f;
        float centerY = (viewport.getWorldHeight() - weaponSprite.getHeight()) / 2f;
        weaponSprite.setPosition(centerX, centerY);
    }


    @Override
    public void render(float delta) {
        inputManager.update();


        // Update player movement + dodge + sprint + boundary + facing
        player.update(delta, inputManager, movementManager, viewport);

        // Update weapon aiming
        updateWeaponAiming();

        // Handle ranged attacks if applicable
        if (player instanceof Ranged) {
            Ranged r = (Ranged) player;
            if (inputManager.isAttacking()) r.handleAttack(weaponAimingRad, delta, movementManager);
            r.updateBullets(delta);
        }

        updateCamera();
        draw();
    }


    private void updateWeaponAiming() {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();
        Vector3 worldCoords = viewport.getCamera().unproject(new Vector3(mouseX, mouseY, 0));

        float charX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float charY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        float angleRad = (float) Math.atan2(worldCoords.y - charY, worldCoords.x - charX);
        weaponAimingRad = angleRad;

        // 🧱 Weapon rotation + position update only
        weaponSprite.setOrigin(weaponSprite.getWidth() / 2f, weaponSprite.getHeight() * 0.25f);
        weaponSprite.setRotation((float) Math.toDegrees(angleRad));

        float gap = 0.1f;
        float weaponCenterX = charX + (float) Math.cos(angleRad) * gap;
        float weaponCenterY = charY + (float) Math.sin(angleRad) * gap;

        weaponSprite.setPosition(
            weaponCenterX - weaponSprite.getOriginX(),
            weaponCenterY - weaponSprite.getOriginY()
        );
    }

    private void updateCamera() {
        float halfViewWidth = viewport.getWorldWidth()/2f;
        float halfViewHeight = viewport.getWorldHeight()/2f;

        float cameraX = MathUtils.clamp(player.getSprite().getX() + player.getSprite().getWidth()/2f,
            halfViewWidth, worldWidth - halfViewWidth);
        float cameraY = MathUtils.clamp(player.getSprite().getY() + player.getSprite().getHeight()/2f,
            halfViewHeight, worldHeight - halfViewHeight);

        camera.position.set(cameraX, cameraY, 0);
        camera.update();
    }

    private void draw() {
        Gdx.gl.glClearColor(1,1,1,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        float camX = camera.position.x - viewport.getWorldWidth()/2f;
        float camY = camera.position.y - viewport.getWorldHeight()/2f;
        float tileRepeatX = 2f, tileRepeatY = 2f;

        float u1 = camX/worldWidth * tileRepeatX;
        float v1 = camY/worldHeight * tileRepeatY;
        float u2 = (camX + viewport.getWorldWidth())/worldWidth * tileRepeatX;
        float v2 = (camY + viewport.getWorldHeight())/worldHeight * tileRepeatY;

        spriteBatch.draw(backgroundTexture, camX, camY,
            viewport.getWorldWidth(), viewport.getWorldHeight(),
            u1, v1, u2, v2);

        player.getSprite().draw(spriteBatch);
        weaponSprite.draw(spriteBatch);

        if (player instanceof Ranged) {
            Ranged r = (Ranged) player;
            for (Bullet b : r.getBullets()) b.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;

        viewport.update(width, height);

        float targetAspect = viewport.getWorldWidth() / viewport.getWorldHeight();
        float windowAspect = (float) width / height;

        if (windowAspect > targetAspect) {
            // window is wider than world: zoom out horizontally
            camera.zoom = windowAspect / targetAspect;
        } else {
            // window is taller than world: zoom out vertically
            camera.zoom = targetAspect / windowAspect;
        }

        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
