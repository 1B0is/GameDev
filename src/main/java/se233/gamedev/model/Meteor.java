package se233.gamedev.model;

/** อุกกาบาตที่ตกลงมาเป็นภัยในเหตุการณ์ฝนดาวตก */
public final class Meteor extends GameObject {
    public static final double FALL_SPEED = 300;
    public static final double WIDTH = 36;
    public static final double HEIGHT = 72;

    public Meteor(double centerX, double startY) {
        super(centerX - WIDTH / 2, startY, WIDTH, HEIGHT);
    }

    /** คืนความเร็วแนวตั้งของอุกกาบาต */
    public double getVelocityY() {
        return FALL_SPEED;
    }

    /** เคลื่อนอุกกาบาตลงตามเวลา โดย dt มีหน่วยเป็นวินาที */
    public void update(double dtSeconds) {
        if (dtSeconds < 0) throw new IllegalArgumentException("Delta time cannot be negative");
        setY(getY() + FALL_SPEED * dtSeconds);
    }

    /** ตรวจว่าตัวอุกกาบาตหลุดพ้นขอบล่างของสนามแล้วหรือไม่ */
    public boolean isBelowField(double fieldHeight) {
        return getY() > fieldHeight;
    }
}
