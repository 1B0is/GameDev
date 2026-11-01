package se233.gamedev.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** ทดสอบการเคลื่อนที่และการสะท้อนของลูกบอลตามกฎเกม */
class BallTest {
    /** ตรวจการเคลื่อนที่และการสะท้อนเมื่อชนขอบสนาม */
    @Test
    void movesAndBouncesSeparatelyAtWalls() {
        Ball ball = new Ball(1, 41, 12);
        ball.setVelocity(-4, -3);
        ball.move(1);
        assertEquals(-3, ball.getX());
        assertEquals(38, ball.getY());

        ball.bounceOffWalls(0, 40, 800, 600);
        assertEquals(0, ball.getX());
        assertTrue(ball.getVelocityX() > 0);
        assertEquals(40, ball.getY());
        assertTrue(ball.getVelocityY() > 0);
    }

    /** ตรวจการสะท้อนเมื่อลูกบอลชนไม้พายและอิฐ */
    @Test
    void bouncesFromPaddleAndBrick() {
        Ball ball = new Ball(50, 80, 12);
        ball.setVelocity(0, 5);
        ball.move(1);
        Paddle paddle = new Paddle(35, 90, 60, 14, 800, 8);
        assertTrue(ball.bounceOffPaddle(paddle));
        assertTrue(ball.getVelocityY() < 0);

        Brick brick = new Brick(50, 45, 50, 20, BrickType.NORMAL);
        Ball brickBall = new Ball(60, 59, 12);
        brickBall.setVelocity(0, -4);
        brickBall.move(1);
        assertTrue(brickBall.intersects(brick));
        assertTrue(brickBall.bounceOffBrick(brick));
        assertTrue(brickBall.getVelocityY() > 0);
    }

    /** ตรวจว่าความเร็วแนวนอนเป็นศูนย์ทำให้ลูกบอลเคลื่อนขึ้นตรง */
    @Test
    void launchesStraightUpWhenGivenZeroHorizontalVelocity() {
        Ball ball = new Ball(100, 100, 12);
        ball.launch(0, -4.4);
        ball.move(2);
        assertEquals(100, ball.getX());
        assertEquals(91.2, ball.getY(), 0.0001);
    }
}
