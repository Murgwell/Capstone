package capstone.main.Skills.Quiboloy;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Managers.SoundManager;
import capstone.main.Skills.Skill;
import capstone.main.Sprites.DamageNumber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class DivineHealing extends Skill {

    private static final float MIN_HEAL = 6f;
    private static final float MAX_HEAL = 10f;
    private static final int HEAL_TICKS = 5;          // Number of heal pulses
    private static final float HEAL_INTERVAL = 0.5f; // Time between heals

    private AbstractPlayer player;
    private ArrayList<DamageNumber> damageNumbers;
    private BitmapFont healFont;

    private int healsRemaining;
    private float healTimer;

    public DivineHealing(AbstractPlayer player,
                         ArrayList<DamageNumber> damageNumbers,
                         BitmapFont healFont) {
        super("Divine Healing", 12f); // cooldown in seconds
        this.player = player;
        this.damageNumbers = damageNumbers;
        this.healFont = healFont;
        this.healsRemaining = 0;
        this.healTimer = 0f;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (isActive) {
            healTimer -= delta;

            if (healTimer <= 0 && healsRemaining > 0) {
                performHeal();
                healsRemaining--;
                healTimer = HEAL_INTERVAL;

                if (healsRemaining <= 0) {
                    deactivate();
                }
            }
        }
    }

    @Override
    public void activate() {
        if (!canUse()) return;

        isActive = true;
        healsRemaining = HEAL_TICKS;
        healTimer = 0f;
        startCooldown();

        SoundManager.getInstance().playSound("quiboloy_heal");
        com.badlogic.gdx.Gdx.app.log("DivineHealing", "Healing started!");
    }

    private void performHeal() {
        float healAmount = MathUtils.random(MIN_HEAL, MAX_HEAL);
        player.heal(healAmount);

        Vector2 playerPos = player.getPosition();
        damageNumbers.add(new DamageNumber(
            "+" + String.format("%.0f", healAmount),
            playerPos.x + MathUtils.random(-0.2f, 0.2f),
            playerPos.y + 0.5f,
            healFont,
            Color.GREEN
        ));
    }

    @Override
    public void deactivate() {
        isActive = false;
        healsRemaining = 0;
    }
}
