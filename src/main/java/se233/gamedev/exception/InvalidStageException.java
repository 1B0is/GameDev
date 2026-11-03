package se233.gamedev.exception;

/** ระบุหมายเลขด่านหรือการกำหนดค่าด่านที่ไม่ถูกต้อง */
public final class InvalidStageException extends GameException {
    /** สร้างข้อผิดพลาดสำหรับข้อมูลด่านที่ไม่ถูกต้อง */
    public InvalidStageException(String message) {
        super(message);
    }
}