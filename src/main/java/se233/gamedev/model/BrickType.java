package se233.gamedev.model;

/** ชนิดอิฐ จำนวนครั้งที่ต้องชน และคะแนนเมื่อทำลายสำเร็จ */
public enum BrickType {
    NORMAL(1, 50),
    STRONG(2, 100),
    BONUS(1, 150);

    private final int hitPoints;
    private final int score;

    /** กำหนดจำนวนครั้งที่ต้องชนก่อนอิฐแตก */
    BrickType(int hitPoints, int score) {
        this.hitPoints = hitPoints;
        this.score = score;
    }

    /** คืนค่าพลังชีวิตเริ่มต้นของอิฐชนิดนี้ */
    public int getHitPoints() { return hitPoints; }
    /** คืนค่าคะแนนที่ได้เมื่อทำลายอิฐชนิดนี้ */
    public int getScore() { return score; }
}
