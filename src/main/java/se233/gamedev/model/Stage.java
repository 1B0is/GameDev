package se233.gamedev.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** เก็บรูปแบบอิฐ เป้าหมายการทำลาย และสถานะความคืบหน้าของด่านหนึ่ง */
public final class Stage {
    public static final int COLUMNS = 12;
    public static final int BASE_ROWS = 3;
    public static final int ROWS_PER_STAGE = 1;
    public static final double BRICK_WIDTH = 54;
    public static final double BRICK_HEIGHT = 24;
    public static final double COLUMN_SPACING = 62;
    public static final double ROW_SPACING = 34;
    private static final double BONUS_CHANCE = 0.08;

    private final int number;
    private final int totalStages;
    private final int brickGoal;
    private final List<Brick> bricks;
    private int bricksDestroyed;

    /** สร้างด่านโดยใช้ตัวสุ่มที่กำหนด เพื่อให้รูปแบบและชนิดอิฐทำซ้ำได้ในการทดสอบ */
    public Stage(int number, int totalStages, Random random) {
        if (totalStages < 1 || number < 1 || number > totalStages) {
            throw new IllegalArgumentException("Stage number must be within 1..totalStages");
        }
        this.number = number;
        this.totalStages = totalStages;
        this.bricks = buildBricks(java.util.Objects.requireNonNull(random, "random"));
        this.brickGoal = bricks.size();
    }

    /** สร้างผังอิฐจากรูปแบบที่สุ่มได้ โดยทุกอิฐอยู่ในแถวที่เข้าถึงได้จากด้านบน */
    private List<Brick> buildBricks(Random random) {
        int rows = BASE_ROWS + (number - 1) * ROWS_PER_STAGE;
        Pattern pattern = number == 1
                ? Pattern.SOLID
                : Pattern.values()[random.nextInt(Pattern.values().length)];
        List<Brick> layout = new ArrayList<>();
        double startX = (GameBounds.FIELD_WIDTH
                - (COLUMNS * BRICK_WIDTH + (COLUMNS - 1) * (COLUMN_SPACING - BRICK_WIDTH))) / 2;
        for (int row = 0; row < rows; row++) {
            boolean rowHasBrick = false;
            for (int column = 0; column < COLUMNS; column++) {
                if (!pattern.contains(row, column, rows, random, number)) continue;
                rowHasBrick = true;
                BrickType type = chooseBrickType(random, row, column);
                layout.add(new Brick(startX + column * COLUMN_SPACING,
                        GameBounds.BRICK_START_Y + row * ROW_SPACING,
                        BRICK_WIDTH, BRICK_HEIGHT, type));
            }
            if (!rowHasBrick) {
                int column = random.nextInt(COLUMNS);
                layout.add(new Brick(startX + column * COLUMN_SPACING,
                        GameBounds.BRICK_START_Y + row * ROW_SPACING,
                        BRICK_WIDTH, BRICK_HEIGHT, BrickType.NORMAL));
            }
        }
        addStageStrongBricks(layout, random);
        return layout;
    }

    /** สุ่มอิฐโบนัสและกำหนดจำนวนอิฐแข็งให้เพิ่มตามหมายเลขด่าน */
    private BrickType chooseBrickType(Random random, int row, int column) {
        if ((row + column) % 7 == 0 && random.nextDouble() < BONUS_CHANCE) {
            return BrickType.BONUS;
        }
        return BrickType.NORMAL;
    }

    /** แปลงอิฐปกติจำนวนด่านลบหนึ่งให้เป็นอิฐแข็ง โดยสลับตำแหน่งแบบสุ่ม */
    private void addStageStrongBricks(List<Brick> layout, Random random) {
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < layout.size(); i++) {
            if (layout.get(i).getType() == BrickType.NORMAL) candidates.add(i);
        }
        Collections.shuffle(candidates, random);
        int strongCount = Math.min(number - 1, candidates.size());
        for (int i = 0; i < strongCount; i++) {
            int index = candidates.get(i);
            Brick brick = layout.get(index);
            layout.set(index, new Brick(brick.getX(), brick.getY(), brick.getWidth(),
                    brick.getHeight(), BrickType.STRONG));
        }
    }

    /** รูปแบบผังที่เว้นช่องได้ แต่ไม่สร้างอิฐที่ถูกปิดล้อม */
    private enum Pattern {
        SOLID {
            /** กำหนดให้ทุกตำแหน่งในผังมีอิฐ */
            @Override boolean contains(int row, int column, int rows, Random random, int stage) {
                return true;
            }
        },
        CHECKERBOARD {
            /** สลับตำแหน่งอิฐแบบกระดานหมากรุกโดยคงแถวบนสุดไว้ */
            @Override boolean contains(int row, int column, int rows, Random random, int stage) {
                return row == 0 || (row + column) % 2 == 0;
            }
        },
        PYRAMID {
            /** วางอิฐเป็นรูปพีระมิดโดยขยายตามลำดับแถว */
            @Override boolean contains(int row, int column, int rows, Random random, int stage) {
                return Math.abs(column - (COLUMNS - 1) / 2.0) <= row + 1;
            }
        },
        GAPPED_COLUMNS {
            /** เว้นบางคอลัมน์สลับแถวเพื่อสร้างช่องในผังอิฐ */
            @Override boolean contains(int row, int column, int rows, Random random, int stage) {
                return column % Math.max(2, 5 - Math.min(stage, 3)) != 0 || row % 2 == 0;
            }
        },
        RANDOM_SCATTER {
            /** สุ่มตำแหน่งอิฐโดยเพิ่มความหนาแน่นตามด่าน */
            @Override boolean contains(int row, int column, int rows, Random random, int stage) {
                return random.nextDouble() < Math.min(0.92, 0.62 + stage * 0.05);
            }
        };

        /** ตรวจว่ารูปแบบปัจจุบันกำหนดให้สร้างอิฐในตำแหน่งนี้หรือไม่ */
        abstract boolean contains(int row, int column, int rows, Random random, int stage);
    }

    /** คืนจำนวนด่านทั้งหมดของเกม */
    public int getTotalStages(){return totalStages;}
    /** คืนจำนวนแถวอิฐของด่านนี้ */
    public int getRows(){return BASE_ROWS + (number - 1) * ROWS_PER_STAGE;}
    /** คืนจำนวนคอลัมน์อิฐของด่าน */
    public int getColumns(){return COLUMNS;}
    /** คืนจำนวนอิฐที่ต้องทำลายเพื่อผ่านด่าน */
    public int getBrickGoal(){return brickGoal;}
    /** คืนรายการอิฐแบบอ่านอย่างเดียวเพื่อป้องกันการแก้ไขจากภายนอก */
    public List<Brick> getBricks(){return Collections.unmodifiableList(bricks);}
    /** บันทึกการทำลายอิฐหนึ่งก้อนและคืน false หากเป้าหมายครบแล้ว */
    public boolean recordBrickDestroyed(){if(bricksDestroyed>=brickGoal)return false;bricksDestroyed++;return true;}
    /** ตรวจว่าทำลายอิฐครบเป้าหมายของด่านแล้วหรือไม่ */
    public boolean isCleared(){return bricksDestroyed>=brickGoal;}
    /** ตรวจว่าด่านนี้เป็นด่านสุดท้ายของเกมหรือไม่ */
    public boolean isFinalStage(){return number==totalStages;}
}
