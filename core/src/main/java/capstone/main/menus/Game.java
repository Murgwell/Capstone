
package capstone.main.menus;

import capstone.main.Pathfinding.NavMesh;
import capstone.main.UI.HeartsHud;
import capstone.main.UI.InventoryUI;
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
import com.badlogic.gdx.math.Rectangle;
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
    // Logs player world coordinates every 0.5s to console
    private float playerPosLogTimer = 0f;
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
    private capstone.main.Managers.UIManager uiManager;

    // Inventory system
    private Inventory inventory;
    private InventoryUI inventoryUI;
    private boolean isInventoryOpen = false;

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
    private boolean isGameOver = false;

    // World transitions
    private WorldMapManager worldMapManager;
    private java.util.List<PortalRect> portals = new java.util.ArrayList<>();
    private float portalCooldown = 0f;
    private boolean bossSpawnedInCurrentWorld = false;
    private boolean bossDefeatedInCurrentWorld = false;
    private boolean bossClearedToastShown = false;

    // Debug
    private boolean debugPortals = false;

    // Teleportation FX
    private boolean teleportFxActive = false;
    private float teleportFxTimer = 0f;
    private final float teleportFxDuration = 1.2f; // seconds, more dramatic

    private boolean teleportFxUseBlack = true; // set to true for simple black transition

    // Teleport FX particles
    private static class TeleportParticle {
        float x, y, vx, vy, life, maxLife, size, r, g, b, a;
    }
    private final java.util.ArrayList<TeleportParticle> teleportFxParticles = new java.util.ArrayList<>();

    private void spawnTeleportFxBurst() {
        teleportFxParticles.clear();
        float cx = Gdx.graphics.getWidth() / 2f;
        float cy = Gdx.graphics.getHeight() / 2f;
        java.util.concurrent.ThreadLocalRandom rng = java.util.concurrent.ThreadLocalRandom.current();
        int count = 140;
        for (int i = 0; i < count; i++) {
            TeleportParticle p = new TeleportParticle();
            double ang = rng.nextDouble() * Math.PI * 2;
            double speed = 220 + rng.nextDouble() * 880; // px/s
            p.vx = (float)(Math.cos(ang) * speed);
            p.vy = (float)(Math.sin(ang) * speed);
            p.x = cx + (float)(Math.cos(ang) * 6);
            p.y = cy + (float)(Math.sin(ang) * 6);
            p.maxLife = 0.45f + rng.nextFloat() * 0.65f;
            p.life = p.maxLife;
            p.size = 2.5f + rng.nextFloat() * 3.5f;
            // cool cyan-magenta palette
            float huePick = rng.nextFloat();
            if (huePick < 0.5f) { p.r = 0.3f; p.g = 1.0f; p.b = 0.95f; } else { p.r = 1.0f; p.g = 0.3f; p.b = 0.8f; }
            p.a = 1f;
            teleportFxParticles.add(p);
        }
    }

    private static class PortalRect {
        com.badlogic.gdx.math.Rectangle rect;
        String targetMap;
        Float spawnX; // optional override
        Float spawnY; // optional override
        PortalRect(com.badlogic.gdx.math.Rectangle r, String targetMap, Float spawnX, Float spawnY) {
            this.rect = r; this.targetMap = targetMap; this.spawnX = spawnX; this.spawnY = spawnY;
        }
    }


    public Game(Corrupted game, int selectedCharacterIndex) {
        this.game = game;
        this.selectedCharacterIndex = selectedCharacterIndex;
    }

    @Override
    public void show() {
        // --- Physics & Map ---
        physicsManager = new PhysicsManager();
        mapManager = new MapManager(physicsManager);

        // --- World Map Manager ---
        worldMapManager = new WorldMapManager();
        worldMapManager.setCurrentWorld(WorldMapManager.WorldMap.WORLD_1);
        bossSpawnedInCurrentWorld = false;
        bossDefeatedInCurrentWorld = false;
        bossClearedToastShown = false;
        mapManager.load(worldMapManager.getCurrentWorldPath());
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
        damageFont.getData().setScale(0.1f); // Reverted back to original 0.1f

        // Set up damage number system for physics collisions (bullets/fireballs)
        physicsManager.setDamageNumberSystem(damageNumbers, damageFont);

        // MEMORY FIX: Create smaller, more efficient NavMesh
        // Instead of using full map size, use a reasonable maximum
        int maxNavMeshSize = 200; // Limit to 200x200 = 40k nodes max (vs potentially millions)
        int navWidth = Math.min(maxNavMeshSize, (int) mapManager.getWorldWidth());
        int navHeight = Math.min(maxNavMeshSize, (int) mapManager.getWorldHeight());

        System.out.println("========== NAVMESH DEBUG ==========");
        System.out.println("Original Map Size: " + mapManager.getWorldWidth() + " x " + mapManager.getWorldHeight());
        System.out.println("NavMesh Size: " + navWidth + " x " + navHeight + " = " + (navWidth * navHeight) + " nodes");
        System.out.println("Memory estimate: ~" + ((navWidth * navHeight * 200) / 1024 / 1024) + " MB");
        System.out.println("===================================");

        NavMesh navMesh = new NavMesh(navWidth, navHeight,
            CollisionLoader.getCollisionRectangles(mapManager.getTiledMap(), "collisionLayer", 1 / 32f));

        // --- Create enemy spawner ---
        enemySpawner = new EnemySpawner(mapWidth, mapHeight, screenShake, physicsManager, navMesh);
        enemySpawner.setWorldSize(mapWidth, mapHeight); // ensure spawner knows world bounds
        enemySpawner.setCurrentWorld(worldMapManager.getCurrentWorldPath()); // Set current world for world-specific spawning
        enemySpawner.setCollisionMap(mapManager.getTiledMap()); // Set collision map for proper spawn detection
        enemySpawner.spawnInitial(worldMapManager.getEnemyCount(worldMapManager.getCurrentWorld()));

        // --- Create player (with enemies available for Manny) ---
        player = createPlayer();

        // --- LOAD BACKGROUND MUSIC ---
        MusicManager musicManager = MusicManager.getInstance();
        musicManager.loadMusic("Music/World1_Music.mp3"); // Put your music file in assets/Music/
        musicManager.play();

        // --- STOP MENU MUSIC AND START WORLD 1 MUSIC ---
        musicManager.stop(); // Stop the menu/character selection music
        musicManager.dispose(); // Clear the old music
        musicManager.loadMusic("Music/World1_Music.mp3"); // Load World 1 music (corrected filename)
        musicManager.play(); // Start playing

        // --- LOAD CHARACTER SOUNDS ---
        SoundManager soundManager = SoundManager.getInstance();

        switch (selectedCharacterIndex) {
            case 1: // Manny Pacquiao
                soundManager.loadSound("manny_airpunch", "Sounds/manny_airpunch.mp3");
                soundManager.loadSound("manny_punch", "Sounds/manny_punch.mp3");
                soundManager.loadSound("manny_skill1", "Sounds/manny_skill1.mp3");
                soundManager.loadSound("manny_skill2", "Sounds/manny_skill2.wav");
                soundManager.loadSound("manny_ult", "Sounds/manny_ult.mp3");
                weaponTexture = new Texture("fist.png"); // melee weapon
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
                    if (isInventoryOpen) {
                        // Close inventory first
                        isInventoryOpen = false;
                        inventoryUI.toggle();
                        Gdx.input.setInputProcessor(gameplayInputs);
                        return true;
                    } else {
                        // Toggle pause menu
                        isPaused = !isPaused;
                        Gdx.input.setInputProcessor(isPaused ? pauseStage : gameplayInputs);
                        return true;
                    }
                }
                if (keycode == com.badlogic.gdx.Input.Keys.I) {
                    if (!isPaused && !isGameOver) {
                        inventoryUI.toggle();
                        return true;
                    }
                }
                if (keycode == com.badlogic.gdx.Input.Keys.F9) {
                    // Debug: log player position for choosing spawn points
                    float sx = player.getSprite().getX();
                    float sy = player.getSprite().getY();
                    float cx = player.getBody().getPosition().x;
                    float cy = player.getBody().getPosition().y;
                    int px = Math.round(sx * 32f);
                    int py = Math.round(sy * 32f);
                    Gdx.app.log("SpawnDebug", String.format("sprite=(%.2f,%.2f) center=(%.2f,%.2f) pixels=(%d,%d)", sx, sy, cx, cy, px, py));
                    return true;
                }
                if (keycode == com.badlogic.gdx.Input.Keys.F7) {
                        // Toggle portal debug overlay
                        debugPortals = !debugPortals;
                        Gdx.app.log("Portals", "debugPortals=" + debugPortals);
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.F8) {
                    if (!isPaused && !isGameOver) {
                        // Trigger portal-like FX and jump to World1_Boss immediately
                        teleportFxActive = true;
                        teleportFxTimer = 0f;
                        teleportFxUseBlack = true; // use black transition on F8
                        teleportFxParticles.clear();
                        transitionToMap(WorldMapManager.WorldMap.WORLD_1_BOSS.getFilePath(), null, null);
                        return true;
                    }
                }
                if (keycode == com.badlogic.gdx.Input.Keys.F11) {
                    boolean newFs = !VideoSettings.isFullscreen();
                    VideoSettings.setFullscreen(newFs);
                    VideoSettings.apply();
                    return true;
                }

                // Quick access hotkeys (1-4)
                if (!isPaused && !isGameOver && !isInventoryOpen) {
                    if (keycode == com.badlogic.gdx.Input.Keys.NUM_1) {
                        useQuickAccessItem(0);
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.NUM_2) {
                        useQuickAccessItem(1);
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.NUM_3) {
                        useQuickAccessItem(2);
                        return true;
                    }
                    if (keycode == com.badlogic.gdx.Input.Keys.NUM_4) {
                        useQuickAccessItem(3);
                        return true;
                    }
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
        // Ensure back button texture is available; create a fallback if asset missing
        if (backBtnTexture == null) {
            try {
                backBtnTexture = new Texture(Gdx.files.internal("ui/back_button.png"));
            } catch (Exception ex) {
                Pixmap pm = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
                pm.setColor(1, 1, 1, 1);
                pm.fillRectangle(0, 0, 32, 32);
                backBtnTexture = new Texture(pm);
                pm.dispose();
            }
        }
        backBtnSprite = new Sprite(backBtnTexture);
        backBtnSprite.setSize(1.5f, 1.5f);
        backBtnSprite.setPosition(0.5f, viewport.getWorldHeight() - 2f);

        worldRenderer = new WorldRenderer(mapManager.getRenderer(), mapManager.getTiledMap());
        // Load any portal rectangles from the current map
        loadPortalsFromMap();
        entityRenderer = new EntityRenderer(spriteBatch, shapeRenderer, player, enemySpawner.getEnemies(), damageNumbers);
        weaponRenderer = new WeaponRenderer(player, weaponSprite);

        // --- UI Manager (Boss UI, etc.) ---
        uiManager = new UIManager(uiViewport, spriteBatch, damageFont);

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

        // --- Inventory System ---
        inventory = new Inventory();

        // Add some test items (only bandages now)
        inventory.addItem("Bandage", "Textures/UI/Inventory/Objects/Icon_Bandage.png", 5);

        // Set up quick access slots
        inventory.setQuickAccessSlot(0, 0); // Bandages in slot 1

        inventoryUI = new InventoryUI(uiViewport, spriteBatch, inventory);
        inventoryUI.setOnToggleCallback(() -> {
            isInventoryOpen = inventoryUI.isOpen();
            if (isInventoryOpen) {
                Gdx.input.setInputProcessor(inventoryUI.getStage());
            } else {
                Gdx.input.setInputProcessor(gameplayInputs);
            }
        });

        // Set item use callback
        inventoryUI.setOnItemUseCallback(item -> {
            if (item.getType() == Inventory.ItemType.HEALING) {
                // Heal the player
                int oldHp = player.getHp();
                player.heal(item.getHealAmount());
                int newHp = player.getHp();
                int actualHealing = newHp - oldHp;

                // Show healing feedback
                Gdx.app.log("Inventory", "Used " + item.getName() + " - Healed for " + actualHealing + " HP (" + newHp + "/" + player.getMaxHp() + ")");
            }
        });

        // Set health check callback
        inventoryUI.setHealthCheckCallback(() -> player.getHp() < player.getMaxHp());

        // Set floating text callback
        inventoryUI.setFloatingTextCallback((text, r, g, b) -> {
            // Create floating text above the player
            float playerX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
            float playerY = player.getSprite().getY() + player.getSprite().getHeight() + 1f;
            Color textColor = new Color(r / 255f, g / 255f, b / 255f, 1f);

            damageNumbers.add(new DamageNumber(text, playerX, playerY, damageFont, textColor));
        });
    }

    private void useQuickAccessItem(int quickSlotIndex) {
        // Check if healing item and health is full before consuming
        int inventoryIndex = inventory.getQuickAccessSlot(quickSlotIndex);
        if (inventoryIndex >= 0) {
            Inventory.InventoryItem item = inventory.getItem(inventoryIndex);
            if (item != null && item.getType() == Inventory.ItemType.HEALING) {
                if (player.getHp() >= player.getMaxHp()) {
                    Gdx.app.log("Inventory", "Health is already full! Cannot use " + item.getName() + " from quick slot " + (quickSlotIndex + 1));

                    // Show floating text above player
                    float playerX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
                    float playerY = player.getSprite().getY() + player.getSprite().getHeight() + 1f;
                    Color textColor = new Color(1f, 0.4f, 0.4f, 1f); // Red color
                    damageNumbers.add(new DamageNumber("Health is full!", playerX, playerY, damageFont, textColor));

                    return; // Don't consume the item
                }
            }
        }

        Inventory.InventoryItem usedItem = inventory.useQuickAccessItem(quickSlotIndex);
        if (usedItem != null) {
            if (usedItem.getType() == Inventory.ItemType.HEALING) {
                // Heal the player
                int oldHp = player.getHp();
                player.heal(usedItem.getHealAmount());
                int newHp = player.getHp();
                int actualHealing = newHp - oldHp;

                // Show healing feedback
                Gdx.app.log("Inventory", "Used " + usedItem.getName() + " from quick slot " + (quickSlotIndex + 1) + " - Healed for " + actualHealing + " HP (" + newHp + "/" + player.getMaxHp() + ")");
            }
        }
    }

    @Override
    public void render(float delta) {
        // Throttled player position logger (every 0.5s)
        if (playerPosLogTimer == Float.NaN) { // ensure initialized
            playerPosLogTimer = 0f;
        }
        // MEMORY DEBUG: Monitor memory usage every 5 seconds
        if (System.currentTimeMillis() % 5000 < 16) {
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
            System.out.println("=== MEMORY DEBUG ===");
            System.out.println("Used Memory: " + usedMemory + " MB");
            System.out.println("Total Enemies: " + enemySpawner.getEnemies().size());
            System.out.println("==================");
        }

        // --- Update logic ---
        if (teleportFxActive) {
            teleportFxTimer += delta;
            // particles update
            for (int i = teleportFxParticles.size() - 1; i >= 0; i--) {
                TeleportParticle p = teleportFxParticles.get(i);
                p.life -= delta;
                if (p.life <= 0f) { teleportFxParticles.remove(i); continue; }
                p.x += p.vx * delta;
                p.y += p.vy * delta;
            }
            if (teleportFxTimer >= teleportFxDuration) {
                teleportFxActive = false;
                teleportFxParticles.clear();
            }
        }
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
            // Decrease portal cooldown if active
            if (portalCooldown > 0f) {
                portalCooldown = Math.max(0f, portalCooldown - delta);
            }
            // Accumulate time and print player position every 0.5s
            playerPosLogTimer += delta;
            if (playerPosLogTimer >= 0.5f) {
                float px = player.getSprite().getX();
                float py = player.getSprite().getY();
                com.badlogic.gdx.math.Vector2 bc = player.getBody().getPosition();
                System.out.println(String.format("PLAYER POSITION: sprite=(%.3f, %.3f) bodyCenter=(%.3f, %.3f)", px, py, bc.x, bc.y));
                playerPosLogTimer = 0f;
            }

            // Check for portal overlap after movement update
            // Keep boss reference fresh before gating portals
            uiManager.updateBossReference(enemySpawner.getEnemies());
            // Update boss spawn/death flags robustly
            capstone.main.Enemies.BossEntity bossRef = uiManager.getBossManager().getCurrentBoss();
            if (bossRef != null) {
                bossSpawnedInCurrentWorld = true;
                if (bossRef.isDead()) bossDefeatedInCurrentWorld = true;
            } else {
                // If a boss had spawned earlier but is now gone, consider it defeated
                // as BossManager likely cleared the reference upon death/removal
                if (bossSpawnedInCurrentWorld) {
                    // Double-check enemy list has no remaining bosses
                    boolean anyBossLeft = false;
                    for (Object e : enemySpawner.getEnemies()) {
                        if (e instanceof capstone.main.Enemies.BossEntity) { anyBossLeft = true; break; }
                    }
                    if (!anyBossLeft) bossDefeatedInCurrentWorld = true;
                }
            }
            // Show a one-time cleared toast once boss is defeated in boss worlds
            capstone.main.Managers.WorldMapManager.WorldMap curWorldTmp = worldMapManager.getCurrentWorld();
            boolean isBossWorldTmp = (curWorldTmp == capstone.main.Managers.WorldMapManager.WorldMap.WORLD_1_BOSS
                || curWorldTmp == capstone.main.Managers.WorldMapManager.WorldMap.WORLD_2_BOSS
                || curWorldTmp == capstone.main.Managers.WorldMapManager.WorldMap.WORLD_3_BOSS);
            if (isBossWorldTmp && bossDefeatedInCurrentWorld && !bossClearedToastShown) {
                uiManager.showToast("Area cleared! Portal unlocked.", 1.6f);
                bossClearedToastShown = true;
            }
            checkPortalsAndMaybeTransition();

            if (player instanceof MannyPacquiao) {
                ((MannyPacquiao) player).updateSkills(delta);
                ((MannyPacquiao) player).updatePunchAnimation(delta);
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
        // Teleportation FX: black transition (cinematic fade with vignette)
        if (teleportFxActive) {
            float t = Math.min(1f, teleportFxTimer / teleportFxDuration);
            float w = Gdx.graphics.getWidth();
            float h = Gdx.graphics.getHeight();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(uiViewport.getCamera().combined);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            // ease in/out to make it feel smoother
            float k = (float)(1.0 - Math.cos(Math.PI * t)) * 0.5f; // 0..1
            float alpha = Math.min(1f, 1.2f * k);
            // full-screen black
            shapeRenderer.setColor(0f, 0f, 0f, alpha);
            shapeRenderer.rect(0, 0, w, h);

            // subtle inward vignette near the end
            float cx = w / 2f;
            float cy = h / 2f;
            float ringR = (0.25f + 0.75f * k) * Math.max(w, h);
            float ringAlpha = 0.35f * (1f - k);
            shapeRenderer.setColor(0f, 0f, 0f, ringAlpha);
            int steps = 42;
            for (int i = 0; i < steps; i++) {
                float ang0 = (i / (float)steps) * 6.2831853f;
                float ang1 = ((i + 1) / (float)steps) * 6.2831853f;
                float x0 = cx + (float)Math.cos(ang0) * ringR;
                float y0 = cy + (float)Math.sin(ang0) * ringR;
                float x1 = cx + (float)Math.cos(ang1) * ringR;
                float y1 = cy + (float)Math.sin(ang1) * ringR;
                shapeRenderer.triangle(cx, cy, x0, y0, x1, y1);
            }
            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        updateCamera();
        updateWeaponAiming();
        viewport.apply();
        worldRenderer.render(camera);
        entityRenderer.render(camera);
        // Debug draw portal rectangles
        if (debugPortals && portals != null) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 1f, 0f, 0.25f);
            for (PortalRect p : portals) {
                // Draw inflated portal rect to match overlap logic
                float x = p.rect.x - 0.1f;
                float y = p.rect.y - 0.1f;
                float w = p.rect.width + 0.2f;
                float h = p.rect.height + 0.6f;
                shapeRenderer.rect(x, y, w, h);
            }
            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        // Update and render boss UI (Scene2D, screen-space)
        uiManager.updateBossReference(enemySpawner.getEnemies());
        uiManager.actAndDraw(delta);

        if (bulletLogic != null) {
            bulletLogic.render(spriteBatch, camera);
        }

        if (fireballLogic != null) {
            fireballLogic.render(spriteBatch, camera);
        }

        weaponRenderer.update();
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

        // --- Inventory System ---
        inventoryUI.update(delta);
        inventoryUI.draw();

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

    private void loadPortalsFromMap() {
        portals.clear();
        com.badlogic.gdx.maps.MapLayer layer = mapManager.getTiledMap().getLayers().get("portals");
        if (layer == null) {
            Gdx.app.log("Portals", "No 'portals' layer found in map " + worldMapManager.getCurrentWorldPath());
            return;
        }
        float scale = 1 / 32f;
        for (com.badlogic.gdx.maps.MapObject obj : layer.getObjects()) {
            if (obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject) {
                com.badlogic.gdx.math.Rectangle r = ((com.badlogic.gdx.maps.objects.RectangleMapObject) obj).getRectangle();
                com.badlogic.gdx.math.Rectangle worldRect = new com.badlogic.gdx.math.Rectangle(r.x * scale, r.y * scale, r.width * scale, r.height * scale);
                // Inflate upward to make portals more forgiving
                worldRect.y -= 0.1f;
                worldRect.height += 0.9f;
                String target = obj.getProperties().get("target", String.class);
                Float sx = obj.getProperties().containsKey("spawnX") ? Float.parseFloat(obj.getProperties().get("spawnX").toString()) : null;
                Float sy = obj.getProperties().containsKey("spawnY") ? Float.parseFloat(obj.getProperties().get("spawnY").toString()) : null;
                if (target != null && !target.isEmpty()) {
                    // Disable reverse portal from World1_Boss -> World1
                    String currentPath = worldMapManager.getCurrentWorldPath();
                    if (currentPath != null && currentPath.contains("World1_Boss") && target.contains("Textures/World1.tmx")) {
                        Gdx.app.log("Portals", "Skipping reverse portal back to World1 from World1_Boss");
                    } else {
                        portals.add(new PortalRect(worldRect, target, sx, sy));
                        String trimmed = target.trim();
                        if (!trimmed.equals(target)) {
                            Gdx.app.log("Portals", "WARNING: target has trailing/leading whitespace: '" + target + "' -> '" + trimmed + "'");
                        }
                        Gdx.app.log("Portals", "Loaded portal -> target=" + target + ", spawnX=" + sx + ", spawnY=" + sy + ", rect=" + worldRect);
                    }
                }
            }
        }
        Gdx.app.log("Portals", "Total portals loaded: " + portals.size());
    }

    private void checkPortalsAndMaybeTransition() {
        // Block portal usage if a boss exists and is not dead
        BossEntity boss = uiManager.getBossManager().getCurrentBoss();
        // Gate portals only in boss worlds (e.g., World1_Boss)
        capstone.main.Managers.WorldMapManager.WorldMap curWorld = worldMapManager.getCurrentWorld();
        boolean isBossWorld = (curWorld == capstone.main.Managers.WorldMapManager.WorldMap.WORLD_1_BOSS
            || curWorld == capstone.main.Managers.WorldMapManager.WorldMap.WORLD_2_BOSS
            || curWorld == capstone.main.Managers.WorldMapManager.WorldMap.WORLD_3_BOSS);

        // Determine spawn/clear state from live enemies each frame (source of truth)
        boolean anyBossPresent = false;
        boolean anyBossAlive = false;
        for (Object e : enemySpawner.getEnemies()) {
            if (e instanceof capstone.main.Enemies.BossEntity) {
                anyBossPresent = true;
                if (!((capstone.main.Enemies.BossEntity)e).isDead()) anyBossAlive = true;
            }
        }
        boolean bossSpawned = anyBossPresent || bossSpawnedInCurrentWorld; // remember if we saw one earlier
        boolean bossCleared = bossSpawned && !anyBossAlive; // spawned at some point and no alive bosses now
        // Persist flags
        if (bossSpawned) bossSpawnedInCurrentWorld = true;
        if (bossCleared) bossDefeatedInCurrentWorld = true;

        // If in a boss world and boss either not spawned yet OR spawned but not dead, block with toast when entering portal area
        if (isBossWorld && (!bossSpawned || !bossCleared)) {
            // Only show toast if player is attempting to walk into any portal area
            float sx = player.getSprite().getX();
            float sy = player.getSprite().getY();
            float sw = player.getSprite().getWidth();
            float sh = player.getSprite().getHeight();
            com.badlogic.gdx.math.Rectangle playerRect = new com.badlogic.gdx.math.Rectangle(sx, sy, sw, sh);
            for (PortalRect p : portals) {
                com.badlogic.gdx.math.Rectangle expandedPortal = new com.badlogic.gdx.math.Rectangle(
                    p.rect.x - 0.1f, p.rect.y - 0.1f, p.rect.width + 0.2f, p.rect.height + 0.6f
                );
                com.badlogic.gdx.math.Rectangle expandedPlayer = new com.badlogic.gdx.math.Rectangle(
                    playerRect.x, playerRect.y - 0.15f, playerRect.width, playerRect.height + 0.3f
                );
                if (expandedPortal.overlaps(expandedPlayer)) {
                    uiManager.showToast("Area locked: Defeat the boss to proceed", 1.2f);
                    return; // Block transition
                }
            }
        }
        if (portalCooldown > 0f || portals.isEmpty()) return;
        // Use player's full hitbox instead of only center point for overlap detection
        float sx = player.getSprite().getX();
        float sy = player.getSprite().getY();
        float sw = player.getSprite().getWidth();
        float sh = player.getSprite().getHeight();
        com.badlogic.gdx.math.Rectangle playerRect = new com.badlogic.gdx.math.Rectangle(sx, sy, sw, sh);
        for (PortalRect p : portals) {
            // Expand portal slightly to be more forgiving
            com.badlogic.gdx.math.Rectangle expandedPortal = new com.badlogic.gdx.math.Rectangle(
                p.rect.x - 0.1f, p.rect.y - 0.1f, p.rect.width + 0.2f, p.rect.height + 0.6f
            );
            // Also expand player rect slightly downward (feet)
            com.badlogic.gdx.math.Rectangle expandedPlayer = new com.badlogic.gdx.math.Rectangle(
                playerRect.x, playerRect.y - 0.15f, playerRect.width, playerRect.height + 0.3f
            );
            if (expandedPortal.overlaps(expandedPlayer)) {
                Gdx.app.log("Portals", "Player overlapped portal -> " + p.targetMap + " playerRect=" + playerRect + " portalRect=" + p.rect + " expandedPortal=" + expandedPortal + " expandedPlayer=" + expandedPlayer);
                transitionToMap(p.targetMap, p.spawnX, p.spawnY);
                break;
            }
        }
    }

    private void transitionToMap(String targetMapPath, Float overrideSpawnX, Float overrideSpawnY) {
        // Avoid retrigger
        portalCooldown = 1.0f;

        // Start teleport FX for any transition
        teleportFxActive = true;
        teleportFxTimer = 0f;

        // Load target world
        worldMapManager.setCurrentWorld(targetMapPath);
        // Reset boss state when entering a new world
        bossSpawnedInCurrentWorld = false;
        bossDefeatedInCurrentWorld = false;
        bossClearedToastShown = false;

        // Pause player movement
        player.getBody().setLinearVelocity(0, 0);

        // Load new map and renderer
        mapManager.load(targetMapPath);
        mapRenderer = mapManager.getRenderer();
        mapWidth = mapManager.getWorldWidth();
        mapHeight = mapManager.getWorldHeight();

        // update spawner after map load
        if (enemySpawner != null) {
            enemySpawner.setWorldSize(mapWidth, mapHeight);
            enemySpawner.setCurrentWorld(worldMapManager.getCurrentWorldPath());
            enemySpawner.setCollisionMap(mapManager.getTiledMap());
        }

        // Rebuild camera constraints
        cameraManager = new CameraManager((OrthographicCamera) viewport.getCamera(), screenShake, mapWidth, mapHeight);

        // Rebuild NavMesh and update spawner
        int maxNavMeshSize = 200;
        int navWidth = Math.min(maxNavMeshSize, (int) mapManager.getWorldWidth());
        int navHeight = Math.min(maxNavMeshSize, (int) mapManager.getWorldHeight());
        NavMesh navMesh = new NavMesh(navWidth, navHeight,
            CollisionLoader.getCollisionRectangles(mapManager.getTiledMap(), "collisionLayer", 1 / 32f));
        enemySpawner.setNavMesh(navMesh);
        enemySpawner.setCurrentWorld(targetMapPath);
        enemySpawner.setCollisionMap(mapManager.getTiledMap());
        // Apply world-specific spawn policy and handle boss spawning
        capstone.main.Managers.WorldMapManager.WorldMap wm = worldMapManager.getCurrentWorld();
        capstone.main.Managers.WorldSpawnPolicy policy = capstone.main.Managers.SpawnPolicies.getPolicy(wm);
        enemySpawner.setPolicy(policy);
        enemySpawner.clearEnemies();
        if (policy != null) {
            policy.onEnterWorld(enemySpawner, wm);
        }
        // Only do periodic initial spawns if allowed by policy
        if (policy == null || policy.allowPeriodicSpawns()) {
            enemySpawner.spawnInitial(worldMapManager.getEnemyCount(worldMapManager.getCurrentWorld()));
        }

        // Rebuild world renderer
        worldRenderer = new WorldRenderer(mapManager.getRenderer(), mapManager.getTiledMap());

        // Re-read portals from new map
        loadPortalsFromMap();

        // Determine spawn
        float sx, sy;
        if (overrideSpawnX != null && overrideSpawnY != null) {
            sx = overrideSpawnX;
            sy = overrideSpawnY;
        } else {
            WorldMapManager.SpawnPoint sp = worldMapManager.getSpawnPoint(worldMapManager.getCurrentWorld());
            sx = sp.x;
            sy = sp.y;
        }
        // Teleport player (center is body position)
        player.getBody().setTransform(sx + player.getWidth() / 2f, sy + player.getHeight() / 2f, 0);
        player.getBody().setLinearVelocity(0, 0);
        // Ensure sprite sync next update
        player.getSprite().setPosition(sx, sy);

        Gdx.app.log("Transition", "Moved to " + targetMapPath + " spawn=(" + sx + "," + sy + ")");
    }

    private void createPauseMenu() {
        pauseStage.clear();

        // Main table
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Title
        Label titleLabel = new Label("PAUSED", pauseSkin);
        titleLabel.setFontScale(2f); // Reverted back to 2f
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
        musicToggle.getLabel().setFontScale(0.8f); // Reverted back to 0.8f
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

        /* --- Music slider table --- */
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
        soundToggle.getLabel().setFontScale(0.8f); // Reverted back to 0.8f
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
        if (uiManager != null) uiManager.resize(width, height);
        heartsHud.resize(width, height);
        if (skillHud != null) skillHud.resize(width, height);
        if (inventoryUI != null) inventoryUI.resize(width, height);
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
        try {
            if (treeFadeShader != null) treeFadeShader.dispose();
        } catch (Exception ignored) {
        }
        if (physicsManager != null) physicsManager.dispose();
        if (mapManager != null) mapManager.dispose();
        if (heartsHud != null) heartsHud.dispose();
        if (player != null) player.dispose();
        if (skillHud != null) skillHud.dispose();
        if (inventoryUI != null) inventoryUI.dispose();
       if (uiManager != null) uiManager.dispose();

       // Stop World 1 music when leaving game screen
       MusicManager.getInstance().stop();
    }


    private AbstractPlayer createPlayer() {
        switch (selectedCharacterIndex) {
            case 1:
                // Manny Pacquiao - Melee fighter
                // High HP, moderate damage, moderate speed, low mana
                return new MannyPacquiao(
                    130,           // healthPoints (rebalanced: 150→130 - less tanky)
                    60,            // manaPoints (lowest - skills cost mana)
                    12,            // baseDamage (rebalanced: 10→12)
                    16,            // maxDamage (rebalanced: 15→16, now 12-16 damage range)
                    1.0f,          // attackSpeed (rebalanced: 1.2→1.0 - slightly slower)
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
                    100,           // healthPoints (rebalanced: 90→100 - slightly less fragile)
                    120,           // manaPoints (highest - mage needs mana)
                    1000,            // baseDamage (rebalanced: 12→10)
                    1500,            // maxDamage (rebalanced: 18→15, now 10-15 damage range)
                    1.0f,          // attackSpeed (rebalanced: 0.8→1.0 - faster to compensate)
                    9f,            // x
                    9f,            // y
                    2f,            // width
                    2f,         // height
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
                    120,           // healthPoints (middle ground)
                    70,            // manaPoints (moderate)
                    8,             // baseDamage (rebalanced: 6→8)
                    12,            // maxDamage (rebalanced: 10→12, now 8-12 damage range)
                    1.8f,          // attackSpeed (rebalanced: 2.0→1.8 - slightly slower but stronger)
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
