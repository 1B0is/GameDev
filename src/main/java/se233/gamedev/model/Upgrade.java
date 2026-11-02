package se233.gamedev.model;
import java.util.Objects;
/** ตัวเลือกอัปเกรดหนึ่งรายการที่แสดงชื่อและคำอธิบายจากชนิดอัปเกรด */
public final class Upgrade {
    private final UpgradeType type;
    /** สร้างตัวเลือกอัปเกรดและปฏิเสธชนิดที่เป็นค่าว่าง */
    public Upgrade(UpgradeType type){this.type=Objects.requireNonNull(type,"type");}
    public UpgradeType getType(){return type;}
    public String getName(){return type.getDisplayName();}
    public String getDescription(){return type.getDescription();}
    /** เปรียบเทียบตัวเลือกจากชนิดอัปเกรด */
    @Override
    public boolean equals(Object other){return other instanceof Upgrade u&&u.type==type;}
    /** สร้างค่าแฮชที่สอดคล้องกับการเปรียบเทียบชนิดอัปเกรด */
    @Override
    public int hashCode(){return type.hashCode();}
    /** สร้างข้อความสรุปชื่อและคำอธิบายอัปเกรด */
    @Override
    public String toString(){return getName()+" - "+getDescription();}
}
