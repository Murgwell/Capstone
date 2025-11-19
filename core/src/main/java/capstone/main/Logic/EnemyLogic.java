package capstone.main.Logic;

import capstone.main.Enemies.*;
import capstone.main.Characters.AbstractPlayer;

import java.util.ArrayList;

public class EnemyLogic {
    private final EnemySpawner spawner;
    private final ArrayList<AbstractEnemy> enemies;
    private final AbstractPlayer player;

    public EnemyLogic(EnemySpawner spawner, ArrayList<AbstractEnemy> enemies, AbstractPlayer player) {
        this.spawner = spawner;
        this.enemies = enemies;
        this.player = player;
    }

    public void update(float delta) {
        spawner.update(delta);

        // Update enemies
        for (AbstractEnemy e : enemies) {
            if (!e.isDead()) e.update(delta, player);
        }

        // Resolve collisions
        EnemySpawner.resolveEnemyCollisions(enemies, delta);

        // Remove dead enemies safely
        for (int i = enemies.size() - 1; i >= 0; i--) {
            AbstractEnemy e = enemies.get(i);
            if (e.isPendingRemoval()) {
                e.getBody().getWorld().destroyBody(e.getBody());
                enemies.remove(i);
            }
        }
    }

}
