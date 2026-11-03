package se233.gamedev.util;
import se233.gamedev.exception.GameException;
import java.util.logging.Level;
import java.util.logging.Logger;
/** จุดรวมสำหรับเรียกการทำงานที่อาจเกิดข้อยกเว้นเกมและบันทึกข้อผิดพลาด */
public final class GameErrorHandler {
    private static final Logger LOG=Logger.getLogger("se233.gamedev");
    /** สัญญาการทำงานที่อาจแจ้งข้อยกเว้นเฉพาะของเกม */
    @FunctionalInterface public interface GameAction {
        /** ทำงานหนึ่งรายการที่อาจเกิดข้อยกเว้นตามกฎเกม */
        void run() throws GameException;
    }
    /** ป้องกันการสร้างออบเจ็กต์จากคลาสจัดการข้อผิดพลาด */
    private GameErrorHandler(){}
    /** เรียกการทำงานและคืนผลสำเร็จหรือบันทึกข้อผิดพลาดแล้วคืน false */
    public static boolean run(GameAction action){
        try{action.run();return true;}
        catch(GameException exception){LOG.log(Level.SEVERE,exception.getMessage(),exception);return false;}
    }
}
