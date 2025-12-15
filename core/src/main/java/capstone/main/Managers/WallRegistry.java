package capstone.main.Managers;

import com.badlogic.gdx.math.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class WallRegistry {
    private static final List<Rectangle> WALLS = new CopyOnWriteArrayList<>();

    private WallRegistry() {}

    public static void setWalls(List<Rectangle> rects) {
        WALLS.clear();
        if (rects != null) {
            WALLS.addAll(rects);
        }
    }

    public static List<Rectangle> getWalls() {
        return Collections.unmodifiableList(WALLS);
    }
}