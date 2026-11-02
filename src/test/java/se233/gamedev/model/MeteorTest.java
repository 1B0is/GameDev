package se233.gamedev.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** ทดสอบการตก การเกิดตามเวลา และการชนของอุกกาบาต */
class MeteorTest {
    /** ตรวจความเร็วตกและการชนกับไม้พาย */
    @Test
    void fallsAtConfiguredSpeedAndDetectsPaddleCollision() {
        Meteor meteor = new Meteor(100, 100);
        double startY = meteor.getY();
        meteor.update(0.5);
        assertEquals(startY + Meteor.FALL_SPEED * 0.5, meteor.getY(), 1e-9);
        Paddle paddle = new Paddle(80, meteor.getY(), 60, 20, 800, 8);
        assertTrue(meteor.intersects(paddle));
    }

    /** ตรวจตำแหน่งเกิดแบบสุ่มตามช่วงเวลาและการหลุดขอบสนาม */
    @Test
    void spawnsAtRandomFieldPositionAfterOneSecondAndDisappearsBelowField() {
        MeteorSpawner spawner = new MeteorSpawner(new Random(7));
        assertNull(spawner.update(0.5, GameBounds.FIELD_WIDTH));
        Meteor meteor = spawner.update(0.5, GameBounds.FIELD_WIDTH);
        assertNotNull(meteor);
        assertTrue(meteor.getX() >= 0);
        assertTrue(meteor.getX() + meteor.getWidth() <= GameBounds.FIELD_WIDTH);
        Meteor nextMeteor = spawner.update(1, GameBounds.FIELD_WIDTH);
        assertNotNull(nextMeteor);
        assertNotEquals(meteor.getX(), nextMeteor.getX());
        meteor.update(3);
        assertTrue(meteor.isBelowField(GameBounds.FIELD_HEIGHT));
    }
}
