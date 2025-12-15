package capstone.main.Managers;

import com.badlogic.gdx.math.Rectangle;

/**
 * Represents a portal in the game world that allows transitions between maps.
 * Each portal has a position (rectangle), a target map destination, and optional
 * spawn position overrides for precise player placement after transition.
 * 
 * <p>This class encapsulates portal data following the Single Responsibility Principle,
 * making it easier to manage, test, and extend portal functionality.</p>
 * 
 * @author Capstone Team
 * @version 1.0
 */
public class Portal {
    private final Rectangle bounds;
    private final String targetMapPath;
    private final Float spawnX;
    private final Float spawnY;
    
    /**
     * Creates a new Portal with specified bounds and target map.
     * 
     * @param bounds The rectangular area that triggers the portal transition
     * @param targetMapPath The file path to the target map (e.g., "Textures/World2.tmx")
     * @param spawnX Optional X coordinate override for player spawn (null to use default)
     * @param spawnY Optional Y coordinate override for player spawn (null to use default)
     * @throws IllegalArgumentException if bounds or targetMapPath is null
     */
    public Portal(Rectangle bounds, String targetMapPath, Float spawnX, Float spawnY) {
        if (bounds == null) {
            throw new IllegalArgumentException("Portal bounds cannot be null");
        }
        if (targetMapPath == null || targetMapPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Target map path cannot be null or empty");
        }
        
        this.bounds = new Rectangle(bounds); // Create defensive copy
        this.targetMapPath = targetMapPath;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }
    
    /**
     * Creates a new Portal without spawn position overrides.
     * Player will spawn at the default position defined by the target map.
     * 
     * @param bounds The rectangular area that triggers the portal transition
     * @param targetMapPath The file path to the target map
     */
    public Portal(Rectangle bounds, String targetMapPath) {
        this(bounds, targetMapPath, null, null);
    }
    
    /**
     * Gets the rectangular bounds of this portal.
     * 
     * @return A defensive copy of the portal's bounds rectangle
     */
    public Rectangle getBounds() {
        return new Rectangle(bounds); // Return defensive copy
    }
    
    /**
     * Gets the target map path for this portal.
     * 
     * @return The file path to the target map
     */
    public String getTargetMapPath() {
        return targetMapPath;
    }
    
    /**
     * Gets the optional spawn X coordinate override.
     * 
     * @return The X coordinate to spawn player at, or null to use default
     */
    public Float getSpawnX() {
        return spawnX;
    }
    
    /**
     * Gets the optional spawn Y coordinate override.
     * 
     * @return The Y coordinate to spawn player at, or null to use default
     */
    public Float getSpawnY() {
        return spawnY;
    }
    
    /**
     * Checks if this portal has custom spawn coordinates defined.
     * 
     * @return true if both spawnX and spawnY are not null
     */
    public boolean hasCustomSpawn() {
        return spawnX != null && spawnY != null;
    }
    
    /**
     * Checks if a given rectangle overlaps with this portal's bounds.
     * Useful for detecting player collision with the portal.
     * 
     * @param rect The rectangle to check for overlap
     * @return true if the rectangles overlap
     */
    public boolean overlaps(Rectangle rect) {
        return bounds.overlaps(rect);
    }
    
    /**
     * Creates an expanded version of this portal's bounds.
     * Useful for creating larger trigger areas or detection zones.
     * 
     * @param expansion The amount to expand in all directions (in world units)
     * @return A new Rectangle representing the expanded bounds
     */
    public Rectangle getExpandedBounds(float expansion) {
        return new Rectangle(
            bounds.x - expansion,
            bounds.y - expansion,
            bounds.width + (expansion * 2),
            bounds.height + (expansion * 2)
        );
    }
    
    @Override
    public String toString() {
        return String.format("Portal{bounds=(%.1f,%.1f,%.1fx%.1f), target='%s', spawn=(%s,%s)}", 
            bounds.x, bounds.y, bounds.width, bounds.height,
            targetMapPath,
            spawnX != null ? String.format("%.1f", spawnX) : "default",
            spawnY != null ? String.format("%.1f", spawnY) : "default"
        );
    }
}
