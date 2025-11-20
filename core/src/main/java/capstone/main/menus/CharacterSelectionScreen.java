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

    // Character sprite sheets
    private Texture[] characterSpriteSheets;
    private String[] characterNames;
    
    // Frame extraction parameters
    private static final int PREVIEW_FRAME_X = 0; // X index of preview frame (0 = first frame from left)
    private static final int PREVIEW_FRAME_Y = 0; // Y index of preview frame (0 = top row)

    private Texture bgTexture;
    private Image bgImage;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Image characterPreview;
    private Label characterNameLabel;
    private int currentCharacterIndex = 0; // Start with first character (Vico Sotto)
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

        // Ensure background music is playing (continues from previous screen)
        MusicManager musicManager = MusicManager.getInstance();
        musicManager.ensurePlaying();

        // Background
        bgTexture = new Texture("character_screen_bg.png");
        bgImage = new Image(bgTexture);
        bgImage.setFillParent(true);
        bgImage.setScaling(Scaling.stretch);
        stage.addActor(bgImage);

        // Load character sprite sheets
        characterSpriteSheets = new Texture[NUM_CHARACTERS];
        characterNames = new String[NUM_CHARACTERS];
        
        characterSpriteSheets[0] = new Texture("Textures/Characters/Vico_Sotto_Idle_Run_Animation.png");
        characterNames[0] = "Vico Sotto";
        
        characterSpriteSheets[1] = new Texture("Textures/Characters/Manny_Pacquiao_Idle_Run_Animation.png");
        characterNames[1] = "Manny Pacquiao";
        
        characterSpriteSheets[2] = new Texture("Textures/Characters/Quiboloy_Idle_Run_Anim.png");
        characterNames[2] = "Quiboloy";

        // Load arrow button textures (simple arrows with no pressed animations)
        leftArrowTexture = new Texture("ui/Menu/left_arrow.png");
        rightArrowTexture = new Texture("ui/Menu/right_arrow.png");

        // Load confirm button textures
        confirmNormalTexture = new Texture("ui/Menu/confirm_button_normal.png");
        confirmPressedTexture = new Texture("ui/Menu/confirm_button_pressed.png");

        // Create root table
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Title
        Label title = new Label("SELECT CHARACTER", skin);
        title.setFontScale(3.0f);
        title.setColor(Color.WHITE);
        root.add(title).colspan(3).center().padBottom(-80f).padTop(80f);
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

        // Character preview (center) - extract a single frame from sprite sheet
        TextureRegion previewFrame = extractFrame(
            characterSpriteSheets[currentCharacterIndex],
            PREVIEW_FRAME_X,
            PREVIEW_FRAME_Y
        );

        // Create preview image using ONE frame
        characterPreview = new Image(new TextureRegionDrawable(previewFrame));

        // Make the preview BIGGER and cleaner
        characterPreview.setSize(350, 350);          // enlarge character
        characterPreview.setScaling(Scaling.fill);   // fills the box, avoids small rendering

        characterNameLabel = new Label(characterNames[currentCharacterIndex], skin);
        characterNameLabel.setFontScale(2.0f);
        characterNameLabel.setColor(Color.WHITE);

        // Create preview table
        Table previewTable = new Table();
        previewTable.add(characterPreview)
                .width(350)
                .height(350)
                .row();
        previewTable.add(characterNameLabel)
                .padTop(20f);

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

        root.add(characterArea).colspan(3).expand().center();
        root.row().padTop(40f);

        // Confirm button
        ImageButton confirmButton = createConfirmButton();
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Selected character: " + currentCharacterIndex + " - " + characterNames[currentCharacterIndex]);
                // Stop music when entering game (Game.java doesn't have music)
                MusicManager.getInstance().stop();
                game.setScreen(new Game(game, currentCharacterIndex));
            }
        });

        root.add(confirmButton).colspan(3).width(250).height(70).padTop(30f).padBottom(100f);
    }

    private ImageButton createArrowButton(Texture arrowTexture) {
        TextureRegion arrowRegion = new TextureRegion(arrowTexture);

        ImageButton.ImageButtonStyle arrowStyle = new ImageButton.ImageButtonStyle();
        arrowStyle.up = new TextureRegionDrawable(arrowRegion);
        arrowStyle.down = new TextureRegionDrawable(arrowRegion); // Same texture for pressed state (no animation)

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

    /**
     * Extracts a single frame from a sprite sheet
     * Assumes sprite sheets are 2 rows x 13 columns (26 frames total)
     * @param spriteSheet The sprite sheet texture
     * @param frameX The X index of the frame (0 = first frame from left, 0-12)
     * @param frameY The Y index of the frame (0 = top row, 0-1)
     * @return TextureRegion containing the extracted frame
     */
    private TextureRegion extractFrame(Texture spriteSheet, int frameX, int frameY) {
        int sheetWidth = spriteSheet.getWidth();
        int sheetHeight = spriteSheet.getHeight();
        
        // Calculate frame dimensions: 2 rows x 13 columns
        float frameWidthFloat = sheetWidth / 13f;
        float frameHeightFloat = sheetHeight / 2f;
        
        // Round to nearest integer for pixel boundaries
        int frameWidth = Math.round(frameWidthFloat);
        int frameHeight = Math.round(frameHeightFloat);
        
        // Calculate position
        int x = Math.round(frameX * frameWidthFloat);
        int y = Math.round(frameY * frameHeightFloat);
        
        // Debug output to help diagnose the issue
        System.out.println("Sprite Sheet: " + sheetWidth + "x" + sheetHeight);
        System.out.println("Calculated Frame: " + frameWidth + "x" + frameHeight);
        System.out.println("Extracting at: (" + x + "," + y + ")");
        
        // Ensure bounds
        if (x + frameWidth > sheetWidth) frameWidth = sheetWidth - x;
        if (y + frameHeight > sheetHeight) frameHeight = sheetHeight - y;
        
        return new TextureRegion(spriteSheet, x, y, frameWidth, frameHeight);
    }

    private void navigateCharacter(int direction) {
        currentCharacterIndex += direction;
    
        // Wrap around character index
        if (currentCharacterIndex < 0) {
            currentCharacterIndex = NUM_CHARACTERS - 1;
        } else if (currentCharacterIndex >= NUM_CHARACTERS) {
            currentCharacterIndex = 0;
        }
    
        // Extract a clean preview frame again
        TextureRegion previewFrame = extractFrame(
                characterSpriteSheets[currentCharacterIndex],
                PREVIEW_FRAME_X,
                PREVIEW_FRAME_Y
        );
    
        // Update the preview image
        characterPreview.setDrawable(new TextureRegionDrawable(previewFrame));
        characterPreview.setSize(350, 350);
        characterPreview.setScaling(Scaling.fill);
    
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
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (bgTexture != null) {
            bgTexture.dispose();
        }
        if (characterSpriteSheets != null) {
            for (int i = 0; i < characterSpriteSheets.length; i++) {
                if (characterSpriteSheets[i] != null) {
                    characterSpriteSheets[i].dispose();
                }
            }
        }
        if (confirmNormalTexture != null) {
            confirmNormalTexture.dispose();
        }
        if (confirmPressedTexture != null) {
            confirmPressedTexture.dispose();
        }
        if (leftArrowTexture != null) {
            leftArrowTexture.dispose();
        }
        if (rightArrowTexture != null) {
            rightArrowTexture.dispose();
        }
    }
}
