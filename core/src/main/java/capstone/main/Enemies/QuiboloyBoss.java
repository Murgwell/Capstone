package capstone.main.Enemies;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.ScreenShake;
import capstone.main.Pathfinding.NavMesh;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * QuiboloyBoss - The final boss of World 3 representing corruption in the Philippines.
 * 
 * <p>Boss Mechanics:
 * <ul>
 *   <li>Phase-based combat (3 phases based on HP)</li>
 *   <li>5 unique skills with different behaviors per phase</li>
 *   <li>Corruption zones that damage player over time</li>
 *   <li>Summons Follower minions</li>
 *   <li>Teleportation and area attacks</li>
 * </ul>
 * 
 * <p>Skills:
 * <ul>
 *   <li><b>FIREBALL BARRAGE</b>: Launches multiple fireballs</li>
 *   <li><b>TELEPORT STRIKE</b>: Teleports and deals AoE damage</li>
 *   <li><b>CORRUPTION ZONE</b>: Creates damage-over-time fields</li>
 *   <li><b>SUMMON FOLLOWERS</b>: Spawns 3 Follower minions</li>
 *   <li><b>DEVASTATING WAVE</b>: Large AoE knockback attack</li>
 * </ul>
 * 
 * @author Capstone Team
 * @version 1.0
 */
public class QuiboloyBoss extends AbstractEnemy implements BossEntity, TelegraphProvider {

    // Corruption Zone data structure (similar to Discaya's poison cloud)
    public static class CorruptionZone {
        public float x, y, radius;
        public float duration;
        public float maxDuration;
        public float tickTimer;
        
        public CorruptionZone(float x, float y, float radius, float duration) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.duration = duration;
            this.maxDuration = duration;
            this.tickTimer = 0f;
        }
        
        public boolean isExpired() {
            return duration <= 0f;
        }
        
        public float getAlpha() {
            // Fade out as it expires
            return Math.max(0f, duration / maxDuration);
        }
    }
    
    // Active corruption zones
    private final java.util.List<CorruptionZone> activeCorruptionZones = new java.util.ArrayList<>();
    
    public java.util.List<CorruptionZone> getActiveCorruptionZones() {
        return activeCorruptionZones;
    }

    // Boss-specific stats and skill system
    private final float maxHealthBoss = QuiboloyConfig.MAX_HP;
    private float skillTimer = 0f;
    private float skillCooldown = 4.0f;
    private boolean telegraphing = false;
    private String currentSkill = null; // for UI
    private float castTime = 0f;
    private float castTotalTime = 0f; // for UI progress

    // BossEntity implementation
    @Override
    public boolean isBoss() { return true; }
    @Override
    public float getMaxHealth() { return maxHealthBoss; }
    @Override
    public float getCurrentHealth() { return this.health; }
    public float getHealthRatio() { return Math.max(0f, Math.min(1f, health / maxHealthBoss)); }
    @Override
    public String getSkillWarning() { return telegraphing ? ("WARNING: " + currentSkill + " incoming!") : ""; }

    // --- Telegraph info for rendering ---
    @Override
    public boolean isTelegraphing() { return telegraphing; }
    @Override
    public String getTelegraphSkill() { return currentSkill; }

    // Telegraph origin and angle (fixed when telegraph starts)
    private float telegraphX = 0f, telegraphY = 0f, telegraphAngleDeg = 0f;
    @Override
    public float getTelegraphOriginX() { return telegraphX; }
    @Override
    public float getTelegraphOriginY() { return telegraphY; }
    @Override
    public float getTelegraphAngleDegrees() { return telegraphAngleDeg; }

    // UI: expose cast progress for telegraphing phase
    public float getCastProgress() {
        if (!telegraphing || castTotalTime <= 0f) return 0f;
        float p = 1f - (castTime / castTotalTime);
        if (p < 0f) p = 0f; if (p > 1f) p = 1f; return p;
    }

    private Animation<TextureRegion> animDown;
    private Animation<TextureRegion> animUp;
    private Animation<TextureRegion> animLeft;
    private Animation<TextureRegion> animRight;

    private float stateTime = 0f;
    private float lastVX = 0f;
    private float lastVY = -1f; // default looking down

    private final Array<Texture> ownedTextures = new Array<>();
    private final Array<TextureAtlas> ownedAtlases = new Array<>();

    private final float spriteWidth = 5.0f;
    private final float spriteHeight = 5.0f;
    private NavMesh navMesh;
    private EnemySpawner enemySpawner; // Reference to spawner for summoning

    public QuiboloyBoss(float x, float y, ScreenShake screenShake, PhysicsManager physics, NavMesh navMesh, EnemySpawner spawner) {
        super(x, y,
            new Texture("Textures/Enemies/World3/Quiboloy/Run-Forward/quiboloy_walk-0.png"),
            5.0f, 5.0f, (int)QuiboloyConfig.MAX_HP, screenShake, physics, navMesh); // Final boss HP
        
        this.navMesh = navMesh;
        this.enemySpawner = spawner;

        // -----------------------------
        // Atlas or folder animations
        // -----------------------------
        animDown = loadAtlasAnim("Textures/Enemies/World3/Quiboloy/Run-Forward",
            "Quiboloy_Run-Forward.atlas", "quiboloy_walk-", 0.10f);
        if (animDown == null) animDown = loadFolderAnim(
            "Textures/Enemies/World3/Quiboloy/Run-Forward", "quiboloy_walk-", 0, 5, 0.10f);

        animUp = loadAtlasAnim("Textures/Enemies/World3/Quiboloy/Run-Backward",
            "Quiboloy_Run-Backward.atlas", "quiboloy_walk-", 0.10f);
        if (animUp == null) animUp = loadFolderAnim(
            "Textures/Enemies/World3/Quiboloy/Run-Backward", "quiboloy_walk-", 0, 5, 0.10f);

        animLeft = loadAtlasAnim("Textures/Enemies/World3/Quiboloy/Run-Left",
            "Quiboloy_Run-Left.atlas", "quiboloy_walk-", 0.10f);
        if (animLeft == null) animLeft = loadFolderAnim(
            "Textures/Enemies/World3/Quiboloy/Run-Left", "quiboloy_walk-", 0, 5, 0.10f);

        animRight = loadAtlasAnim("Textures/Enemies/World3/Quiboloy/Run-Right",
            "Quiboloy_Run-Right.atlas", "quiboloy_walk-", 0.10f);
        if (animRight == null) loadFolderAnim(
            "Textures/Enemies/World3/Quiboloy/Run-Right", "quiboloy_walk-", 0, 5, 0.10f);

        // Random starting facing direction
        boolean facingLeft = MathUtils.randomBoolean();
        directionManager.setFacingLeft(facingLeft);

        // Apply initial sprite frame
        TextureRegion initial = safeFrame(animDown);
        if (initial != null) {
            sprite.setRegion(initial);
            sprite.setSize(spriteWidth, spriteHeight);
            // Set origin to center for consistent positioning
            sprite.setOrigin(spriteWidth / 2f, spriteHeight / 2f);
        }

        this.speed = 1.5f;
        Gdx.app.log("QuiboloyBoss", "Spawned at (" + x + ", " + y + ")");

        // Reduce friction and collision size to avoid getting snagged on collision layer
        for (com.badlogic.gdx.physics.box2d.Fixture fx : body.getFixtureList()) {
            fx.setFriction(0f);
            com.badlogic.gdx.physics.box2d.Shape.Type t = fx.getShape().getType();
            if (t == com.badlogic.gdx.physics.box2d.Shape.Type.Circle) {
                ((com.badlogic.gdx.physics.box2d.CircleShape) fx.getShape()).setRadius(0.6f);
            }
        }
        body.setSleepingAllowed(false);

        // Final boss should aggressively chase the player across the arena
        this.defaultChaseDistance = 1000f;
        this.aggroChaseDistance = 1000f;
    }

    @Override
    public void update(float delta, AbstractPlayer player) {
        if (isDead()) {
            body.setLinearVelocity(0, 0);
            return;
        }

        // Update active corruption zones
        updateCorruptionZones(delta, player);

        // Core behavior & hit flash
        updateHitFlash(delta);
        pathfindingChaseBehavior(delta, player);

        // Telegraph visuals (golden/divine glow while telegraphing)
        if (telegraphing) {
            float pulse = (MathUtils.sin(stateTime * 10f) * 0.25f) + 0.75f; // 0.5..1.0
            
            // Different visual feedback based on skill being charged
            if ("DIVINE JUDGMENT".equals(currentSkill)) {
                // Golden glow for divine judgment
                sprite.setColor(1.0f, 0.9f, 0.3f, pulse);
            } else if ("FIREBALL BARRAGE".equals(currentSkill)) {
                // Orange-red for fireballs
                float fastPulse = (MathUtils.sin(stateTime * 15f) * 0.3f) + 0.7f;
                sprite.setColor(1.0f, 0.4f, 0.1f, fastPulse);
            } else if ("TELEPORT STRIKE".equals(currentSkill)) {
                // Purple for teleport
                sprite.setColor(0.7f, 0.2f, 1.0f, pulse);
            } else if ("CORRUPTION ZONE".equals(currentSkill)) {
                // Dark purple for corruption
                sprite.setColor(0.5f, 0.1f, 0.5f, pulse);
            } else if ("SUMMON FOLLOWERS".equals(currentSkill)) {
                // Blue-ish for summoning
                sprite.setColor(0.3f, 0.5f, 1.0f, pulse);
            } else {
                // Default golden
                sprite.setColor(1.0f, 0.8f, 0.2f, pulse);
            }
        } else {
            sprite.setColor(1f, 1f, 1f, 1f);
        }

        // Boss skill state machine
        skillTimer += delta;
        if (!telegraphing && skillTimer >= skillCooldown) {
            // Pick a skill to telegraph - all 5 skills available
            int pick = MathUtils.random(0, 4);
            switch (pick) {
                case 0: currentSkill = "DIVINE JUDGMENT"; castTime = QuiboloyConfig.DIVINE_TELEGRAPH; castTotalTime = castTime; break;
                case 1: currentSkill = "FIREBALL BARRAGE"; castTime = QuiboloyConfig.BARRAGE_TELEGRAPH; castTotalTime = castTime; break;
                case 2: currentSkill = "TELEPORT STRIKE"; castTime = QuiboloyConfig.TELEPORT_TELEGRAPH; castTotalTime = castTime; break;
                case 3: currentSkill = "CORRUPTION ZONE"; castTime = QuiboloyConfig.CORRUPTION_TELEGRAPH; castTotalTime = castTime; break;
                default: currentSkill = "SUMMON FOLLOWERS"; castTime = QuiboloyConfig.SUMMON_TELEGRAPH; castTotalTime = castTime; break;
            }
            telegraphing = true;

            // Fix telegraph origin and angle at start
            Vector2 pos = getBody().getPosition();
            telegraphX = pos.x;
            telegraphY = pos.y;
            // Freeze angle based on current facing (last velocity)
            telegraphAngleDeg = (float) Math.toDegrees(Math.atan2(lastVY, lastVX));
            if (Float.isNaN(telegraphAngleDeg)) telegraphAngleDeg = 0f;
        } else if (telegraphing) {
            castTime -= delta;
            // Halt movement during telegraph for most skills (except summon)
            if (!"SUMMON FOLLOWERS".equals(currentSkill)) {
                body.setLinearVelocity(0,0);
            }
            if (castTime <= 0f) {
                // Execute skill
                performSkill(currentSkill, player);
                telegraphing = false;
                currentSkill = null;
                skillTimer = 0f;
                castTotalTime = 0f;
                // Slightly randomize cooldown for variety
                skillCooldown = MathUtils.random(QuiboloyConfig.COOLDOWN_MIN, QuiboloyConfig.COOLDOWN_MAX);
            }
        }

        stateTime += delta;

        Vector2 velocity = body.getLinearVelocity();

        if (velocity.len() > 0.01f) {
            lastVX = velocity.x;
            lastVY = velocity.y;
        }

        TextureRegion frame;
        if (isAggro && velocity.len() > 0.01f) {
            frame = selectFrame(); // movement animation
        } else {
            frame = idleFrameFromLastDir(); // idle frame
        }

        if (frame != null) {
            sprite.setRegion(frame);
            // Force consistent size and origin after region change to prevent blinking
            sprite.setSize(spriteWidth, spriteHeight);
            sprite.setOrigin(spriteWidth / 2f, spriteHeight / 2f);
        }

    }

    private void updateCorruptionZones(float delta, AbstractPlayer player) {
        // Update all active corruption zones
        for (int i = activeCorruptionZones.size() - 1; i >= 0; i--) {
            CorruptionZone zone = activeCorruptionZones.get(i);
            zone.duration -= delta;
            zone.tickTimer += delta;
            
            // Deal damage at faster rate than poison (more aggressive)
            if (zone.tickTimer >= QuiboloyConfig.CORRUPTION_TICK_RATE) {
                zone.tickTimer = 0f;
                
                // Check if player is in the zone
                Vector2 playerCenter = new Vector2(
                    player.getSprite().getX() + player.getSprite().getWidth() / 2f,
                    player.getSprite().getY() + player.getSprite().getHeight() / 2f
                );
                float dx = playerCenter.x - zone.x;
                float dy = playerCenter.y - zone.y;
                float distance = (float)Math.sqrt(dx*dx + dy*dy);
                
                if (distance <= zone.radius) {
                    player.damage(QuiboloyConfig.CORRUPTION_DAMAGE);
                    // Small shake for corruption tick
                    screenShake.shake(0.2f, 0.08f);
                }
            }
            
            // Remove expired zones
            if (zone.isExpired()) {
                activeCorruptionZones.remove(i);
            }
        }
    }

    private void performSkill(String skill, AbstractPlayer player) {
        if (skill == null) return;
        Vector2 bossPos = getBody().getPosition();
        Vector2 playerCenter = new Vector2(
                player.getSprite().getX() + player.getSprite().getWidth() / 2f,
                player.getSprite().getY() + player.getSprite().getHeight() / 2f
        );
        
        // Get world bounds (with null safety)
        float worldWidth = 32.0f;  // Default World3_Boss size
        float worldHeight = 32.0f;
        if (navMesh != null) {
            worldWidth = navMesh.getWidth();
            worldHeight = navMesh.getHeight();
        } else {
            Gdx.app.log("QuiboloyBoss", "WARNING: navMesh is null in performSkill, using default bounds");
        }
        float dx = playerCenter.x - bossPos.x;
        float dy = playerCenter.y - bossPos.y;
        float distance = (float)Math.sqrt(dx*dx + dy*dy);

        switch (skill) {
            case "DIVINE JUDGMENT":
                // Massive AOE attack in rings - closer = more damage
                if (distance <= QuiboloyConfig.DIVINE_INNER_RANGE) {
                    player.damage(QuiboloyConfig.DIVINE_INNER_DAMAGE);
                    screenShake.shake(0.6f, 0.25f); // Massive shake for close hit
                } else if (distance <= QuiboloyConfig.DIVINE_MIDDLE_RANGE) {
                    player.damage(QuiboloyConfig.DIVINE_MIDDLE_DAMAGE);
                    screenShake.shake(0.4f, 0.18f);
                } else if (distance <= QuiboloyConfig.DIVINE_OUTER_RANGE) {
                    player.damage(QuiboloyConfig.DIVINE_OUTER_DAMAGE);
                    screenShake.shake(0.3f, 0.12f);
                }
                // Always shake a bit when skill activates
                if (distance > QuiboloyConfig.DIVINE_OUTER_RANGE) {
                    screenShake.shake(0.2f, 0.08f);
                }
                Gdx.app.log("QuiboloyBoss", "Used DIVINE JUDGMENT! Divine light radiates across the arena!");
                break;
                
            case "FIREBALL BARRAGE":
                // Shoot multiple fireballs in a spread pattern
                // Note: Since we can't directly spawn Fireball objects without World reference,
                // we'll simulate with multiple hit checks in a cone pattern
                float angleToPlayer = (float)Math.toDegrees(Math.atan2(dy, dx));
                float halfSpread = QuiboloyConfig.BARRAGE_SPREAD_ANGLE / 2f;
                
                // Check if player is hit by any of the fireballs (simplified collision check)
                // In a real implementation, we'd spawn actual Fireball objects
                boolean playerHit = false;
                if (distance <= 15f) { // Fireball range
                    for (int i = 0; i < QuiboloyConfig.BARRAGE_FIREBALL_COUNT; i++) {
                        float offset = -halfSpread + (halfSpread * 2f * i / (QuiboloyConfig.BARRAGE_FIREBALL_COUNT - 1));
                        float fireballAngle = angleToPlayer + offset;
                        float fireballAngleRad = (float)Math.toRadians(fireballAngle);
                        
                        // Check if player is in this fireball's path (simplified)
                        float playerAngle = (float)Math.toDegrees(Math.atan2(dy, dx));
                        float angleDiff = Math.abs(normalizeAngle(fireballAngle - playerAngle));
                        
                        if (angleDiff <= 15f) { // Hit tolerance
                            playerHit = true;
                            break;
                        }
                    }
                }
                
                if (playerHit) {
                    player.damage(QuiboloyConfig.BARRAGE_DAMAGE);
                    screenShake.shake(0.35f, 0.15f);
                }
                screenShake.shake(0.2f, 0.08f); // Visual feedback
                Gdx.app.log("QuiboloyBoss", "Used FIREBALL BARRAGE! Multiple fireballs spread across the arena!");
                break;
                
            case "TELEPORT STRIKE":
                // Teleport behind/near player and strike with AOE
                float teleportAngle = (float)Math.atan2(dy, dx);
                float teleportX = playerCenter.x - (float)Math.cos(teleportAngle) * QuiboloyConfig.TELEPORT_DISTANCE;
                float teleportY = playerCenter.y - (float)Math.sin(teleportAngle) * QuiboloyConfig.TELEPORT_DISTANCE;
                
                // Clamp to world bounds
                teleportX = Math.max(2f, Math.min(teleportX, worldWidth - 2f));
                teleportY = Math.max(2f, Math.min(teleportY, worldHeight - 2f));
                
                // Teleport the boss
                body.setTransform(teleportX, teleportY, 0);
                
                // Deal damage in AOE around new position
                Vector2 newPos = body.getPosition();
                float distAfterTeleport = (float)Math.sqrt(
                    Math.pow(playerCenter.x - newPos.x, 2) + 
                    Math.pow(playerCenter.y - newPos.y, 2)
                );
                
                if (distAfterTeleport <= QuiboloyConfig.TELEPORT_STRIKE_RANGE) {
                    player.damage(QuiboloyConfig.TELEPORT_DAMAGE);
                    screenShake.shake(0.5f, 0.2f);
                }
                screenShake.shake(0.25f, 0.1f); // Teleport visual
                Gdx.app.log("QuiboloyBoss", "Used TELEPORT STRIKE! The boss vanishes and reappears!");
                break;
                
            case "CORRUPTION ZONE":
                // Create a lingering damage field
                CorruptionZone newZone = new CorruptionZone(
                    bossPos.x, bossPos.y, 
                    QuiboloyConfig.CORRUPTION_RANGE, 
                    QuiboloyConfig.CORRUPTION_DURATION
                );
                activeCorruptionZones.add(newZone);
                
                // Initial damage to player if in range
                if (distance <= QuiboloyConfig.CORRUPTION_RANGE) {
                    player.damage(QuiboloyConfig.CORRUPTION_DAMAGE);
                    screenShake.shake(0.35f, 0.15f);
                }
                screenShake.shake(0.2f, 0.08f);
                Gdx.app.log("QuiboloyBoss", "Used CORRUPTION ZONE! Dark energy spreads across the ground!");
                break;
                
            case "SUMMON FOLLOWERS":
                // Summon multiple Follower minions around the boss
                if (enemySpawner != null) {
                    for (int i = 0; i < QuiboloyConfig.SUMMON_COUNT; i++) {
                        // Calculate spawn position in a circle around the boss
                        float angle = (float)(i * 2 * Math.PI / QuiboloyConfig.SUMMON_COUNT);
                        float spawnX = bossPos.x + (float)Math.cos(angle) * QuiboloyConfig.SUMMON_RADIUS;
                        float spawnY = bossPos.y + (float)Math.sin(angle) * QuiboloyConfig.SUMMON_RADIUS;
                        
                        // Clamp to world bounds
                        spawnX = Math.max(2f, Math.min(spawnX, worldWidth - 2f));
                        spawnY = Math.max(2f, Math.min(spawnY, worldHeight - 2f));
                        
                        // Spawn a Follower at this position
                        enemySpawner.spawnSpecific(Follower.class, spawnX, spawnY);
                    }
                    Gdx.app.log("QuiboloyBoss", "Used SUMMON FOLLOWERS! Summoned " + QuiboloyConfig.SUMMON_COUNT + " Followers!");
                    screenShake.shake(0.4f, 0.15f); // Bigger shake for successful summon
                } else {
                    // Fallback if spawner is null (shouldn't happen)
                    Gdx.app.log("QuiboloyBoss", "Used SUMMON FOLLOWERS! (EnemySpawner is null - cannot spawn)");
                    if (distance <= 8f) {
                        player.damage(15);
                        screenShake.shake(0.3f, 0.12f);
                    }
                }
                screenShake.shake(0.2f, 0.08f);
                break;
        }
    }

    private float normalizeAngle(float a) {
        while (a > 180f) a -= 360f;
        while (a < -180f) a += 360f;
        return a;
    }

    private TextureRegion idleFrameFromLastDir() {
        Animation<TextureRegion> currentAnim = animFromLastDir();
        if (currentAnim == null || currentAnim.getKeyFrames().length == 0) return null;
        // Always show first frame of animation as "idle"
        return currentAnim.getKeyFrames()[0];
    }

    private TextureRegion selectFrame() {
        // Always use last direction, even if velocity is zero
        Animation<TextureRegion> currentAnim = animFromLastDir();

        if (currentAnim != null) {
            return currentAnim.getKeyFrame(stateTime, true); // continuous looping
        }
        return null;
    }

    private Animation<TextureRegion> animFromLastDir() {
        if (Math.abs(lastVX) > Math.abs(lastVY)) {
            return lastVX > 0 ? animRight : animLeft;
        } else {
            return lastVY > 0 ? animUp : animDown;
        }
    }

    private TextureRegion safeFrame(Animation<TextureRegion> anim) {
        if (anim == null || anim.getKeyFrames().length == 0) return null;
        return anim.getKeyFrame(stateTime, true);
    }

    private Animation<TextureRegion> loadFolderAnim(String folder, String prefix, int startIndex, int endIndex, float frameDuration) {
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);
        for (int i = startIndex; i <= endIndex; i++) {
            String path = folder + "/" + prefix + i + ".png";
            FileHandle fh = Gdx.files.internal(path);
            if (!fh.exists()) {
                if (i == startIndex) return null;
                break;
            }
            Texture tex = new Texture(fh);
            ownedTextures.add(tex);
            frames.add(new TextureRegion(tex));
        }
        if (frames.size == 0) return null;
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }

    private Animation<TextureRegion> loadAtlasAnim(String folder, String atlasFile, String frameBaseName, float frameDuration) {
        FileHandle fh = Gdx.files.internal(folder + "/" + atlasFile);
        if (!fh.exists()) return null;

        TextureAtlas atlas = new TextureAtlas(fh);
        ownedAtlases.add(atlas);

        Array<TextureRegion> frames = new Array<>(TextureRegion.class);
        for (int i = 0; i < 100; i++) {
            TextureRegion region = atlas.findRegion(frameBaseName + i);
            if (region == null) {
                if (i == 0) return null;
                break;
            }
            frames.add(region);
        }
        if (frames.size == 0) return null;
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }
}
