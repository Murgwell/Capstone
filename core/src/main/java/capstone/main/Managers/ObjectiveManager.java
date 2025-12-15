package capstone.main.Managers;

import capstone.main.Enemies.AbstractEnemy;

/**
 * Tracks world objectives (e.g., kill N enemies) and exposes portal gating state.
 * Designed to be updated by EnemyLogic when enemies are removed.
 */
public class ObjectiveManager {
    public interface Objective {
        String getIconPath();
        int getProgress();
        boolean isComplete();
        /**
         * Returns true if this call caused completion transition (first time reached requirement)
         */
        boolean onEnemyKilled(AbstractEnemy enemy);
        String getDisplayText();
    }

    public static class KillObjective implements Objective {
        private final Class<? extends AbstractEnemy> enemyClass;
        private final String displayName;
        private final String iconPath;
        private final int required;
        private int progress = 0;
        private boolean complete = false;

        public KillObjective(Class<? extends AbstractEnemy> enemyClass,
                             String displayName,
                             String iconPath,
                             int required) {
            this.enemyClass = enemyClass;
            this.displayName = displayName;
            this.iconPath = iconPath;
            this.required = required;
        }

        @Override public String getIconPath() { return iconPath; }
        @Override public int getProgress() { return progress; }
        @Override public boolean isComplete() { return complete; }

        @Override
        public boolean onEnemyKilled(AbstractEnemy enemy) {
            if (complete) return false;
            if (enemyClass.isInstance(enemy)) {
                progress++;
                if (progress >= required) {
                    complete = true;
                    return true; // newly completed
                }
            }
            return false;
        }

        @Override
        public String getDisplayText() {
            return displayName + ": (" + progress + "/" + required + ")";
        }
    }

    /**
     * Composite objective that requires multiple sub-objectives to be completed
     */
    public static class CompositeObjective implements Objective {
        private final java.util.List<Objective> subObjectives;
        private final String displayName;
        private final String iconPath;

        public CompositeObjective(String displayName, String iconPath, java.util.List<Objective> subObjectives) {
            this.displayName = displayName;
            this.iconPath = iconPath;
            this.subObjectives = subObjectives;
        }

        @Override public String getIconPath() { return iconPath; }


        @Override
        public int getProgress() {
            int completed = 0;
            for (Objective obj : subObjectives) {
                if (obj.isComplete()) completed++;
            }
            return completed;
        }

        @Override
        public boolean isComplete() {
            for (Objective obj : subObjectives) {
                if (!obj.isComplete()) return false;
            }
            return true;
        }

        @Override
        public boolean onEnemyKilled(AbstractEnemy enemy) {
            boolean anyNewlyCompleted = false;
            for (Objective obj : subObjectives) {
                if (obj.onEnemyKilled(enemy)) {
                    anyNewlyCompleted = true;
                }
            }
            return anyNewlyCompleted && isComplete();
        }

        @Override
        public String getDisplayText() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < subObjectives.size(); i++) {
                Objective obj = subObjectives.get(i);
                sb.append(obj.getDisplayText());
                if (obj.isComplete()) {
                    sb.append(" ✓");
                }
                if (i < subObjectives.size() - 1) {
                    sb.append("\n");
                }
            }
            return sb.toString();
        }

    }

    private Objective currentObjective;
    private boolean justCompleted = false;

    public void setObjective(Objective objective) {
        this.currentObjective = objective;
        this.justCompleted = false;
    }

    public Objective getObjective() { return currentObjective; }

    public void onEnemyKilled(AbstractEnemy enemy) {
        if (currentObjective == null) return;
        boolean newlyCompleted = currentObjective.onEnemyKilled(enemy);
        if (newlyCompleted) justCompleted = true;
    }

    /**
     * Returns true exactly once after an objective just completed since last call.
     */
    public boolean consumeJustCompleted() {
        boolean res = justCompleted;
        justCompleted = false;
        return res;
    }

    public boolean isObjectiveComplete() {
        return currentObjective != null && currentObjective.isComplete();
    }
}
