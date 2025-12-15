package capstone.main.UI;

import capstone.main.Enemies.BossEntity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

public class BossUI {
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private BossEntity boss; // current boss reference

    public BossUI(ShapeRenderer shapeRenderer, BitmapFont font) {
        this.shapeRenderer = shapeRenderer;
        this.font = font;
    }

    public void setBoss(BossEntity boss) {
        this.boss = boss;
    }

    public void render(SpriteBatch batch, float screenWidth, float screenHeight) {
        if (boss == null) return;
        if (boss.isDead()) return;

        // Big health bar at top
        float barWidth = screenWidth * 0.6f;
        float barHeight = 18f;
        float barX = (screenWidth - barWidth) / 2f;
        float barY = screenHeight - 40f;

        float hpRatio = Math.max(0f, Math.min(1f, boss.getHealthRatio()));

        // Draw background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0,0,0,0.6f));
        shapeRenderer.rect(barX - 2, barY - 2, barWidth + 4, barHeight + 4);

        // Draw red bar
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth * hpRatio, barHeight);
        shapeRenderer.end();

        // Draw boss name and HP text
        batch.begin();
        
        // Save original font scale
        float originalScale = font.getData().scaleX;
        
        String title = boss != null ? boss.getClass().getSimpleName().toUpperCase() : "BOSS";
        font.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
        font.draw(batch, title, 0, barY + barHeight + 18f, screenWidth, Align.center, false);
        String hpText = String.format("%d / %d", (int)boss.getCurrentHealth(), (int)boss.getMaxHealth());
        font.getData().setScale(0.9f);
        font.draw(batch, hpText, 0, barY + barHeight - 2f, screenWidth, Align.center, false);

        // Skill warning if boss is telegraphing or casting
        String warn = boss.getSkillWarning();
        if (warn != null && !warn.isEmpty()) {
            font.setColor(Color.YELLOW);
            font.getData().setScale(1.0f);
            font.draw(batch, warn, 0, screenHeight - 80f, screenWidth, Align.center, false);
        }
        
        // CRITICAL: Restore original font scale for damage numbers!
        font.getData().setScale(originalScale);
        
        batch.end();
    }
}
