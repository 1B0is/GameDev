package se233.gamedev.util;
import se233.gamedev.exception.GameException;
import java.util.logging.Level;
import java.util.logging.Logger;
/** Centralized event, score and exception logging with explicit severity levels. */
public final class GameLogger {
    private static final Logger LOG=Logger.getLogger("se233.gamedev");
    private GameLogger(){}
    public static void gameEvent(String message){LOG.info(message);}
    public static void playerAction(String message){LOG.fine(message);}
    public static void scoreEvent(int delta,int total){LOG.fine("Score change +"+delta+"; total="+total);}
    public static void warning(String message){LOG.warning(message);}
    public static void handle(GameException exception){LOG.log(Level.SEVERE,exception.getMessage(),exception);}
}
