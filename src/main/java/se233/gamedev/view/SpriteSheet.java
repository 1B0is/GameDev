package se233.gamedev.view;

/** แปลงพิกัดแถวและคอลัมน์เชิงตรรกะของไม้พายเป็นกรอบภาพต้นฉบับ */
public final class SpriteSheet {
    public static final int VAUS_ROW = 2;
    public static final int VAUS_FRAME_WIDTH = 32;
    public static final int VAUS_FRAME_HEIGHT = 8;
    public static final int VAUS_FRAME_COUNT = 6;


    /**
     * แปลงแถวที่ 2 คอลัมน์ 1-6 เป็นกรอบภาพที่วางเรียงแนวตั้งในไฟล์ต้นฉบับ
     */
    public static SourceRect sourceRect(int row, int column) {
        if (row != VAUS_ROW || column < 1 || column > VAUS_FRAME_COUNT) {
            throw new IllegalArgumentException("Vaus frames are row 2, columns 1-6");
        }
        return new SourceRect(32, (column - 1) * VAUS_FRAME_HEIGHT,
                VAUS_FRAME_WIDTH, VAUS_FRAME_HEIGHT);
    }

    /** เก็บพิกัดและขนาดของกรอบภาพที่ตัดจากสไปรต์ชีต */
    public record SourceRect(int x, int y, int width, int height) {
    }
}
