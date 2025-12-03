package capstone.main.Skills;

public abstract class Skill {
    protected String name;
    protected float cooldown;
    protected float currentCooldown;
    protected boolean isActive;

    public Skill(String name, float cooldown) {
        this.name = name;
        this.cooldown = cooldown;
        this.currentCooldown = 0f;
        this.isActive = false;
    }

    public void update(float delta) {
        if (currentCooldown > 0) {
            currentCooldown -= delta;
            if (currentCooldown < 0) currentCooldown = 0;
        }
    }

    public boolean canUse() {
        return currentCooldown <= 0 && !isActive;
    }

    public void startCooldown() {
        currentCooldown = cooldown;
    }

    public float getCooldownProgress() {
        return 1f - (currentCooldown / cooldown);
    }

    public float getCurrentCooldown() {
        return currentCooldown;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getName() {
        return name;
    }

    public abstract void activate();
    public abstract void deactivate();
}
