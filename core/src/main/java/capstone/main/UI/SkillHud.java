package capstone.main.UI;

import capstone.main.Characters.AbstractPlayer;
import capstone.main.Characters.MannyPacquiao;
import capstone.main.Skills.Skill;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SkillHud {
    private final Stage stage;
    private final AbstractPlayer player;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    // Skill icons
    private Texture skill1Icon;
    private Texture skill2Icon;
    private Texture skill3Icon;

    // UI elements
    private Table skillTable;
    private SkillSlot[] skillSlots;

    // Font for cooldown text
    private BitmapFont cooldownFont;

    public SkillHud(Viewport uiViewport, SpriteBatch batch, AbstractPlayer player) {
        this.stage = new Stage(uiViewport, batch);
        this.batch = batch;
        this.player = player;
        this.shapeRenderer = new ShapeRenderer();

        cooldownFont = new BitmapFont();
        cooldownFont.getData().setScale(1.5f);
        cooldownFont.setColor(Color.WHITE);

        loadSkillIcons();
        buildUI();
    }

    private void loadSkillIcons() {
        if (player instanceof MannyPacquiao) {
            // Load Manny's skill icons
            skill1Icon = new Texture(Gdx.files.internal("ui/Skills/Characters/Manny Pacquiao/meteor_fist.jpg"));
            skill2Icon = new Texture(Gdx.files.internal("ui/Skills/Characters/Manny Pacquiao/barrage_combo.jpg"));
            skill3Icon = new Texture(Gdx.files.internal("ui/Skills/Characters/Manny Pacquiao/champions_knockout.jpg"));
        }
        // Add more characters here later

        // Set filter for crisp icons
        if (skill1Icon != null) skill1Icon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        if (skill2Icon != null) skill2Icon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        if (skill3Icon != null) skill3Icon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private void buildUI() {
        skillTable = new Table();
        skillTable.setFillParent(true);
        skillTable.bottom().left(); // Position at bottom-left
        skillTable.pad(20f);

        if (player instanceof MannyPacquiao) {
            MannyPacquiao manny = (MannyPacquiao) player;

            skillSlots = new SkillSlot[3];
            skillSlots[0] = new SkillSlot(skill1Icon, "Q", manny.getMeteorFist());
            skillSlots[1] = new SkillSlot(skill2Icon, "E", manny.getBarrageCombo());
            skillSlots[2] = new SkillSlot(skill3Icon, "R", manny.getChampionsKnockout());

            // Add skill slots to table
            for (SkillSlot slot : skillSlots) {
                skillTable.add(slot.table).size(70f, 70f).padRight(10f);
            }
        }

        stage.addActor(skillTable);
    }

    public void update(float delta) {
        if (skillSlots != null) {
            for (SkillSlot slot : skillSlots) {
                slot.update();
            }
        }
        stage.act(delta);
    }

    public void draw() {
        stage.draw();

        // Draw cooldown overlays and text
        if (skillSlots != null) {
            batch.setProjectionMatrix(stage.getCamera().combined);
            shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

            for (SkillSlot slot : skillSlots) {
                slot.drawCooldown(batch, shapeRenderer, cooldownFont);
            }
        }
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        cooldownFont.dispose();
        shapeRenderer.dispose();
        if (skill1Icon != null) skill1Icon.dispose();
        if (skill2Icon != null) skill2Icon.dispose();
        if (skill3Icon != null) skill3Icon.dispose();
    }

    // Inner class for individual skill slots
    private class SkillSlot {
        Table table;
        Image icon;
        Label keybindLabel;
        Skill skill;
        float x, y, width, height;

        public SkillSlot(Texture iconTexture, String keybind, Skill skill) {
            this.skill = skill;

            table = new Table();
            table.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(
                    new Texture(Gdx.files.internal("ui/Skills/skill_frame.png"))
                )
            ));

            // Icon
            if (iconTexture != null) {
                icon = new Image(iconTexture);
                table.add(icon).expand().fill().row();
            }

            // Keybind label at bottom
            Label.LabelStyle style = new Label.LabelStyle();
            style.font = new BitmapFont();
            style.fontColor = Color.WHITE;
            keybindLabel = new Label(keybind, style);
            keybindLabel.setFontScale(1.2f);
            table.add(keybindLabel).padBottom(5f);
        }

        public void update() {
            // Store position for drawing cooldown overlay
            x = table.getX();
            y = table.getY();
            width = table.getWidth();
            height = table.getHeight();
        }

        public void drawCooldown(SpriteBatch batch, ShapeRenderer shapeRenderer, BitmapFont font) {
            if (skill == null) return;

            float cooldownProgress = skill.getCooldownProgress();

            // If skill is on cooldown, draw dark overlay
            if (cooldownProgress < 1f) {
                Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                    com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0, 0, 0, 0.6f);

                // Draw overlay from bottom up based on cooldown progress
                float overlayHeight = height * (1f - cooldownProgress);
                shapeRenderer.rect(x, y, width, overlayHeight);

                shapeRenderer.end();

                // Draw cooldown text
                batch.begin();
                String cooldownText = String.format("%.1f", skill.getCurrentCooldown());

                // Center the text
                com.badlogic.gdx.graphics.g2d.GlyphLayout layout =
                    new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, cooldownText);
                float textX = x + (width - layout.width) / 2f;
                float textY = y + (height + layout.height) / 2f;

                // Draw shadow
                font.setColor(Color.BLACK);
                font.draw(batch, cooldownText, textX + 2, textY - 2);

                // Draw text
                font.setColor(Color.WHITE);
                font.draw(batch, cooldownText, textX, textY);

                batch.end();

                Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            }
        }
    }
}
