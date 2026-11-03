package se233.gamedev.view;

import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import se233.gamedev.model.GameBounds;

/** จัดกึ่งกลางและปรับขนาดหน้าจอความละเอียดคงที่ให้พอดีกับหน้าต่างเกม */
public final class ScaledRoot extends Pane {
    public static final double LOGICAL_WIDTH = GameBounds.FIELD_WIDTH;
    public static final double LOGICAL_HEIGHT = GameBounds.FIELD_HEIGHT;
    public static final Color VOID_VIOLET = Color.web("#140B2E");
    public static final Color PANEL_PLUM = Color.web("#2A1B5C");
    public static final Color ION_CYAN = Color.web("#35E8FF");
    public static final Color HOT_MAGENTA = Color.web("#FF3D9A");
    public static final Color SIGNAL_AMBER = Color.web("#FFC24A");
    public static final Color FROST_WHITE = Color.web("#EAF2FF");
    public static final Color PANEL_DARK = Color.web("#211746");
    public static final Color PANEL_EDGE = Color.web("#443778");
    public static final Color TEXT_MUTED = Color.web("#b6b2cd");
    public static final Color TEXT_DIM = Color.web("#aaa4c7");
    public static final Color METER_OFF = Color.web("#51466e");
    public static final Color DISABLED_PANEL = Color.web("#252039");
    public static final Color DISABLED_EDGE = Color.web("#625d71");
    public static final Color DISABLED_TEXT = Color.web("#898594");
    public static final Color EXTRA_LIFE_ACCENT = Color.web("#FF647C");
    public static final Color QUICK_BALL_ACCENT = Color.web("#8CF56B");
    public static final Color MULTI_BALL_ACCENT = Color.web("#B879FF");

    private final Pane logicalLayer = new Pane() {
        /** จัดขนาด child ที่เป็น Region ให้เต็มพื้นที่เชิงตรรกะ */
        @Override
        protected void layoutChildren() {
            for (var child : getChildren()) {
                if (child instanceof Region region) {
                    region.resize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
                }
            }
        }
    };
    private final Scale scaleTransform = new Scale(1, 1, 0, 0);
    private Parent activeScreen;

    /** สร้างชั้นหน้าจอขนาดคงที่และปรับสเกลตามขนาดหน้าต่าง */
    public ScaledRoot() {
        setStyle("-fx-background-color: #140B2E;");
        logicalLayer.setPrefSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        logicalLayer.setMinSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        logicalLayer.setMaxSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        logicalLayer.getTransforms().add(scaleTransform);
        getChildren().add(logicalLayer);
        widthProperty().addListener((observable, oldValue, newValue) -> updateScale());
        heightProperty().addListener((observable, oldValue, newValue) -> updateScale());
    }

    /** เปลี่ยนหน้าจอปัจจุบันโดยคง Scene ของแอปพลิเคชันไว้ */
    public void show(Parent screen) {
        activeScreen = screen;
        logicalLayer.getChildren().setAll(screen);
        screen.setLayoutX(0);
        screen.setLayoutY(0);
        if (screen instanceof Region region) {
            region.setPrefSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
            region.setMinSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
            region.setMaxSize(LOGICAL_WIDTH, LOGICAL_HEIGHT);
        }
        updateScale();
    }

    /** คำนวณสเกลและตำแหน่งกึ่งกลางใหม่เมื่อขนาดหน้าต่างเปลี่ยน */
    private void updateScale() {
        ScreenScale fit = ScreenScale.compute(Math.max(1, getWidth()), Math.max(1, getHeight()),
                LOGICAL_WIDTH, LOGICAL_HEIGHT);
        scaleTransform.setX(fit.scale());
        scaleTransform.setY(fit.scale());
        logicalLayer.setLayoutX(fit.offsetX());
        logicalLayer.setLayoutY(fit.offsetY());
        if (activeScreen instanceof ScaleAwareScreen scaleAware) {
            scaleAware.setViewportScale(fit.scale());
        }
    }
}
