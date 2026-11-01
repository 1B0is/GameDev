package se233.gamedev.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/** เก็บรูปแบบอิฐ เป้าหมายการทำลาย และสถานะความคืบหน้าของด่านหนึ่ง */
public final class Stage {
    private final int number;
    private final int totalStages;
    private final int brickGoal;
    private final List<Brick> bricks;
    private int bricksDestroyed;
    /** สร้างด่านตามหมายเลขและจำนวนด่านทั้งหมด พร้อมสร้างตำแหน่งและชนิดอิฐ */
    public Stage(int number,int totalStages){
        if(totalStages<1||number<1||number>totalStages)throw new IllegalArgumentException("Stage number must be within 1..totalStages");
        this.number=number;this.totalStages=totalStages;this.brickGoal=10+(number-1)*2;
        this.bricks=buildBricks();
    }
    /** สร้างผังอิฐโดยเพิ่มจำนวนและระดับความแข็งตามความยากของด่าน */
    private List<Brick> buildBricks(){
        List<Brick> layout=new ArrayList<>();
        int columns=10;
        for(int i=0;i<brickGoal;i++){
            int row=i/columns,column=i%columns;
            BrickType type;
            if(number>1&&(row+column)%4==0)type=BrickType.STRONG;
            else if((row*columns+column)%7==0)type=BrickType.BONUS;
            else type=BrickType.NORMAL;
            layout.add(new Brick(74+column*65,112+row*34,54,24,type));
        }
        return layout;
    }
    /** คืนหมายเลขด่านปัจจุบัน */
    public int getNumber(){return number;}
    /** คืนจำนวนด่านทั้งหมด */
    public int getTotalStages(){return totalStages;}
    /** คืนจำนวนอิฐที่ต้องทำลายในด่านนี้ */
    public int getBrickGoal(){return brickGoal;}
    /** คืนจำนวนอิฐที่ถูกบันทึกว่าทำลายแล้ว */
    public int getBricksDestroyed(){return bricksDestroyed;}
    /** คืนจำนวนอิฐที่ยังเหลือ โดยไม่ให้ผลลัพธ์ติดลบ */
    public int getBricksRemaining(){return Math.max(0,brickGoal-bricksDestroyed);}
    /** คืนรายการอิฐที่ไม่อนุญาตให้แก้ไขโครงสร้างรายการจากภายนอก */
    public List<Brick> getBricks(){return Collections.unmodifiableList(bricks);}
    /** บันทึกการทำลายอิฐหนึ่งก้อนและคืน false หากเป้าหมายครบแล้ว */
    public boolean recordBrickDestroyed(){if(bricksDestroyed>=brickGoal)return false;bricksDestroyed++;return true;}
    /** ตรวจว่าทำลายอิฐครบตามเป้าหมายของด่านแล้วหรือไม่ */
    public boolean isCleared(){return bricksDestroyed>=brickGoal;}
    /** ตรวจว่าด่านนี้เป็นด่านสุดท้ายของรอบหรือไม่ */
    public boolean isFinalStage(){return number==totalStages;}
}
