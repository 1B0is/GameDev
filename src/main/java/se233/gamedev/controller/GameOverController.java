package se233.gamedev.controller;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import se233.gamedev.model.GameSession;
import se233.gamedev.model.GameState;
import se233.gamedev.util.AudioManager;
import se233.gamedev.view.PixelFont;
import se233.gamedev.view.PixelFonts;
import se233.gamedev.view.ScaledRoot;

/** Displays the final result and offers a way to start a new run. */
public final class GameOverController {
    private final GameSession session;
    private final Runnable onRestart;
    private final Canvas canvas = new Canvas(800, 720);

    public GameOverController(GameSession session, Runnable onRestart) {
        this.session = session;
        this.onRestart = onRestart;
    }

    public Parent createView() {
        Pane root = new Pane(canvas);
        root.setPrefSize(800, 720);
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                AudioManager.getInstance().playSfx(AudioManager.MENU_SELECT);
                onRestart.run();
            }
        });
        canvas.setOnMouseClicked(event -> {
            if (event.getX() >= 290 && event.getX() <= 510
                    && event.getY() >= 452 && event.getY() <= 510) onRestart.run();
        });
        canvas.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null) Platform.runLater(canvas::requestFocus);
        });
        draw(canvas.getGraphicsContext2D());
        return root;
    }

    private void draw(GraphicsContext gc) {
        boolean won = session.getState() == GameState.VICTORY;
        gc.setImageSmoothing(false);
        gc.setFill(ScaledRoot.VOID_VIOLET);
        gc.fillRect(0, 0, 800, 720);
        for (int i = 0; i < 80; i++) {
            gc.setFill(Color.color(0.55, 0.8, 1, i % 2 == 0 ? 0.26 : 0.12));
            gc.fillRect(i * 97 % 800, i * 53 % 720, 2, 2);
        }
        gc.setFill(ScaledRoot.PANEL_DARK);
        gc.fillRect(152, 180, 496, 360);
        gc.setStroke(won ? ScaledRoot.ION_CYAN : ScaledRoot.HOT_MAGENTA);
        gc.setLineWidth(4);
        gc.strokeRect(152, 180, 496, 360);
        String title = won ? "SHIP SECURED" : "RUN COMPLETE";
        gc.setFont(Font.font(PixelFonts.DISPLAY.getFamily(), 48));
        gc.setFill(won ? ScaledRoot.ION_CYAN : ScaledRoot.HOT_MAGENTA);
        gc.fillText(title, 400 - title.length() * 12, 280);
        PixelFont.draw(gc, "FINAL SCORE", 325, 340, 2, ScaledRoot.FROST_WHITE);
        gc.setFont(Font.font(PixelFonts.BODY.getFamily(), 42));
        gc.setFill(ScaledRoot.SIGNAL_AMBER);
        gc.fillText(Integer.toString(session.getScore()), 400, 405);
        gc.setFill(ScaledRoot.PANEL_PLUM);
        gc.fillRect(294, 454, 212, 54);
        gc.setStroke(ScaledRoot.SIGNAL_AMBER);
        gc.setLineWidth(3);
        gc.strokeRect(294, 454, 212, 54);
        PixelFont.draw(gc, "PLAY AGAIN", 338, 473, 2, ScaledRoot.FROST_WHITE);
        PixelFont.draw(gc, "PRESS ENTER OR SPACE", 306, 572, 1, ScaledRoot.FROST_WHITE);
    }
}