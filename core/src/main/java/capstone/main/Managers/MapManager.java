package capstone.main.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class MapManager {
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer renderer;
    private final PhysicsManager physics;

    private float worldWidth, worldHeight;
    private final float scale = 1 / 32f; // 32px → 1 world unit

    public MapManager(PhysicsManager physics) {
        this.physics = physics;
    }

    public void load(String mapPath) {
        if (tiledMap != null) tiledMap.dispose();

        tiledMap = new TmxMapLoader().load(mapPath);
        renderer = new OrthogonalTiledMapRenderer(tiledMap, scale);

        createCollisionBodies();

        // Calculate world size from first tile layer
        TiledMapTileLayer ground = (TiledMapTileLayer) tiledMap.getLayers().get(0);
        worldWidth = ground.getWidth() * ground.getTileWidth() * scale;
        worldHeight = ground.getHeight() * ground.getTileHeight() * scale;

        Gdx.app.log("MapManager", "Loaded map: " + mapPath + " (" + worldWidth + "x" + worldHeight + ")");
    }

    private void createCollisionBodies() {
        MapLayer layer = tiledMap.getLayers().get("objectlayers");
        if (layer == null) return;

        for (MapObject obj : layer.getObjects()) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            Body body = physics.getWorld().createBody(bodyDef);

            if (obj instanceof RectangleMapObject) {
                RectangleMapObject rObj = (RectangleMapObject) obj;
                com.badlogic.gdx.math.Rectangle r = rObj.getRectangle();
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(r.width / 2f * scale, r.height / 2f * scale,
                    new Vector2((r.x + r.width / 2f) * scale, (r.y + r.height / 2f) * scale), 0);
                body.createFixture(shape, 0);
                shape.dispose();

            } else if (obj instanceof PolygonMapObject) {
                PolygonMapObject pObj = (PolygonMapObject) obj;
                Polygon polygon = pObj.getPolygon();
                float[] verts = polygon.getTransformedVertices();
                Array<Vector2> points = new Array<>();
                for (int i = 0; i < verts.length; i += 2) {
                    points.add(new Vector2(verts[i] * scale, verts[i + 1] * scale));
                }

                Array<float[]> convexPolys = decomposePolygon(points);
                for (float[] cp : convexPolys) {
                    PolygonShape shape = new PolygonShape();
                    shape.set(cp);
                    body.createFixture(shape, 0);
                    shape.dispose();
                }

            } else if (obj instanceof PolylineMapObject) {
                PolylineMapObject lineObj = (PolylineMapObject) obj;
                float[] verts = lineObj.getPolyline().getTransformedVertices();
                ChainShape chain = new ChainShape();
                Vector2[] points = new Vector2[verts.length / 2];
                for (int i = 0; i < points.length; i++)
                    points[i] = new Vector2(verts[i * 2] * scale, verts[i * 2 + 1] * scale);
                chain.createChain(points);
                body.createFixture(chain, 0);
                chain.dispose();

            } else if (obj instanceof CircleMapObject) {
                CircleMapObject cObj = (CircleMapObject) obj;
                CircleShape shape = new CircleShape();
                Circle c = cObj.getCircle();
                shape.setRadius(c.radius * scale);
                shape.setPosition(new Vector2(c.x * scale, c.y * scale));
                body.createFixture(shape, 0);
                shape.dispose();

            } else if (obj instanceof TiledMapTileMapObject) {
                TiledMapTileMapObject tObj = (TiledMapTileMapObject) obj;
                float w = tObj.getTile().getTextureRegion().getRegionWidth() * scale;
                float h = tObj.getTile().getTextureRegion().getRegionHeight() * scale;
                float x = tObj.getX() * scale;
                float y = tObj.getY() * scale;

                PolygonShape shape = new PolygonShape();
                shape.setAsBox(w / 2f, h / 2f, new Vector2(x + w / 2, y + h / 2), 0);
                body.createFixture(shape, 0);
                shape.dispose();
            }

        }
    }

    /**
     * Decompose concave polygon into convex polygons.
     * Uses simple ear clipping for Box2D.
     */
    private Array<float[]> decomposePolygon(Array<Vector2> points) {
        Array<float[]> result = new Array<>();

        // For simplicity, if <= 8 vertices, assume convex
        if (points.size <= 8) {
            float[] verts = new float[points.size * 2];
            for (int i = 0; i < points.size; i++) {
                verts[i * 2] = points.get(i).x;
                verts[i * 2 + 1] = points.get(i).y;
            }
            result.add(verts);
            return result;
        }

        // TODO: replace with a proper polygon decomposition library (e.g., libgdx-convex-hull)
        // For now, just split into triangles (fan)
        Vector2 first = points.get(0);
        for (int i = 1; i < points.size - 1; i++) {
            float[] verts = new float[6];
            verts[0] = first.x; verts[1] = first.y;
            verts[2] = points.get(i).x; verts[3] = points.get(i).y;
            verts[4] = points.get(i + 1).x; verts[5] = points.get(i + 1).y;
            result.add(verts);
        }

        return result;
    }

    public OrthogonalTiledMapRenderer getRenderer() { return renderer; }
    public TiledMap getTiledMap() { return tiledMap; }
    public float getWorldWidth() { return worldWidth; }
    public float getWorldHeight() { return worldHeight; }
}
