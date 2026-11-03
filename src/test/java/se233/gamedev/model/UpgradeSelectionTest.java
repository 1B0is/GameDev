package se233.gamedev.model;
import org.junit.jupiter.api.Test;
import se233.gamedev.exception.GameException;
import se233.gamedev.exception.UpgradeException;
import static org.junit.jupiter.api.Assertions.*;
/** ทดสอบการสุ่มตัวเลือกอัปเกรด การเลือก และข้อจำกัดการสุ่มซ้ำ */
class UpgradeSelectionTest {
    /** ตรวจว่ามีตัวเลือกสามชนิดไม่ซ้ำและอัปเกรดชีวิตทำงานได้ */
    @Test void offersThreeUniqueUpgradesAndAppliesEffect() throws GameException {
        GameSession game=null;
        for(long seed=0;seed<100&&game==null;seed++){
            GameSession candidate=new GameSession(2,3,new java.util.Random(seed));
            candidate.start();candidate.clearStage();
            if(candidate.getOfferedUpgrades().stream()
                    .anyMatch(upgrade->upgrade.getType()==UpgradeType.EXTRA_LIFE))game=candidate;
        }
        assertNotNull(game);
        assertEquals(3,game.getOfferedUpgrades().size());
        assertEquals(3,game.getOfferedUpgrades().stream().map(Upgrade::getType).distinct().count());
        int idx=game.getOfferedUpgrades().stream().map(Upgrade::getType).toList().indexOf(UpgradeType.EXTRA_LIFE);
        assertTrue(idx>=0);game.chooseUpgrade(idx);
        assertEquals(3,game.getLives());assertTrue(game.getAcquiredUpgrades().contains(UpgradeType.EXTRA_LIFE));
    }
    /** ตรวจว่าดัชนีตัวเลือกที่อยู่นอกช่วงถูกปฏิเสธ */
    @Test void rejectsChoiceOutsideOfferList() throws GameException {
        GameSession game=new GameSession(2,2);game.start();game.clearStage();
        assertThrows(UpgradeException.class,()->game.chooseUpgrade(9));
        assertEquals(GameState.UPGRADE,game.getState());
    }

    /** ตรวจจำนวนครั้งสุ่มซ้ำและการแสดงอัปเกรดซ้ำได้หลังได้รับแล้ว */
    @Test void rerollsAreLimitedAndChoicesExcludeAcquiredUpgrades() throws GameException {
        GameSession game=new GameSession(2,3,new java.util.Random(19));
        game.start();
        game.clearStage();
        var firstOffer=game.getOfferedUpgrades().stream().map(Upgrade::getType).toList();
        var rerolled=game.rerollUpgrades();
        assertEquals(2,game.getRerollsRemaining());
        assertTrue(rerolled.stream().map(Upgrade::getType).noneMatch(firstOffer::contains));

        game.chooseUpgrade(0);
        UpgradeType acquired=game.getAcquiredUpgrades().iterator().next();
        game.clearStage();
        if (!acquired.isRepeatable()) {
            assertFalse(game.getOfferedUpgrades().stream()
                    .anyMatch(upgrade->upgrade.getType()==acquired));
        }
        game.rerollUpgrades();
        game.rerollUpgrades();
        assertEquals(0,game.getRerollsRemaining());
        assertThrows(UpgradeException.class,game::rerollUpgrades);
    }

    /** ตรวจว่าอัปเกรดเพิ่มไม้พาย คะแนน และความเร็วบอลได้รับซ้ำได้ */
    @Test void repeatableUpgradesCanBeAppliedMoreThanOnce() throws GameException {
        assertRepeatableUpgradeStacks(UpgradeType.WIDE_PADDLE);
        assertRepeatableUpgradeStacks(UpgradeType.POWER_BALL);
        assertRepeatableUpgradeStacks(UpgradeType.QUICK_BALL);
    }

    /** ตรวจผลของอัปเกรดมัลติบอลและบอลทะลุอิฐ */
    @Test void appliesMultiBallAndPiercingUpgradeEffects() throws GameException {
        GameSession multiBall=findOffer(UpgradeType.MULTI_BALL);
        multiBall.chooseUpgrade(indexOf(multiBall,UpgradeType.MULTI_BALL));
        assertEquals(1,multiBall.getAdditionalBallCount());

        GameSession piercing=findOffer(UpgradeType.PIERCING_BALL);
        piercing.chooseUpgrade(indexOf(piercing,UpgradeType.PIERCING_BALL));
        assertTrue(piercing.hasUpgrade(UpgradeType.PIERCING_BALL));

    }

    /** ตรวจว่าการเลือกอัปเกรดเพิ่มตัวคูณความกว้างไม้พายตามค่าที่กำหนด */
    @Test void appliesWidePaddleMultiplier() throws GameException {
        GameSession game=findOffer(UpgradeType.WIDE_PADDLE);
        double before=game.getPaddleWidthMultiplier();

        game.chooseUpgrade(indexOf(game,UpgradeType.WIDE_PADDLE));

        assertEquals(before*UpgradeType.WIDE_PADDLE.getMultiplier(),
                game.getPaddleWidthMultiplier(),1e-9);
        assertTrue(game.hasUpgrade(UpgradeType.WIDE_PADDLE));
    }

    /** ตรวจว่าการเลือกอัปเกรดเพิ่มตัวคูณความเร็วลูกบอลตามค่าที่กำหนด */
    @Test void appliesQuickBallMultiplier() throws GameException {
        GameSession game=findOffer(UpgradeType.QUICK_BALL);
        double before=game.getBallSpeedMultiplier();

        game.chooseUpgrade(indexOf(game,UpgradeType.QUICK_BALL));

        assertEquals(before*UpgradeType.QUICK_BALL.getMultiplier(),
                game.getBallSpeedMultiplier(),1e-9);
        assertTrue(game.hasUpgrade(UpgradeType.QUICK_BALL));
    }
    /** ค้นหา seed ที่ทำให้อัปเกรดชนิดที่กำหนดปรากฏในข้อเสนอ */
    private static GameSession findOffer(UpgradeType type) throws GameException {
        for(long seed=0;seed<100;seed++){
            GameSession game=new GameSession(2,3,new java.util.Random(seed));
            game.start();game.clearStage();
            if(game.getOfferedUpgrades().stream().anyMatch(upgrade->upgrade.getType()==type)){
                return game;
            }
        }
        fail("No deterministic seed offered "+type);
        throw new AssertionError();
    }

    /** คืนตำแหน่งของอัปเกรดชนิดที่ระบุในรายการข้อเสนอ */
    private static int indexOf(GameSession game,UpgradeType type){
        return game.getOfferedUpgrades().stream().map(Upgrade::getType).toList().indexOf(type);
    }

    /** ค้นหาเกมทดสอบที่เสนออัปเกรดซ้ำได้ในด่านถัดไป แล้วตรวจการสะสมผล */
    private static void assertRepeatableUpgradeStacks(UpgradeType type) throws GameException {
        for (long seed = 0; seed < 100; seed++) {
            GameSession game = new GameSession(2, 4, new java.util.Random(seed));
            game.start();
            game.clearStage();
            int firstIndex = indexOf(game, type);
            if (firstIndex < 0) continue;

            game.chooseUpgrade(firstIndex);
            double firstMultiplier = effectiveMultiplier(game, type);
            game.clearStage();
            int secondIndex = indexOf(game, type);
            if (secondIndex < 0) continue;

            game.chooseUpgrade(secondIndex);
            double secondMultiplier = effectiveMultiplier(game, type);
            assertEquals(firstMultiplier * type.getMultiplier(), secondMultiplier, 0.001);
            return;
        }
        fail("No deterministic seed offered " + type + " twice");
    }

    /** อ่านตัวคูณจริงของอัปเกรด รวมการวัดคะแนนผ่านคะแนนที่ได้รับ */
    private static double effectiveMultiplier(GameSession game, UpgradeType type)
            throws GameException {
        return switch (type) {
            case WIDE_PADDLE -> game.getPaddleWidthMultiplier();
            case POWER_BALL -> game.addScore(10_000) / 10_000.0
                    / game.getWeatherScoreMultiplier();
            case QUICK_BALL -> game.getBallSpeedMultiplier();
            default -> throw new IllegalArgumentException("Upgrade is not repeatable: " + type);
        };
    }
}
