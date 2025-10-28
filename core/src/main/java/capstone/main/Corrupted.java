package capstone.main;

import capstone.main.menus.mainMenu;
import com.badlogic.gdx.Game;

public class Corrupted extends Game {


    @Override
    public void create() {
        setScreen(new mainMenu(this));
    }
}
