package se233.gamedev.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** ทดสอบพลังชีวิต การทำลาย และคะแนนของอิฐแต่ละชนิด */
class BrickTest {
    /** ตรวจว่าอิฐปกติแตกหลังถูกชนหนึ่งครั้งและให้คะแนนถูกต้อง */
    @Test
    void normalBrickIsDestroyedOnOneHit() {
        Brick brick = new Brick(0, 0, 50, 20, BrickType.NORMAL);
        assertTrue(brick.hit());
        assertTrue(brick.isDestroyed());
        assertEquals(BrickType.NORMAL.getScore(), brick.getScoreValue());
    }

    /** ตรวจว่าอิฐแข็งต้องชนสองครั้งและอิฐโบนัสให้คะแนนมากกว่า */
    @Test
    void strongBrickTakesTwoHitsAndBonusBrickPaysMore() {
        Brick strong = new Brick(0, 0, 50, 20, BrickType.STRONG);
        assertFalse(strong.hit());
        assertFalse(strong.isDestroyed());
        assertTrue(strong.hit());
        assertEquals(2, BrickType.STRONG.getHitPoints());

        Brick bonus = new Brick(0, 0, 50, 20, BrickType.BONUS);
        assertTrue(bonus.getScoreValue() > BrickType.NORMAL.getScore());
    }
}
