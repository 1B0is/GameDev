package se233.gamedev.util;

/** คำนวณตำแหน่งหัวปล่อยลำแสงที่ใช้ร่วมกันระหว่างการวาดและตรวจการชน */
public final class BeamGeometry {
    private static final double LEFT_FRACTION = 0.08;
    private static final double RIGHT_FRACTION = 0.92;

    /** คืนตำแหน่งแนวนอนของหัวปล่อยลำแสงทั้งสองข้างของไม้พาย */
    public static double[] emitterXPositions(double paddleX, double paddleWidth) {
        return new double[] {
                paddleX + paddleWidth * LEFT_FRACTION,
                paddleX + paddleWidth * RIGHT_FRACTION
        };
    }
}
