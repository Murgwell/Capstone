package capstone.main.UI;

import capstone.main.Managers.Inventory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.Actor;
import java.util.HashMap;
import java.util.Map;

public class InventoryUI {
    private final Stage stage;
    private final Stage quickAccessStage;
    private final Inventory inventory;
    private final Skin skin;
    
    // Textures
    private Texture inventoryPanelTex;
    private Texture inventoryCellTex;
    private Texture inventoryChosenTex;
    private Texture quickAccessTex;
    private Texture closeButtonUpTex;
    private Texture closeButtonDownTex;
    
    // UI elements
    private Table quickAccessTable;
    private ImageButton closeButton;
    private Image[] inventoryCells;
    private Image[] quickAccessCells;
    private Label[] quantityLabels;
    private Label[] quickAccessQuantityLabels;
    
    private boolean isOpen = false;
    private int selectedSlot = -1;
    
    // Texture cache for item icons
    private Map<String, Texture> itemTextureCache;
    
    // Callback for when inventory is toggled
    private Runnable onToggleCallback;
    
    // Callback for when item is used
    private ItemUseCallback onItemUseCallback;
    
    // Callback for health checking
    private HealthCheckCallback healthCheckCallback;
    
    // Callback for floating text
    private FloatingTextCallback floatingTextCallback;
    
    // Item description label
    private Label descriptionLabel;
    
    // Drag and drop system
    private DragAndDrop dragAndDrop;
    
    public InventoryUI(Viewport uiViewport, SpriteBatch batch, Inventory inventory) {
        this.stage = new Stage(uiViewport, batch);
        this.quickAccessStage = new Stage(uiViewport, batch);
        this.inventory = inventory;
        this.skin = new Skin(Gdx.files.internal("uiskin.json"));
        this.itemTextureCache = new HashMap<>();
        
        // Load textures
        inventoryPanelTex = new Texture("Textures/UI/Inventory/Inventory_1.png");
        inventoryCellTex = new Texture("Textures/UI/Inventory/Inventory-Cell.png");
        inventoryChosenTex = new Texture("Textures/UI/Inventory/Inventory-Chosen.png");
        quickAccessTex = new Texture("Textures/UI/Inventory/Quick-Access-Inventory.png");
        closeButtonUpTex = new Texture("Textures/UI/Inventory/Inventory_Close_Not-Pressed.png");
        closeButtonDownTex = new Texture("Textures/UI/Inventory/Inventory_Close_Pressed.png");
        
        // Initialize arrays
        inventoryCells = new Image[inventory.getMaxSlots()];
        quickAccessCells = new Image[4];
        quantityLabels = new Label[inventory.getMaxSlots()];
        quickAccessQuantityLabels = new Label[4];
        
        // Initialize drag and drop
        dragAndDrop = new DragAndDrop();
        
        buildQuickAccessInventory();
        buildFullInventory();
    }
    
    private Texture getItemTexture(String iconPath) {
        if (!itemTextureCache.containsKey(iconPath)) {
            try {
                itemTextureCache.put(iconPath, new Texture(iconPath));
            } catch (Exception e) {
                Gdx.app.error("InventoryUI", "Failed to load item texture: " + iconPath);
                return inventoryCellTex; // Return empty cell if texture fails to load
            }
        }
        return itemTextureCache.get(iconPath);
    }
    
    private void buildQuickAccessInventory() {
        quickAccessTable = new Table();
        quickAccessTable.setFillParent(true);
        quickAccessTable.bottom().right();
        
        // Quick access background
        Image quickAccessBg = new Image(quickAccessTex);
        quickAccessTable.add(quickAccessBg).size(quickAccessTex.getWidth() * 0.5f, quickAccessTex.getHeight() * 0.5f)
            .pad(10f);
        
        // Quick access slots (4 slots)
        Table slotsTable = new Table();
        for (int i = 0; i < 4; i++) {
            final int slotIndex = i;
            Image cell = new Image(inventoryCellTex);
            cell.setSize(32f, 32f);
            quickAccessCells[i] = cell;
            
            Label qtyLabel = new Label("", skin);
            qtyLabel.setFontScale(0.5f); // Reverted back to 0.5f
            qtyLabel.setColor(Color.WHITE);
            quickAccessQuantityLabels[i] = qtyLabel;
            
            // Add hotkey indicator
            Label hotkeyLabel = new Label(String.valueOf(i + 1), skin);
            hotkeyLabel.setFontScale(0.4f); // Reverted back to 0.4f
            hotkeyLabel.setColor(Color.YELLOW);
            
            // Add click listener to quick access slots
            cell.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Use item from quick access
                    int inventoryIndex = inventory.getQuickAccessSlot(slotIndex);
                    if (inventoryIndex >= 0) {
                        Inventory.InventoryItem usedItem = inventory.useItem(inventoryIndex);
                        if (usedItem != null && onItemUseCallback != null) {
                            onItemUseCallback.onItemUse(usedItem);
                        }
                    }
                }
            });
            
            
            Table slotTable = new Table();
            slotTable.add(hotkeyLabel).size(12f, 12f).top().left().row();
            slotTable.add(cell).size(32f, 32f).row();
            slotTable.add(qtyLabel).padTop(2f);
            slotsTable.add(slotTable).pad(2f);
        }
        
        quickAccessTable.add(slotsTable).padLeft(10f);
        quickAccessStage.addActor(quickAccessTable);
    }
    
    private void buildFullInventory() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        
        // Add drag-enabled quick access slots to the main inventory stage when open
        Table quickAccessForDrag = new Table();
        quickAccessForDrag.setPosition(Gdx.graphics.getWidth() - 200f, 100f);
        
        Table dragQuickSlotsTable = new Table();
        for (int i = 0; i < 4; i++) {
            final int slotIndex = i;
            
            // Create drop zones for drag and drop
            Image dropZone = new Image(inventoryCellTex);
            dropZone.setSize(40f, 40f);
            dropZone.setColor(1, 1, 1, 0.7f); // Semi-transparent
            
            // Add drop target for drag and drop
            dragAndDrop.addTarget(new DragAndDrop.Target(dropZone) {
                @Override
                public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                    dropZone.setColor(0, 1, 0, 0.8f); // Green highlight during drag
                    return true;
                }
                
                @Override
                public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload) {
                    dropZone.setColor(1, 1, 1, 0.7f); // Reset to semi-transparent
                }
                
                @Override
                public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                    Integer inventoryIndex = (Integer) payload.getObject();
                    if (inventoryIndex != null && inventoryIndex >= 0 && inventoryIndex < inventory.getSize()) {
                        // Check if this item is already in another quick access slot
                        int existingSlot = inventory.findQuickAccessSlot(inventoryIndex);
                        if (existingSlot != -1 && existingSlot != slotIndex) {
                            // Clear the old slot
                            inventory.clearQuickAccessSlot(existingSlot);
                        }
                        
                        // Set the new quick access slot
                        inventory.setQuickAccessSlot(slotIndex, inventoryIndex);
                        Gdx.app.log("Inventory", "Added " + inventory.getItem(inventoryIndex).getName() + " to quick slot " + (slotIndex + 1));
                    }
                    dropZone.setColor(1, 1, 1, 0.7f); // Reset color
                }
            });
            
            // Add hotkey label
            Label hotkeyLabel = new Label(String.valueOf(i + 1), skin);
            hotkeyLabel.setFontScale(0.6f); // Reverted back to 0.6f
            hotkeyLabel.setColor(Color.YELLOW);
            
            Table dropSlotTable = new Table();
            dropSlotTable.add(hotkeyLabel).row();
            dropSlotTable.add(dropZone).size(40f, 40f);
            
            dragQuickSlotsTable.add(dropSlotTable).pad(5f);
        }
        
        quickAccessForDrag.add(new Label("Drag items here:", skin)).row();
        quickAccessForDrag.add(dragQuickSlotsTable);
        stage.addActor(quickAccessForDrag);
        
        // Inventory panel background
        Image panelBg = new Image(inventoryPanelTex);
        root.add(panelBg).size(inventoryPanelTex.getWidth() * 0.8f, inventoryPanelTex.getHeight() * 0.8f)
            .padBottom(20f).row();
        
        // Inventory grid (3 rows x 6 columns = 18 slots)
        Table gridTable = new Table();
        for (int i = 0; i < inventory.getMaxSlots(); i++) {
            final int slotIndex = i;
            Image cell = new Image(inventoryCellTex);
            cell.setSize(48f, 48f);
            inventoryCells[i] = cell;
            
            Label qtyLabel = new Label("", skin);
            qtyLabel.setFontScale(0.6f); // Reverted back to 0.6f
            qtyLabel.setColor(Color.WHITE);
            quantityLabels[i] = qtyLabel;
            
            Table slotTable = new Table();
            slotTable.add(cell).size(48f, 48f);
            slotTable.add(qtyLabel).padLeft(2f);
            
            // Add click and hover listeners
            cell.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (event.getButton() == com.badlogic.gdx.Input.Buttons.LEFT) {
                        selectSlot(slotIndex);
                        updateItemDescription(slotIndex);
                    } else if (event.getButton() == com.badlogic.gdx.Input.Buttons.RIGHT) {
                        // Right click to use item
                        useItem(slotIndex);
                    }
                }
                
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    // Show description on hover
                    updateItemDescription(slotIndex);
                }
                
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    // Clear description when not hovering (only if not selected)
                    if (selectedSlot != slotIndex) {
                        descriptionLabel.setText("");
                    }
                }
            });
            
            // Add drag source for this inventory slot
            dragAndDrop.addSource(new DragAndDrop.Source(cell) {
                @Override
                public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                    if (slotIndex < inventory.getSize() && inventory.getItem(slotIndex) != null) {
                        DragAndDrop.Payload payload = new DragAndDrop.Payload();
                        payload.setObject(slotIndex); // Store the inventory index
                        
                        // Create drag actor (visual representation)
                        Inventory.InventoryItem item = inventory.getItem(slotIndex);
                        Image dragActor = new Image(getItemTexture(item.getIconPath()));
                        dragActor.setSize(32f, 32f);
                        payload.setDragActor(dragActor);
                        
                        return payload;
                    }
                    return null;
                }
            });
            
            if (i % 6 == 0 && i > 0) {
                gridTable.row();
            }
            gridTable.add(slotTable).pad(2f);
        }
        
        root.add(gridTable).padTop(20f).row();
        
        // Close button
        ImageButton.ImageButtonStyle closeStyle = new ImageButton.ImageButtonStyle();
        closeStyle.up = new TextureRegionDrawable(new TextureRegion(closeButtonUpTex));
        closeStyle.down = new TextureRegionDrawable(new TextureRegion(closeButtonDownTex));
        closeButton = new ImageButton(closeStyle);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggle();
            }
        });
        
        root.add(closeButton).size(closeButtonUpTex.getWidth() * 0.5f, closeButtonUpTex.getHeight() * 0.5f)
            .padTop(10f).row();
        
        // Item description area
        descriptionLabel = new Label("", skin);
        descriptionLabel.setFontScale(0.8f); // Reverted back to 0.8f
        descriptionLabel.setColor(Color.YELLOW);
        descriptionLabel.setWrap(true);
        root.add(descriptionLabel).width(300f).height(40f).padTop(10f);
        
        stage.addActor(root);
        
        // Add input processor to handle "I" key to close inventory
        stage.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean keyDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, int keycode) {
                if (keycode == com.badlogic.gdx.Input.Keys.I || keycode == com.badlogic.gdx.Input.Keys.ESCAPE) {
                    toggle();
                    return true;
                }
                return false;
            }
        });
    }
    
    private void selectSlot(int slotIndex) {
        selectedSlot = slotIndex;
        updateInventoryDisplay();
    }
    
    private void updateItemDescription(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < inventory.getSize()) {
            Inventory.InventoryItem item = inventory.getItem(slotIndex);
            if (item != null) {
                String description = item.getName() + "\n" + item.getDescription();
                if (item.isConsumable()) {
                    description += "\nRight-click to use";
                }
                descriptionLabel.setText(description);
            } else {
                descriptionLabel.setText("");
            }
        } else {
            descriptionLabel.setText("");
        }
    }
    
    private void useItem(int slotIndex) {
        // Check the item first without consuming it
        Inventory.InventoryItem testItem = inventory.tryUseItem(slotIndex);
        if (testItem != null && testItem.getType() == Inventory.ItemType.HEALING && onItemUseCallback != null) {
            // For healing items, we need to pass the health check logic to Game.java
            // We'll use a special interface for this
            if (healthCheckCallback != null && !healthCheckCallback.canUseHealingItem()) {
                Gdx.app.log("Inventory", "Health is already full! Cannot use " + testItem.getName());
                
                // Show floating text
                if (floatingTextCallback != null) {
                    floatingTextCallback.showFloatingText("Health is full!", 255, 100, 100); // Red color
                }
                
                return; // Don't consume the item
            }
        }
        
        // If we get here, consume the item
        Inventory.InventoryItem usedItem = inventory.useItem(slotIndex);
        if (usedItem != null && onItemUseCallback != null) {
            onItemUseCallback.onItemUse(usedItem);
        }
        // Update description after use
        updateItemDescription(slotIndex);
    }
    
    public void toggle() {
        isOpen = !isOpen;
        if (onToggleCallback != null) {
            onToggleCallback.run();
        }
    }
    
    public void setOnToggleCallback(Runnable callback) {
        this.onToggleCallback = callback;
    }
    
    public void setOnItemUseCallback(ItemUseCallback callback) {
        this.onItemUseCallback = callback;
    }
    
    public void setHealthCheckCallback(HealthCheckCallback callback) {
        this.healthCheckCallback = callback;
    }
    
    public void setFloatingTextCallback(FloatingTextCallback callback) {
        this.floatingTextCallback = callback;
    }
    
    public boolean isOpen() {
        return isOpen;
    }
    
    public void update(float delta) {
        updateQuickAccessDisplay();
        if (isOpen) {
            updateInventoryDisplay();
            stage.act(delta);
        }
        quickAccessStage.act(delta);
    }
    
    private void updateQuickAccessDisplay() {
        int[] quickSlots = inventory.getQuickAccessSlots();
        for (int i = 0; i < 4; i++) {
            int invIndex = quickSlots[i];
            if (invIndex >= 0 && invIndex < inventory.getSize()) {
                Inventory.InventoryItem item = inventory.getItem(invIndex);
                if (item != null) {
                    Texture itemTex = getItemTexture(item.getIconPath());
                    quickAccessCells[i].setDrawable(new TextureRegionDrawable(new TextureRegion(itemTex)));
                    if (item.getQuantity() > 1) {
                        quickAccessQuantityLabels[i].setText(String.valueOf(item.getQuantity()));
                    } else {
                        quickAccessQuantityLabels[i].setText("");
                    }
                } else {
                    quickAccessCells[i].setDrawable(new TextureRegionDrawable(new TextureRegion(inventoryCellTex)));
                    quickAccessQuantityLabels[i].setText("");
                }
            } else {
                quickAccessCells[i].setDrawable(new TextureRegionDrawable(new TextureRegion(inventoryCellTex)));
                quickAccessQuantityLabels[i].setText("");
            }
        }
    }
    
    private void updateInventoryDisplay() {
        for (int i = 0; i < inventory.getMaxSlots(); i++) {
            // Highlight selected slot first
            if (i == selectedSlot && i < inventory.getSize()) {
                Inventory.InventoryItem item = inventory.getItem(i);
                if (item != null) {
                    Texture itemTex = getItemTexture(item.getIconPath());
                    // Show item texture (chosen background could be added as overlay later)
                    inventoryCells[i].setDrawable(new TextureRegionDrawable(new TextureRegion(itemTex)));
                    if (item.getQuantity() > 1) {
                        quantityLabels[i].setText(String.valueOf(item.getQuantity()));
                    } else {
                        quantityLabels[i].setText("");
                    }
                } else {
                    inventoryCells[i].setDrawable(new TextureRegionDrawable(new TextureRegion(inventoryChosenTex)));
                    quantityLabels[i].setText("");
                }
            } else if (i < inventory.getSize()) {
                Inventory.InventoryItem item = inventory.getItem(i);
                if (item != null) {
                    Texture itemTex = getItemTexture(item.getIconPath());
                    inventoryCells[i].setDrawable(new TextureRegionDrawable(new TextureRegion(itemTex)));
                    if (item.getQuantity() > 1) {
                        quantityLabels[i].setText(String.valueOf(item.getQuantity()));
                    } else {
                        quantityLabels[i].setText("");
                    }
                } else {
                    inventoryCells[i].setDrawable(new TextureRegionDrawable(new TextureRegion(inventoryCellTex)));
                    quantityLabels[i].setText("");
                }
            } else {
                inventoryCells[i].setDrawable(new TextureRegionDrawable(new TextureRegion(inventoryCellTex)));
                quantityLabels[i].setText("");
            }
        }
    }
    
    public void draw() {
        quickAccessStage.draw();
        if (isOpen) {
            // Draw semi-transparent overlay
            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, 
                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
            
            com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer = 
                new com.badlogic.gdx.graphics.glutils.ShapeRenderer();
            shapeRenderer.setProjectionMatrix(stage.getViewport().getCamera().combined);
            shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0, 0, 0, 0.6f);
            shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shapeRenderer.end();
            shapeRenderer.dispose();
            
            Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            
            stage.draw();
        }
    }
    
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        quickAccessStage.getViewport().update(width, height, true);
    }
    
    public Stage getStage() {
        return stage;
    }
    
    public void dispose() {
        if (dragAndDrop != null) {
            dragAndDrop.clear();
        }
        stage.dispose();
        quickAccessStage.dispose();
        skin.dispose();
        inventoryPanelTex.dispose();
        inventoryCellTex.dispose();
        inventoryChosenTex.dispose();
        quickAccessTex.dispose();
        closeButtonUpTex.dispose();
        closeButtonDownTex.dispose();
        
        // Dispose cached item textures
        for (Texture tex : itemTextureCache.values()) {
            tex.dispose();
        }
        itemTextureCache.clear();
    }
}

