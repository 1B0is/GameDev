package se233.gamedev.model;

import org.junit.jupiter.api.Test;
import se233.gamedev.exception.GameException;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/** ทดสอบคะแนนอิฐ การผ่านด่าน การเลือกอัปเกรด และการชนะในรอบเดียว */
class GameSessionIntegrationTest {
    @Test
    void completesTwoStagesAndKeepsRunStateAcrossUpgradeChoice() throws GameException {
        GameSession game = new GameSession(3, 2, new Random(11));
        game.start();
        clearCurrentStage(game, 10);

        int scoreAfterFirstStage = game.getScore();
        assertTrue(game.getStage().isCleared());
        List<Upgrade> choices = game.clearStage();
        assertEquals(GameState.UPGRADE, game.getState());
        assertEquals(3, choices.size());

        Upgrade selected = choices.get(0);
        game.chooseUpgrade(0);
        assertTrue(game.hasUpgrade(selected.getType()));
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(2, game.getStageNumber());
        assertEquals(3, game.getLives());

        clearCurrentStage(game, 10);
        assertTrue(game.getScore() > scoreAfterFirstStage);
        assertTrue(game.getStage().isCleared());
        assertTrue(game.clearStage().isEmpty());
        assertEquals(GameState.VICTORY, game.getState());
    }

    /** ทำลายอิฐทั้งหมดของด่านและสะสมคะแนนสำหรับการทดสอบ */
    private static void clearCurrentStage(GameSession game, int pointsPerBrick) throws GameException {
        int brickGoal = game.getStage().getBrickGoal();
        for (int i = 0; i < brickGoal; i++) {
            assertTrue(game.getStage().recordBrickDestroyed());
            game.addScore(pointsPerBrick);
        }
    }
}
