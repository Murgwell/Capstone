package capstone.main.UI;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

public class SlowEffect {
    private float x, y;
    private float[] particleX;
    private float[] particleY;
    private float[] particleAlpha;
    private int particleCount = 8;
    private float timer = 0f;

    public SlowEffect(float x, float y) {
        this.x = x;
        this.y = y;

        particleX = new float[particleCount];
        particleY = new float[particleCount];
        particleAlpha = new float[particleCount];

        for (int i = 0; i < particleCount; i++) {
            particleX[i] = MathUtils.random(-0.2f, 0.2f);
            particleY[i] = MathUtils.random(-0.2f, 0.2f);
            particleAlpha[i] = MathUtils.random(0.5f, 1f);
        }
    }

    public void update(float delta, float enemyX, float enemyY) {
        this.x = enemyX;
        this.y = enemyY;
        timer += delta;

        for (int i = 0; i < particleCount; i++) {
            particleY[i] += delta * 0.3f;
            if (particleY[i] > 0.3f) {
                particleY[i] = -0.3f;
            }
        }
    }

    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < particleCount; i++) {
            shapeRenderer.setColor(0.3f, 0.8f, 1f, particleAlpha[i] * 0.6f);
            shapeRenderer.circle(x + particleX[i], y + particleY[i], 0.03f, 8);
        }
        shapeRenderer.end();
    }
}
