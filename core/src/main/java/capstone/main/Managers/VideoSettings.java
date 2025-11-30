
package capstone.main.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Preferences;

/**
 * Centralized video settings (fullscreen/windowed) + persistence.
 * Place in Managers package to match your architecture.
 */
public final class VideoSettings {
    private static final String PREF_NAME = "user_settings";
    private static final String KEY_FS    = "fullscreen";
    private static final String KEY_W     = "window_width";
    private static final String KEY_H     = "window_height";

    private static boolean fullscreen = false;
    private static int windowWidth = 1280;
    private static int windowHeight = 720;

    private VideoSettings() {}

    /** Load saved settings at app start. Call once in Game.create(). */
    public static void load() {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        fullscreen  = prefs.getBoolean(KEY_FS, false);
        windowWidth = prefs.getInteger(KEY_W, 1280);
        windowHeight= prefs.getInteger(KEY_H, 720);
    }

    /** Save current settings. */
    public static void save() {
        Preferences prefs = Gdx.app.getPreferences(PREF_NAME);
        prefs.putBoolean(KEY_FS, fullscreen);
        prefs.putInteger(KEY_W, windowWidth);
        prefs.putInteger(KEY_H, windowHeight);
        prefs.flush();
    }

    public static boolean isFullscreen() { return fullscreen; }

    /** Update preference; does NOT change the OS window until apply() is called. */
    public static void setFullscreen(boolean value) {
        fullscreen = value;
        save();
    }

    /** Optional: remember last window size. */
    public static void setWindowSize(int w, int h) {
        windowWidth  = Math.max(320, w);
        windowHeight = Math.max(240, h);
        save();
    }

    /** Apply current setting to the OS window. Safe to call anytime. */
    public static void apply() {
        if (fullscreen) {
            DisplayMode dm = Gdx.graphics.getDisplayMode(); // current monitor’s native mode
            Gdx.graphics.setFullscreenMode(dm);
        } else {
            Gdx.graphics.setWindowedMode(windowWidth, windowHeight);
        }
    }
}
