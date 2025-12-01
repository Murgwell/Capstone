
package capstone.main.menus;

import capstone.main.Corrupted;
import capstone.main.Managers.MusicManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class CharacterSelectionScreen implements Screen {
    private final Corrupted game;

    private Stage stage;
    private Skin skin;

    private Texture confirmNormalTexture;
    private Texture confirmPressedTexture;
    private Texture leftArrowTexture;
    private Texture rightArrowTexture;

    // One preview texture per character (no sprite-sheet slicing)
    private Texture[] characterPreviewTextures;
    private String[] characterNames;

    private Texture bgTexture;
    private Image bgImage;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Image characterPreview;
    private Label characterNameLabel;

    private int currentCharacterIndex = 0; // Start with first character
    private static final int NUM_CHARACTERS = 3;

    public CharacterSelectionScreen(Corrupted game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        // Ensure background music is playing
        MusicManager.getInstance().ensurePlaying();

        // Background
        bgTexture = new Texture("character_screen_bg.png");
        bgImage = new Image(bgTexture);
        bgImage.setFillParent(true);
        bgImage.setScaling(Scaling.stretch);
        stage.addActor(bgImage);

        // --- Load character preview images (single texture per character) ---
        characterPreviewTextures = new Texture[NUM_CHARACTERS];
        characterNames = new String[NUM_CHARACTERS];

        characterPreviewTextures[0] = new Texture("Textures/Characters/Vico Sotto/Frame.png");
        characterNames[0] = "Vico Sotto";

        characterPreviewTextures[1] = new Texture("Textures/Characters/Manny Pacquiao/Frame.png");
        characterNames[1] = "Manny Pacquiao";

        characterPreviewTextures[2] = new Texture("Textures/Characters/Quiboloy/Frame.png");
        characterNames[2] = "Quiboloy";

        // --- Load arrow button textures ---
        leftArrowTexture = new Texture("ui/Menu/left_arrow.png");
        rightArrowTexture = new Texture("ui/Menu/right_arrow.png");

        // --- Load confirm button textures ---
        confirmNormalTexture = new Texture("ui/Menu/confirm_button_normal.png");
        confirmPressedTexture = new Texture("ui/Menu/confirm_button_pressed.png");

        // Root table
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Title
        Label title = new Label("SELECT CHARACTER", skin);
        title.setFontScale(3.0f);
        title.setColor(Color.WHITE);
        root.add(title).colspan(3).center().padBottom(30f).padTop(60f);
        root.row();

        // Character preview area with navigation
        Table characterArea = new Table();

        // Left arrow button
        ImageButton leftArrowButton = createArrowButton(leftArrowTexture);
        leftArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                navigateCharacter(-1);
            }
        });
        characterArea.add(leftArrowButton).width(80).height(80).padRight(40f);

        // Character preview (center) - direct image (no slicing)
        TextureRegion initialRegion = new TextureRegion(characterPreviewTextures[currentCharacterIndex]);
        characterPreview = new Image(new TextureRegionDrawable(initialRegion));
        characterPreview.setSize(350, 350);
        characterPreview.setScaling(Scaling.fit);

        characterNameLabel = new Label(characterNames[currentCharacterIndex], skin);
        characterNameLabel.setFontScale(2.0f);
        characterNameLabel.setColor(Color.WHITE);

        // Preview table
        Table previewTable = new Table();
        previewTable.add(characterPreview)
            .width(350)
            .height(350)
            .row();
        previewTable.add(characterNameLabel)
            .padTop(10f);

        characterArea.add(previewTable).expand().center();

        // Right arrow button
        ImageButton rightArrowButton = createArrowButton(rightArrowTexture);
        rightArrowButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                navigateCharacter(1);
            }
        });
        characterArea.add(rightArrowButton).width(80).height(80).padLeft(40f);

        // Align preview lower
        root.add(characterArea).colspan(3).expand().center().padBottom(20f);
        root.row();

        // Confirm button
        ImageButton confirmButton = createConfirmButton();
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Selected character: " + currentCharacterIndex + " - " + characterNames[currentCharacterIndex]);
                // Stop music when entering game
                MusicManager.getInstance().stop();
                game.setScreen(new Game(game, currentCharacterIndex));
            }
        });
        root.add(confirmButton).colspan(3).width(250).height(70).padTop(20f).padBottom(40f);
    }

    private ImageButton createArrowButton(Texture arrowTexture) {
        TextureRegion arrowRegion = new TextureRegion(arrowTexture);
        ImageButton.ImageButtonStyle arrowStyle = new ImageButton.ImageButtonStyle();
        arrowStyle.up = new TextureRegionDrawable(arrowRegion);
        arrowStyle.down = new TextureRegionDrawable(arrowRegion); // same as up (no pressed animation)
        return new ImageButton(arrowStyle);
    }

    private ImageButton createConfirmButton() {
        TextureRegion normalRegion = new TextureRegion(confirmNormalTexture);
        TextureRegion pressedRegion = new TextureRegion(confirmPressedTexture);
        ImageButton.ImageButtonStyle confirmStyle = new ImageButton.ImageButtonStyle();
        confirmStyle.up = new TextureRegionDrawable(normalRegion);
        confirmStyle.down = new TextureRegionDrawable(pressedRegion);
        return new ImageButton(confirmStyle);
    }

    private void navigateCharacter(int direction) {
        currentCharacterIndex += direction;
        // Wrap around character index
        if (currentCharacterIndex < 0) {
            currentCharacterIndex = NUM_CHARACTERS - 1;
        } else if (currentCharacterIndex >= NUM_CHARACTERS) {
            currentCharacterIndex = 0;
        }

        // Update the preview image with the full texture (no slicing)
        TextureRegion newRegion = new TextureRegion(characterPreviewTextures[currentCharacterIndex]);
        characterPreview.setDrawable(new TextureRegionDrawable(newRegion));
        characterPreview.setSize(350, 350);
        characterPreview.setScaling(Scaling.fit);

        // Update displayed character name
        characterNameLabel.setText(characterNames[currentCharacterIndex]);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
        if (bgImage != null) {
            bgImage.setSize(width, height);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (bgTexture != null) {
            bgTexture.dispose();
        }
        if (characterPreviewTextures != null) {
            for (Texture t : characterPreviewTextures) {
                if (t != null) t.dispose();
            }
        }
        if (confirmNormalTexture != null) confirmNormalTexture.dispose();
        if (confirmPressedTexture != null) confirmPressedTexture.dispose();
        if (leftArrowTexture != null) leftArrowTexture.dispose();
        if (rightArrowTexture != null) rightArrowTexture.dispose();
        if (skin != null) skin.dispose();
    }
}
