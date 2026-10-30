package se233.gamedev.exception;
/** Base type for game rule failures, handled together at the application boundary. */
public class GameException extends Exception {
    public GameException(String message){super(message);}
    public GameException(String message,Throwable cause){super(message,cause);}
}
