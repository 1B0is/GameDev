package se233.gamedev.model;

/** จัดการช่วงคุ้มกันหลังถูกอุกกาบาต เพื่อไม่ให้รับความเสียหายซ้ำติดกัน */
public final class MeteorHitGuard {
    public static final double INVULNERABILITY_SECONDS = 1.0;
    private double remainingSeconds;

    /** ลดเวลาคุ้มกันตามเวลาจำลอง */
    public void update(double dtSeconds) {
        if (dtSeconds < 0) throw new IllegalArgumentException("Delta time cannot be negative");
        remainingSeconds = Math.max(0, remainingSeconds - dtSeconds);
    }

    /** เริ่มช่วงคุ้มกันเมื่อมีการรับความเสียหายจากอุกกาบาต */
    public void start() {
        remainingSeconds = INVULNERABILITY_SECONDS;
    }

    /** คืนค่าว่าผู้เล่นสามารถรับความเสียหายจากอุกกาบาตได้หรือไม่ */
    public boolean canBeHit() {
        return remainingSeconds <= 0;
    }

    public boolean isInvulnerable() {
        return !canBeHit();
    }
}
