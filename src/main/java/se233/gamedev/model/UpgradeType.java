package se233.gamedev.model;

/** ชนิดอัปเกรดที่มีชื่อ คำอธิบาย และค่าตัวคูณสำหรับระบบเกม */
public enum UpgradeType {
    WIDE_PADDLE("Wide Paddle","Paddle width +20%",1.20,true),
    EXTRA_LIFE("Extra Life","Gain one life",1.0,false),
    POWER_BALL("Power Ball","Brick score +25%",1.25,true),
    QUICK_BALL("Quick Ball","Ball speed +10%",1.10,true),
    MULTI_BALL("Multi Ball","Start each stage with an additional ball",1.0,false),
    PIERCING_BALL("Piercing Ball","Ball passes through bricks",1.0,false);
    private final String displayName,description;
    private final double multiplier;
    private final boolean repeatable;
    /** กำหนดชื่อที่แสดง คำอธิบาย และตัวคูณของอัปเกรดแต่ละชนิด */
    UpgradeType(String name,String description,double multiplier,boolean repeatable){
        this.displayName=name;
        this.description=description;
        this.multiplier=multiplier;
        this.repeatable=repeatable;
    }
    /** คืนชื่ออัปเกรดสำหรับแสดงในหน้าจอเกม */
    public String getDisplayName(){return displayName;}
    /** คืนคำอธิบายผลของอัปเกรดตามประเภท */
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
    /** คืนค่าตัวคูณที่ใช้ปรับความสามารถของอัปเกรด */
    public double getMultiplier(){return multiplier;}
    /** ตรวจว่าอัปเกรดชนิดนี้สามารถได้รับซ้ำได้หรือไม่ */
    public boolean isRepeatable(){return repeatable;}
    /** แปลงตัวคูณเป็นเปอร์เซ็นต์โบนัสสำหรับข้อความแสดงผล */
    private static int percentage(double multiplier) {
        return (int) Math.round((multiplier - 1) * 100);
    }
}
