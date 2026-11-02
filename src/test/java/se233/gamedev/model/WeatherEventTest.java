package se233.gamedev.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** ทดสอบการสุ่มเหตุการณ์อากาศ ตัวคูณคะแนน และการปิดสถานะ */
class WeatherEventTest {
    /** ตรวจว่าเกมใช้โอกาสสุ่มสภาพอากาศแปดสิบเปอร์เซ็นต์ต่อด่าน */
    @Test
    void gameSessionUsesEightyPercentPerStageWeatherChance() {
        assertEquals(0.80, GameSession.WEATHER_CHANCE_PER_STAGE, 1e-9);
    }

    /** ตรวจว่า Fog และ Meteor มีโอกาสเท่ากันเมื่อสุ่มได้สภาพอากาศ */
    @Test
    void distributesWeatherTypesEvenlyWhenAnEventOccurs() {
        Random random = new Random(20261102);
        int fogCount = 0;
        int meteorCount = 0;
        int rolls = 10_000;

        for (int i = 0; i < rolls; i++) {
            WeatherType type = WeatherEvent.roll(random, 1)
                    .orElseThrow().getType();
            if (type == WeatherType.FOG_VALLEY) fogCount++;
            else if (type == WeatherType.METEOR_SHOWER) meteorCount++;
        }

        assertEquals(rolls, fogCount + meteorCount);
        assertTrue(Math.abs(fogCount - meteorCount) < 400,
                "Weather types should be approximately evenly distributed");
    }

    /** ตรวจว่าการสุ่มสภาพอากาศให้ผลเดิมเมื่อใช้ seed เดียวกัน */
    @Test
    void seededWeatherRollIsReproducible() {
        var first = WeatherEvent.roll(new Random(94), 0.4);
        var second = WeatherEvent.roll(new Random(94), 0.4);
        assertEquals(first.map(WeatherEvent::getType), second.map(WeatherEvent::getType));
        assertEquals(first.isPresent(), second.isPresent());
    }

    /** ตรวจว่าการจบสภาพอากาศคืนตัวคูณคะแนนเป็นหนึ่ง */
    @Test
    void inactiveWeatherDoesNotRetainScoreMultiplier() {
        WeatherEvent event = WeatherEvent.active(WeatherType.FOG_VALLEY);
        assertEquals(1.25, event.getScoreMultiplier(), 1e-9);
        event.end();
        assertFalse(event.isActive());
        assertEquals(1.0, event.getScoreMultiplier(), 1e-9);
    }
}
