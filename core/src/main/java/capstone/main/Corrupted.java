package capstone.main;

import capstone.main.menus.MainMenuScreen;
//import capstone.main.menus.World1;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Corrupted extends Game {

    public SpriteBatch spriteBatch;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        setScreen(new MainMenuScreen(this));
    }
}
