package capstone.main.Enemies;

public interface BossEntity {
    boolean isBoss();
    float getMaxHealth();
    float getCurrentHealth();
    default float getHealthRatio() { return getMaxHealth() <= 0 ? 0 : getCurrentHealth() / getMaxHealth(); }
    String getSkillWarning();
    boolean isDead();
}
