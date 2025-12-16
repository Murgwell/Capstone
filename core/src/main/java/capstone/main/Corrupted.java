
package capstone.main;

import capstone.main.menus.MainMenuScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import capstone.main.Managers.VideoSettings;

public class Corrupted extends Game {

    public SpriteBatch spriteBatch;

    @Override
    public void create() {
        // OPTIMIZATION: Larger batch size reduces flush calls (default is 1000)
        spriteBatch = new SpriteBatch(2000);

        // Load and apply saved video settings
        VideoSettings.load();
        VideoSettings.apply();

        // Apply custom cursor globally
        capstone.main.Managers.CursorManager.apply("UI/cursor.png", 0, 0);

        setScreen(new MainMenuScreen(this));
    }


    @Override
    public void dispose() {
        // ... dispose other global resources ...
        capstone.main.Managers.CursorManager.dispose();
    }
}
