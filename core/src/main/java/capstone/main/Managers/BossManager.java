package capstone.main.Managers;

import capstone.main.Enemies.AbstractEnemy;
import capstone.main.Enemies.BossEntity;

import java.util.List;

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
