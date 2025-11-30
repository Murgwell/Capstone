
package capstone.main.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Cursor;

/**
 * Creates a LWJGL3 cursor from any image by padding to power-of-two width/height.
 * Places the original image at (0,0) of the POT canvas to preserve the hotspot.
 * Works around LWJGL3's requirement that cursor pixmap dimensions must be POT.
 */
public final class CursorManager {
    private static Cursor cursor;
    private static Pixmap pixmap;

    private CursorManager() {}

    /** Next power-of-two >= n (min 1). */
    private static int nextPot(int n) {
        if (n <= 1) return 1;
        int pot = 1;
        while (pot < n) pot <<= 1;
        return pot;
    }

    /**
     * @param filePath path to cursor image (e.g., "ui/cursor.png")
     * @param hotspotX hotspot X (in original image coords, from left)
     * @param hotspotY hotspot Y (in original image coords, from top)
     */
    public static void apply(String filePath, int hotspotX, int hotspotY) {
        dispose(); // free previous cursor/pixmap

        if (!Gdx.files.internal(filePath).exists()) {
            Gdx.app.error("Cursor", "Missing cursor asset: " + filePath);
            safeSystemArrow();
            return;
        }

        Pixmap src = null;
        try {
            src = new Pixmap(Gdx.files.internal(filePath));
            int srcW = src.getWidth();
            int srcH = src.getHeight();

            // Compute POT canvas
            int potW = nextPot(srcW);
            int potH = nextPot(srcH);

            // Create transparent POT canvas (same format)
            Pixmap pot = new Pixmap(potW, potH, src.getFormat());
            pot.setBlending(Pixmap.Blending.None);

            // Draw the original image at (0,0) with NO scaling (padding only)
            pot.drawPixmap(src, 0, 0);

            // Hotspot remains the same coordinate relative to the original image
            int hx = Math.max(0, Math.min(potW - 1, hotspotX));
            int hy = Math.max(0, Math.min(potH - 1, hotspotY));

            // Create and apply cursor
            pixmap = pot;
            cursor = Gdx.graphics.newCursor(pixmap, hx, hy);
            Gdx.graphics.setCursor(cursor);

            // Cleanup source pixmap
            src.dispose();

            Gdx.app.log("Cursor", "Applied " + filePath + " (src " + srcW + "x" + srcH +
                " → POT " + potW + "x" + potH + "), hotspot=(" + hx + "," + hy + ")");
        } catch (Throwable t) {
            Gdx.app.error("Cursor", "Failed to apply custom cursor", t);
            safeSystemArrow();
            if (src != null) src.dispose();
        }
    }

    /** Reset to system arrow and free resources. */
    public static void resetToSystemDefault() {
        safeSystemArrow();
        dispose();
    }

    /** Dispose any allocated resources. */
    public static void dispose() {
        if (cursor != null) { cursor.dispose(); cursor = null; }
        if (pixmap != null) { pixmap.dispose(); pixmap = null; }
    }

    private static void safeSystemArrow() {
        try { Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow); } catch (Exception ignored) {}
    }
}
