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

public class Discaya extends AbstractEnemy implements BossEntity, TelegraphProvider {

    // Poison Cloud data structure
    public static class PoisonCloud {
        public float x, y, radius;
        public float duration;
        public float maxDuration;
        public float tickTimer;
        
        public PoisonCloud(float x, float y, float radius, float duration) {
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
    
    // Active poison clouds
    private final java.util.List<PoisonCloud> activePoisonClouds = new java.util.ArrayList<>();
    
    public java.util.List<PoisonCloud> getActivePoisonClouds() {
        return activePoisonClouds;
    }

    // Boss-specific stats and skill system
    private final float maxHealthBoss = DiscayaConfig.MAX_HP;
    private float skillTimer = 0f;
    private float skillCooldown = 3.5f;
    private boolean telegraphing = false;
    private String currentSkill = null; // for UI
    private float castTime = 0f;
    private float castTotalTime = 0f; // for UI progress

    public boolean isBoss() { return true; }
    public float getMaxHealth() { return maxHealthBoss; }
    public float getCurrentHealth() { return this.health; }
    public float getHealthRatio() { return Math.max(0f, Math.min(1f, health / maxHealthBoss)); }
    public String getSkillWarning() { return telegraphing ? ("WARNING: " + currentSkill + " incoming!") : ""; }

    // --- Telegraph info for rendering ---
    public boolean isTelegraphing() { return telegraphing; }
    public String getTelegraphSkill() { return currentSkill; }

    // Telegraph origin and angle (fixed when telegraph starts)
    private float telegraphX = 0f, telegraphY = 0f, telegraphAngleDeg = 0f;
    public float getTelegraphOriginX() { return telegraphX; }
    public float getTelegraphOriginY() { return telegraphY; }
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

    private final float spriteWidth;
    private final float spriteHeight;
    private NavMesh navMesh;

    public Discaya(float x, float y, ScreenShake screenShake, PhysicsManager physics, NavMesh navMesh) {

        super(x, y,
            new Texture("Textures/Enemies/World2/Discaya/Run-Forward/Discaya_Walk-0.png"),
            5.0f, 5.0f, 1500, // Boss HP and size
            screenShake, physics, navMesh);

        // static sprite size
        this.spriteWidth = 5.0f;
        this.spriteHeight = 5.0f;

        // -----------------------------
        // Load Animations
        // -----------------------------
        animDown = loadAtlasAnim(
            "Textures/Enemies/World2/Discaya/Run-Forward",
            "Discaya_Run-Forward.atlas",
            "Discaya_Walk-", 0.10f
        );
        if (animDown == null) animDown = loadFolderAnim(
            "Textures/Enemies/World2/Discaya/Run-Forward",
            "Discaya_Walk-", 0, 5, 0.10f
        );

        animUp = loadAtlasAnim(
            "Textures/Enemies/World2/Discaya/Run-Backward",
            "Discaya_Run-Backward.atlas",
            "Discaya_Walk-", 0.10f
        );
        if (animUp == null) animUp = loadFolderAnim(
            "Textures/Enemies/World2/Discaya/Run-Backward",
            "Discaya_Walk-", 0, 5, 0.10f
        );

        animLeft = loadAtlasAnim(
            "Textures/Enemies/World2/Discaya/Run-Left",
            "Discaya_Run-Left.atlas",
            "Discaya_Walk-", 0.10f
        );
        if (animLeft == null) animLeft = loadFolderAnim(
            "Textures/Enemies/World2/Discaya/Run-Left",
            "Discaya_Walk-", 0, 5, 0.10f
        );

        animRight = loadAtlasAnim(
            "Textures/Enemies/World2/Discaya/Run-Right",
            "Discaya_Run-Right.atlas",
            "Discaya_Walk-", 0.10f
        );
        if (animRight == null) animRight = loadFolderAnim(
            "Textures/Enemies/World2/Discaya/Run-Right",
            "Discaya_Walk-", 0, 5, 0.10f
        );

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
        com.badlogic.gdx.Gdx.app.log("Discaya", "Spawned at (" + x + ", " + y + ")");

        // Reduce friction and collision size to avoid getting snagged on collision layer
        for (com.badlogic.gdx.physics.box2d.Fixture fx : body.getFixtureList()) {
            fx.setFriction(0f);
            com.badlogic.gdx.physics.box2d.Shape.Type t = fx.getShape().getType();
            if (t == com.badlogic.gdx.physics.box2d.Shape.Type.Circle) {
                ((com.badlogic.gdx.physics.box2d.CircleShape) fx.getShape()).setRadius(0.6f);
            }
        }
        body.setSleepingAllowed(false);

        // Boss should aggressively chase the player across the arena
        this.defaultChaseDistance = 1000f;
        this.aggroChaseDistance = 1000f;
    }

    @Override
    public void update(float delta, AbstractPlayer player) {
        if (isDead()) {
            body.setLinearVelocity(0, 0);
            return;
        }

        // Update active poison clouds
        updatePoisonClouds(delta, player);

        // Core behavior & hit flash
        updateHitFlash(delta);
        pathfindingChaseBehavior(delta, player);

        // Telegraph visuals (dark purple pulse while telegraphing)
        if (telegraphing) {
            float pulse = (MathUtils.sin(stateTime * 10f) * 0.25f) + 0.75f; // 0.5..1.0
            
            // Different visual feedback based on skill being charged
            if ("POISON CLOUD".equals(currentSkill)) {
                // Green-purple for poison
                sprite.setColor(0.4f, 0.8f, 0.3f, pulse);
            } else if ("SHADOW DASH".equals(currentSkill)) {
                // Dark purple for shadow dash (faster pulse)
                float fastPulse = (MathUtils.sin(stateTime * 15f) * 0.3f) + 0.7f;
                sprite.setColor(0.3f, 0.1f, 0.6f, fastPulse);
            } else if ("CORRUPTION PULSE".equals(currentSkill)) {
                // Bright purple for corruption
                sprite.setColor(0.8f, 0.2f, 1.0f, pulse);
            } else {
                // Default purple
                sprite.setColor(0.6f, 0.2f, 0.8f, pulse);
            }
        } else {
            sprite.setColor(1f, 1f, 1f, 1f);
        }

        // Boss skill state machine
        skillTimer += delta;
        if (!telegraphing && skillTimer >= skillCooldown) {
            // Pick a unique skill to telegraph
            int pick = MathUtils.random(0, 2);
            switch (pick) {
                case 0: currentSkill = "POISON CLOUD"; castTime = DiscayaConfig.POISON_TELEGRAPH; castTotalTime = castTime; break;
                case 1: currentSkill = "SHADOW DASH"; castTime = DiscayaConfig.DASH_TELEGRAPH; castTotalTime = castTime; break;
                default: currentSkill = "CORRUPTION PULSE"; castTime = DiscayaConfig.PULSE_TELEGRAPH; castTotalTime = castTime; break;
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
            // If dash is being cast, halt movement during telegraph
            if ("SHADOW DASH".equals(currentSkill)) {
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
                skillCooldown = MathUtils.random(DiscayaConfig.COOLDOWN_MIN, DiscayaConfig.COOLDOWN_MAX);
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

    private void updatePoisonClouds(float delta, AbstractPlayer player) {
        // Update all active poison clouds
        for (int i = activePoisonClouds.size() - 1; i >= 0; i--) {
            PoisonCloud cloud = activePoisonClouds.get(i);
            cloud.duration -= delta;
            cloud.tickTimer += delta;
            
            // Deal damage every 0.5 seconds
            if (cloud.tickTimer >= 0.5f) {
                cloud.tickTimer = 0f;
                
                // Check if player is in the cloud
                Vector2 playerCenter = new Vector2(
                    player.getSprite().getX() + player.getSprite().getWidth() / 2f,
                    player.getSprite().getY() + player.getSprite().getHeight() / 2f
                );
                float dx = playerCenter.x - cloud.x;
                float dy = playerCenter.y - cloud.y;
                float distance = (float)Math.sqrt(dx*dx + dy*dy);
                
                if (distance <= cloud.radius) {
                    player.damage(DiscayaConfig.POISON_DAMAGE);
                    // Small shake for poison tick
                    screenShake.shake(0.15f, 0.05f);
                }
            }
            
            // Remove expired clouds
            if (cloud.isExpired()) {
                activePoisonClouds.remove(i);
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
        float dx = playerCenter.x - bossPos.x;
        float dy = playerCenter.y - bossPos.y;
        float distance = (float)Math.sqrt(dx*dx + dy*dy);

        switch (skill) {
            case "POISON CLOUD":
                // Spawn a lingering poison cloud at Discaya's position
                PoisonCloud newCloud = new PoisonCloud(bossPos.x, bossPos.y, DiscayaConfig.POISON_RANGE, 5.0f);
                activePoisonClouds.add(newCloud);
                
                // Initial damage to player if in range
                if (distance <= DiscayaConfig.POISON_RANGE) {
                    player.damage(DiscayaConfig.POISON_DAMAGE);
                    // Moderate shake for poison cloud spreading
                    screenShake.shake(0.3f, 0.12f);
                }
                // Always shake a bit when skill activates (visual feedback)
                screenShake.shake(0.15f, 0.06f);
                Gdx.app.log("Discaya", "Used POISON CLOUD! Purple mist spreads across the arena!");
                break;
                
            case "SHADOW DASH":
                // Linear dash attack - check if player is in the dash path
                float dashAngle = (float)Math.atan2(dy, dx);
                float playerAngle = (float)Math.atan2(dy, dx);
                float angleDiff = Math.abs(normalizeAngle((float)Math.toDegrees(dashAngle - playerAngle)));
                
                // Check if player is within the dash cone and range
                if (distance <= DiscayaConfig.DASH_RANGE && angleDiff <= 15f) {
                    player.damage(DiscayaConfig.DASH_DAMAGE);
                    // Strong shake for direct dash hit
                    screenShake.shake(0.4f, 0.15f);
                } else {
                    // Smaller shake for dash impact even if missed
                    screenShake.shake(0.2f, 0.08f);
                }
                
                // Actually move Discaya forward (dash effect)
                float dashForceX = (float)Math.cos(dashAngle) * 15f;
                float dashForceY = (float)Math.sin(dashAngle) * 15f;
                body.applyLinearImpulse(dashForceX, dashForceY, bossPos.x, bossPos.y, true);
                
                Gdx.app.log("Discaya", "Used SHADOW DASH! Discaya charges forward!");
                break;
                
            case "CORRUPTION PULSE":
                // Ring attack - damage between inner and outer radius (donut shape)
                if (distance >= DiscayaConfig.PULSE_INNER_RANGE && distance <= DiscayaConfig.PULSE_OUTER_RANGE) {
                    player.damage(DiscayaConfig.PULSE_DAMAGE);
                    // Strong shake for pulse hit
                    screenShake.shake(0.35f, 0.13f);
                }
                // Always shake for the pulse wave expanding
                screenShake.shake(0.2f, 0.09f);
                Gdx.app.log("Discaya", "Used CORRUPTION PULSE! A dark wave ripples outward!");
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
