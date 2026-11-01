package se233.gamedev.util;
import se233.gamedev.exception.GameException;
import java.util.logging.Level;
import java.util.logging.Logger;
/** จุดรวมบันทึกเหตุการณ์ การกระทำ คะแนน คำเตือน และข้อผิดพลาดของเกม */
public final class GameLogger {
    private static final Logger LOG=Logger.getLogger("se233.gamedev");
    /** ป้องกันการสร้างตัวบันทึกที่เรียกใช้ผ่านเมธอดสถิติเท่านั้น */
    private GameLogger(){}
    /** บันทึกเหตุการณ์สำคัญของเกมในระดับข้อมูล */
    public static void gameEvent(String message){LOG.info(message);}
    /** บันทึกการกระทำของผู้เล่นในระดับรายละเอียด */
    public static void playerAction(String message){LOG.fine(message);}
    /** บันทึกคะแนนที่เปลี่ยนแปลงและคะแนนสะสม */
    public static void scoreEvent(int delta,int total){LOG.fine("Score change +"+delta+"; total="+total);}
    /** บันทึกสถานการณ์เตือน เช่น การเสียชีวิต */
    public static void warning(String message){LOG.warning(message);}
    /** บันทึกข้อยกเว้นเกมพร้อมรายละเอียดและ stack trace */
    public static void handle(GameException exception){LOG.log(Level.SEVERE,exception.getMessage(),exception);}
}
