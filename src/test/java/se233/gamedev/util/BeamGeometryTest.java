package se233.gamedev.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies collision emitters align with the beam art at 8% and 92% of the paddle. */
class BeamGeometryTest {
    @Test
    void emitsAtTheSamePositionsUsedByTheRenderer() {
        double[] positions = BeamGeometry.emitterXPositions(100, 200);
        assertEquals(116, positions[0], 1e-9);
        assertEquals(284, positions[1], 1e-9);
    }
}
