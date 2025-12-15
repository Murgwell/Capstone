package capstone.main.UI;

import capstone.main.Enemies.BossEntity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;

/**
 * Scene2D Actor that renders a boss health bar and labels in screen-space.
 */
public class BossHudActor extends Actor {
    private final BitmapFont font;
    private final Texture whiteTex; // 1x1 white for drawing rectangles via Batch
    private BossEntity boss;

    public BossHudActor(BitmapFont font) {
        this.font = font;
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        this.whiteTex = new Texture(pm);
        pm.dispose();
        setTouchable(null);
    }

    public void setBoss(BossEntity boss) {
        this.boss = boss;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (boss == null || boss.isDead()) return;

        float screenWidth = getStage().getViewport().getWorldWidth();
        float screenHeight = getStage().getViewport().getWorldHeight();

        float barWidth = screenWidth * 0.6f;
        float barHeight = 18f;
        float barX = (screenWidth - barWidth) / 2f;
        float barY = screenHeight - 40f;

        float hpRatio = Math.max(0f, Math.min(1f, boss.getHealthRatio()));

        // Background with border
        batch.setColor(0, 0, 0, 0.6f);
        batch.draw(whiteTex, barX - 2, barY - 2, barWidth + 4, barHeight + 4);

        // Empty bar
        batch.setColor(Color.DARK_GRAY);
        batch.draw(whiteTex, barX, barY, barWidth, barHeight);

        // HP fill
        batch.setColor(Color.RED);
        batch.draw(whiteTex, barX, barY, barWidth * hpRatio, barHeight);

        // Reset color for text
        batch.setColor(Color.WHITE);

        // Save original font scale
        float originalScale = font.getData().scaleX;

        // Title and HP text
        String title = boss.getClass().getSimpleName().toUpperCase();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
        font.draw(batch, title, 0, barY + barHeight + 18f, screenWidth, Align.center, false);

        String hpText = String.format("%d / %d", (int) boss.getCurrentHealth(), (int) boss.getMaxHealth());
        font.getData().setScale(0.9f);
        font.draw(batch, hpText, 0, barY + barHeight - 2f, screenWidth, Align.center, false);

        String warn = boss.getSkillWarning();
        if (warn != null && !warn.isEmpty()) {
            font.setColor(Color.YELLOW);
            font.getData().setScale(1.0f);
            font.draw(batch, warn, 0, screenHeight - 80f, screenWidth, Align.center, false);
        }
        
        // CRITICAL: Restore original font scale for damage numbers!
        font.getData().setScale(originalScale);
    }

    public void dispose() {
        whiteTex.dispose();
    }
}
