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

public class Greed extends AbstractEnemy {

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

    private final float spriteWidth;
    private final float spriteHeight;

    public Greed(float x, float y, ScreenShake screenShake, PhysicsManager physics, NavMesh navMesh) {
        // Initial placeholder texture (will be replaced by animation frames each update)
        super(x, y, new Texture("Textures/Enemies/World1/Greed/Run-Forward/orc1_walk_full-0.png"), 1.0f, 1.0f, 100, screenShake, physics, navMesh);

        this.spriteWidth = 2.0f;
        this.spriteHeight = 2.0f;

        animDown = loadAtlasAnim("Textures/Enemies/World1/Greed/Run-Forward",
            "Greed_Run-Forward.atlas", "orc1_walk_full-", 0.10f);  // Changed here
        if (animDown == null) animDown = loadFolderAnim("Textures/Enemies/World1/Greed/Run-Forward",
            "Greed_Walk-", 0, 99, 0.10f);

        animUp = loadAtlasAnim("Textures/Enemies/World1/Greed/Run-Backward",
            "Greed_Run-Backward.atlas", "orc1_walk_full-", 0.10f);  // Changed here
        if (animUp == null) animUp = loadFolderAnim("Textures/Enemies/World1/Greed/Run-Backward",
            "Greed_Walk-", 0, 99, 0.10f);

        animLeft = loadAtlasAnim("Textures/Enemies/World1/Greed/Run-Left",
            "Greed_Run-Left.atlas", "orc1_walk_full-", 0.10f);  // Changed here
        if (animLeft == null) animLeft = loadFolderAnim("Textures/Enemies/World1/Greed/Run-Left",
            "Greed_Walk-", 0, 99, 0.10f);

        animRight = loadAtlasAnim("Textures/Enemies/World1/Greed/Run-Right",
            "Greed_Run-Right.atlas", "orc1_walk_full-", 0.10f);
        if (animRight == null) animRight = loadFolderAnim("Textures/Enemies/World1/Greed/Run-Right",
            "Greed_Walk-", 0, 99, 0.10f);

        // random initial facing
        boolean facingLeft = MathUtils.randomBoolean();
        directionManager.setFacingLeft(facingLeft);

        // set an initial frame if available
        TextureRegion initial = safeFrame(animDown);
        if (initial != null) {
            sprite.setRegion(initial);
            sprite.setSize(spriteWidth, spriteHeight);
        }

        this.speed = 1.5f; // optional per-enemy speed
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
        for (int i = 0; i < 100; i++) {
            TextureRegion region = atlas.findRegion(frameBaseName + i);
            if (region == null) {
                if (i == 0) return null; // no frames matched in this atlas
                break;
            }
            frames.add(region);
        }
        if (frames.size == 0) return null;
        return new Animation<>(frameDuration, frames, Animation.PlayMode.LOOP);
    }
}
