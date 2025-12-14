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

public class QuiboloyBoss extends AbstractEnemy implements BossEntity, TelegraphProvider {

    // BossEntity minimal implementation
    @Override
    public boolean isBoss() { return true; }
    @Override
    public float getCurrentHealth() { return this.health; }
    @Override
    public String getSkillWarning() { return ""; }

    // TelegraphProvider defaults (no telegraphing yet)
    @Override
    public boolean isTelegraphing() { return false; }
    @Override
    public String getTelegraphSkill() { return null; }
    @Override
    public float getTelegraphOriginX() { return getBody().getPosition().x; }
    @Override
    public float getTelegraphOriginY() { return getBody().getPosition().y; }
    @Override
    public float getTelegraphAngleDegrees() { return 0f; }

    private Animation<TextureRegion> animDown;
    private Animation<TextureRegion> animUp;
    private Animation<TextureRegion> animLeft;
    private Animation<TextureRegion> animRight;

    private float stateTime = 0f;
    private float lastVX = 0f;
    private float lastVY = -1f; // default looking down

    private final Array<Texture> ownedTextures = new Array<>();
    private final Array<TextureAtlas> ownedAtlases = new Array<>();

    private NavMesh navMesh;
    public QuiboloyBoss(float x, float y, ScreenShake screenShake, PhysicsManager physics, NavMesh navMesh) {

        super(x, y,
            new Texture("Textures/Enemies/World3/Quiboloy/Run-Forward/quiboloy_walk-0.png"),
            3.0f, 3.0f, 200, screenShake, physics, navMesh); // Boss HP higher

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
            sprite.setSize(2.0f, 2.0f); // bigger boss sprite
        }

        this.speed = 1.2f; // maybe slower than normal enemies
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

            float aspectRatio = (float) frame.getRegionWidth() / frame.getRegionHeight();
            float height = 1.0f;
            float width = height * aspectRatio;

            sprite.setSize(width, height);
        }

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
