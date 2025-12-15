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

public class Greed extends AbstractEnemy implements BossEntity, TelegraphProvider {

    // Boss-specific stats and skill system
    private final float maxHealthBoss = GreedConfig.MAX_HP;
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

    // Optional: keep refs to textures so they can be disposed later if needed
    private final Array<Texture> ownedTextures = new Array<>();
    private final Array<TextureAtlas> ownedAtlases = new Array<>();

    private final float spriteWidth = 5.0f;
    private final float spriteHeight = 5.0f;

    public Greed(float x, float y, ScreenShake screenShake, PhysicsManager physics, NavMesh navMesh) {
        super(
            x, y,
            new Texture("Textures/Enemies/World1/Greed/Run-Forward/orc1_walk_full-0.png"),
            5.0f, 5.0f, 1200, // boss HP
            screenShake, physics, navMesh
        );

        // -----------------------------
        // Load Animations
        // -----------------------------
        animDown = loadAtlasAnim(
            "Textures/Enemies/World1/Greed/Run-Forward",
            "Greed_Run-Forward.atlas",
            "orc1_walk_full-", 0.10f
        );
        if (animDown == null) animDown = loadFolderAnim(
            "Textures/Enemies/World1/Greed/Run-Forward",
            "orc1_walk_full-", 0, 5, 0.10f
        );

        animUp = loadAtlasAnim(
            "Textures/Enemies/World1/Greed/Run-Backward",
            "Greed_Run-Backward.atlas",
            "orc1_walk_full-", 0.10f
        );
        if (animUp == null) animUp = loadFolderAnim(
            "Textures/Enemies/World1/Greed/Run-Backward",
            "orc1_walk_full-", 0, 5, 0.10f
        );

        animLeft = loadAtlasAnim(
            "Textures/Enemies/World1/Greed/Run-Left",
            "Greed_Run-Left.atlas",
            "orc1_walk_full-", 0.10f
        );
        if (animLeft == null) animLeft = loadFolderAnim(
            "Textures/Enemies/World1/Greed/Run-Left",
            "orc1_walk_full-", 0, 5, 0.10f
        );

        animRight = loadAtlasAnim(
            "Textures/Enemies/World1/Greed/Run-Right",
            "Greed_Run-Right.atlas",
            "orc1_walk_full-", 0.10f
        );
        if (animRight == null) animRight = loadFolderAnim(
            "Textures/Enemies/World1/Greed/Run-Right",
            "orc1_walk_full-", 0, 5, 0.10f
        );

        // Random direction
        boolean facingLeft = MathUtils.randomBoolean();
        directionManager.setFacingLeft(facingLeft);

        // Initial frame
        TextureRegion initial = safeFrame(animDown);
        if (initial != null) {
            sprite.setRegion(initial);
            sprite.setSize(spriteWidth, spriteHeight);
            // Set origin to center for consistent positioning
            sprite.setOrigin(spriteWidth / 2f, spriteHeight / 2f);
        }

        this.speed = 1.5f;
        com.badlogic.gdx.Gdx.app.log("Greed", "Spawned at (" + x + ", " + y + ")");

        // Reduce friction and collision size to avoid getting snagged on collision layer
        for (com.badlogic.gdx.physics.box2d.Fixture fx : body.getFixtureList()) {
            fx.setFriction(0f);
            com.badlogic.gdx.physics.box2d.Shape.Type t = fx.getShape().getType();
            if (t == com.badlogic.gdx.physics.box2d.Shape.Type.Circle) {
                ((com.badlogic.gdx.physics.box2d.CircleShape) fx.getShape()).setRadius(0.6f); // narrower body for corridors
            }
        }
        body.setSleepingAllowed(false);

        // Boss should aggressively chase the player across the arena
        this.defaultChaseDistance = 1000f; // effectively always aggro when spawned
        this.aggroChaseDistance = 1000f;   // do not leash within the arena
    }

    @Override
    public void update(float delta, AbstractPlayer player) {
        if (isDead()) {
            body.setLinearVelocity(0, 0);
            return;
        }

        // Core behavior & hit flash
        updateHitFlash(delta);
        pathfindingChaseBehavior(delta, player);

        // Telegraph visuals (simple tint pulse while telegraphing)
        if (telegraphing) {
            float pulse = (MathUtils.sin(stateTime * 10f) * 0.25f) + 0.75f; // 0.5..1.0
            sprite.setColor(1f, 0.6f, 0.1f, pulse); // orange-ish
        } else {
            sprite.setColor(1f, 1f, 1f, 1f);
        }

        // Boss skill state machine
        skillTimer += delta;
        if (!telegraphing && skillTimer >= skillCooldown) {
            // Pick a skill to telegraph
            int pick = MathUtils.random(0, 2);
            switch (pick) {
                case 0: currentSkill = "SLAM"; castTime = GreedConfig.SLAM_TELEGRAPH; castTotalTime = castTime; break;
                case 1: currentSkill = "CONE BARRAGE"; castTime = GreedConfig.BARRAGE_TELEGRAPH; castTotalTime = castTime; break;
                default: currentSkill = "RING WAVE"; castTime = GreedConfig.RING_TELEGRAPH; castTotalTime = castTime; break;
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
            // If cone is being cast, halt movement during telegraph
            if ("CONE BARRAGE".equals(currentSkill)) {
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
                skillCooldown = MathUtils.random(GreedConfig.COOLDOWN_MIN, GreedConfig.COOLDOWN_MAX);
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
            case "SLAM":
                if (distance <= GreedConfig.SLAM_RANGE) {
                    player.damage(GreedConfig.SLAM_DAMAGE);
                    screenShake.shake(0.25f, 0.1f);
                }
                break;
            case "CONE BARRAGE":
                float angleToPlayer = (float)Math.toDegrees(Math.atan2(dy, dx));
                float facingAngle = lastVX >= 0 ? 0f : 180f; // crude facing
                float deltaAngle = Math.abs(normalizeAngle(angleToPlayer - facingAngle));
                if (deltaAngle <= GreedConfig.BARRAGE_CONE_HALF_ANGLE && distance <= GreedConfig.BARRAGE_RANGE) {
                    player.damage(GreedConfig.BARRAGE_DAMAGE);
                    screenShake.shake(0.2f, 0.1f);
                }
                break;
            case "RING WAVE":
                if (distance <= GreedConfig.RING_RANGE) {
                    player.damage(GreedConfig.RING_DAMAGE);
                    screenShake.shake(0.2f, 0.1f);
                }
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
                if (i == startIndex) return null; // no frames found
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

        // Your Greed sprites are always 0..5
        for (int i = 0; i < 6; i++) {
            TextureRegion region = atlas.findRegion(frameBaseName + i);
            if (region == null) {
                return null;
            }
            frames.add(region);
        }
        if (frames.size == 0) return null;
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }
}
