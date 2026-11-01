package se233.gamedev.util;

/** Shares laser emitter coordinates between drawing and collision detection. */
public final class BeamGeometry {
    private static final double LEFT_FRACTION = 0.08;
    private static final double RIGHT_FRACTION = 0.92;

    private BeamGeometry() { }

    public static double[] emitterXPositions(double paddleX, double paddleWidth) {
        return new double[] {
                paddleX + paddleWidth * LEFT_FRACTION,
                paddleX + paddleWidth * RIGHT_FRACTION
        };
    }
}
