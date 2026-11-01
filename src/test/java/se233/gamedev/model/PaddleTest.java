package se233.gamedev.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** ทดสอบการเคลื่อนที่ของไม้พายและการจำกัดตำแหน่งในสนาม */
class PaddleTest {
    /** ตรวจการเคลื่อนที่ซ้ายขวาและการหยุดที่ขอบสนาม */
    @Test
    void movesLeftAndRightWithoutLeavingField() {
        Paddle paddle = new Paddle(10, 500, 100, 18, 800, 8);
        paddle.moveLeft(3);
        assertEquals(0, paddle.getX());
        paddle.moveRight(2);
        assertEquals(16, paddle.getX());
        paddle.setX(795);
        paddle.moveRight(3);
        assertEquals(700, paddle.getX());
    }
}
