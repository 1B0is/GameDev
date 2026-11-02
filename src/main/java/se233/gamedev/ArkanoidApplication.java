package se233.gamedev;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import se233.gamedev.controller.GameController;
import se233.gamedev.controller.GameOverController;
import se233.gamedev.controller.HowToPlayController;
import se233.gamedev.controller.MenuController;
import se233.gamedev.controller.UpgradeController;
import se233.gamedev.model.GameSession;
import se233.gamedev.model.GameState;
import se233.gamedev.util.GameErrorHandler;
import se233.gamedev.util.GameLogger;
import se233.gamedev.util.AudioManager;
import se233.gamedev.view.ScaledRoot;

/** จุดเริ่มต้น JavaFX และตัวเชื่อมการเปลี่ยนหน้าจอของเกม */
public final class ArkanoidApplication extends Application {
    private Stage window;
    private GameController activeGame;
    private ScaledRoot root;
    private Scene scene;
    private MenuController menuController;
    private UpgradeController upgradeController;

    /** เริ่มหน้าต่างหลักและแสดงเมนูเริ่มเกม */
    @Override
    public void start(Stage stage) {
        window = stage;
        window.setTitle("SE233 Arkanoid Roguelike");
        root = new ScaledRoot();
        scene = new Scene(root, ScaledRoot.LOGICAL_WIDTH, ScaledRoot.LOGICAL_HEIGHT);
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F11) {
                window.setFullScreen(!window.isFullScreen());
                event.consume();
            }
        });
        window.setFullScreenExitHint("");
        window.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        window.setScene(scene);
        window.setOnCloseRequest(event -> {
            if (activeGame != null) activeGame.stop();
            if (menuController != null) menuController.stop();
            if (upgradeController != null) upgradeController.stop();
            AudioManager.getInstance().dispose();
        });
        showMenu();
        window.show();
    }

    /** สร้างและแสดงหน้าจอเมนูหลัก */
    private void showMenu() {
        if (activeGame != null) {
            activeGame.stop();
            activeGame = null;
        }
        stopMenuAndUpgrade();
        menuController = new MenuController(this::startGame, this::showHowToPlay, () -> {
            stopMenuAndUpgrade();
            AudioManager.getInstance().dispose();
            Platform.exit();
        });
        root.show(menuController.createView());
    }

    /** Displays instructions inside the shared scaled scene. */
    private void showHowToPlay() {
        stopMenuAndUpgrade();
        root.show(new HowToPlayController(this::showMenu).createView());
    }

    /** สร้างเซสชันใหม่และเปิดหน้าจอตามสถานะเริ่มต้นของเกม */
    private void startGame() {
        GameSession session = new GameSession();
        if (!GameErrorHandler.run(session::start)) return;
        if (menuController != null) menuController.stop();
        menuController = null;
        GameLogger.gameEvent("Basic game session started");
        AudioManager.getInstance().playGameMusic();
        showState(session);
    }

    /** เลือกหน้าจอและตัวควบคุมให้ตรงกับสถานะปัจจุบันของเซสชัน */
    private void showState(GameSession session) {
        switch (session.getState()) {
            case PLAYING -> {
                activeGame = new GameController(session, this::showState);
                root.show(activeGame);
            }
            case UPGRADE -> {
                if (activeGame != null) activeGame.stop();
                WritableImage backdrop = activeGame == null ? null : activeGame.snapshotGameplay();
                activeGame = null;
                if (upgradeController != null) upgradeController.stop();
                upgradeController = new UpgradeController(session, this::showState, backdrop);
                root.show(upgradeController.createView());
            }
            case GAME_OVER, VICTORY -> {
                if (activeGame != null) activeGame.stop();
                activeGame = null;
                if (upgradeController != null) {
                    upgradeController.stop();
                    upgradeController = null;
                }
                root.show(new GameOverController(session, this::startGame).createView());
            }

            default -> showMenu();
        }
    }

    private void stopMenuAndUpgrade() {
        if (menuController != null) {
            menuController.stop();
            menuController = null;
        }
        if (upgradeController != null) {
            upgradeController.stop();
            upgradeController = null;
        }
    }

    /** ส่งอาร์กิวเมนต์เริ่มต้นไปยังระบบ JavaFX */
    public static void main(String[] args) {
        Application.launch(ArkanoidApplication.class, args);
    }
}
