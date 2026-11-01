package se233.gamedev.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import se233.gamedev.model.GameSession;
import se233.gamedev.util.GameErrorHandler;

import java.util.function.Consumer;

/** สร้างหน้าจอเลือกอัปเกรดและส่งการเลือกกลับไปยัง GameSession */
public final class UpgradeController {
    private final GameSession session;
    private final Consumer<GameSession> onContinue;

    /** เก็บสถานะเกมและการทำงานที่จะเรียกหลังเลือกหรือสุ่มอัปเกรดใหม่ */
    public UpgradeController(GameSession session, Consumer<GameSession> onContinue) {
        this.session = session;
        this.onContinue = onContinue;
    }

    /** สร้างปุ่มตัวเลือกอัปเกรด ปุ่มสุ่มใหม่ และแสดงจำนวนครั้งที่เหลือ */
    public Parent createView() {
        Label title = new Label("STAGE CLEAR  /  CHOOSE AN UPGRADE");
        title.setStyle("-fx-font: bold 22px Monospaced; -fx-text-fill: #61efff;");
        VBox choices = new VBox(14);
        choices.setAlignment(Pos.CENTER);

        for (int i = 0; i < session.getOfferedUpgrades().size(); i++) {
            int choice = i;
            var upgrade = session.getOfferedUpgrades().get(i);
            Button button = new Button(upgrade.getName() + "  —  " + upgrade.getDescription());
            button.setMaxWidth(620);
            button.setStyle("-fx-font: 15px Monospaced; -fx-padding: 12 16;");
            button.setOnAction(event -> {
                if (GameErrorHandler.run(() -> session.chooseUpgrade(choice))) {
                    onContinue.accept(session);
                }
            });
            choices.getChildren().add(button);
        }

        Label rerolls = new Label("REROLLS REMAINING: " + session.getRerollsRemaining()
                + "/" + GameSession.MAX_UPGRADE_REROLLS);
        rerolls.setStyle("-fx-font: 14px Monospaced; -fx-text-fill: #f4f4f4;");
        Button reroll = new Button("REROLL CHOICES");
        reroll.setDisable(session.getRerollsRemaining() == 0);
        reroll.setOnAction(event -> {
            if (GameErrorHandler.run(session::rerollUpgrades)) {
                onContinue.accept(session);
            }
        });

        VBox root = new VBox(20, title, choices, rerolls, reroll);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(36));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #07122e, #101c3b);");
        return root;
    }
}
