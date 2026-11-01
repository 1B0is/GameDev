package se233.gamedev.model;

/** อิฐที่มีจำนวนครั้งการชนและคะแนนตามชนิด */
public final class Brick extends GameObject {
    private final BrickType type;
    private int hitPoints;

    /** สร้างอิฐและกำหนดพลังชีวิตเริ่มต้นจากชนิดของอิฐ */
    public Brick(double x, double y, double width, double height, BrickType type) {
        super(x, y, width, height);
        this.type = java.util.Objects.requireNonNull(type, "type");
        hitPoints = type.getHitPoints();
    }

    /** คืนค่าชนิดของอิฐ */
    public BrickType getType() { return type; }
    /** คืนค่าพลังชีวิตที่เหลือของอิฐ */
    public int getHitPoints() { return hitPoints; }
    /** คืนค่าคะแนนที่ได้รับเมื่อทำลายอิฐชนิดนี้ */
    public int getScoreValue() { return type.getScore(); }
    /** ตรวจว่าอิฐถูกทำลายจนพลังชีวิตหมดแล้วหรือไม่ */
    public boolean isDestroyed() { return hitPoints == 0; }

    /** ลดพลังชีวิตหนึ่งหน่วย และคืนค่า true เมื่ออิฐถูกทำลาย */
    public boolean hit() {
        if (hitPoints > 0) hitPoints--;
        return isDestroyed();
    }
}
