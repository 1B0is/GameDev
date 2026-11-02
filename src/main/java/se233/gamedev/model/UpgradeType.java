package se233.gamedev.model;

/** ชนิดอัปเกรดที่มีชื่อ คำอธิบาย และค่าตัวคูณสำหรับระบบเกม */
public enum UpgradeType {
    WIDE_PADDLE("Wide Paddle","Paddle width +20%",1.20),
    EXTRA_LIFE("Extra Life","Gain one life",1.0),
    POWER_BALL("Power Ball","Brick score +25%",1.25),
    QUICK_BALL("Quick Ball","Ball speed +10%",1.10),
    MULTI_BALL("Multi Ball","Start each stage with an additional ball",1.0),
    PIERCING_BALL("Piercing Ball","Ball passes through bricks",1.0);
    private final String displayName,description;
    private final double multiplier;
    /** กำหนดชื่อที่แสดง คำอธิบาย และตัวคูณของอัปเกรดแต่ละชนิด */
    UpgradeType(String name,String description,double multiplier){this.displayName=name;this.description=description;this.multiplier=multiplier;}
    /** คืนชื่ออัปเกรดที่แสดงในส่วนติดต่อผู้เล่น */
    public String getDisplayName(){return displayName;}
    /** คืนคำอธิบายผลการทำงานของอัปเกรด */
    public String getDescription(){
        return switch (this) {
            case WIDE_PADDLE -> "Paddle width +" + percentage(multiplier) + "%";
            case EXTRA_LIFE -> "Gain one life";
            case POWER_BALL -> "Brick score +" + percentage(multiplier) + "%";
            case QUICK_BALL -> "Ball speed +" + percentage(multiplier) + "%";
            case MULTI_BALL -> "Start each stage with an additional ball";
            case PIERCING_BALL -> "Ball passes through bricks";
        };
    }
    /** คืนค่าตัวคูณที่ใช้ปรับความสามารถของเกม */
    public double getMultiplier(){return multiplier;}
    /** Derives the displayed bonus percentage from this upgrade's gameplay multiplier. */
    private static int percentage(double multiplier) {
        return (int) Math.round((multiplier - 1) * 100);
    }
}
