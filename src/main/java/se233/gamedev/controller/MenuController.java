package se233.gamedev.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import se233.gamedev.model.AudioSettings;
import se233.gamedev.model.MenuSelection;
import se233.gamedev.util.AudioManager;
import se233.gamedev.view.PixelFont;
import se233.gamedev.view.PixelFonts;
import se233.gamedev.view.ScaledRoot;

/** วาดเมนูหลักและจัดการการควบคุมเมนูกับระดับเสียง */
public final class MenuController {
    public static final String GAME_TITLE = "Arkanoid";
    public static final boolean REDUCE_MOTION = false;

    private final Runnable onStart;
    private final Runnable onHowToPlay;
    private final Runnable onExit;
    private final MenuSelection selection = new MenuSelection();
    private final AudioManager audio = AudioManager.getInstance();
    private final AudioSettings settings = audio.getSettings();
    private final Canvas canvas = new Canvas(ScaledRoot.LOGICAL_WIDTH, ScaledRoot.LOGICAL_HEIGHT);
    private long startedAt;
    private int focusedVolumeRow = -1;
    private boolean draggingMusic;
    private boolean draggingEffects;
    private AnimationTimer loop;

    /** สร้างเมนูพร้อมคำสั่งสำหรับเริ่มเกม ดูวิธีเล่น และออกจากเกม */
    public MenuController(Runnable onStart, Runnable onHowToPlay, Runnable onExit) {
        this.onStart = onStart;
        this.onHowToPlay = onHowToPlay;
        this.onExit = onExit;
    }

    /** สร้างเมนู ติดตั้ง input และเริ่มแอนิเมชันพื้นหลัง */
    public Parent createView() {
        Pane root = new Pane(canvas);
        root.setPrefSize(ScaledRoot.LOGICAL_WIDTH, ScaledRoot.LOGICAL_HEIGHT);
        root.setStyle("-fx-background-color: #140B2E;");
        canvas.setFocusTraversable(true);
        canvas.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null) Platform.runLater(canvas::requestFocus);
        });
        canvas.setOnKeyPressed(event -> onKey(event.getCode()));
        canvas.setOnMouseMoved(event -> updateHover(event.getX(), event.getY()));
        canvas.setOnMousePressed(event -> {
            canvas.requestFocus();
            if (event.getX() >= 190 && event.getX() <= 222
                    && event.getY() >= 549 && event.getY() <= 626) {
                if (event.getY() < 584) audio.muteBgm();
                else audio.muteSfx();
            } else if (inMeter(event.getX(), event.getY(), 558)) {
                draggingMusic = true;
                updateMeter(event.getX(), true);
            } else if (inMeter(event.getX(), event.getY(), 598)) {
                draggingEffects = true;
                updateMeter(event.getX(), false);
            } else if (event.getX() >= 316 && event.getX() <= 500
                    && event.getY() >= 290 && event.getY() <= 452) {
                int index = Math.min(2, Math.max(0, (int) ((event.getY() - 290) / 54)));
                select(index);
                activate();
            }
        });
        canvas.setOnMouseDragged(event -> {
            if (draggingMusic) updateMeter(event.getX(), true);
            if (draggingEffects) updateMeter(event.getX(), false);
        });
        canvas.setOnMouseReleased(event -> {
            draggingMusic = false;
            draggingEffects = false;
        });

        audio.playMenuMusic();
        startedAt = System.nanoTime();
        loop = new AnimationTimer() {
            /** วาดเฟรมเมนูตามเวลาที่ผ่านไป */
            @Override
            public void handle(long now) {
                draw(canvas.getGraphicsContext2D(), (now - startedAt) / 1_000_000_000.0);
            }
        };
        loop.start();
        return root;
    }

    /** หยุดแอนิเมชันเมื่อออกจากหน้าเมนู */
    public void stop() {
        if (loop != null) loop.stop();
    }

    /** แปลปุ่มคีย์บอร์ดเป็นการนำทาง เลือก หรือปรับเสียง */
    private void onKey(KeyCode code) {
        if (code == KeyCode.TAB) {
            focusedVolumeRow = (focusedVolumeRow + 1) % 2;
        } else if (focusedVolumeRow >= 0 && (code == KeyCode.LEFT || code == KeyCode.RIGHT)) {
            int change = code == KeyCode.RIGHT ? 1 : -1;
            if (focusedVolumeRow == 0) audio.setBgmVolume(settings.getBgmVolume() + change);
            else {
                audio.setSfxVolume(settings.getSfxVolume() + change);
                audio.playSfx(AudioManager.MENU_MOVE);
            }
        } else if (focusedVolumeRow >= 0 && code == KeyCode.M) {
            if (focusedVolumeRow == 0) audio.muteBgm();
            else audio.muteSfx();
        } else if (code == KeyCode.UP || code == KeyCode.DOWN) {
            focusedVolumeRow = -1;
            select(selection.getSelectedIndex() + (code == KeyCode.DOWN ? 1 : 2));
        } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
            activate();
        }
    }

    /** เลือกรายการเมนูและเล่นเสียงเมื่อเปลี่ยนรายการ */
    private void select(int index) {
        int next = Math.floorMod(index, MenuSelection.Action.values().length);
        if (next != selection.getSelectedIndex()) {
            while (selection.getSelectedIndex() != next) selection.move(1);
            audio.playSfx(AudioManager.MENU_MOVE);
        }
    }

    /** เรียกคำสั่งของรายการเมนูที่เลือกอยู่ */
    private void activate() {
        audio.playSfx(AudioManager.MENU_SELECT);
        switch (selection.selected()) {
            case START -> onStart.run();
            case HOW_TO_PLAY -> onHowToPlay.run();
            case EXIT -> onExit.run();
        }
    }

    /** เปลี่ยนรายการที่เลือกตามตำแหน่งเมาส์ */
    private void updateHover(double x, double y) {
        if (x >= 316 && x <= 500 && y >= 290 && y <= 452) {
            select(Math.min(2, Math.max(0, (int) ((y - 290) / 54))));
        }
    }

    /** ตรวจว่าตำแหน่งเมาส์อยู่ในแถบปรับเสียงหรือไม่ */
    private boolean inMeter(double x, double y, double rowY) {
        return x >= 36 && x <= 226 && y >= rowY - 9 && y <= rowY + 22;
    }

    /** แปลงตำแหน่งเมาส์เป็นระดับเสียงเพลงหรือเอฟเฟกต์ */
    private void updateMeter(double x, boolean music) {
        int volume = (int) Math.round(Math.max(0, Math.min(1, (x - 42) / 140)) * 10);
        if (music) audio.setBgmVolume(volume);
        else {
            audio.setSfxVolume(volume);
            audio.playSfx(AudioManager.MENU_MOVE);
        }
    }

    /** วาดพื้นหลัง เมนู และแผงควบคุมเสียง */
    private void draw(GraphicsContext gc, double elapsed) {
        gc.setImageSmoothing(false);
        gc.setFill(ScaledRoot.VOID_VIOLET);
        gc.fillRect(0, 0, 800, 720);
        drawStars(gc, elapsed);
        drawScanlines(gc, elapsed);
        double intro = REDUCE_MOTION ? 1 : Math.min(1, elapsed / 0.8);
        drawTitle(gc, elapsed, intro);

        String[] labels = {"Start game", "How to play", "Exit"};
        double[] ys = {290, 344, 398};
        for (int i = 0; i < labels.length; i++) drawMenuButton(gc, labels[i], ys[i], i, intro);
        drawVolumePanel(gc, intro);
        PixelFont.draw(gc, "V1.0", 720, 680, 1, ScaledRoot.FROST_WHITE);
    }

    /** วาดชื่อเกมและภาพเคลื่อนไหวตกแต่งส่วนหัว */
    private void drawTitle(GraphicsContext gc, double elapsed, double intro) {
        String title = GAME_TITLE.toUpperCase();
        double x = (800 - title.length() * 30) / 2;
        double y = 112;
        PixelFont.drawBricked(gc, title, x, y, 5, intro,
                ScaledRoot.ION_CYAN, ScaledRoot.HOT_MAGENTA);
        PixelFont.draw(gc, "LAUNCH CONSOLE  /  SHIP HANGAR", 280, y + 62,
                1, ScaledRoot.FROST_WHITE);
        gc.setFill(ScaledRoot.PANEL_PLUM);
        gc.fillRect(294, y + 78, 212, 4);
        gc.setFill(ScaledRoot.ION_CYAN);
        gc.fillRect(294, y + 78, 212 * intro, 4);
        if (!REDUCE_MOTION) {
            double ballX = 270 + ((elapsed * 92) % 310);
            double ballY = 218 + Math.abs(Math.sin(elapsed * 2.8)) * 30;
            gc.setFill(ScaledRoot.FROST_WHITE);
            gc.fillRect(ballX, ballY, 8, 8);
            gc.setFill(ScaledRoot.ION_CYAN);
            gc.fillRect(ballX - 10, ballY + 3, 7, 2);
            gc.fillRect(ballX - 18, ballY + 3, 5, 2);
        }
    }

    /** วาดรายการเมนูหนึ่งรายการและสถานะการเลือก */
    private void drawMenuButton(GraphicsContext gc, String text, double y, int index, double intro) {
        y += (1 - intro) * 55;
        boolean selected = selection.getSelectedIndex() == index;
        double x = 312;
        gc.setFill(selected ? ScaledRoot.PANEL_PLUM : ScaledRoot.PANEL_DARK);
        gc.fillRect(x, y, 176, 42);
        gc.setStroke(selected ? ScaledRoot.SIGNAL_AMBER : ScaledRoot.PANEL_EDGE);
        gc.setLineWidth(selected ? 3 : 2);
        gc.strokeRect(x, y, 176, 42);
        if (selected) {
            gc.setFill(ScaledRoot.SIGNAL_AMBER);
            gc.fillRect(x + 9, y + 12, 4, 18);
        }
        gc.setFont(Font.font(PixelFonts.DISPLAY.getFamily(), 25));
        gc.setFill(selected ? ScaledRoot.FROST_WHITE : ScaledRoot.TEXT_MUTED);
        gc.fillText(text, x + 22, y + 29);
    }

    /** วาดแผงระดับเสียงและแถวควบคุมเพลงกับเอฟเฟกต์ */
    private void drawVolumePanel(GraphicsContext gc, double intro) {
        gc.save();
        gc.translate(0, (1 - intro) * 75);
        gc.setFill(ScaledRoot.PANEL_DARK);
        gc.fillRect(26, 540, 244, 132);
        gc.setStroke(ScaledRoot.ION_CYAN);
        gc.setLineWidth(2);
        gc.strokeRect(26, 540, 244, 132);
        drawVolumeRow(gc, "MUSIC", settings.getBgmVolume(), settings.isBgmMuted(), 558,
                focusedVolumeRow == 0);
        drawVolumeRow(gc, "EFFECTS", settings.getSfxVolume(), settings.isSfxMuted(), 598,
                focusedVolumeRow == 1);
        PixelFont.draw(gc, "TAB FOCUS  M MUTE", 40, 638, 1, ScaledRoot.TEXT_DIM);
        gc.restore();
    }

    private void drawVolumeRow(GraphicsContext gc, String label, int volume, boolean muted,
                               double y, boolean focused) {
        PixelFont.draw(gc, label, 40, y, 1, focused ? ScaledRoot.SIGNAL_AMBER : ScaledRoot.FROST_WHITE);
        for (int segment = 0; segment < 10; segment++) {
            gc.setFill(!muted && segment < volume ? ScaledRoot.ION_CYAN : ScaledRoot.METER_OFF);
            gc.fillRect(42 + segment * 14, y + 12, 10, 8);
        }
        gc.setStroke(muted ? ScaledRoot.HOT_MAGENTA : ScaledRoot.FROST_WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(194, y + 7, 22, 18);
        gc.setFill(muted ? ScaledRoot.HOT_MAGENTA : ScaledRoot.FROST_WHITE);
        gc.fillRect(198, y + 12, 5, 8);
        gc.fillPolygon(new double[]{203, 210, 210, 203},
                new double[]{y + 10, y + 6, y + 26, y + 22}, 4);
        if (muted) gc.strokeLine(196, y + 8, 214, y + 24);
    }

    /** วาดดาวพื้นหลังที่เคลื่อนตามเวลาของเมนู */
    private void drawStars(GraphicsContext gc, double elapsed) {
        for (int i = 0; i < 80; i++) {
            double parallax = i < 48 ? 1.0 : 0.45;
            double x = (i * 97 + (REDUCE_MOTION ? 0 : elapsed * (3 + parallax * 5))) % 800;
            double y = i * 53 % 520;
            gc.setFill(Color.color(0.55, 0.8, 1, i < 48 ? 0.34 : 0.18));
            int size = i % 9 == 0 ? 3 : 2;
            gc.fillRect(x, y, size, size);
        }
    }

    /** วาดเส้นสแกนและแสงเคลื่อนเพื่อเพิ่มบรรยากาศภาพ */
    private void drawScanlines(GraphicsContext gc, double elapsed) {
        gc.setFill(Color.color(1, 1, 1, 0.025));
        for (int y = 0; y < 720; y += 5) gc.fillRect(0, y, 800, 1);
        if (!REDUCE_MOTION) {
            gc.setFill(Color.color(0.2, 0.9, 1, 0.05));
            gc.fillRect(0, (elapsed * 22) % 720, 800, 2);
        }
    }
}
