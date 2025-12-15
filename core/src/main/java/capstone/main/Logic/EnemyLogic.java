package capstone.main.Logic;

import capstone.main.Enemies.*;
import capstone.main.Characters.AbstractPlayer;
import com.badlogic.gdx.Gdx;

import java.util.ArrayList;

public class EnemyLogic {
    private final EnemySpawner spawner;
    private final ArrayList<AbstractEnemy> enemies;
    private final AbstractPlayer player;
    private final capstone.main.Managers.ObjectiveManager objectiveManager;

    public EnemyLogic(EnemySpawner spawner, ArrayList<AbstractEnemy> enemies, AbstractPlayer player, capstone.main.Managers.ObjectiveManager objectiveManager) {
        this.spawner = spawner;
        this.enemies = enemies;
        this.player = player;
        this.objectiveManager = objectiveManager;
    }

    public void update(float delta) {
        spawner.update(delta);

        // Check deferred boss spawn trigger near center (or scheduled point)
        if (spawner.hasScheduledBoss()) {
            Gdx.app.log("EnemyLogic", "Scheduled boss present. Player checking trigger zone...");
            float px = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
            float py = player.getSprite().getY() + player.getSprite().getHeight() / 2f;
            float dx = px - spawner.getScheduledBossX();
            float dy = py - spawner.getScheduledBossY();
            float dist2 = dx*dx + dy*dy;
            float r = spawner.getScheduledBossRadius();
            if (dist2 <= r*r) {
                spawner.executeScheduledBoss();
            }
        }

        // Update enemies and check for melee attacks
        // Use indexed loop to avoid ConcurrentModificationException when enemies spawn during update
        int enemyCount = enemies.size();
        for (int i = 0; i < enemyCount; i++) {
            // Check bounds in case list was modified
            if (i >= enemies.size()) break;

            AbstractEnemy e = enemies.get(i);
            if (!e.isDead()) {
                e.update(delta, player);

                // Check if enemy is close enough to attack player
                checkMeleeAttack(e);
            }
        }

        // Resolve collisions
        EnemySpawner.resolveEnemyCollisions(enemies, delta);

        // Remove dead enemies safely
        for (int i = enemies.size() - 1; i >= 0; i--) {
            AbstractEnemy e = enemies.get(i);
            if (e.isPendingRemoval()) {
                // Notify objective manager before removal
                if (objectiveManager != null) {
                    try { objectiveManager.onEnemyKilled(e); } catch (Exception ignored) {}
                }
                // MEMORY FIX: Call dispose() before removing enemy
                System.out.println("DISPOSING ENEMY - Remaining: " + (enemies.size() - 1) + " (Memory Debug)");
                e.dispose();
                enemies.remove(i);
            }
        }
    }

    private void checkMeleeAttack(AbstractEnemy enemy) {
        // Calculate distance between enemy and player
        float enemyX = enemy.getBody().getPosition().x;
        float enemyY = enemy.getBody().getPosition().y;

        float playerX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float playerY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        float dx = playerX - enemyX;
        float dy = playerY - enemyY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // If enemy is in melee range and can attack
        if (distance <= enemy.getMeleeRange() && enemy.canAttack()) {
            player.damage(enemy.getMeleeDamage());
            enemy.resetAttackCooldown();

            Gdx.app.log("EnemyAttack", "Enemy attacked player! Player HP: " + player.getHp() + "/" + player.getMaxHp());
        }
    }
}
