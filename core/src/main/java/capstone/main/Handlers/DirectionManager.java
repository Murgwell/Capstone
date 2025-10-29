package capstone.main.Handlers;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class DirectionManager {

    Sprite sprite;
    Texture normalTexture;
    Texture reversedTexture;

    public DirectionManager(Sprite sprite, Texture normalTexture, Texture reversedTexture) {
        this.sprite = sprite;
        this.normalTexture = normalTexture;
        this.reversedTexture = reversedTexture;
        this.sprite.setTexture(normalTexture);
    }

    public void updateDirection(Vector2 velocity) {
        if (velocity.x < 0) {
            sprite.setTexture(reversedTexture); // moving left
        } else if (velocity.x > 0) {
            sprite.setTexture(normalTexture); // moving right
        }
    }
}
