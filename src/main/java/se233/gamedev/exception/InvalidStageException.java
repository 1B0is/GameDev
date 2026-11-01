package se233.gamedev.exception;
/** แจ้งว่าหมายเลขด่านหรือค่าที่ใช้สร้างด่านไม่ถูกต้อง */
public final class InvalidStageException extends GameException {
    /** สร้างข้อยกเว้นพร้อมข้อความเกี่ยวกับด่านที่ไม่ถูกต้อง */
    public InvalidStageException(String message){super(message);}
}
