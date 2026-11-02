package se233.gamedev.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpgradeSelectionStateTest {
    private final List<Upgrade> initial = List.of(
            new Upgrade(UpgradeType.EXTRA_LIFE),
            new Upgrade(UpgradeType.WIDE_PADDLE),
            new Upgrade(UpgradeType.POWER_BALL));

    @Test
    void gatesInputDuringIntroWrapsAndConfirmsOnlyOnce() {
        UpgradeSelectionState state = new UpgradeSelectionState(initial);
        assertFalse(state.isInputEnabled());
        state.move(1);
        assertEquals(0, state.getSelectedIndex());
        assertNull(state.confirm());

        state.update(UpgradeSelectionState.INTRO_DURATION_SECONDS);
        state.move(-1);
        assertEquals(2, state.getSelectedIndex());
        assertSame(initial.get(2), state.confirm());
        assertNull(state.confirm());
    }

    @Test
    void rerollLocksInputAndReplacesOffersWithDistinctChoices() {
        UpgradeSelectionState state = new UpgradeSelectionState(initial);
        state.update(UpgradeSelectionState.INTRO_DURATION_SECONDS);
        List<Upgrade> replacements = List.of(
                new Upgrade(UpgradeType.QUICK_BALL),
                new Upgrade(UpgradeType.MULTI_BALL),
                new Upgrade(UpgradeType.PIERCING_BALL));
        assertTrue(state.reroll(replacements));
        assertFalse(state.isInputEnabled());
        assertEquals(3, state.getOffers().stream().map(Upgrade::getType).distinct().count());
        state.update(UpgradeSelectionState.REROLL_DURATION_SECONDS);
        assertFalse(state.isRerolling());
        assertEquals(replacements, state.getOffers());
    }

    @Test
    void effectTextUsesUpgradeMultipliers() {
        assertEquals("Paddle width +20%", UpgradeType.WIDE_PADDLE.getDescription());
        assertEquals("Brick score +25%", UpgradeType.POWER_BALL.getDescription());
        assertEquals("Ball speed +10%", UpgradeType.QUICK_BALL.getDescription());
    }
}
