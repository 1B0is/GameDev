package se233.gamedev.view;

/** จัดการช่วงเวลาแสดงคำเตือนสภาพอากาศโดยไม่ผูกกับ JavaFX */
public final class WarningPopupState {
    public enum Phase {
        ENTER,
        HOLD,
        EXIT,
        DONE
    }

    public static final double ENTER_DURATION_SECONDS = 0.25;
    public static final double EXIT_DURATION_SECONDS = 0.15;

    private Phase phase = Phase.ENTER;
    private double phaseElapsedSeconds;
    private double elapsedSeconds;

    /** เริ่มต้นหน้าต่างในเฟสแอนิเมชันเข้าแสดง */
    public WarningPopupState() {
    }

    /** เพิ่มเวลาจำลองและเปลี่ยนเฟสแอนิเมชันตามระยะเวลาที่กำหนด */
    public void update(double dtSeconds) {
        if (!Double.isFinite(dtSeconds) || dtSeconds < 0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        elapsedSeconds += dtSeconds;
        if (phase == Phase.DONE || phase == Phase.HOLD) return;

        phaseElapsedSeconds += dtSeconds;
        if (phase == Phase.ENTER && phaseElapsedSeconds >= ENTER_DURATION_SECONDS) {
            phaseElapsedSeconds -= ENTER_DURATION_SECONDS;
            phase = Phase.HOLD;
        }
        if (phase == Phase.EXIT && phaseElapsedSeconds >= EXIT_DURATION_SECONDS) {
            phaseElapsedSeconds = EXIT_DURATION_SECONDS;
            phase = Phase.DONE;
        }
    }

    /** เริ่มแอนิเมชันปิดหน้าต่างจากช่วงเข้าแสดงหรือช่วงค้าง */
    public void dismiss() {
        if (phase == Phase.ENTER || phase == Phase.HOLD) {
            phase = Phase.EXIT;
            phaseElapsedSeconds = 0;
        }
    }

    /** ตรวจว่าหน้าต่างยังอยู่ในสถานะที่ต้องวาดหรือไม่ */
    public boolean isVisible() {
        return phase != Phase.DONE;
    }

    /** คืนความคืบหน้าของเฟสปัจจุบันในช่วงศูนย์ถึงหนึ่ง */
    public double progress() {
        return switch (phase) {
            case ENTER -> clamp(phaseElapsedSeconds / ENTER_DURATION_SECONDS);
            case HOLD -> 1;
            case EXIT -> 1 - clamp(phaseElapsedSeconds / EXIT_DURATION_SECONDS);
            case DONE -> 0;
        };
    }

    /** คืนเฟสแอนิเมชันปัจจุบัน */
    public Phase getPhase() {
        return phase;
    }

    /** คืนเวลารวมที่ผ่านไปเพื่อใช้คำนวณเอฟเฟกต์ภาพ */
    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    /** จำกัดค่าให้อยู่ในช่วงศูนย์ถึงหนึ่ง */
    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
