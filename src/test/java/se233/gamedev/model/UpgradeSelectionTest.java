package se233.gamedev.model;
import org.junit.jupiter.api.Test;
import se233.gamedev.exception.GameException;
import se233.gamedev.exception.UpgradeException;
import static org.junit.jupiter.api.Assertions.*;
class UpgradeSelectionTest {
    @Test void offersThreeUniqueUpgradesAndAppliesEffect() throws GameException {
        GameSession game=new GameSession(2,3,new java.util.Random(7));game.start();
        game.clearStage();assertEquals(3,game.getOfferedUpgrades().size());
        assertEquals(3,game.getOfferedUpgrades().stream().map(Upgrade::getType).distinct().count());
        int idx=game.getOfferedUpgrades().stream().map(Upgrade::getType).toList().indexOf(UpgradeType.EXTRA_LIFE);
        assertTrue(idx>=0);game.chooseUpgrade(idx);
        assertEquals(3,game.getLives());assertTrue(game.getAcquiredUpgrades().contains(UpgradeType.EXTRA_LIFE));
    }
    @Test void rejectsChoiceOutsideOfferList() throws GameException {
        GameSession game=new GameSession(2,2);game.start();game.clearStage();
        assertThrows(UpgradeException.class,()->game.chooseUpgrade(9));
        assertEquals(GameState.UPGRADE,game.getState());
    }
}
