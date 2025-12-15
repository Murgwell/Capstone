package capstone.main.Sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Represents a pickable item in the game world that players can collect.
 * 
 * <p>Features:
 * <ul>
 *   <li>Visual bobbing animation for attention</li>
 *   <li>Proximity-based glow indicator when player is nearby</li>
 *   <li>Box2D sensor for collision detection</li>
 *   <li>Automatic texture and physics cleanup on disposal</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>{@code
 * PickableItem item = new PickableItem(10f, 10f, "Bandage", "textures/bandage.png", world);
 * item.update(delta);
 * item.render(batch);
 * if (playerNearby) {
 *     item.setPlayerNearby(true);
 *     item.renderGlowIndicator(shapeRenderer);
 * }
 * item.dispose(); // Always call when done
 * }</pre>
 * 
 * @author Capstone Team
 * @version 1.0
 */
public class PickableItem {
    private Sprite sprite;
    private Body body;
    private String itemName;
    private String iconPath;
    private boolean isPickedUp = false;
    private boolean isPlayerNearby = false;
    
    // Visual feedback
    private float bobTimer = 0f;
    private float bobSpeed = 2f;
    private float bobHeight = 0.1f;
    
    // Glow effect
    private float glowTimer = 0f;
    private float glowSpeed = 3f;
    
    /**
     * Creates a new pickable item at the specified position.
     * 
     * @param x The x-coordinate in world units
     * @param y The y-coordinate in world units
     * @param itemName The display name of the item (e.g., "Bandage", "First Aid Kit")
     * @param iconPath The texture path for the item's icon
     * @param world The Box2D world to create the physics body in
     */
    public PickableItem(float x, float y, String itemName, String iconPath, World world) {
        this.itemName = itemName;
        this.iconPath = iconPath;
        
        // Create sprite
        Texture texture = new Texture(iconPath);
        sprite = new Sprite(texture);
        sprite.setSize(1f, 1f); // 1 world unit size
        sprite.setPosition(x - sprite.getWidth() / 2f, y - sprite.getHeight() / 2f);
        
        // Create Box2D body for collision detection
        createBody(world, x, y);
    }
    
    private void createBody(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        
        body = world.createBody(bodyDef);
        
        // Create a circular sensor for pickup detection
        CircleShape shape = new CircleShape();
        shape.setRadius(0.8f); // Pickup radius
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.isSensor = true; // Sensor so it doesn't collide physically
        fixtureDef.filter.categoryBits = 0x0010; // Item category
        fixtureDef.filter.maskBits = 0x0001; // Only detect player
        
        body.createFixture(fixtureDef);
        body.setUserData(this);
        
        shape.dispose();
    }
    
    /**
     * Updates the item's animations and visual effects.
     * 
     * @param delta Time elapsed since last frame in seconds
     */
    public void update(float delta) {
        if (!isPickedUp) {
            // Bob animation
            bobTimer += delta * bobSpeed;
            float offset = (float) Math.sin(bobTimer) * bobHeight;
            
            Vector2 pos = body.getPosition();
            sprite.setPosition(
                pos.x - sprite.getWidth() / 2f,
                pos.y - sprite.getHeight() / 2f + offset
            );
            
            // Glow animation
            glowTimer += delta * glowSpeed;
        }
    }
    
    /**
     * Sets whether the player is nearby, affecting visual indicators.
     * 
     * @param nearby true if player is within indicator range, false otherwise
     */
    public void setPlayerNearby(boolean nearby) {
        this.isPlayerNearby = nearby;
    }
    
    /**
     * Renders the item sprite with visual effects.
     * Call this within a SpriteBatch begin/end block.
     * 
     * @param batch The SpriteBatch to draw with (must be active)
     */
    public void render(SpriteBatch batch) {
        if (!isPickedUp) {
            // Draw sprite with glow effect if player is nearby
            if (isPlayerNearby) {
                // Pulsing alpha effect
                float alpha = 0.7f + 0.3f * (float) Math.sin(glowTimer);
                sprite.setAlpha(alpha);
            } else {
                sprite.setAlpha(1.0f);
            }
            sprite.draw(batch);
        }
    }
    
    /**
     * Render glow indicator when player is nearby (call after batch.end())
     */
    public void renderGlowIndicator(ShapeRenderer shapeRenderer) {
        if (!isPickedUp && isPlayerNearby) {
            Vector2 pos = body.getPosition();
            
            // Pulsing circle effect
            float glowIntensity = 0.5f + 0.5f * (float) Math.sin(glowTimer);
            float radius = 0.6f + 0.2f * glowIntensity;
            
            // Draw outer glow circle
            shapeRenderer.setColor(1f, 1f, 0.5f, 0.3f * glowIntensity); // Yellow glow
            shapeRenderer.circle(pos.x, pos.y, radius, 32);
            
            // Draw inner glow circle
            shapeRenderer.setColor(1f, 1f, 1f, 0.5f * glowIntensity); // White center
            shapeRenderer.circle(pos.x, pos.y, radius * 0.7f, 32);
        }
    }
    
    public void pickup() {
        isPickedUp = true;
    }
    
    public boolean isPickedUp() {
        return isPickedUp;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public String getIconPath() {
        return iconPath;
    }
    
    public Body getBody() {
        return body;
    }
    
    public Vector2 getPosition() {
        return body.getPosition();
    }
    
    /**
     * Disposes of all resources used by this item.
     * This method is idempotent and safe to call multiple times.
     * Cleans up:
     * - Texture resources from the sprite
     * - Box2D body from the physics world
     */
    public void dispose() {
        // Dispose sprite texture
        if (sprite != null && sprite.getTexture() != null) {
            try {
                sprite.getTexture().dispose();
            } catch (Exception e) {
                com.badlogic.gdx.Gdx.app.error("PickableItem", "Error disposing texture: " + e.getMessage());
            }
        }
        
        // Destroy Box2D body
        if (body != null) {
            try {
                World world = body.getWorld();
                if (world != null) {
                    world.destroyBody(body);
                }
            } catch (Exception e) {
                com.badlogic.gdx.Gdx.app.error("PickableItem", "Error destroying body: " + e.getMessage());
            } finally {
                body = null;
            }
        }
    }
}
