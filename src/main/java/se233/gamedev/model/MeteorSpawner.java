package se233.gamedev.model;

import java.util.Objects;
import java.util.Random;

/** สร้างอุกกาบาตตามเวลาจำลอง โดยไม่พึ่งนาฬิกาจริงหรือ JavaFX */
public final class MeteorSpawner {
    public static final double SPAWN_INTERVAL_SECONDS = 4.0;
    private final Random random;
    private double elapsedSeconds;

    public MeteorSpawner(Random random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    /** สะสมเวลาและคืนอุกกาบาตใหม่เมื่อครบช่วง โดยเริ่มนับหลังปล่อยลูกบอล */
    public Meteor update(double dtSeconds, double fieldWidth, boolean gameLaunched) {
        if (dtSeconds < 0) throw new IllegalArgumentException("Delta time cannot be negative");
        if (!gameLaunched) return null;
        elapsedSeconds += dtSeconds;
        if (elapsedSeconds + 1e-9 < SPAWN_INTERVAL_SECONDS) return null;
        elapsedSeconds = Math.max(0, elapsedSeconds - SPAWN_INTERVAL_SECONDS);
        double x = Meteor.WIDTH / 2
                + random.nextDouble() * (fieldWidth - Meteor.WIDTH);
        return new Meteor(x, GameBounds.HUD_HEIGHT);
    }

    /** ล้างเวลาสะสมเมื่อสภาพอากาศหรือด่านสิ้นสุด */
    public void reset() {
        elapsedSeconds = 0;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }
}
