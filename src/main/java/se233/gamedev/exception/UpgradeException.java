package se233.gamedev.exception;
/** แจ้งข้อผิดพลาดระหว่างการเลือก ใช้ หรือสุ่มอัปเกรด */
public final class UpgradeException extends GameException {
    /** สร้างข้อยกเว้นพร้อมข้อความเกี่ยวกับการอัปเกรดที่ผิดพลาด */
    public UpgradeException(String message){super(message);}
}
