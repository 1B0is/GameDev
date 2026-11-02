package se233.gamedev.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreenScaleTest {
    @Test
    void fitsWideTallAndEqualWindowSizesCenteredWithoutOverflow() {
        assertFit(ScreenScale.compute(1600, 900, 800, 720), 1600, 900, 1.25, 300, 0);
        assertFit(ScreenScale.compute(900, 1600, 800, 720), 900, 1600,
                1.125, 0, 395);
        assertFit(ScreenScale.compute(800, 720, 800, 720), 800, 720, 1, 0, 0);
    }

    private void assertFit(ScreenScale fit, double width, double height, double scale,
                           double offsetX, double offsetY) {
        assertEquals(scale, fit.scale(), 1e-9);
        assertEquals(offsetX, fit.offsetX(), 1e-9);
        assertEquals(offsetY, fit.offsetY(), 1e-9);
        assertTrue(fit.width() <= width);
        assertTrue(fit.height() <= height);
        assertEquals(width, fit.offsetX() * 2 + fit.width(), 1e-9);
        assertEquals(height, fit.offsetY() * 2 + fit.height(), 1e-9);
    }
}
