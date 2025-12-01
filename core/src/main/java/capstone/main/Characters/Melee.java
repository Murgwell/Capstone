package capstone.main.Characters;

public interface Melee {
    void performMeleeAttack(float delta, float weaponRotationRad);
    float getMeleeRange();
    float getMeleeDamage();
}
