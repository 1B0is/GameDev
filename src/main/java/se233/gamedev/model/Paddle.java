package se233.gamedev.model;

/** ไม้พายที่รับการควบคุมจากผู้เล่นและไม่ออกนอกขอบสนาม */
public final class Paddle extends GameObject {
    private final double fieldWidth;
    private final double speed;

    /** สร้างไม้พายพร้อมขอบเขตสนามและความเร็วในการเคลื่อนที่ */
    public Paddle(double x, double y, double width, double height, double fieldWidth, double speed) {
        super(x, y, width, height);
        this.fieldWidth = fieldWidth;
        this.speed = speed;
        clamp();
    }

    /** เลื่อนไม้พายไปทางซ้ายตามสเกลเวลาและจำกัดไม่ให้ออกนอกสนาม */
    public void moveLeft(double frameScale) {
        setX(Math.max(0, getX() - speed * frameScale));
    }
    /** เลื่อนไม้พายไปทางขวาตามสเกลเวลาและจำกัดให้อยู่ในสนาม */
    public void moveRight(double frameScale) {
        setX(Math.min(fieldWidth - getWidth(), getX() + speed * frameScale));
    }

    /** ปรับตำแหน่งไม้พายให้อยู่ภายในขอบเขตหลังสร้างวัตถุ */
    private void clamp() {
        setX(Math.max(0, Math.min(getX(), fieldWidth - getWidth())));
    }
}
