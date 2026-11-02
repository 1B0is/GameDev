package se233.gamedev.controller;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import se233.gamedev.view.PixelFont;
import se233.gamedev.view.ScaledRoot;
import se233.gamedev.view.WeatherTheme;

/** Displays pixel-art controls and weather information. */
public final class HowToPlayController {
    private final Runnable onBack;
    private final Canvas canvas = new Canvas(800, 720);

    public HowToPlayController(Runnable onBack) {
        this.onBack = onBack;
    }

    public Parent createView() {
        Pane root = new Pane(canvas);
        root.setPrefSize(800, 720);
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) onBack.run();
        });
        canvas.setOnMouseClicked(event -> {
            if (event.getX() >= 30 && event.getX() <= 180
                    && event.getY() >= 640 && event.getY() <= 690) onBack.run();
        });
        canvas.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null) Platform.runLater(canvas::requestFocus);
        });
        draw(canvas.getGraphicsContext2D());
        return root;
    }

    private void draw(GraphicsContext gc) {
        gc.setImageSmoothing(false);
        gc.setFill(ScaledRoot.VOID_VIOLET);
        gc.fillRect(0, 0, 800, 720);
        PixelFont.draw(gc, "HOW TO PLAY", 286, 55, 3, ScaledRoot.ION_CYAN);
        drawPanel(gc, 65, 155, 310, 390, "CONTROLS");
        drawPanel(gc, 425, 155, 310, 390, "EVENTS");
        drawKey(gc, 94, 236, "A");
        PixelFont.draw(gc, "MOVE LEFT", 160, 247, 2, ScaledRoot.FROST_WHITE);
        drawKey(gc, 94, 300, "D");
        PixelFont.draw(gc, "MOVE RIGHT", 160, 311, 2, ScaledRoot.FROST_WHITE);
        drawKey(gc, 94, 364, "SPACE");
        PixelFont.draw(gc, "LAUNCH BALL", 160, 375, 2, ScaledRoot.FROST_WHITE);
        PixelFont.draw(gc, "SPACE ALSO DISMISSES WEATHER", 93, 447, 1,
                ScaledRoot.TEXT_MUTED);

        PixelFont.draw(gc, "UPGRADES", 458, 230, 2, ScaledRoot.SIGNAL_AMBER);
        PixelFont.draw(gc, "After each stage, choose one", 458, 266, 1, ScaledRoot.FROST_WHITE);
        PixelFont.draw(gc, "of the offered ship modules.", 458, 282, 1, ScaledRoot.FROST_WHITE);
        PixelFont.draw(gc, "FOG VALLEY", 458, 329, 2, WeatherTheme.FOG.accent());
        PixelFont.draw(gc, "Fog hides bricks. Score x1.25.", 458, 361, 1, ScaledRoot.FROST_WHITE);
        PixelFont.draw(gc, "METEOR SHOWER", 458, 405, 2, WeatherTheme.METEOR.accent());
        PixelFont.draw(gc, "A hit costs 0.5 life. Score x1.25.", 458, 437, 1,
                ScaledRoot.FROST_WHITE);
        gc.setFill(ScaledRoot.PANEL_PLUM);
        gc.fillRect(30, 640, 150, 45);
        gc.setStroke(ScaledRoot.ION_CYAN);
        gc.setLineWidth(2);
        gc.strokeRect(30, 640, 150, 45);
        PixelFont.draw(gc, "BACK  /  ESC", 48, 655, 1, ScaledRoot.FROST_WHITE);
    }

    private void drawPanel(GraphicsContext gc, double x, double y, double width,
                           double height, String title) {
        gc.setFill(ScaledRoot.PANEL_DARK);
        gc.fillRect(x, y, width, height);
        gc.setStroke(ScaledRoot.ION_CYAN);
        gc.setLineWidth(3);
        gc.strokeRect(x, y, width, height);
        PixelFont.draw(gc, title, x + 24, y + 24, 2, ScaledRoot.ION_CYAN);
    }

    private void drawKey(GraphicsContext gc, double x, double y, String label) {
        gc.setFill(ScaledRoot.PANEL_PLUM);
        gc.fillRect(x, y, label.length() > 1 ? 54 : 42, 42);
        gc.setStroke(ScaledRoot.SIGNAL_AMBER);
        gc.setLineWidth(2);
        gc.strokeRect(x, y, label.length() > 1 ? 54 : 42, 42);
        PixelFont.draw(gc, label, x + (label.length() > 1 ? 7 : 14), y + 13, 2,
                ScaledRoot.FROST_WHITE);
    }
}