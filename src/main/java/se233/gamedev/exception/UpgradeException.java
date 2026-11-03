package se233.gamedev.exception;

/** ระบุข้อผิดพลาดระหว่างเลือก ใช้ หรือสุ่มอัปเกรดใหม่ */
public final class UpgradeException extends GameException {
    /** สร้างข้อผิดพลาดเกี่ยวกับการจัดการอัปเกรด */
    public UpgradeException(String message) {
        super(message);
    }
}