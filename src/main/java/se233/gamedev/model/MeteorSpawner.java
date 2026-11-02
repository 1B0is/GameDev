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

    /** สะสมเวลาและคืนอุกกาบาตใหม่เมื่อครบหนึ่งวินาที */
    public Meteor update(double dtSeconds, double fieldWidth) {
        if (dtSeconds < 0) throw new IllegalArgumentException("Delta time cannot be negative");
        elapsedSeconds += dtSeconds;
        if (elapsedSeconds < SPAWN_INTERVAL_SECONDS) return null;
        elapsedSeconds -= SPAWN_INTERVAL_SECONDS;
        double x = Meteor.WIDTH / 2
                + random.nextDouble() * (fieldWidth - Meteor.WIDTH);
        return new Meteor(x, GameBounds.HUD_HEIGHT);
    }

    /** ล้างเวลาสะสมเมื่อสภาพอากาศหรือด่านสิ้นสุด */
    public void reset() {
        elapsedSeconds = 0;
    }

    /** คืนเวลาที่สะสมไว้สำหรับตรวจสอบการสร้างอุกกาบาต */
    public double getElapsedSeconds() {
        return elapsedSeconds;
    }
}
