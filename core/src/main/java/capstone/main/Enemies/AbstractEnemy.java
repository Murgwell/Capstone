package capstone.main.Enemies;

import capstone.main.Characters.AbstractPlayer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class AbstractEnemy {
    protected Sprite sprite;
    protected float health;
    protected float defaultChaseDistance = 3f;
    protected float aggroChaseDistance = 6f;
    protected boolean isAggro = false;

    public AbstractEnemy(float x, float y, Texture texture, float width, float height) {
        sprite = new Sprite(texture);
        sprite.setPosition(x, y);
        sprite.setSize(width, height);
    }

    public Sprite getSprite() {
        return sprite;
    }

    public abstract void update(float delta, AbstractPlayer player);

    public void takeHit(float damage) {
        health -= damage;
        isAggro = true;
    }

    public boolean isDead() {
        return health <= 0;
    }
}
