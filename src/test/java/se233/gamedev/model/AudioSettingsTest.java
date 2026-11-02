package se233.gamedev.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

class AudioSettingsTest {
    @Test
    void clampsLevelsAndMuteIsIndependentPerChannel() throws Exception {
        Preferences node = Preferences.userRoot().node("se233-test-" + UUID.randomUUID());
        try {
            AudioSettings settings = new AudioSettings(node);
            settings.setBgmVolume(-4);
            settings.setSfxVolume(15);
            assertEquals(0, settings.getBgmVolume());
            assertEquals(10, settings.getSfxVolume());
            settings.setBgmVolume(8);
            settings.toggleBgmMuted();
            assertEquals(0, settings.getEffectiveBgmVolume());
            assertEquals(1, settings.getEffectiveSfxVolume());
            settings.toggleSfxMuted();
            assertEquals(0, settings.getEffectiveSfxVolume());
        } finally {
            node.removeNode();
        }
    }

    @Test
    void persistsVolumesAndMuteSettingsAcrossInstances() throws Exception {
        Preferences node = Preferences.userRoot().node("se233-test-" + UUID.randomUUID());
        try {
            AudioSettings before = new AudioSettings(node);
            before.setBgmVolume(3);
            before.setSfxVolume(9);
            before.toggleSfxMuted();
            AudioSettings after = new AudioSettings(node);
            assertEquals(3, after.getBgmVolume());
            assertEquals(9, after.getSfxVolume());
            assertTrue(after.isSfxMuted());
        } finally {
            node.removeNode();
        }
    }
}
