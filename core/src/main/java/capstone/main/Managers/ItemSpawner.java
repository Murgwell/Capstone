package capstone.main.Managers;

import capstone.main.Sprites.PickableItem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages spawning and lifecycle of pickable items in the game world.
 * 
 * <p>Features:
 * <ul>
 *   <li>Smart layer-based spawning near map objects</li>
 *   <li>Collision-aware placement (avoids walls and obstacles)</li>
 *   <li>Support for multiple item types (bandages, first-aid kits)</li>
 *   <li>Automatic cleanup of picked-up items</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>{@code
 * ItemSpawner spawner = new ItemSpawner(world, mapWidth, mapHeight);
 * spawner.setTiledMap(tiledMap);
 * spawner.spawnNearTileLayer("props", 8); // Spawn 8 items near props
 * spawner.update(delta);
 * spawner.removePickedUpItems();
 * }</pre>
 * 
 * @author Capstone Team
 * @version 1.0
 */
public class ItemSpawner {
    private ArrayList<PickableItem> items;
    private World world;
    private float worldWidth;
    private float worldHeight;
    private TiledMap tiledMap;
    private List<Rectangle> collisionRectangles;
    
    /**
     * Creates a new ItemSpawner for managing pickable items.
     * 
     * @param world The Box2D world for physics bodies
     * @param worldWidth The width of the game world in units
     * @param worldHeight The height of the game world in units
     */
    public ItemSpawner(World world, float worldWidth, float worldHeight) {
        this.items = new ArrayList<>();
        this.world = world;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }
    
    /**
     * Set the tiled map for layer-based spawning
     */
    public void setTiledMap(TiledMap tiledMap) {
        this.tiledMap = tiledMap;
        
        // Load collision rectangles from the collision layer
        if (tiledMap != null) {
            this.collisionRectangles = CollisionLoader.getCollisionRectangles(tiledMap, "collisionLayer", 1/32f);
            Gdx.app.log("ItemSpawner", "Loaded " + (collisionRectangles != null ? collisionRectangles.size() : 0) + " collision rectangles");
        }
    }
    
    /**
     * Check if a position overlaps with any collision rectangle
     */
    private boolean isPositionBlocked(float x, float y, float radius) {
        if (collisionRectangles == null || collisionRectangles.isEmpty()) {
            return false;
        }
        
        // Create a small circle around the spawn point
        Rectangle checkRect = new Rectangle(x - radius, y - radius, radius * 2, radius * 2);
        
        for (Rectangle collision : collisionRectangles) {
            if (collision.overlaps(checkRect)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Spawn a specific item at a specific location
     */
    public void spawnItem(float x, float y, String itemName, String iconPath) {
        PickableItem item = new PickableItem(x, y, itemName, iconPath, world);
        items.add(item);
    }
    
    /**
     * Spawn first-aid kit (red) at specific location
     */
    public void spawnFirstAidRed(float x, float y) {
        spawnItem(x, y, "First Aid Kit", "Textures/UI/Inventory/Objects/Icon_First-Aid-Kit_Red.png");
    }
    
    /**
     * Spawn first-aid kit (white) at specific location
     */
    public void spawnFirstAidWhite(float x, float y) {
        spawnItem(x, y, "First Aid Kit", "Textures/UI/Inventory/Objects/Icon_First-Aid-Kit_White.png");
    }
    
    /**
     * Spawn bandage at specific location
     */
    public void spawnBandage(float x, float y) {
        spawnItem(x, y, "Bandage", "Textures/UI/Inventory/Objects/Icon_Bandage.png");
    }
    
    /**
     * Spawn random healing items across the map
     */
    public void spawnRandomHealingItems(int count) {
        for (int i = 0; i < count; i++) {
            float x = MathUtils.random(2f, worldWidth - 2f);
            float y = MathUtils.random(2f, worldHeight - 2f);
            
            // Randomly choose between red and white first-aid kits, and bandages
            int itemType = MathUtils.random(0, 2);
            switch (itemType) {
                case 0:
                    spawnFirstAidRed(x, y);
                    break;
                case 1:
                    spawnFirstAidWhite(x, y);
                    break;
                case 2:
                    spawnBandage(x, y);
                    break;
            }
        }
    }
    
    /**
     * Spawn items near objects in a specific tile layer
     * @param layerName The name of the object layer (e.g., "props", "smallProps", "Decos")
     * @param count Number of items to spawn
     */
    public void spawnNearTileLayer(String layerName, int count) {
        if (tiledMap == null) {
            Gdx.app.log("ItemSpawner", "Warning: TiledMap not set, using random spawn");
            spawnRandomHealingItems(count);
            return;
        }
        
        MapLayer layer = tiledMap.getLayers().get(layerName);
        if (layer == null) {
            Gdx.app.log("ItemSpawner", "Warning: Layer '" + layerName + "' not found, using random spawn");
            spawnRandomHealingItems(count);
            return;
        }
        
        // Collect all object positions from the layer
        ArrayList<Rectangle> objectPositions = new ArrayList<>();
        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                // Convert from pixel coordinates to world coordinates
                Rectangle worldRect = new Rectangle(
                    rect.x / 32f,
                    rect.y / 32f,
                    rect.width / 32f,
                    rect.height / 32f
                );
                objectPositions.add(worldRect);
            }
        }
        
        if (objectPositions.isEmpty()) {
            Gdx.app.log("ItemSpawner", "Warning: No objects found in layer '" + layerName + "', using random spawn");
            spawnRandomHealingItems(count);
            return;
        }
        
        Gdx.app.log("ItemSpawner", "Found " + objectPositions.size() + " objects in layer '" + layerName + "'");
        
        // Spawn items near random objects
        int spawnAttempts = 0;
        int maxAttempts = count * 10; // Allow multiple attempts to find valid positions
        int itemsSpawned = 0;
        
        while (itemsSpawned < count && spawnAttempts < maxAttempts) {
            spawnAttempts++;
            
            // Pick a random object
            Rectangle obj = objectPositions.get(MathUtils.random(0, objectPositions.size() - 1));
            
            // Spawn near the object (offset by 1-2 units)
            float offsetX = MathUtils.random(-2f, 2f);
            float offsetY = MathUtils.random(-2f, 2f);
            float x = obj.x + obj.width / 2f + offsetX;
            float y = obj.y + obj.height / 2f + offsetY;
            
            // Clamp to world bounds
            x = Math.max(2f, Math.min(x, worldWidth - 2f));
            y = Math.max(2f, Math.min(y, worldHeight - 2f));
            
            // Check if position is blocked by collision
            if (isPositionBlocked(x, y, 0.5f)) {
                Gdx.app.log("ItemSpawner", "Position (" + x + ", " + y + ") blocked by collision, retrying...");
                continue; // Try another position
            }
            
            // Randomly choose item type
            int itemType = MathUtils.random(0, 2);
            switch (itemType) {
                case 0:
                    spawnFirstAidRed(x, y);
                    break;
                case 1:
                    spawnFirstAidWhite(x, y);
                    break;
                case 2:
                    spawnBandage(x, y);
                    break;
            }
            
            Gdx.app.log("ItemSpawner", "Spawned item near object at (" + x + ", " + y + ")");
            itemsSpawned++;
        }
        
        if (itemsSpawned < count) {
            Gdx.app.log("ItemSpawner", "Warning: Only spawned " + itemsSpawned + " out of " + count + " items (some positions were blocked)");
        }
    }
    
    /**
     * Update all items
     */
    public void update(float delta) {
        for (PickableItem item : items) {
            item.update(delta);
        }
    }
    
    /**
     * Remove picked up items
     */
    public void removePickedUpItems() {
        Iterator<PickableItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            PickableItem item = iterator.next();
            if (item.isPickedUp()) {
                item.dispose();
                iterator.remove();
            }
        }
    }
    
    /**
     * Get all items
     */
    public ArrayList<PickableItem> getItems() {
        return items;
    }
    
    /**
     * Clear all items
     */
    public void clear() {
        for (PickableItem item : items) {
            item.dispose();
        }
        items.clear();
    }
    
    /**
     * Disposes of all items and their resources.
     * This method is idempotent and safe to call multiple times.
     * Cleans up:
     * - All PickableItem textures and Box2D bodies
     * - Clears the items list
     */
    public void dispose() {
        clear();
    }
}
