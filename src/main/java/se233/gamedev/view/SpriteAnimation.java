package se233.gamedev.view;

/** เลือกเฟรมภาพเคลื่อนไหวตามเวลาจริง โดยไม่พึ่ง JavaFX หรือสถานะของโมเดล */
public final class SpriteAnimation {
    public static final long FRAME_DURATION_NANOS = 100_000_000L;
    public static final int FRAME_COUNT = 6;

    /** ป้องกันการสร้างตัวช่วยเลือกเฟรมที่ใช้เมธอดสถิติเท่านั้น */
    private SpriteAnimation() {
    }

    /** คืนหมายเลขเฟรมเริ่มที่ 1 โดยวนซ้ำตามเวลาที่ผ่านไป */
    public static int frameAt(long elapsedNanos) {
        return (int) Math.floorMod(elapsedNanos / FRAME_DURATION_NANOS, FRAME_COUNT) + 1;
    }
}
