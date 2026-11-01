package se233.gamedev.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** ทดสอบการเลือกเฟรมตามเวลาและการวนซ้ำของแอนิเมชันสไปรต์ */
class SpriteAnimationTest {
    /** ตรวจเฟรมเริ่มต้น เฟรมถัดไป และการวนกลับหลังครบหกเฟรม */
    @Test
    void selectsFramesByElapsedTimeAndLoops() {
        long duration = SpriteAnimation.FRAME_DURATION_NANOS;
        assertEquals(1, SpriteAnimation.frameAt(0));
        assertEquals(2, SpriteAnimation.frameAt(duration));
        assertEquals(1, SpriteAnimation.frameAt(6 * duration));
    }

    /** ตรวจว่าเวลาติดลบและค่าขนาดใหญ่มากไม่ทำให้เลือกเฟรมผิดพลาด */
    @Test
    void handlesNegativeAndVeryLargeElapsedTimes() {
        SpriteAnimation.frameAt(-1);
        SpriteAnimation.frameAt(Long.MIN_VALUE);
        SpriteAnimation.frameAt(Long.MAX_VALUE);
    }
}
