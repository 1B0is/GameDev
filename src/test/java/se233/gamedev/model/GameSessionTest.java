package se233.gamedev.model;
import org.junit.jupiter.api.Test;
import se233.gamedev.exception.GameException;
import static org.junit.jupiter.api.Assertions.*;
/** ทดสอบคะแนน ชีวิต และการเปลี่ยนสถานะระหว่างด่านของเซสชันเกม */
class GameSessionTest {
    /** ตรวจการสะสมคะแนนและตัวคูณคะแนนจากอัปเกรด */
    @Test void updatesScoreAndAppliesMultiplier() throws GameException {
        GameSession game=null;
        for(long seed=0;seed<100&&game==null;seed++){
            GameSession candidate=new GameSession(3,2,new java.util.Random(seed));
            candidate.start();candidate.addScore(100);candidate.clearStage();
            if(candidate.getOfferedUpgrades().stream()
                    .anyMatch(upgrade->upgrade.getType()==UpgradeType.POWER_BALL))game=candidate;
        }
        assertNotNull(game);
        assertEquals(100,game.getScore());
        int idx=game.getOfferedUpgrades().stream().map(Upgrade::getType).toList().indexOf(UpgradeType.POWER_BALL);
        assertTrue(idx>=0);game.chooseUpgrade(idx);
        assertEquals(125,game.addScore(100));assertEquals(225,game.getScore());
    }
    /** ตรวจโบนัสคอมโบทุกสามอิฐและการรีเซ็ตหลังเสียลูกบอล */
    @Test void comboAddsScoreBonusAndResetsAfterLosingLife() throws GameException {
        GameSession game=new GameSession(2,2);
        game.start();

        assertEquals(100,game.addScore(100));
        assertEquals(100,game.addScore(100));
        assertEquals(110,game.addScore(100));
        assertEquals(1.1,game.getComboMultiplier(),1e-9);
        assertEquals(3,game.getComboStreak());

        game.loseLife();
        assertEquals(0,game.getComboStreak());
        assertEquals(1.0,game.getComboMultiplier(),1e-9);
        assertEquals(100,game.addScore(100));
    }
    /** ตรวจการลดชีวิตจนแพ้และปฏิเสธคำสั่งที่ไม่ตรงสถานะ */
    @Test void lifeLossReachesGameOver() throws GameException {
        GameSession game=new GameSession(1,2);game.start();game.loseLife();
        assertEquals(0,game.getLives());assertEquals(GameState.GAME_OVER,game.getState());
        assertThrows(GameException.class,game::clearStage);
    }
    /** ตรวจการพักเกมเพื่อเลือกอัปเกรดก่อนเริ่มด่านถัดไป */
    @Test void clearingIntermediateStageWaitsForChoiceThenAdvances() throws GameException {
        GameSession game=new GameSession(3,3);game.start();
        assertEquals(3,game.clearStage().size());assertEquals(GameState.UPGRADE,game.getState());
        game.chooseUpgrade(0);assertEquals(2,game.getStageNumber());assertEquals(GameState.PLAYING,game.getState());
    }
    /** ตรวจว่าการผ่านด่านสุดท้ายเปลี่ยนสถานะเป็นชนะโดยไม่เสนออัปเกรด */
    @Test void finalStageEndsInVictoryWithoutUpgrade() throws GameException {
        GameSession game=new GameSession(3,1);game.start();
        assertTrue(game.clearStage().isEmpty());assertEquals(GameState.VICTORY,game.getState());
    }
}

