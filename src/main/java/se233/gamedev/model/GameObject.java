package se233.gamedev.model;

/** คลาสพื้นฐานของวัตถุที่มีตำแหน่งและขนาดในสนามเกม */
public abstract class GameObject {
    private double x;
    private double y;
    private double width;
    private double height;

    /** สร้างวัตถุเกมพร้อมตรวจสอบมิติผ่านตัวกำหนดความกว้างและความสูง */
    protected GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        setWidth(width);
        setHeight(height);
    }


    /** คืนพิกัดแนวนอนของวัตถุ */
    public double getX() { return x; }

    /** คืนพิกัดแนวตั้งของวัตถุ */
    public double getY() { return y; }

    /** คืนความกว้างของวัตถุ */
    public double getWidth() { return width; }

    /** คืนความสูงของวัตถุ */
    public double getHeight() { return height; }

    /** กำหนดพิกัดแนวนอนของวัตถุ */
    public void setX(double x) { this.x = x; }

    /** กำหนดพิกัดแนวตั้งของวัตถุ */
    public void setY(double y) { this.y = y; }

    /** กำหนดความกว้าง โดยปฏิเสธค่าที่ไม่เป็นบวก */
    public void setWidth(double width) {
        if (width <= 0) throw new IllegalArgumentException("Width must be positive");
        this.width = width;
    }

    /** กำหนดความสูง โดยปฏิเสธค่าที่ไม่เป็นบวก */
    public void setHeight(double height) {
        if (height <= 0) throw new IllegalArgumentException("Height must be positive");
        this.height = height;
    }

    /** ตรวจสอบการทับซ้อนของกรอบวัตถุแบบแกนตรง */
    public boolean intersects(GameObject other) {
        return other != null
                && x < other.x + other.width
                && x + width > other.x
                && y < other.y + other.height
                && y + height > other.y;
    }
}
