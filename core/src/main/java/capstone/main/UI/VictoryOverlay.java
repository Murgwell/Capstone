package capstone.main.UI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

public class VictoryOverlay {
    private final Table overlayTable;
    private final Image backgroundDim;
    private boolean isVisible = false;
    
    // Textures that need to be disposed
    private Texture backgroundTexture;
    private Texture buttonNormalTexture;
    private Texture buttonPressedTexture;
    
    // Callbacks for button actions
    public interface VictoryCallback {
        void onPlayAgain();
        void onExitToMainMenu();
    }
    
    private VictoryCallback callback;
    
    public VictoryOverlay(Stage stage, BitmapFont font, VictoryCallback callback) {
        this.callback = callback;
        
        // Create semi-transparent background
        backgroundTexture = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.85f);
        pixmap.fill();
        backgroundTexture.draw(pixmap, 0, 0);
        pixmap.dispose();
        
        backgroundDim = new Image(backgroundTexture);
        backgroundDim.setFillParent(true);
        backgroundDim.setVisible(false);
        stage.addActor(backgroundDim);
        
        // Create main overlay table
        overlayTable = new Table();
        overlayTable.setFillParent(true);
        overlayTable.setVisible(false);
        
        // Title label with larger font
        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.GOLD);
        Label titleLabel = new Label("VICTORY!", titleStyle);
        titleLabel.setFontScale(3.0f);
        titleLabel.setAlignment(Align.center);
        
        // Story message
        Label.LabelStyle messageStyle = new Label.LabelStyle(font, Color.WHITE);
        String victoryMessage = 
            "The corruption has been vanquished!\n\n" +
            "With Quiboloy defeated, the shadows that plagued\n" +
            "the Philippines begin to fade. The people can\n" +
            "finally breathe freely once more.\n\n" +
            "But remember, hero...\n\n" +
            "This victory is just the beginning.\n" +
            "Vigilance is eternal, for corruption never\n" +
            "truly sleeps. New challenges await,\n" +
            "and the fight for justice continues.\n\n" +
            "The future of the nation rests in your hands.";
        
        Label messageLabel = new Label(victoryMessage, messageStyle);
        messageLabel.setFontScale(1.3f);
        messageLabel.setAlignment(Align.center);
        messageLabel.setWrap(true);
        
        // Create button styles
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.YELLOW;
        buttonStyle.overFontColor = Color.LIGHT_GRAY;
        
        // Load button textures (stored as fields for proper disposal)
        buttonNormalTexture = new Texture("ui/Menu/Main Menu/Blank_Not-Pressed.png");
        buttonPressedTexture = new Texture("ui/Menu/Main Menu/Blank_Pressed.png");
        
        buttonStyle.up = new TextureRegionDrawable(buttonNormalTexture);
        buttonStyle.down = new TextureRegionDrawable(buttonPressedTexture);
        buttonStyle.over = new TextureRegionDrawable(buttonNormalTexture);
        
        // Create buttons
        TextButton playAgainButton = new TextButton("Play Again", buttonStyle);
        playAgainButton.getLabel().setFontScale(1.8f);
        playAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (callback != null) {
                    callback.onPlayAgain();
                }
            }
        });
        
        TextButton exitButton = new TextButton("Exit to Main Menu", buttonStyle);
        exitButton.getLabel().setFontScale(1.8f);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (callback != null) {
                    callback.onExitToMainMenu();
                }
            }
        });
        
        // Layout the table
        overlayTable.add(titleLabel).padBottom(30).row();
        overlayTable.add(messageLabel).width(800).padBottom(50).row();
        overlayTable.add(playAgainButton).width(300).height(80).padBottom(20).row();
        overlayTable.add(exitButton).width(300).height(80).row();
        
        stage.addActor(overlayTable);
    }
    
    public void show() {
        isVisible = true;
        backgroundDim.setVisible(true);
        overlayTable.setVisible(true);
        Gdx.app.log("VictoryOverlay", "Victory screen shown");
    }
    
    public void hide() {
        isVisible = false;
        backgroundDim.setVisible(false);
        overlayTable.setVisible(false);
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Disposes of all textures and resources used by this overlay.
     * This method is idempotent and safe to call multiple times.
     * Cleans up:
     * - Background texture
     * - Button textures (normal and pressed states)
     */
    public void dispose() {
        // Dispose background texture
        if (backgroundTexture != null) {
            try {
                backgroundTexture.dispose();
                backgroundTexture = null;
            } catch (Exception e) {
                Gdx.app.error("VictoryOverlay", "Error disposing background texture: " + e.getMessage());
            }
        }
        
        // Dispose button textures
        if (buttonNormalTexture != null) {
            try {
                buttonNormalTexture.dispose();
                buttonNormalTexture = null;
            } catch (Exception e) {
                Gdx.app.error("VictoryOverlay", "Error disposing button normal texture: " + e.getMessage());
            }
        }
        
        if (buttonPressedTexture != null) {
            try {
                buttonPressedTexture.dispose();
                buttonPressedTexture = null;
            } catch (Exception e) {
                Gdx.app.error("VictoryOverlay", "Error disposing button pressed texture: " + e.getMessage());
            }
        }
    }
}
