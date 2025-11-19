package capstone.main.Managers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class CameraManager {

    private final OrthographicCamera camera;
    private final ScreenShake screenShake;

    private float mapWidth, mapHeight;

    public CameraManager(OrthographicCamera camera, ScreenShake shake, float mapWidth, float mapHeight) {
        this.camera = camera;
        this.screenShake = shake;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public void update(float delta, float targetX, float targetY, Vector3 mouseWorld) {
        screenShake.update(delta);

        float halfViewWidth = camera.viewportWidth / 2f;
        float halfViewHeight = camera.viewportHeight / 2f;

        float panFactor = 0.2f;
        targetX += (mouseWorld.x - targetX) * panFactor;
        targetY += (mouseWorld.y - targetY) * panFactor;

        float camX = MathUtils.clamp(targetX, halfViewWidth, mapWidth - halfViewWidth);
        float camY = MathUtils.clamp(targetY, halfViewHeight, mapHeight - halfViewHeight);

        camera.position.lerp(new Vector3(camX, camY, 0), 0.1f);

        // Apply shake offset
        camera.position.x += screenShake.getOffsetX();
        camera.position.y += screenShake.getOffsetY();

        camera.update();
    }
}
