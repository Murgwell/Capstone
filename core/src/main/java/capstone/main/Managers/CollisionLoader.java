package capstone.main.Managers;

import capstone.main.CollisionBits;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;

public class CollisionLoader {

    public static void buildCollision(World world, TiledMap map, String layerName, float ppm) {

        MapLayer layer = map.getLayers().get(layerName);
        if (layer == null) return;

        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) continue;

            Rectangle rect = ((RectangleMapObject) obj).getRectangle();

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;

            float centerX = (rect.x + rect.width * 0.5f) / ppm;
            float centerY = (rect.y + rect.height * 0.5f) / ppm;

            Body body = world.createBody(bodyDef);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(
                (rect.width * 0.5f) / ppm,
                (rect.height * 0.5f) / ppm,
                new Vector2(centerX, centerY),
                0
            );

            FixtureDef fix = new FixtureDef();
            fix.shape = shape;
            fix.friction = 0.2f;
            fix.filter.categoryBits = CollisionBits.WALL;
            // Let bullets pass through walls: do NOT include BULLET in maskBits
            fix.filter.maskBits = (short)(CollisionBits.PLAYER | CollisionBits.ENEMY);
            body.createFixture(fix).setUserData("solid");
            shape.dispose();
        }
    }

    public static ArrayList<Rectangle> getCollisionRectangles(TiledMap map, String layerName, float scale) {
        ArrayList<Rectangle> rects = new ArrayList<>();
        MapLayer layer = map.getLayers().get(layerName);
        if (layer == null) return rects;

        for (MapObject obj : layer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                rects.add(new Rectangle(r.x * scale, r.y * scale, r.width * scale, r.height * scale));
            }
        }
        return rects;
    }
}
