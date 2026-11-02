package se233.gamedev.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import se233.gamedev.model.AudioSettings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/** Non-blocking JavaFX music and synthesized sound-effect playback. */
public final class AudioManager {
    public static final String MENU_MOVE = "menu_move";
    public static final String MENU_SELECT = "menu_select";
    public static final String BRICK_HIT = "brick_hit";
    public static final double CROSSFADE_MILLIS = 600;

    private static final AudioManager INSTANCE = new AudioManager(new AudioSettings());
    private final AudioSettings settings;
    private final MediaPlayer[] players = new MediaPlayer[2];
    private final AudioClip[] effects = new AudioClip[3];
    private int activePlayer = -1;
    private String activeTrack;
    private Timeline crossfade;

    private AudioManager(AudioSettings settings) {
        this.settings = settings;
        try {
            effects[0] = createTone(660, 45);
            effects[1] = createTone(880, 90);
            effects[2] = createTone(440, 55);
        } catch (IOException | RuntimeException exception) {
            GameLogger.error("Unable to initialize synthesized sound effects", exception);
        }
        activeTrack = null;
    }

    public static AudioManager getInstance() { return INSTANCE; }
    public AudioSettings getSettings() { return settings; }

    /** Starts or crossfades to the looping menu track. */
    public void playMenuMusic() { crossfadeTo("/se233/gamedev/audio/NEON.m4a"); }
    /** Starts or crossfades to the looping game-session track. */
    public void playGameMusic() { crossfadeTo("/se233/gamedev/audio/RED.mp3"); }

    /** Applies music volume immediately and persists the new ten-step setting. */
    public void setBgmVolume(int volume) {
        settings.setBgmVolume(volume);
        applyVolumes();
    }

    /** Applies effects volume immediately and persists the new ten-step setting. */
    public void setSfxVolume(int volume) {
        settings.setSfxVolume(volume);
    }

    /** Toggles music mute and applies the effective volume. */
    public void muteBgm() {
        settings.toggleBgmMuted();
        applyVolumes();
    }

    /** Toggles sound-effect mute. */
    public void muteSfx() { settings.toggleSfxMuted(); }

    /** Plays a named effect when available and not muted. */
    public void playSfx(String name) {
        if (settings.getEffectiveSfxVolume() == 0) return;
        int index = switch (name) {
            case MENU_MOVE -> 0;
            case MENU_SELECT -> 1;
            case BRICK_HIT -> 2;
            default -> -1;
        };
        if (index >= 0 && effects[index] != null) {
            effects[index].setVolume(settings.getEffectiveSfxVolume());
            effects[index].play();
        }
    }

    /** Stops playback and releases all JavaFX audio resources on application exit. */
    public void dispose() {
        if (crossfade != null) crossfade.stop();
        for (int i = 0; i < players.length; i++) {
            if (players[i] != null) {
                players[i].stop();
                players[i].dispose();
                players[i] = null;
            }
        }
    }

    private void crossfadeTo(String resource) {
        if (resource.equals(activeTrack)) return;
        if (crossfade != null) crossfade.stop();
        int nextIndex = activePlayer == 0 ? 1 : 0;
        MediaPlayer oldPlayer = activePlayer < 0 ? null : players[activePlayer];
        MediaPlayer nextPlayer = players[nextIndex];
        if (nextPlayer != null) {
            nextPlayer.stop();
            nextPlayer.dispose();
            players[nextIndex] = null;
        }
        try {
            Path file = extract(resource);
            MediaPlayer loadedPlayer = new MediaPlayer(new Media(file.toUri().toString()));
            loadedPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            loadedPlayer.setVolume(0);
            loadedPlayer.setOnReady(() -> GameLogger.gameEvent("Audio track ready: " + resource));
            loadedPlayer.setOnError(() -> GameLogger.error("Audio playback failed: "
                    + nextPlayerError(loadedPlayer)));
            players[nextIndex] = loadedPlayer;
            loadedPlayer.play();
            nextPlayer = loadedPlayer;
        } catch (IOException | RuntimeException exception) {
            GameLogger.error("Unable to load audio track " + resource, exception);
            return;
        }
        MediaPlayer incoming = nextPlayer;
        final int steps = 20;
        final int[] step = {0};
        crossfade = new Timeline(new KeyFrame(Duration.millis(CROSSFADE_MILLIS / steps), event -> {
            step[0]++;
            double progress = Math.min(1, step[0] / (double) steps);
            incoming.setVolume(settings.getEffectiveBgmVolume() * progress);
            if (oldPlayer != null) oldPlayer.setVolume(settings.getEffectiveBgmVolume() * (1 - progress));
            if (progress >= 1 && oldPlayer != null) {
                oldPlayer.stop();
                oldPlayer.dispose();
                for (int i = 0; i < players.length; i++) {
                    if (players[i] == oldPlayer) players[i] = null;
                }
            }
        }));
        crossfade.setCycleCount(steps);
        activePlayer = nextIndex;
        activeTrack = resource;
        crossfade.play();
    }

    private void applyVolumes() {
        if (activePlayer >= 0 && players[activePlayer] != null) {
            players[activePlayer].setVolume(settings.getEffectiveBgmVolume());
        }
    }

    private static String nextPlayerError(MediaPlayer player) {
        return player.getError() == null ? "unknown media error" : player.getError().getMessage();
    }

    private static Path extract(String resource) throws IOException {
        try (InputStream stream = AudioManager.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IOException("Missing classpath audio resource " + resource);
            String suffix = resource.substring(resource.lastIndexOf('.'));
            Path file = Files.createTempFile("arkanoid-audio-", suffix);
            Files.copy(stream, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            file.toFile().deleteOnExit();
            return file;
        }
    }

    private AudioClip createTone(int frequency, int durationMillis) throws IOException {
        int sampleRate = 22050;
        int samples = sampleRate * durationMillis / 1000;
        Path wav = Files.createTempFile("arkanoid-sfx-", ".wav");
        try (var output = Files.newOutputStream(wav)) {
            int dataLength = samples * 2;
            output.write(new byte[]{'R','I','F','F'});
            writeLittleEndian(output, 36 + dataLength, 4);
            output.write(new byte[]{'W','A','V','E','f','m','t',' '});
            writeLittleEndian(output, 16, 4);
            writeLittleEndian(output, 1, 2);
            writeLittleEndian(output, 1, 2);
            writeLittleEndian(output, sampleRate, 4);
            writeLittleEndian(output, sampleRate * 2, 4);
            writeLittleEndian(output, 2, 2);
            writeLittleEndian(output, 16, 2);
            output.write(new byte[]{'d','a','t','a'});
            writeLittleEndian(output, dataLength, 4);
            for (int i = 0; i < samples; i++) {
                double envelope = Math.min(1, Math.min(i / 180.0, (samples - i) / 250.0));
                short value = (short) (Math.sin(2 * Math.PI * frequency * i / sampleRate)
                        * 8500 * Math.max(0, envelope));
                writeLittleEndian(output, value, 2);
            }
        }
        wav.toFile().deleteOnExit();
        return new AudioClip(wav.toUri().toString());
    }

    private static void writeLittleEndian(java.io.OutputStream output, int value, int bytes)
            throws IOException {
        for (int i = 0; i < bytes; i++) output.write(value >>> (8 * i) & 0xff);
    }
}
