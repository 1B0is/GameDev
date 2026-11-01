package se233.gamedev.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** ทดสอบการแปลงแถวและคอลัมน์ของไม้พายเป็นกรอบภาพต้นฉบับ */
class SpriteSheetTest {
    /** ตรวจตำแหน่งกรอบภาพของคอลัมน์แรกและคอลัมน์สุดท้าย */
    @Test
    void mapsHorizontalRowTwoToVerticallyStackedSourceFrames() {
        assertEquals(new SpriteSheet.SourceRect(32, 0, 32, 8),
                SpriteSheet.sourceRect(2, 1));
        assertEquals(new SpriteSheet.SourceRect(32, 40, 32, 8),
                SpriteSheet.sourceRect(2, 6));
    }
}
