package capstone.main.Managers;

import java.util.ArrayList;

/**
 * Simple inventory system to manage items
 */
public class Inventory {
    private static final int MAX_SLOTS = 18; // 3 rows x 6 columns
    private ArrayList<InventoryItem> items;
    private int[] quickAccessSlots; // Quick access slots (bottom right)
    private static final int QUICK_ACCESS_SIZE = 4;

    public Inventory() {
        items = new ArrayList<>();
        quickAccessSlots = new int[QUICK_ACCESS_SIZE];
        for (int i = 0; i < QUICK_ACCESS_SIZE; i++) {
            quickAccessSlots[i] = -1; // -1 means empty
        }
    }

    public void addItem(String itemName, String iconPath) {
        if (items.size() < MAX_SLOTS) {
            items.add(new InventoryItem(itemName, iconPath));
        }
    }

    public void addItem(String itemName, String iconPath, int quantity) {
        // Check if item already exists
        for (InventoryItem item : items) {
            if (item.getName().equals(itemName)) {
                item.addQuantity(quantity);
                return;
            }
        }
        // Add new item
        if (items.size() < MAX_SLOTS) {
            items.add(new InventoryItem(itemName, iconPath, quantity));
        }
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            // Update quick access slots
            for (int i = 0; i < quickAccessSlots.length; i++) {
                if (quickAccessSlots[i] == index) {
                    quickAccessSlots[i] = -1;
                } else if (quickAccessSlots[i] > index) {
                    quickAccessSlots[i]--; // Adjust indices
                }
            }
        }
    }

    public InventoryItem getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }

    public ArrayList<InventoryItem> getItems() {
        return items;
    }

    public int getSize() {
        return items.size();
    }

    public int getMaxSlots() {
        return MAX_SLOTS;
    }

    public void setQuickAccessSlot(int quickSlotIndex, int inventoryIndex) {
        if (quickSlotIndex >= 0 && quickSlotIndex < QUICK_ACCESS_SIZE) {
            if (inventoryIndex >= -1 && inventoryIndex < items.size()) {
                quickAccessSlots[quickSlotIndex] = inventoryIndex;
            }
        }
    }

    public int getQuickAccessSlot(int quickSlotIndex) {
        if (quickSlotIndex >= 0 && quickSlotIndex < QUICK_ACCESS_SIZE) {
            return quickAccessSlots[quickSlotIndex];
        }
        return -1;
    }

    public int[] getQuickAccessSlots() {
        return quickAccessSlots;
    }

    /**
     * Use an item at the specified index
     * @param index The inventory index of the item to use
     * @return The item that was used, or null if invalid/unusable
     */
    public InventoryItem useItem(int index) {
        if (index >= 0 && index < items.size()) {
            InventoryItem item = items.get(index);
            if (item != null && item.isConsumable()) {
                // Create a copy of the item to return (before modifying quantities)
                InventoryItem usedItem = new InventoryItem(item.getName(), item.getIconPath(), 1);

                // Reduce quantity
                item.setQuantity(item.getQuantity() - 1);

                // Remove item if quantity reaches 0
                if (item.getQuantity() <= 0) {
                    removeItem(index);
                }

                return usedItem;
            }
        }
        return null;
    }

    /**
     * Try to use an item (returns the item without consuming it, for checking purposes)
     * @param index The inventory index of the item
     * @return The item if it exists and is consumable, null otherwise
     */
    public InventoryItem tryUseItem(int index) {
        if (index >= 0 && index < items.size()) {
            InventoryItem item = items.get(index);
            if (item != null && item.isConsumable()) {
                return new InventoryItem(item.getName(), item.getIconPath(), 1);
            }
        }
        return null;
    }

    /**
     * Use an item from quick access slot
     * @param quickSlotIndex The quick access slot (0-3)
     * @return The item that was used, or null if invalid/unusable
     */
    public InventoryItem useQuickAccessItem(int quickSlotIndex) {
        if (quickSlotIndex >= 0 && quickSlotIndex < QUICK_ACCESS_SIZE) {
            int inventoryIndex = quickAccessSlots[quickSlotIndex];
            return useItem(inventoryIndex);
        }
        return null;
    }

    /**
     * Clear a quick access slot
     * @param quickSlotIndex The quick access slot (0-3)
     */
    public void clearQuickAccessSlot(int quickSlotIndex) {
        if (quickSlotIndex >= 0 && quickSlotIndex < QUICK_ACCESS_SIZE) {
            quickAccessSlots[quickSlotIndex] = -1;
        }
    }

    /**
     * Check if an item is already assigned to a quick access slot
     * @param inventoryIndex The inventory index to check
     * @return The quick access slot index, or -1 if not assigned
     */
    public int findQuickAccessSlot(int inventoryIndex) {
        for (int i = 0; i < QUICK_ACCESS_SIZE; i++) {
            if (quickAccessSlots[i] == inventoryIndex) {
                return i;
            }
        }
        return -1;
    }

    public static class InventoryItem {
        private String name;
        private String iconPath;
        private int quantity;
        private ItemType type;
        private int healAmount;
        private boolean isConsumable;

        public InventoryItem(String name, String iconPath) {
            this.name = name;
            this.iconPath = iconPath;
            this.quantity = 1;
            this.type = determineItemType(name);
            this.healAmount = determineHealAmount(name);
            this.isConsumable = determineConsumable(name);
        }

        public InventoryItem(String name, String iconPath, int quantity) {
            this.name = name;
            this.iconPath = iconPath;
            this.quantity = quantity;
            this.type = determineItemType(name);
            this.healAmount = determineHealAmount(name);
            this.isConsumable = determineConsumable(name);
        }

        private ItemType determineItemType(String name) {
            if (name.toLowerCase().contains("bandage") || name.toLowerCase().contains("first aid") || name.toLowerCase().contains("food") || name.toLowerCase().contains("soup")) {
                return ItemType.HEALING;
            } else if (name.toLowerCase().contains("gun") || name.toLowerCase().contains("pistol") || name.toLowerCase().contains("shotgun")) {
                return ItemType.WEAPON;
            } else if (name.toLowerCase().contains("bullet")) {
                return ItemType.AMMO;
            } else if (name.toLowerCase().contains("wall")) {
                return ItemType.BUILDING;
            }
            return ItemType.MISC;
        }

        private int determineHealAmount(String name) {
            if (name.toLowerCase().contains("bandage")) {
                return 20;
            } else if (name.toLowerCase().contains("first aid")) {
                return 50;
            } else if (name.toLowerCase().contains("food") || name.toLowerCase().contains("soup")) {
                return 15;
            }
            return 0;
        }

        private boolean determineConsumable(String name) {
            return name.toLowerCase().contains("bandage") ||
                   name.toLowerCase().contains("first aid") ||
                   name.toLowerCase().contains("food") ||
                   name.toLowerCase().contains("soup");
        }

        public String getName() {
            return name;
        }

        public String getIconPath() {
            return iconPath;
        }

        public int getQuantity() {
            return quantity;
        }

        public ItemType getType() {
            return type;
        }

        public int getHealAmount() {
            return healAmount;
        }

        public boolean isConsumable() {
            return isConsumable;
        }

        public void addQuantity(int amount) {
            this.quantity += amount;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getDescription() {
            switch (type) {
                case HEALING:
                    return "Heals " + healAmount + " HP";
                case WEAPON:
                    return "Weapon";
                case AMMO:
                    return "Ammunition";
                case BUILDING:
                    return "Building material";
                default:
                    return "Miscellaneous item";
            }
        }
    }

    public enum ItemType {
        HEALING,
        WEAPON,
        AMMO,
        BUILDING,
        MISC
    }
}




