package se233.gamedev.exception;

/** Base exception for game-rule errors handled by the central error handler. */
public class GameException extends Exception {
    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}