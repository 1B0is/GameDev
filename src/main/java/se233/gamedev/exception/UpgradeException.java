package se233.gamedev.exception;

/** Indicates an error while selecting, applying, or rerolling an upgrade. */
public final class UpgradeException extends GameException {
    public UpgradeException(String message) {
        super(message);
    }
}