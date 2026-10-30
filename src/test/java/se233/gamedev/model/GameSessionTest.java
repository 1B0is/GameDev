package se233.gamedev.model;
import org.junit.jupiter.api.Test;
import se233.gamedev.exception.GameException;
import static org.junit.jupiter.api.Assertions.*;
class GameSessionTest {
    @Test void updatesScoreAndAppliesMultiplier() throws GameException {
        GameSession game=new GameSession(3,2,new java.util.Random(4));game.start();
        assertEquals(100,game.addScore(100));assertEquals(100,game.getScore());
        game.clearStage();int idx=game.getOfferedUpgrades().stream().map(Upgrade::getType).toList().indexOf(UpgradeType.POWER_BALL);
        assertTrue(idx>=0);game.chooseUpgrade(idx);
        assertEquals(125,game.addScore(100));assertEquals(225,game.getScore());
    }
    @Test void lifeLossReachesGameOver() throws GameException {
        GameSession game=new GameSession(1,2);game.start();game.loseLife();
        assertEquals(0,game.getLives());assertEquals(GameState.GAME_OVER,game.getState());
        assertThrows(GameException.class,game::clearStage);
    }
    @Test void clearingIntermediateStageWaitsForChoiceThenAdvances() throws GameException {
        GameSession game=new GameSession(3,3);game.start();
        assertEquals(3,game.clearStage().size());assertEquals(GameState.UPGRADE,game.getState());
        game.chooseUpgrade(0);assertEquals(2,game.getStageNumber());assertEquals(GameState.PLAYING,game.getState());
    }
    @Test void finalStageEndsInVictoryWithoutUpgrade() throws GameException {
        GameSession game=new GameSession(3,1);game.start();
        assertTrue(game.clearStage().isEmpty());assertEquals(GameState.VICTORY,game.getState());
    }
}
