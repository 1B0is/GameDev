package se233.gamedev.model;

import java.util.Objects;
import java.util.prefs.Preferences;

/** เก็บและบันทึกค่าระดับเสียงและสถานะปิดเสียงแบบสิบระดับ */
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

    /** โหลดการตั้งค่าเสียงจากค่ากำหนดของผู้ใช้แอปพลิเคชัน */
    public AudioSettings() {
        this(Preferences.userNodeForPackage(AudioSettings.class));
    }

    /** โหลดการตั้งค่าจากโหนดที่ระบุเพื่อรองรับการทดสอบการบันทึกแยก */
    public AudioSettings(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
        bgmVolume = clamp(preferences.getInt(BGM_VOLUME, 7));
        sfxVolume = clamp(preferences.getInt(SFX_VOLUME, 7));
        bgmMuted = preferences.getBoolean(BGM_MUTED, false);
        sfxMuted = preferences.getBoolean(SFX_MUTED, false);
    }

    /** คืนระดับเสียงเพลงที่บันทึกไว้ */
    public int getBgmVolume() { return bgmVolume; }
    /** คืนระดับเสียงเอฟเฟกต์ที่บันทึกไว้ */
    public int getSfxVolume() { return sfxVolume; }
    /** ตรวจว่าเพลงถูกปิดเสียงอยู่หรือไม่ */
    public boolean isBgmMuted() { return bgmMuted; }
    /** ตรวจว่าเอฟเฟกต์ถูกปิดเสียงอยู่หรือไม่ */
    public boolean isSfxMuted() { return sfxMuted; }
    /** คืนระดับเสียงเพลงหลังคิดสถานะปิดเสียงในช่วงศูนย์ถึงหนึ่ง */
    public double getEffectiveBgmVolume() { return bgmMuted ? 0 : bgmVolume / 10.0; }
    /** คืนระดับเสียงเอฟเฟกต์หลังคิดสถานะปิดเสียงในช่วงศูนย์ถึงหนึ่ง */
    public double getEffectiveSfxVolume() { return sfxMuted ? 0 : sfxVolume / 10.0; }
    /** กำหนดและบันทึกระดับเสียงเพลงภายในช่วงที่อนุญาต */
    public void setBgmVolume(int volume) { bgmVolume = clamp(volume); persist(BGM_VOLUME, bgmVolume); }
    /** กำหนดและบันทึกระดับเสียงเอฟเฟกต์ภายในช่วงที่อนุญาต */
    public void setSfxVolume(int volume) { sfxVolume = clamp(volume); persist(SFX_VOLUME, sfxVolume); }
    /** สลับและบันทึกสถานะปิดเสียงเพลง */
    public void toggleBgmMuted() { bgmMuted = !bgmMuted; persist(BGM_MUTED, bgmMuted); }
    /** สลับและบันทึกสถานะปิดเสียงเอฟเฟกต์ */
    public void toggleSfxMuted() { sfxMuted = !sfxMuted; persist(SFX_MUTED, sfxMuted); }

    /** บันทึกค่าระดับเสียงจำนวนเต็มลงในค่ากำหนด */
    private void persist(String key, int value) { preferences.putInt(key, value); }
    /** บันทึกสถานะเปิดหรือปิดเสียงลงในค่ากำหนด */
    private void persist(String key, boolean value) { preferences.putBoolean(key, value); }
    /** จำกัดระดับเสียงให้อยู่ในช่วงค่าต่ำสุดและสูงสุด */
    private static int clamp(int value) { return Math.max(MIN_VOLUME, Math.min(MAX_VOLUME, value)); }
}
