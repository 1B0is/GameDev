package se233.gamedev.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/** สร้างเมนูหลักและเรียกการเริ่มเกมเมื่อผู้เล่นกดปุ่ม */
public final class MenuController {
    private final Runnable onStart;

    /** เก็บการทำงานที่จะเรียกเมื่อผู้เล่นสั่งเริ่มเกม */
    public MenuController(Runnable onStart) {
        this.onStart = onStart;
    }

    /** สร้างฉากเมนูที่มีชื่อเกม คำอธิบาย และปุ่มเริ่ม */
    public Parent createView() {
        Label title = new Label("ARKANOID");
        title.setStyle("-fx-font: bold 42px Monospaced; -fx-text-fill: #61efff;");
        Label subtitle = new Label("PIXEL BREAKER  /  ROGUELIKE RUN");
        subtitle.setTextFill(Color.LIGHTGRAY);
        Button start = new Button("START GAME");
        start.setOnAction(event -> onStart.run());
        start.setStyle("-fx-font: bold 18px Monospaced; -fx-padding: 12 28;");
        VBox menu = new VBox(20, title, subtitle, start);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(36));
        menu.setStyle("-fx-background-color: linear-gradient(to bottom, #07122e, #101c3b);");
        return menu;
    }
}
