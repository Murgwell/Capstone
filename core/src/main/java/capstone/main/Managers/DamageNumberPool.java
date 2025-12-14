package capstone.main.Managers;

import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

/**
 * Object pool for damage numbers to improve performance and reduce garbage collection
 */

public class DamageNumberPool extends Pool<DamageNumber> {
    private final BitmapFont font;
    private final ArrayList<DamageNumber> activeDamageNumbers;

    public DamageNumberPool(BitmapFont font, ArrayList<DamageNumber> activeDamageNumbers) {
        this.font = font;
        this.activeDamageNumbers = activeDamageNumbers;
    }

    @Override
    protected DamageNumber newObject() {
        // Create a new damage number with default values
        return new DamageNumber("", 0, 0, font, Color.WHITE);
    }

    /**
     * Get a damage number from the pool and configure it
     */
    public DamageNumber obtain(String text, float x, float y, Color color) {
        DamageNumber damageNumber = obtain();
        damageNumber.reset(text, x, y, color);
        activeDamageNumbers.add(damageNumber);
        return damageNumber;
    }

    /**
     * Return a damage number to the pool
     */
    public void free(DamageNumber damageNumber) {
        activeDamageNumbers.remove(damageNumber);
        super.free(damageNumber);
    }

    /**
     * Update all active damage numbers and return expired ones to pool
     */
    public void updateAndCleanup(float delta) {
        for (int i = activeDamageNumbers.size() - 1; i >= 0; i--) {
            DamageNumber damageNumber = activeDamageNumbers.get(i);
            damageNumber.update(delta);

            if (damageNumber.isExpired()) {
                free(damageNumber);
            }
        }
    }
}
