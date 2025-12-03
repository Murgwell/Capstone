package capstone.main.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;

public class SoundManager {
    private static SoundManager instance;
    private HashMap<String, Sound> sounds;
    private boolean isSoundEnabled = true;
    private float volume = 0.5f;

    private SoundManager() {
        sounds = new HashMap<>();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void loadSound(String key, String path) {
        if (!sounds.containsKey(key)) {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
            sounds.put(key, sound);
        }
    }

    public void playSound(String key) {
        if (isSoundEnabled && sounds.containsKey(key)) {
            sounds.get(key).play(volume);
        }
    }

    public void playSound(String key, float pitch) {
        if (isSoundEnabled && sounds.containsKey(key)) {
            sounds.get(key).play(volume, pitch, 0f);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.isSoundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return isSoundEnabled;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    public float getVolume() {
        return volume;
    }

    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        sounds.clear();
    }
}
