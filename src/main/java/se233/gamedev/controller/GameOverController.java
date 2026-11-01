package se233.gamedev.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import se233.gamedev.model.GameSession;
import se233.gamedev.model.GameState;

/** แสดงผลแพ้หรือชนะ พร้อมคะแนนและปุ่มเริ่มรอบใหม่ */
public final class GameOverController {
    private final GameSession session;
    private final Runnable onRestart;

    /** เก็บเซสชันผลลัพธ์และการทำงานเมื่อผู้เล่นเริ่มเกมใหม่ */
    public GameOverController(GameSession session, Runnable onRestart) {
        this.session = session;
        this.onRestart = onRestart;
    }

    /** สร้างฉากแสดงผลชนะ/แพ้ คะแนนสุดท้าย และปุ่มเล่นอีกครั้ง */
    public Parent createView() {
        boolean won = session.getState() == GameState.VICTORY;
        Label title = new Label(won ? "VICTORY!" : "GAME OVER");
        title.setStyle("-fx-font: bold 42px Monospaced; -fx-text-fill: "
                + (won ? "#61efff;" : "#ff6666;"));
        Label score = new Label("FINAL SCORE  " + session.getScore());
        score.setStyle("-fx-font: 20px Monospaced; -fx-text-fill: white;");
        Button restart = new Button("PLAY AGAIN");
        restart.setOnAction(event -> onRestart.run());
        VBox root = new VBox(24, title, score, restart);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #07122e, #101c3b);");
        return root;
    }
}
