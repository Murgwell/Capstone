package capstone.main.Render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorldRenderer {

    private final OrthogonalTiledMapRenderer mapRenderer;
    private final TiledMap map;
    private final int[] layers;
    
    // Layers that should become transparent when player is on top of them
    // World1_Boss: bigProps, bigProps1, Trees
    // World2: Trees, Walls, Walls1, Building
    // World2_Boss: Pillars and Walls
    // World3: Walls, Pillars
    // World3_Boss: pillar
    private static final List<String> OPACITY_LAYER_NAMES = Arrays.asList(
        "bigProps", "bigProps1", "Trees", "Walls", "Walls1", "Building",
        "Pillars and Walls", "Pillars", "pillar"
    );
    
    private final List<Integer> opacityLayerIndices = new ArrayList<>();
    private final List<Integer> normalLayerIndices = new ArrayList<>();
    
    private Vector2 playerPosition = new Vector2(0, 0);
    private static final float OPACITY_DISTANCE = 0.8f; // Horizontal distance threshold (smaller = must be very close)
    private static final float MIN_OPACITY = 0.3f; // Minimum opacity (30%)
    
    private String currentWorldPath = "";

    /**
     * Constructs a WorldRenderer that automatically renders all layers in the map.
     *
     * @param renderer The TiledMap renderer
     * @param map      The TiledMap to render
     */
    public WorldRenderer(OrthogonalTiledMapRenderer renderer, TiledMap map) {
        this.mapRenderer = renderer;
        this.map = map;

        // Dynamically detect all layers and categorize them
        int layerCount = map.getLayers().getCount();
        this.layers = new int[layerCount];
        
        for (int i = 0; i < layerCount; i++) {
            layers[i] = i;
            MapLayer layer = map.getLayers().get(i);
            String layerName = layer.getName();
            
            // Check if this layer should support opacity
            if (OPACITY_LAYER_NAMES.contains(layerName)) {
                opacityLayerIndices.add(i);
            } else {
                normalLayerIndices.add(i);
            }
        }
    }

    /**
     * Updates the player position for opacity calculations.
     *
     * @param x Player's x position in world coordinates
     * @param y Player's y position in world coordinates
     */
    public void setPlayerPosition(float x, float y) {
        playerPosition.set(x, y);
    }
    
    /**
     * Sets the current world path to enable/disable opacity per world.
     *
     * @param worldPath The path to the current world (e.g., "Textures/World2.tmx")
     */
    public void setCurrentWorld(String worldPath) {
        this.currentWorldPath = worldPath;
    }

    /**
     * Renders all layers using the given camera, applying opacity to structure layers
     * based on player proximity.
     *
     * @param camera The camera to set the view
     */
    public void render(OrthographicCamera camera) {
        mapRenderer.setView(camera);
        
        // Render all normal layers at full opacity
        for (int layerIndex : normalLayerIndices) {
            mapRenderer.render(new int[]{layerIndex});
        }
        
        // Render opacity layers with per-tile dynamic transparency
        for (int layerIndex : opacityLayerIndices) {
            MapLayer layer = map.getLayers().get(layerIndex);
            if (layer instanceof TiledMapTileLayer) {
                renderLayerWithPerTileOpacity((TiledMapTileLayer) layer, camera);
            }
        }
    }
    
    /**
     * Renders a tile layer with per-tile opacity based on distance to player.
     */
    private void renderLayerWithPerTileOpacity(TiledMapTileLayer layer, OrthographicCamera camera) {
        // OPTIMIZED: Skip expensive per-tile opacity if player is far from any tiles
        float dx = playerPosition.x - camera.position.x;
        float dy = playerPosition.y - camera.position.y;
        float distToCamera = (float) Math.sqrt(dx * dx + dy * dy);
        
        // If player is very far from camera center, render at full opacity (huge performance boost)
        if (distToCamera > 15f) {
            mapRenderer.render(new int[]{0}); // Just render the layer normally
            return;
        }
        
        // Get the batch from the map renderer
        com.badlogic.gdx.graphics.g2d.Batch batch = mapRenderer.getBatch();
        
        float tileWidth = layer.getTileWidth() / 32f;
        float tileHeight = layer.getTileHeight() / 32f;
        
        // Calculate visible tile range based on camera
        int startX = Math.max(0, (int) ((camera.position.x - camera.viewportWidth / 2) / tileWidth) - 1);
        int endX = Math.min(layer.getWidth(), (int) ((camera.position.x + camera.viewportWidth / 2) / tileWidth) + 2);
        int startY = Math.max(0, (int) ((camera.position.y - camera.viewportHeight / 2) / tileHeight) - 1);
        int endY = Math.min(layer.getHeight(), (int) ((camera.position.y + camera.viewportHeight / 2) / tileHeight) + 2);
        
        batch.begin();
        
        // OPTIMIZED: Reuse color object instead of creating new one each time
        com.badlogic.gdx.graphics.Color batchColor = batch.getColor();
        float originalAlpha = batchColor.a;
        
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    float tileX = x * tileWidth;
                    float tileY = y * tileHeight;
                    float tileCenterX = tileX + tileWidth / 2f;
                    float tileCenterY = tileY + tileHeight / 2f;
                    
                    // Calculate opacity for this specific tile (optimized with early exits)
                    float opacity = calculateTileOpacity(tileCenterX, tileCenterY);
                    
                    // OPTIMIZED: Only change color if opacity is different from 1.0
                    if (opacity < 0.99f) {
                        batchColor.a = opacity;
                        batch.setColor(batchColor);
                    }
                    
                    // Draw the tile
                    com.badlogic.gdx.graphics.g2d.TextureRegion region = cell.getTile().getTextureRegion();
                    batch.draw(region, tileX, tileY, tileWidth, tileHeight);
                    
                    // Restore alpha if changed
                    if (opacity < 0.99f) {
                        batchColor.a = originalAlpha;
                        batch.setColor(batchColor);
                    }
                }
            }
        }
        
        batch.end();
    }
    
    /**
     * Calculates opacity for a single tile based on circular radius detection.
     * Tiles within a larger radius around the player will fade smoothly.
     * Takes into account player's full height (head to toe).
     */
    private float calculateTileOpacity(float tileCenterX, float tileCenterY) {
        // Skip opacity ONLY for World1 (not World1_Boss!)
        if (currentWorldPath.contains("World1.tmx") && !currentWorldPath.contains("World1_Boss")) {
            return 1.0f;
        }
        
        // Player's approximate height (from body position to top of head)
        float playerHeight = 1.0f; // Approximate player sprite height
        
        // Check Y-position: Only apply opacity when tile is BELOW player
        // Tile should fade when player is standing behind/on top of it
        // playerPosition.y is the bottom of the player sprite
        
        // If tile center is ABOVE player's body, don't fade (tile is in front)
        if (tileCenterY > playerPosition.y + 0.5f) {
            // Tile is in front of/above player, don't apply opacity
            return 1.0f;
        }
        
        // If player is completely below the tile, don't fade
        float playerTopY = playerPosition.y + playerHeight;
        if (playerTopY < tileCenterY - 1.0f) {
            // Player is too far below the tile, don't apply opacity
            return 1.0f;
        }
        
        // Calculate distance from player to tile center (circular detection)
        float dx = playerPosition.x - tileCenterX;
        float dy = playerPosition.y - tileCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // Larger radius detection (5.0 units for bigger area)
        float opacityRadius = 5.0f;
        
        if (distance <= opacityRadius) {
            // Within radius - apply opacity with smooth gradient
            // Closer to center = more transparent
            float ratio = distance / opacityRadius;
            return MIN_OPACITY + (1.0f - MIN_OPACITY) * ratio;
        }
        
        return 1.0f; // Full opacity if outside radius
    }
    

    /** Returns the map */
    public TiledMap getMap() {
        return map;
    }
}
