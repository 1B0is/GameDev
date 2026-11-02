package se233.gamedev.model;

import java.util.Objects;
import java.util.prefs.Preferences;

/** Pure, persisted audio preference values with ten-step volume control. */
public final class AudioSettings {
    public static final int MIN_VOLUME = 0;
    public static final int MAX_VOLUME = 10;

    private static final String BGM_VOLUME = "bgmVolume";
    private static final String SFX_VOLUME = "sfxVolume";
    private static final String BGM_MUTED = "bgmMuted";
    private static final String SFX_MUTED = "sfxMuted";
    private final Preferences preferences;
    private int bgmVolume;
    private int sfxVolume;
    private boolean bgmMuted;
    private boolean sfxMuted;

    /** Creates audio settings backed by the application's user preferences. */
    public AudioSettings() {
        this(Preferences.userNodeForPackage(AudioSettings.class));
    }

    /** Creates settings from a preference node, allowing isolated persistence tests. */
    public AudioSettings(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
        bgmVolume = clamp(preferences.getInt(BGM_VOLUME, 7));
        sfxVolume = clamp(preferences.getInt(SFX_VOLUME, 7));
        bgmMuted = preferences.getBoolean(BGM_MUTED, false);
        sfxMuted = preferences.getBoolean(SFX_MUTED, false);
    }

    public int getBgmVolume() { return bgmVolume; }
    public int getSfxVolume() { return sfxVolume; }
    public boolean isBgmMuted() { return bgmMuted; }
    public boolean isSfxMuted() { return sfxMuted; }
    /** Volume after applying mute, normalized to JavaFX's 0..1 scale. */
    public double getEffectiveBgmVolume() { return bgmMuted ? 0 : bgmVolume / 10.0; }
    public double getEffectiveSfxVolume() { return sfxMuted ? 0 : sfxVolume / 10.0; }
    public void setBgmVolume(int volume) { bgmVolume = clamp(volume); persist(BGM_VOLUME, bgmVolume); }
    public void setSfxVolume(int volume) { sfxVolume = clamp(volume); persist(SFX_VOLUME, sfxVolume); }
    /** Toggles and persists music mute state. */
    public void toggleBgmMuted() { bgmMuted = !bgmMuted; persist(BGM_MUTED, bgmMuted); }
    /** Toggles and persists effects mute state. */
    public void toggleSfxMuted() { sfxMuted = !sfxMuted; persist(SFX_MUTED, sfxMuted); }

    private void persist(String key, int value) { preferences.putInt(key, value); }
    private void persist(String key, boolean value) { preferences.putBoolean(key, value); }
    private static int clamp(int value) { return Math.max(MIN_VOLUME, Math.min(MAX_VOLUME, value)); }
}
