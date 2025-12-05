package capstone.main.Enemies;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Managers.PhysicsManager;
import capstone.main.Managers.ScreenShake;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Discaya extends AbstractEnemy {

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

    public Discaya(float x, float y, ScreenShake screenShake, PhysicsManager physics) {

        super(x, y,
            new Texture("Textures/Enemies/World2/Discaya/Run-Forward/Discaya_Walk-0.png"),
            1.0f, 1.0f, 100, screenShake, physics);

        // static sprite size
        this.spriteWidth = 3.0f;
        this.spriteHeight = 3.0f;

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
        }

        this.speed = 1.5f;
    }

    @Override
    public void update(float delta, AbstractPlayer player) {
        if (isDead()) {
            body.setLinearVelocity(0, 0);
            return;
        }

        updateHitFlash(delta);
        defaultChaseBehavior(delta, player);

        stateTime += delta;
        lastVX = body.getLinearVelocity().x;
        lastVY = body.getLinearVelocity().y;

        TextureRegion frame = selectFrame();
        if (frame != null) {
            sprite.setRegion(frame);
            sprite.setSize(spriteWidth, spriteHeight);  // static size
        }
    }

    private TextureRegion selectFrame() {
        if (Math.abs(lastVX) < 0.01f && Math.abs(lastVY) < 0.01f) {
            Animation<TextureRegion> idleAnim = animFromLastDir();
            return idleAnim != null ? idleAnim.getKeyFrame(0) : null;
        }

        if (Math.abs(lastVX) > Math.abs(lastVY)) {
            return lastVX > 0 ? safeFrame(animRight) : safeFrame(animLeft);
        } else {
            return lastVY > 0 ? safeFrame(animUp) : safeFrame(animDown);
        }
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
