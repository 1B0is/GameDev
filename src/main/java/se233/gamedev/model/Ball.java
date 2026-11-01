package se233.gamedev.model;

/** ลูกบอลที่แยกการเคลื่อนที่ออกจากการตรวจและสะท้อนการชน */
public final class Ball extends GameObject {
    private double velocityX;
    private double velocityY;
    private double previousX;
    private double previousY;
    private boolean launched;

    /** สร้างลูกบอลที่ยังไม่ปล่อย โดยใช้เส้นผ่านศูนย์กลางที่กำหนด */
    public Ball(double x, double y, double diameter) {
        super(x, y, diameter, diameter);
    }

    /** คืนค่าความเร็วแนวนอนของลูกบอล */
    public double getVelocityX() { return velocityX; }
    /** คืนค่าความเร็วแนวตั้งของลูกบอล */
    public double getVelocityY() { return velocityY; }
    /** ตรวจว่าลูกบอลถูกปล่อยให้เคลื่อนที่แล้วหรือยัง */
    public boolean isLaunched() { return launched; }

    /** กำหนดความเร็วทั้งสองแกนและทำเครื่องหมายว่าลูกบอลเริ่มเคลื่อนที่ */
    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        launched = true;
    }

    /** ปล่อยลูกบอลด้วยความเร็วเริ่มต้นแนวทแยงตามค่ามาตรฐาน */
    public void launch() {
        launch(3.2, -4.4);
    }

    /** ปล่อยลูกบอลด้วยความเร็วที่ผู้เรียกกำหนด หากยังไม่เคยปล่อย */
    public void launch(double velocityX, double velocityY) {
        if (!launched) {
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            launched = true;
        }
    }

    /** เคลื่อนลูกบอลตามความเร็วและบันทึกตำแหน่งก่อนเคลื่อนที่เพื่อใช้ตรวจการชน */
    public void move(double frameScale) {
        if (!launched) return;
        previousX = getX();
        previousY = getY();
        setX(getX() + velocityX * frameScale);
        setY(getY() + velocityY * frameScale);
    }

    /** สะท้อนลูกบอลเมื่อชนขอบซ้าย ขวา หรือด้านบนของสนาม */
    public void bounceOffWalls(double left, double top, double right, double bottom) {
        if (getX() <= left && velocityX < 0) {
            setX(left);
            velocityX = -velocityX;
        } else if (getX() + getWidth() >= right && velocityX > 0) {
            setX(right - getWidth());
            velocityX = -velocityX;
        }
        if (getY() <= top && velocityY < 0) {
            setY(top);
            velocityY = -velocityY;
        }
    }

    /** คำนวณการสะท้อนและมุมออกจากไม้พายเมื่อบอลกำลังเคลื่อนลง */
    public boolean bounceOffPaddle(Paddle paddle) {
        if (!launched || velocityY <= 0 || !intersects(paddle)) return false;
        setY(paddle.getY() - getHeight());
        double center = getX() + getWidth() / 2;
        double impact = (center - (paddle.getX() + paddle.getWidth() / 2))
                / (paddle.getWidth() / 2);
        velocityX = Math.max(-5.5, Math.min(5.5, impact * 5.5));
        velocityY = -Math.max(3.2, Math.abs(velocityY));
        return true;
    }

    /** สะท้อนลูกบอลตามด้านที่ชนอิฐ และคืนค่าว่ามีการชนเกิดขึ้นหรือไม่ */
    public boolean bounceOffBrick(Brick brick) {
        if (!launched || brick.isDestroyed() || !intersects(brick)) return false;

        double previousRight = previousX + getWidth();
        double previousBottom = previousY + getHeight();
        if (previousBottom <= brick.getY() || previousY >= brick.getY() + brick.getHeight()) {
            velocityY = -velocityY;
        } else if (previousRight <= brick.getX() || previousX >= brick.getX() + brick.getWidth()) {
            velocityX = -velocityX;
        } else {
            velocityY = -velocityY;
        }
        return true;
    }

    /** วางลูกบอลกลับตำแหน่งเริ่มต้นและหยุดความเร็วเพื่อรอการปล่อยใหม่ */
    public void reset(double x, double y) {
        setX(x);
        setY(y);
        velocityX = 0;
        velocityY = 0;
        launched = false;
        previousX = x;
        previousY = y;
    }
}
