package se233.gamedev.util;
import se233.gamedev.exception.GameException;
import java.util.logging.Level;
import java.util.logging.Logger;
/** Centralized event logging and the single catch point for all game-domain exceptions. */
public final class GameErrorHandler {
    private static final Logger LOG=Logger.getLogger("se233.gamedev");
    @FunctionalInterface public interface GameAction { void run() throws GameException; }
    private GameErrorHandler(){}
    public static boolean run(GameAction action){
        try{action.run();return true;}
        catch(GameException exception){LOG.log(Level.SEVERE,exception.getMessage(),exception);return false;}
    }
}
