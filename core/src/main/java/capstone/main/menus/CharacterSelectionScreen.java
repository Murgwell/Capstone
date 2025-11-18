package capstone.main.menus;

import capstone.main.Corrupted;
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
    private Texture[] frameNormal;
    private Texture[] frameSelected;
    private Texture[] portraits;

    private Texture bgTexture;
    private Image bgImage;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Image[] characterFrames;
    private int selectedCharacter = -1;

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

        // Background
        bgTexture = new Texture("character_screen_bg.png");
        bgImage = new Image(bgTexture);
        bgImage.setFillParent(true);
        bgImage.setScaling(Scaling.stretch);
        stage.addActor(bgImage);

        // Load frames and portraits
        frameNormal = new Texture[3];
        frameSelected = new Texture[3];
        portraits = new Texture[3];

        frameNormal[0] = new Texture("ui/Characters/sotto_frame_normal.png");
        frameSelected[0] = new Texture("ui/Characters/sotto_frame_pressed.png");
        portraits[0] = new Texture("character.png");

        frameNormal[1] = new Texture("ui/Characters/pacquiao_frame_normal.png");
        frameSelected[1] = new Texture("ui/Characters/pacquiao_frame_pressed.png");
        portraits[1] = new Texture("character.png");

        frameNormal[2] = new Texture("ui/Characters/quiboloy_frame_normal.png");
        frameSelected[2] = new Texture("ui/Characters/quiboloy_frame_pressed.png");
        portraits[2] = new Texture("character.png");

        characterFrames = new Image[3];

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Title row
        Label title = new Label("SELECT CHARACTER", skin);
        title.setFontScale(2.5f);
        title.setColor(Color.WHITE);
        root.add(title).colspan(3).center().padBottom(40f);
        root.row();

        // Character row: each fills 1/3 of screen width, full height
        root.add(createCharacter(0)).expand().fill();
        root.add(createCharacter(1)).expand().fill();
        root.add(createCharacter(2)).expand().fill();
        root.row().padTop(30f);

        // Load the confirm button textures
        confirmNormalTexture = new Texture("ui/Menu/confirm_button_normal.png");
        confirmPressedTexture = new Texture("ui/Menu/confirm_button_pressed.png");

        // Optionally scale down the textures (50% downscale, for example)
        int targetWidth = 200; // Set your desired button width
        int targetHeight = 60; // Set your desired button height (adjust based on aspect ratio)

        confirmNormalTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        confirmPressedTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Scale down the texture
        TextureRegion normalRegion = new TextureRegion(confirmNormalTexture);
        TextureRegion pressedRegion = new TextureRegion(confirmPressedTexture);

        // Now create the button with the scaled texture
        ImageButton.ImageButtonStyle confirmButtonStyle = new ImageButton.ImageButtonStyle();
        confirmButtonStyle.up = new TextureRegionDrawable(normalRegion);
        confirmButtonStyle.down = new TextureRegionDrawable(pressedRegion);

        ImageButton confirmButton = new ImageButton(confirmButtonStyle);

        // Add padding or use setSize to control the final size
        confirmButton.setSize(targetWidth, targetHeight);

        // Add the button to the layout
        Table table = new Table();
        table.add(confirmButton).width(targetWidth).height(targetHeight).center();

        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedCharacter != -1) {
                    System.out.println("Selected character: " +
                    selectedCharacter);
                    game.setScreen(new Game(game));
                }
            }
        });

        root.add(table).colspan(3).center().padTop(20f).padBottom(20f);


    }

    private Stack createCharacter(final int index) {
        TextureRegionDrawable normalDrawable =
            new TextureRegionDrawable(new TextureRegion(frameNormal[index]));
        TextureRegionDrawable selectedDrawable =
            new TextureRegionDrawable(new TextureRegion(frameSelected[index]));

        // Frame image (fills cell)
        Image frame = new Image(normalDrawable);
        frame.setScaling(Scaling.stretch);
        characterFrames[index] = frame;

        // Portrait image (fills frame area)
        Image portrait = new Image(portraits[index]);
        portrait.setScaling(Scaling.fit);
        portrait.setFillParent(true);

        // Invisible button overlay
        TextButton invisibleButton = new TextButton("", skin);
        invisibleButton.setColor(1, 1, 1, 0.01f);
        invisibleButton.setFillParent(true);

        Stack stack = new Stack();
        stack.add(frame);
        stack.add(portrait);
        stack.add(invisibleButton);

        invisibleButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Store the selected character index
                selectedCharacter = index;
                System.out.println("Character selected: " + selectedCharacter);
                // Update the frame selection UI to reflect the selected character
                updateFrameSelection();
            }
        });

        return stack;
    }

    private void updateFrameSelection() {
        for (int i = 0; i < characterFrames.length; i++) {
            if (i == selectedCharacter) {
                characterFrames[i].setDrawable(
                    new TextureRegionDrawable(new TextureRegion(frameSelected[i])));
            } else {
                characterFrames[i].setDrawable(
                    new TextureRegionDrawable(new TextureRegion(frameNormal[i])));
            }
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        bgImage.setSize(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        bgTexture.dispose();
        for (int i = 0; i < 3; i++) {
            frameNormal[i].dispose();
            frameSelected[i].dispose();
            portraits[i].dispose();
        }
    }
}
