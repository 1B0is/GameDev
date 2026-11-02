package se233.gamedev.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** ทดสอบช่วงคุ้มกันหลังอุกกาบาตชนไม้พาย */
class MeteorHitGuardTest {
    @Test
    void preventsAnotherHitUntilOneGameSecondHasElapsed() {
        MeteorHitGuard guard = new MeteorHitGuard();
        assertTrue(guard.canBeHit());
        guard.start();
        assertFalse(guard.canBeHit());
        guard.update(0.75);
        assertTrue(guard.isInvulnerable());
        guard.update(0.25);
        assertTrue(guard.canBeHit());
    }
}
