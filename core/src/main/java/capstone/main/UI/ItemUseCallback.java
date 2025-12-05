package capstone.main.UI;

import capstone.main.Managers.Inventory;

public interface ItemUseCallback {
    void onItemUse(Inventory.InventoryItem item);
}