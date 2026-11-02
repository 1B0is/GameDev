package se233.gamedev.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** ทดสอบขนาด ความยาก รูปแบบ และการทำซ้ำของผังอิฐ */
class StageTest {
    @Test
    void firstStageHasTwelveColumnsAndThreeRows() {
        Stage stage = new Stage(1, 5, new Random(11));
        assertEquals(12, stage.getColumns());
        assertEquals(3, stage.getRows());
        assertEquals(36, stage.getBrickGoal());
    }

    @Test
    void laterStagesAddRowsAndHardBricks() {
        Stage first = new Stage(1, 5, new Random(21));
        Stage second = new Stage(2, 5, new Random(21));
        Stage third = new Stage(3, 5, new Random(21));
        assertTrue(second.getRows() > first.getRows());
        assertTrue(third.getRows() > second.getRows());
        assertTrue(countStrong(second) > countStrong(first));
        assertTrue(countStrong(third) > countStrong(second));
    }

    @Test
    void seededStageLayoutsAreReproducibleAndEveryBrickIsReachable() {
        Stage a = new Stage(4, 5, new Random(712));
        Stage b = new Stage(4, 5, new Random(712));
        assertEquals(a.getBricks().size(), b.getBricks().size());
        for (int i = 0; i < a.getBricks().size(); i++) {
            Brick left = a.getBricks().get(i);
            Brick right = b.getBricks().get(i);
            assertEquals(left.getX(), right.getX());
            assertEquals(left.getY(), right.getY());
            assertEquals(left.getType(), right.getType());
        }
        assertTrue(a.getBricks().stream().allMatch(brick -> brick.getHitPoints() > 0));
    }

    private static long countStrong(Stage stage) {
        return stage.getBricks().stream().filter(brick -> brick.getType() == BrickType.STRONG).count();
    }
}
