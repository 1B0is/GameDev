package se233.gamedev.model;

/** ขนาดสนามและตำแหน่งหลักที่ใช้ร่วมกันระหว่างโมเดล ตัวควบคุม และตัววาด */
public final class GameBounds {
    public static final double FIELD_WIDTH = 800;
    public static final double FIELD_HEIGHT = 720;
    public static final double HUD_HEIGHT = 88;
    public static final double PADDLE_Y = 660;
    public static final double BALL_START_Y = 638;
    public static final double BRICK_START_Y = 126;

    private GameBounds() {
    }
}
