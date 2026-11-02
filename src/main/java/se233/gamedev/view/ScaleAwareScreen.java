package se233.gamedev.view;

/** Allows a screen containing a Canvas to adapt its backing pixel dimensions to viewport scale. */
public interface ScaleAwareScreen {
    /** Applies the current real-window to logical-screen scale factor. */
    void setViewportScale(double scale);
}
