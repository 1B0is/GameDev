package se233.gamedev;

/** จุดเริ่มต้นสำหรับเรียกแอปพลิเคชัน JavaFX จากไฟล์ JAR */
public final class Launcher {
    /** ป้องกันการสร้างออบเจ็กต์ตัวเปิดโปรแกรมที่ไม่มีสถานะ */
    private Launcher() { }

    /** ส่งต่อการเริ่มโปรแกรมไปยัง JavaFX Application */
    public static void main(String[] args) {
        ArkanoidApplication.main(args);
    }
}
