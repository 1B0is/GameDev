package se233.gamedev.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarningPopupStateTest {
    /** ทดสอบการเปลี่ยนจากเข้าแสดงเป็นค้าง ปิด และซ่อน */
    @Test
    void entersHoldThenExitsAndBecomesInvisible() {
        WarningPopupState state = new WarningPopupState();
        assertEquals(WarningPopupState.Phase.ENTER, state.getPhase());
        state.update(WarningPopupState.ENTER_DURATION_SECONDS);
        assertEquals(WarningPopupState.Phase.HOLD, state.getPhase());
        assertEquals(1, state.progress());

        state.dismiss();
        assertEquals(WarningPopupState.Phase.EXIT, state.getPhase());
        state.update(WarningPopupState.EXIT_DURATION_SECONDS / 2);
        assertEquals(0.5, state.progress(), 1e-9);
        state.update(WarningPopupState.EXIT_DURATION_SECONDS / 2);
        assertEquals(WarningPopupState.Phase.DONE, state.getPhase());
        assertFalse(state.isVisible());
    }

    /** ทดสอบการกดปิดระหว่างเข้าแสดงและการจำกัดความคืบหน้า */
    @Test
    void dismissDuringEntranceIsAcceptedAndProgressIsClamped() {
        WarningPopupState state = new WarningPopupState();
        state.update(WarningPopupState.ENTER_DURATION_SECONDS * 2);
        assertEquals(WarningPopupState.Phase.HOLD, state.getPhase());
        assertTrue(state.progress() >= 0 && state.progress() <= 1);

        WarningPopupState entering = new WarningPopupState();
        entering.update(0.05);
        entering.dismiss();
        assertEquals(WarningPopupState.Phase.EXIT, entering.getPhase());
        assertTrue(entering.progress() >= 0 && entering.progress() <= 1);
        entering.update(WarningPopupState.EXIT_DURATION_SECONDS);
        assertFalse(entering.isVisible());
    }
}
