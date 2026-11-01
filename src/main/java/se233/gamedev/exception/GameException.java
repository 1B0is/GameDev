package se233.gamedev.exception;
/** ข้อยกเว้นฐานสำหรับความผิดพลาดตามกฎเกมที่จัดการผ่านจุดรวมเดียว */
public class GameException extends Exception {
    /** สร้างข้อยกเว้นพร้อมข้อความอธิบายสาเหตุ */
    public GameException(String message){super(message);}
    /** สร้างข้อยกเว้นพร้อมข้อความและสาเหตุเดิม */
    public GameException(String message,Throwable cause){super(message,cause);}
}
