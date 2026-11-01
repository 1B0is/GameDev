package se233.gamedev;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se233.gamedev.controller.GameController;
import se233.gamedev.controller.GameOverController;
import se233.gamedev.controller.MenuController;
import se233.gamedev.controller.UpgradeController;
import se233.gamedev.model.GameSession;
import se233.gamedev.model.GameState;
import se233.gamedev.util.GameErrorHandler;
import se233.gamedev.util.GameLogger;

/** จุดเริ่มต้น JavaFX และตัวเชื่อมการเปลี่ยนหน้าจอของเกม */
public final class ArkanoidApplication extends Application {
    private Stage window;
    private GameController activeGame;

    /** เริ่มหน้าต่างหลักและแสดงเมนูเริ่มเกม */
    @Override
    public void start(Stage stage) {
        window = stage;
        window.setTitle("SE233 Arkanoid Roguelike");
        showMenu();
        window.show();
    }

    /** สร้างและแสดงหน้าจอเมนูหลัก */
    private void showMenu() {
        window.setScene(new Scene(new MenuController(this::startGame).createView(), 800, 600));
    }

    /** สร้างเซสชันใหม่และเปิดหน้าจอตามสถานะเริ่มต้นของเกม */
    private void startGame() {
        GameSession session = new GameSession();
        if (!GameErrorHandler.run(session::start)) return;
        GameLogger.gameEvent("Basic game session started");
        showState(session);
    }

    /** เลือกหน้าจอและตัวควบคุมให้ตรงกับสถานะปัจจุบันของเซสชัน */
    private void showState(GameSession session) {
        switch (session.getState()) {
            case PLAYING -> {
                activeGame = new GameController(session, this::showState);
                window.setScene(new Scene(activeGame, 800, 600));
            }
            case UPGRADE -> {
                if (activeGame != null) activeGame.stop();
                window.setScene(new Scene(
                        new UpgradeController(session, this::showState).createView(), 800, 600));
            }
            case GAME_OVER, VICTORY -> {
                if (activeGame != null) activeGame.stop();
                window.setScene(new Scene(
                        new GameOverController(session, this::startGame).createView(), 800, 600));
            }
            default -> showMenu();
        }
    }

    /** ส่งอาร์กิวเมนต์เริ่มต้นไปยังระบบ JavaFX */
    public static void main(String[] args) {
        Application.launch(ArkanoidApplication.class, args);
    }
}
