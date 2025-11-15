package capstone.main.Logic;

import capstone.main.Sprites.DamageNumber;

import java.util.ArrayList;

public class DamageLogic {
    private final ArrayList<DamageNumber> damageNumbers;

    public DamageLogic(ArrayList<DamageNumber> damageNumbers) {
        this.damageNumbers = damageNumbers;
    }

    public void update(float delta) {
        for (int i = damageNumbers.size() - 1; i >= 0; i--) {
            DamageNumber dn = damageNumbers.get(i);
            dn.update(delta);
            if (!dn.isAlive) damageNumbers.remove(i);
        }
    }
}
