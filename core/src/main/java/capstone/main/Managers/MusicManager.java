package capstone.main.Managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MusicManager {
    private static MusicManager instance;
    private Music backgroundMusic;
    private boolean isMusicEnabled = true;
    private float volume = 0.2f;
    private String currentMusicPath = null; // Track what's currently loaded

    private MusicManager() {
        // Private constructor for singleton
    }

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void loadMusic(String path) {
        // If the same music is already loaded, don't reload
        if (currentMusicPath != null && currentMusicPath.equals(path)) {
            return;
        }

        // Dispose old music if exists
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
        }

        // Load new music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(volume);
        currentMusicPath = path;
    }

    public void play() {
        if (backgroundMusic != null && isMusicEnabled && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    public void ensurePlaying() {
        // Load music if not loaded, then play if not playing
        if (backgroundMusic == null) {
            loadMusic("Music/bg_music.mp3");
        }
        play();
    }

    public void stop() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    public void pause() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    public void resume() {
        if (backgroundMusic != null && isMusicEnabled && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    public void setMusicEnabled(boolean enabled) {
        isMusicEnabled = enabled;
        if (enabled) {
            if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
                backgroundMusic.play();
            }
        } else {
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
        }
    }

    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(this.volume);
        }
    }

    public float getVolume() {
        return volume;
    }

    public void dispose() {
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
            backgroundMusic = null;
            currentMusicPath = null;
        }
    }

    // NEW: Method to switch music smoothly
    public void switchMusic(String newPath) {
        stop();
        dispose();
        loadMusic(newPath);
        play();
    }
}
