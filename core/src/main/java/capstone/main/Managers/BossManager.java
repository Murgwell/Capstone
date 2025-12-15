package capstone.main.Managers;

import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Enemies.BossEntity;

import java.util.List;

/**
 * Manages the current boss entity reference for the active level.
 * 
 * <p>Provides centralized boss tracking for:
 * <ul>
 *   <li>UI elements (boss health bar, cast bar)</li>
 *   <li>Game logic (boss-specific mechanics)</li>
 *   <li>Victory conditions</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>{@code
 * BossManager manager = new BossManager();
 * manager.findAndSetBoss(enemies);
 * BossEntity boss = manager.getCurrentBoss();
 * manager.clearBoss(); // When transitioning maps
 * }</pre>
 * 
 * @author Capstone Team
 * @version 1.0
 */
public class BossManager {
    private BossEntity currentBoss;

    public void setBoss(BossEntity boss) {
        this.currentBoss = boss;
    }

    public BossEntity getCurrentBoss() {
        return currentBoss;
    }

    public void findAndSetBoss(List<AbstractEnemy> enemies) {
        currentBoss = null;
        for (AbstractEnemy e : enemies) {
            if (e instanceof BossEntity) {
                currentBoss = (BossEntity) e;
                break;
            }
        }
    }

    public void clearBoss() {
        currentBoss = null;
    }
}
