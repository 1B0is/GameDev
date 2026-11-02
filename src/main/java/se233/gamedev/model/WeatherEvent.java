package se233.gamedev.model;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/** สถานะเหตุการณ์อากาศของด่าน รวมชนิด ความทำงาน และตัวคูณคะแนน */
public final class WeatherEvent {
    public static final double SCORE_MULTIPLIER = 1.25;
    public static final double FOG_CHANCE_WHEN_WEATHER_OCCURS = 0.5;
    private final WeatherType type;
    private boolean active;

    private WeatherEvent(WeatherType type, boolean active) {
        this.type = type;
        this.active = active;
    }

    /** สุ่มเหตุการณ์ตามโอกาสที่กำหนดและสุ่มชนิดเมื่อเกิดเหตุการณ์ */
    public static Optional<WeatherEvent> roll(Random random, double probability) {
        Objects.requireNonNull(random, "random");
        if (probability < 0 || probability > 1) {
            throw new IllegalArgumentException("Weather probability must be between 0 and 1");
        }

        if (random.nextDouble() >= probability) return Optional.empty();
        WeatherType type = random.nextDouble() < FOG_CHANCE_WHEN_WEATHER_OCCURS
                ? WeatherType.FOG_VALLEY : WeatherType.METEOR_SHOWER;
        return Optional.of(new WeatherEvent(type, true));
    }

    /** สร้างเหตุการณ์ที่กำหนดชนิดโดยตรงสำหรับทดสอบกฎคะแนนและสถานะ */
    public static WeatherEvent active(WeatherType type) {
        return new WeatherEvent(Objects.requireNonNull(type, "type"), true);
    }

    public WeatherType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    /** Returns the weather multiplier only while this event is active. */
    public double getScoreMultiplier() {
        return active ? SCORE_MULTIPLIER : 1.0;
    }

    /** จบเหตุการณ์ปัจจุบันโดยคงชนิดไว้สำหรับบันทึกสถานะ/ข้อความ */
    public void end() {
        active = false;
    }
}
