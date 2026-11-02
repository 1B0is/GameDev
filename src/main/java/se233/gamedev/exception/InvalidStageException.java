package se233.gamedev.exception;

/** Indicates an invalid stage number or stage configuration. */
public final class InvalidStageException extends GameException {
    public InvalidStageException(String message) {
        super(message);
    }
}