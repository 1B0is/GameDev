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

    public BrickType getType() { return type; }
    public int getHitPoints() { return hitPoints; }
    public int getScoreValue() { return type.getScore(); }
    public boolean isDestroyed() { return hitPoints == 0; }

    /** ลดพลังชีวิตหนึ่งหน่วย และคืนค่า true เมื่ออิฐถูกทำลาย */
    public boolean hit() {
        if (hitPoints > 0) hitPoints--;
        return isDestroyed();
    }
}
