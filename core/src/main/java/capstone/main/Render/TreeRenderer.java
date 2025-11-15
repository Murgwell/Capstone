package capstone.main.Render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import capstone.main.Characters.AbstractPlayer;

public class TreeRenderer {

    private final SpriteBatch batch;
    private final TiledMap map;
    private final AbstractPlayer player;

    public TreeRenderer(SpriteBatch batch, TiledMap map, AbstractPlayer player) {
        this.batch = batch;
        this.map = map;
        this.player = player;
    }

    public void render(OrthographicCamera camera) {

        TiledMapTileLayer treesLayer = (TiledMapTileLayer) map.getLayers().get(1);
        if (treesLayer == null) return;

        float tileWidth = treesLayer.getTileWidth();
        float tileHeight = treesLayer.getTileHeight();

        float playerX = player.getSprite().getX() + player.getSprite().getWidth() / 2f;
        float playerY = player.getSprite().getY() + player.getSprite().getHeight() / 2f;

        float fadeRadius = 96f;
        float minAlpha = 0.4f;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (int x = 0; x < treesLayer.getWidth(); x++) {
            for (int y = 0; y < treesLayer.getHeight(); y++) {

                TiledMapTileLayer.Cell cell = treesLayer.getCell(x, y);
                if (cell == null || cell.getTile() == null) continue;

                float tileX = x * tileWidth / 32f;
                float tileY = y * tileHeight / 32f;

                float dist = Vector2.dst(playerX, playerY, tileX, tileY);

                float alpha = 1f;
                if (dist < fadeRadius / 32f) {
                    alpha = MathUtils.lerp(minAlpha, 1f, dist / (fadeRadius / 32f));
                }

                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(cell.getTile().getTextureRegion(), tileX, tileY, tileWidth / 32f, tileHeight / 32f);
            }
        }

        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }
}

