
package capstone.main.menus;

import capstone.main.UI.HeartsHud;
import capstone.main.Corrupted;
import capstone.main.Characters.*;
import capstone.main.Enemies.*;
import capstone.main.Managers.*;
import capstone.main.Logic.*;
import capstone.main.Render.*;
import capstone.main.Sprites.*;
import capstone.main.UI.SkillHud;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.ArrayList;

public class Game implements Screen {
    private final Corrupted game;
    private final int selectedCharacterIndex;

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
    private PunchLogic punchLogic;
    private EnemyLogic enemyLogic;
    private FireballLogic fireballLogic;

    // Renderers
    private WorldRenderer worldRenderer;
    private EntityRenderer entityRenderer;
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

    // Input
    private InputManager inputManager;
    private InputMultiplexer gameplayInputs;
    private InputProcessor globalInputProcessor;

    // FX & HUD
    private ArrayList<DamageNumber> damageNumbers;
    private BitmapFont damageFont;

    // HUD elements
    private HeartsHud heartsHud;
    private SkillHud skillHud;
    private Texture hpFrameTex;
    private Texture hpFillTex;

    // Back button (world-space)
    private Texture backBtnTexture;
    private Sprite backBtnSprite;

    // Weapon
    private Texture weaponTexture;
    private Sprite weaponSprite;

    // State
    private boolean isPaused = false;
    private boolean isGameOver = false;


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
        enemySpawner.spawnInitial(10);

        // --- Create player (with enemies available for Manny) ---
        player = createPlayer();

        // --- LOAD BACKGROUND MUSIC ---
        MusicManager musicManager = MusicManager.getInstance();
        musicManager.loadMusic("Music/World1_Music.mp3"); // Put your music file in assets/Music/
        musicManager.play();

        // --- STOP MENU MUSIC AND START WORLD 1 MUSIC ---
        musicManager.stop(); // Stop the menu/character selection music
        musicManager.dispose(); // Clear the old music
        musicManager.loadMusic("Music/world1_music.mp3"); // Load World 1 music
        musicManager.play(); // Start playing

        // --- LOAD CHARACTER SOUNDS ---
        SoundManager soundManager = SoundManager.getInstance();

        switch (selectedCharacterIndex) {
            case 1: // Manny Pacquiao
                soundManager.loadSound("manny_punch", "Sounds/manny_punch.mp3");
                soundManager.loadSound("manny_punch", "Sounds/manny_airpunch.mp3");
                //soundManager.loadSound("manny_skill1", "Sounds/manny_skill1.wav");
                soundManager.loadSound("manny_skill2", "Sounds/manny_skill2.mp3");
                //soundManager.loadSound("manny_skill3", "Sounds/manny_skill3.wav");
                //soundManager.loadSound("manny_hit", "Sounds/manny_hit.wav");
                weaponTexture = new Texture("fist.png"); // or melee weapon
                weaponSprite = new Sprite(weaponTexture);
                weaponSprite.setSize(0.75f, 0.75f);
                weaponSprite.setOrigin(weaponSprite.getWidth() / 2f, weaponSprite.getHeight() / 2f);
                break;
            case 2: // Quiboloy
                soundManager.loadSound("quiboloy_fireball", "Sounds/quiboloy_fireball.mp3");
                weaponTexture = new Texture("staff.png"); // fireball weapon
                weaponSprite = new Sprite(weaponTexture);
                weaponSprite.setSize(0.5f, 3f);
                weaponSprite.setOrigin(weaponSprite.getWidth() / 2f, weaponSprite.getHeight() / 2f);
                break;
            default: // Vico Sotto
                soundManager.loadSound("vico_shoot", "Sounds/vico_shoot.mp3");
                weaponTexture = new Texture("gun.png"); // bullet weapon
                weaponSprite = new Sprite(weaponTexture);
                weaponSprite.setSize(0.3f, 0.3f);
                weaponSprite.setOrigin(weaponSprite.getWidth() / 2f, weaponSprite.getHeight() / 2f);
                break;
        }

        // Load common sounds
        soundManager.loadSound("enemy_hit", "Sounds/enemy_hit.mp3");
        soundManager.loadSound("player_damage", "Sounds/player_damage.mp3");

        // --- Inputs ---
        inputManager = new InputManager();
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

            @Override
            public boolean keyUp(int keycode) {
                return false;
            }

            @Override
            public boolean keyTyped(char character) {
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return false;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                return false;
            }

            @Override
            public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
                return false;
            }
        };

        gameplayInputs = new InputMultiplexer();
        gameplayInputs.addProcessor(inputManager);
        gameplayInputs.addProcessor(globalInputProcessor);
        Gdx.input.setInputProcessor(gameplayInputs);

        // --- Movement/logic ---
        movementManager = new MovementManager(player);

        // Initialize Manny's skills with enemy list
        if (player instanceof MannyPacquiao) {
            ((MannyPacquiao) player).initializeSkills(enemySpawner.getEnemies(), damageNumbers, damageFont);
        }

        // Initialize bullet logic for ranged characters
        if (player instanceof Ranged) {
            bulletLogic = new BulletLogic((Ranged) player, enemySpawner.getEnemies(), damageNumbers, damageFont, physicsManager);
        } else {
            bulletLogic = null;
        }

        // Initialize Fireball logic
        if (player instanceof Quiboloy) {
            fireballLogic = new FireballLogic((Quiboloy) player, enemySpawner.getEnemies(), damageNumbers, damageFont, physicsManager);
        } else {
            fireballLogic = null;
        }


        // Now construct playerLogic with movementManager and bulletLogic present
        playerLogic = new PlayerLogic(player, inputManager, viewport, movementManager, bulletLogic, fireballLogic);
        enemyLogic = new EnemyLogic(enemySpawner, enemySpawner.getEnemies(), player);

        // --- Batches & renderers ---
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        backBtnTexture = new Texture("ui/Menu/back_button.png");
        backBtnSprite = new Sprite(backBtnTexture);
        backBtnSprite.setSize(1.5f, 1.5f);
        backBtnSprite.setPosition(0.5f, viewport.getWorldHeight() - 2f);

        worldRenderer = new WorldRenderer(mapManager.getRenderer());
        entityRenderer = new EntityRenderer(spriteBatch, shapeRenderer, player, enemySpawner.getEnemies(), damageNumbers);
        weaponRenderer = new WeaponRenderer(player, weaponSprite);

        // --- Pause UI ---
        pauseSkin = new Skin(Gdx.files.internal("uiskin.json"));
        pauseStage = new Stage(uiViewport, spriteBatch);

        // --- Game Over UI: initialize here so it exists if we set input to it
        gameOverSkin = new Skin(Gdx.files.internal("uiskin.json"));
        gameOverStage = new Stage(uiViewport, spriteBatch);

        // --- HUD ---
        heartsHud = new HeartsHud(uiViewport, spriteBatch, player);

        // Create skill HUD if player has skills
        if (player instanceof MannyPacquiao) {
            skillHud = new SkillHud(uiViewport, spriteBatch, player);
        }
    }


    @Override
    public void render(float delta) {

        // --- Update logic ---
        if (!isPaused && !isGameOver) {
            inputManager.update();
            physicsManager.step(delta);
            playerLogic.update(delta);

            if (bulletLogic != null) {
                bulletLogic.update(delta);

            }

            if (fireballLogic != null) {
                fireballLogic.update(delta);
            }

            enemyLogic.update(delta);
            screenShake.update(delta);
            entityRenderer.update(delta);

            if (player instanceof MannyPacquiao) {
                ((MannyPacquiao) player).updateSkills(delta);
            }
        } else if (isPaused) {
            if (pauseStage.getActors().size == 0) createPauseMenu();
            pauseStage.act(delta);
        }

        // --- Check Game Over ---
        if (!isPaused && player.isDead() && !isGameOver) {
            isGameOver = true;
            buildGameOverOverlay();
            Gdx.input.setInputProcessor(gameOverStage);
            return;
        }

        // --- Clear screen ---
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Draw Game Over Screen ---
        if (isGameOver) {
            gameOverStage.act(delta);
            gameOverStage.draw();
            return;
        }

        // --- World rendering ---
        updateCamera();
        updateWeaponAiming();
        viewport.apply();
        worldRenderer.renderGround(camera);
        entityRenderer.render(camera);

        if (bulletLogic != null) {
            bulletLogic.render(spriteBatch, camera);
        }

        if (fireballLogic != null) {
            fireballLogic.render(spriteBatch, camera);
        }

        weaponRenderer.render(spriteBatch);

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        backBtnSprite.draw(spriteBatch);
        spriteBatch.end();

        // --- UI HUD ---
        heartsHud.update(delta);
        heartsHud.draw();

        // Draw skill HUD
        if (skillHud != null) {
            skillHud.update(delta);
            skillHud.draw();
        }

        // --- Draw pause overlay ---
        if (isPaused) {
            // Draw semi-transparent overlay
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.6f); // 60% dark overlay
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);

            pauseStage.draw();
        }

        // --- Back button click ---
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

        // Main table
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Title
        Label titleLabel = new Label("PAUSED", pauseSkin);
        titleLabel.setFontScale(2f);
        table.add(titleLabel).padBottom(40f).colspan(2).row();

        // Get managers
        final MusicManager musicManager = MusicManager.getInstance();
        final SoundManager soundManager = SoundManager.getInstance();

        // --- MUSIC SECTION ---
        Table musicTable = new Table();
        musicTable.left();

        // Music label with status indicator
        final Label musicLabel = new Label("Music: " + (musicManager.isMusicEnabled() ? "ON" : "OFF"), pauseSkin);
        musicLabel.setColor(musicManager.isMusicEnabled() ? Color.GREEN : Color.RED);
        musicTable.add(musicLabel).padRight(10f);

        // Music toggle button
        final TextButton musicToggle = new TextButton(musicManager.isMusicEnabled() ? "ON" : "OFF", pauseSkin);
        musicToggle.getLabel().setFontScale(0.8f);
        musicToggle.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean newState = !musicManager.isMusicEnabled();
                musicManager.setMusicEnabled(newState);
                musicToggle.setText(newState ? "ON" : "OFF");
                musicLabel.setText("Music: " + (newState ? "ON" : "OFF"));
                musicLabel.setColor(newState ? Color.GREEN : Color.RED);
            }
        });
        musicTable.add(musicToggle).width(60f).height(30f);

        table.add(musicTable).left().padBottom(10f).colspan(2).row();

        // Music volume slider
        Label musicVolumeLabel = new Label("Music Volume:", pauseSkin);
        table.add(musicVolumeLabel).left().padRight(10f);

        final Slider musicSlider = new Slider(0f, 1f, 0.01f, false, pauseSkin);
        musicSlider.setValue(musicManager.getVolume());
        final Label musicPercentLabel = new Label(String.format("%.0f%%", musicManager.getVolume() * 100), pauseSkin);

        musicSlider.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                float value = musicSlider.getValue();
                musicManager.setVolume(value);
                musicPercentLabel.setText(String.format("%.0f%%", value * 100));
            }
        });

        Table musicSliderTable = new Table();
        musicSliderTable.add(musicSlider).width(150f).padRight(10f);
        musicSliderTable.add(musicPercentLabel).width(40f);
        table.add(musicSliderTable).left().padBottom(20f).row();

        // --- SOUND SECTION ---
        Table soundTable = new Table();
        soundTable.left();

        // Sound label with status indicator
        final Label soundLabel = new Label("Sound: " + (soundManager.isSoundEnabled() ? "ON" : "OFF"), pauseSkin);
        soundLabel.setColor(soundManager.isSoundEnabled() ? Color.GREEN : Color.RED);
        soundTable.add(soundLabel).padRight(10f);

        // Sound toggle button
        final TextButton soundToggle = new TextButton(soundManager.isSoundEnabled() ? "ON" : "OFF", pauseSkin);
        soundToggle.getLabel().setFontScale(0.8f);
        soundToggle.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean newState = !soundManager.isSoundEnabled();
                soundManager.setSoundEnabled(newState);
                soundToggle.setText(newState ? "ON" : "OFF");
                soundLabel.setText("Sound: " + (newState ? "ON" : "OFF"));
                soundLabel.setColor(newState ? Color.GREEN : Color.RED);
            }
        });
        soundTable.add(soundToggle).width(60f).height(30f);

        table.add(soundTable).left().padBottom(10f).colspan(2).row();

        // Sound volume slider
        Label soundVolumeLabel = new Label("Sound Volume:", pauseSkin);
        table.add(soundVolumeLabel).left().padRight(10f);

        final Slider soundSlider = new Slider(0f, 1f, 0.01f, false, pauseSkin);
        soundSlider.setValue(soundManager.getVolume());
        final Label soundPercentLabel = new Label(String.format("%.0f%%", soundManager.getVolume() * 100), pauseSkin);

        soundSlider.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                float value = soundSlider.getValue();
                soundManager.setVolume(value);
                soundPercentLabel.setText(String.format("%.0f%%", value * 100));
            }
        });

        Table soundSliderTable = new Table();
        soundSliderTable.add(soundSlider).width(150f).padRight(10f);
        soundSliderTable.add(soundPercentLabel).width(40f);
        table.add(soundSliderTable).left().padBottom(30f).row();

        // --- BUTTONS ---
        TextButton resumeButton = new TextButton("Resume", pauseSkin);
        resumeButton.getLabel().setFontScale(1.2f);
        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isPaused = false;
                Gdx.input.setInputProcessor(gameplayInputs);
            }
        });
        table.add(resumeButton).width(180f).height(50f).padBottom(15f).colspan(2).row();

        TextButton backButton = new TextButton("Back to Menu", pauseSkin);
        backButton.getLabel().setFontScale(1.2f);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                VideoSettings.apply();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        table.add(backButton).width(180f).height(50f).colspan(2);

        pauseStage.addActor(table);
        Gdx.input.setInputProcessor(pauseStage);
    }


    private void buildGameOverOverlay() {
        gameOverStage = new Stage(uiViewport, spriteBatch);

        // Root table (full screen)
        Table root = new Table();
        root.setFillParent(true);
        gameOverStage.addActor(root);

        // --- TRANSPARENT BACKGROUND ---
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        Image overlay = new Image(texture);
        overlay.setColor(0, 0, 0, 0.45f); // 45% dark transparent
        overlay.setFillParent(true);
        overlay.getColor().a = 0f; // start invisible
        overlay.addAction(Actions.fadeIn(0.25f)); // smooth fade
        root.addActor(overlay);

        // --- UI CONTENT CONTAINER ---
        Table content = new Table();
        content.defaults().pad(10f);
        content.setFillParent(true);

        // GAME OVER TEXT
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = new BitmapFont();
        style.fontColor = Color.WHITE;
        style.font.getData().setScale(3f); // Make text big
        Label gameOverLabel = new Label("GAME OVER", style);
        gameOverLabel.getColor().a = 0;
        gameOverLabel.addAction(Actions.fadeIn(0.4f));

        // BUTTONS (use skin for proper styling)
        TextButton retryBtn = new TextButton("Retry", gameOverSkin);
        TextButton quitBtn = new TextButton("Quit", gameOverSkin);

        // Increase button font size
        retryBtn.getLabel().setFontScale(2f);
        quitBtn.getLabel().setFontScale(2f);

        // Button logic
        retryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new Game(game, selectedCharacterIndex)); // restart game
                dispose();
            }
        });
        quitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game)); // go to menu
                dispose();
            }
        });

        // Layout
        content.add(gameOverLabel).padBottom(40f).row();
        content.add(retryBtn).width(200f).height(50f).padBottom(20f).row();
        content.add(quitBtn).width(200f).height(50f);
        root.addActor(content);
    }


    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
        uiViewport.update(width, height, true);
        heartsHud.resize(width, height);
        if (skillHud != null) skillHud.resize(width, height);
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
        isPaused = true;
        Gdx.input.setInputProcessor(pauseStage);
    }

    @Override
    public void dispose() {
        try {
            if (spriteBatch != null) spriteBatch.dispose();
        } catch (Exception ignored) {
        }
        try {
            if (shapeRenderer != null) shapeRenderer.dispose();
        } catch (Exception ignored) {
        }
        try {
            if (weaponTexture != null) weaponTexture.dispose();
        } catch (Exception ignored) {
        }
        try {
            if (backBtnTexture != null) backBtnTexture.dispose();
        } catch (Exception ignored) {
        }
        try {
            if (damageFont != null) damageFont.dispose();
        } catch (Exception ignored) {
        }
        try {
            if (pauseStage != null) pauseStage.dispose();
        } catch (Exception ignored) {
        }
        try {
            if (pauseSkin != null) pauseSkin.dispose();
        } catch (Exception ignored) {
        }
        try {
            if (gameOverStage != null) gameOverStage.dispose();
        } catch (Exception ignored) {
        }
        try {
            if (gameOverSkin != null) gameOverSkin.dispose();
        } catch (Exception ignored) {
        }
        if (physicsManager != null) physicsManager.dispose();
        if (mapManager != null) mapManager.dispose();
        if (heartsHud != null) heartsHud.dispose();
        if (player != null) player.dispose();
        if (skillHud != null) skillHud.dispose();

        // Stop World 1 music when leaving game screen
        MusicManager.getInstance().stop();
    }


    private AbstractPlayer createPlayer() {
        switch (selectedCharacterIndex) {
            case 1:
                // Manny Pacquiao - Melee fighter
                return new MannyPacquiao(
                    120,           // healthPoints
                    80,            // manaPoints
                    8,             // baseDamage
                    12,            // maxDamage
                    1.5f,          // attackSpeed
                    9f,            // x
                    9f,            // y
                    2f,            // width
                    2f,            // height
                    enemySpawner.getEnemies(),
                    damageNumbers,
                    damageFont,
                    mapWidth,
                    mapHeight,
                    physicsManager.getWorld(),
                    screenShake
                );
            case 2:
                // Quiboloy - Ranged fireball user
                ArrayList<Fireball> fireballs = new ArrayList<>(); // create list for fireballs
                return new Quiboloy(
                    120,           // healthPoints
                    80,            // manaPoints
                    10,             // baseDamage
                    15,             // maxDamage
                    1f,           // attackSpeed
                    9f,            // x
                    9f,            // y
                    2f,            // width
                    2f,            // height
                    fireballs,     // fireballs list
                    mapWidth,
                    mapHeight,
                    physicsManager.getWorld(),
                    screenShake
                );
            default:
                // Vico Sotto - Ranged bullets
                ArrayList<Bullet> bullets = new ArrayList<>();
                return new VicoSotto(
                    60,            // healthPoints
                    80,            // manaPoints
                    5,             // baseDamage
                    8,             // maxDamage
                    4f,            // attackSpeed
                    9f,            // x
                    9f,            // y
                    2f,            // width
                    2f,            // height
                    bullets,       // bullets list
                    mapWidth,
                    mapHeight,
                    physicsManager.getWorld(),
                    screenShake
                );
        }
    }
}
