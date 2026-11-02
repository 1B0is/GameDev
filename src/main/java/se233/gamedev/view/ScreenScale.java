package se233.gamedev.view;

/** Pure logical-screen scaling math shared by all application screens. */
public record ScreenScale(double scale, double offsetX, double offsetY,
                          double width, double height) {
    /** Computes a centered uniform scale that keeps the logical screen inside the window. */
    public static ScreenScale compute(double windowWidth, double windowHeight,
                                      double logicalWidth, double logicalHeight) {
        if (!(windowWidth > 0) || !(windowHeight > 0)
                || !(logicalWidth > 0) || !(logicalHeight > 0)) {
            throw new IllegalArgumentException("Screen dimensions must be positive");
        }
        double scale = Math.min(windowWidth / logicalWidth, windowHeight / logicalHeight);
        double width = logicalWidth * scale;
        double height = logicalHeight * scale;
        return new ScreenScale(scale, (windowWidth - width) / 2,
                (windowHeight - height) / 2, width, height);
    }
}
