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
        for (AbstractEnemy e : enemies) {
            if (!e.isDead()) e.update(delta, player);
        }
    }
}
