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

    /** Returns the current music level from zero to ten. */
    public int getBgmVolume() { return bgmVolume; }
    /** Returns the current effects level from zero to ten. */
    public int getSfxVolume() { return sfxVolume; }
    /** Returns whether music is muted. */
    public boolean isBgmMuted() { return bgmMuted; }
    /** Returns whether effects are muted. */
    public boolean isSfxMuted() { return sfxMuted; }
    /** Returns the effective music volume from zero to one. */
    public double getEffectiveBgmVolume() { return bgmMuted ? 0 : bgmVolume / 10.0; }
    /** Returns the effective effects volume from zero to one. */
    public double getEffectiveSfxVolume() { return sfxMuted ? 0 : sfxVolume / 10.0; }
    /** Sets and persists the music level after clamping it to zero through ten. */
    public void setBgmVolume(int volume) { bgmVolume = clamp(volume); persist(BGM_VOLUME, bgmVolume); }
    /** Sets and persists the effects level after clamping it to zero through ten. */
    public void setSfxVolume(int volume) { sfxVolume = clamp(volume); persist(SFX_VOLUME, sfxVolume); }
    /** Toggles and persists music mute state. */
    public void toggleBgmMuted() { bgmMuted = !bgmMuted; persist(BGM_MUTED, bgmMuted); }
    /** Toggles and persists effects mute state. */
    public void toggleSfxMuted() { sfxMuted = !sfxMuted; persist(SFX_MUTED, sfxMuted); }

    private void persist(String key, int value) { preferences.putInt(key, value); }
    private void persist(String key, boolean value) { preferences.putBoolean(key, value); }
    private static int clamp(int value) { return Math.max(MIN_VOLUME, Math.min(MAX_VOLUME, value)); }
}
