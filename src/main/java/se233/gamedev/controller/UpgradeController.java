package se233.gamedev.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import se233.gamedev.model.GameSession;
import se233.gamedev.model.Upgrade;
import se233.gamedev.model.UpgradeSelectionState;
import se233.gamedev.model.UpgradeType;
import se233.gamedev.util.AudioManager;
import se233.gamedev.util.GameErrorHandler;
import se233.gamedev.view.PixelFont;
import se233.gamedev.view.PixelFonts;
import se233.gamedev.view.ScaledRoot;

import java.util.List;
import java.util.Set;

/** Presents upgrade modules and forwards confirmed choices to the unchanged game rules. */
public final class UpgradeController {
    public static final boolean REDUCE_MOTION = false;
    private static final double CARD_WIDTH = 205;
    private static final double CARD_HEIGHT = 298;
    private static final double CARD_GAP = 28;
    private final GameSession session;
    private final java.util.function.Consumer<GameSession> onContinue;
    private final UpgradeSelectionState selection;
    private final Image gameplayBackdrop;
    private final Canvas canvas = new Canvas(800, 720);
    private final AudioManager audio = AudioManager.getInstance();
    private long previousFrame;
    private Upgrade pendingConfirm;
    private double confirmElapsed;
    private int hoveredIndex = -1;
    private UpgradeType hoveredOwnedType;
    private AnimationTimer loop;

<<<<<<< HEAD
=======
    public UpgradeController(GameSession session,
                             java.util.function.Consumer<GameSession> onContinue) {
        this(session, onContinue, null);
    }
>>>>>>> a6c40d904f8676d65de2ba310f6dd126bdd46456

    public UpgradeController(GameSession session,
                             java.util.function.Consumer<GameSession> onContinue,
                             Image gameplayBackdrop) {
        this.session = session;
        this.onContinue = onContinue;
        this.gameplayBackdrop = gameplayBackdrop;
        selection = new UpgradeSelectionState(session.getOfferedUpgrades());
    }

    public Parent createView() {
        Pane root = new Pane(canvas);
        root.setPrefSize(800, 720);
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(event -> onKey(event.getCode()));
        canvas.setOnMouseMoved(event -> {
            hoveredIndex = cardAt(event.getX(), event.getY());
            hoveredOwnedType = ownedAt(event.getX(), event.getY());
            if (hoveredIndex >= 0 && selection.isInputEnabled()
                    && hoveredIndex != selection.getSelectedIndex()) {
                select(hoveredIndex);
            }
        });
        canvas.setOnMouseClicked(event -> {
            int card = cardAt(event.getX(), event.getY());
            if (card >= 0) {
                select(card);
                confirm();
            } else if (event.getX() >= 45 && event.getX() <= 240
                    && event.getY() >= 610 && event.getY() <= 662) {
                reroll();
            }
        });
        canvas.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null) Platform.runLater(canvas::requestFocus);
        });
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double dt = previousFrame == 0 ? 0
                        : Math.min(0.05, (now - previousFrame) / 1_000_000_000.0);
                previousFrame = now;
                update(dt);
                draw(canvas.getGraphicsContext2D());
            }
        };
        loop.start();
        return root;
    }

    public void stop() {
        if (loop != null) loop.stop();
    }

    private void onKey(KeyCode code) {
        if (code == KeyCode.A || code == KeyCode.LEFT) select(selection.getSelectedIndex() - 1);
        else if (code == KeyCode.D || code == KeyCode.RIGHT) {
            select(selection.getSelectedIndex() + 1);
        } else if (code == KeyCode.SPACE || code == KeyCode.ENTER) confirm();
        else if (code == KeyCode.R) reroll();
    }

    private void select(int index) {
        if (!selection.isInputEnabled()) return;
        int before = selection.getSelectedIndex();
        int next = Math.floorMod(index, selection.getOffers().size());
        selection.select(next);
        if (before != selection.getSelectedIndex()) audio.playSfx(AudioManager.MENU_MOVE);
    }

    private void confirm() {
        Upgrade choice = selection.confirm();
        if (choice == null) return;
        pendingConfirm = choice;
        confirmElapsed = 0;
        audio.playSfx(AudioManager.MENU_SELECT);
    }


    /* ยืนยัน/reroll พร้อม state/animation ส่งกลับเข้าหน้าเกม */
    private void reroll() {
        if (!selection.isInputEnabled() || session.getRerollsRemaining() == 0) return;
        if (GameErrorHandler.run(session::rerollUpgrades)) {
            List<Upgrade> replacements = session.getOfferedUpgrades();
            if (selection.reroll(replacements)) audio.playSfx(AudioManager.MENU_MOVE);
        }
    }

    private void update(double dt) {
        selection.update(dt);
        if (pendingConfirm == null) return;
        confirmElapsed += dt;
        if (confirmElapsed < 0.35) return;
        int index = session.getOfferedUpgrades().indexOf(pendingConfirm);
        if (index >= 0 && GameErrorHandler.run(() -> session.chooseUpgrade(index))) {
            stop();
            onContinue.accept(session);
        } else {
            pendingConfirm = null;
        }
    }

    private int cardAt(double x, double y) {
        double startX = (800 - selection.getOffers().size() * CARD_WIDTH
                - Math.max(0, selection.getOffers().size() - 1) * CARD_GAP) / 2;
        for (int i = 0; i < selection.getOffers().size(); i++) {
            double left = startX + i * (CARD_WIDTH + CARD_GAP);
            if (x >= left && x <= left + CARD_WIDTH && y >= 210 && y <= 510) return i;
        }
        return -1;
    }


    /* วาดภาพหน้า upgrade */
    private void draw(GraphicsContext gc) {
        gc.setImageSmoothing(false);
        gc.setFill(ScaledRoot.VOID_VIOLET);
        gc.fillRect(0, 0, 800, 720);
        if (gameplayBackdrop != null) {
            gc.setGlobalAlpha(0.24);
            gc.drawImage(gameplayBackdrop, 0, 0, 800, 720);
            gc.setGlobalAlpha(1);
        }
        drawBackdrop(gc);
        drawHeader(gc);
        drawCards(gc);
        drawOwnedStrip(gc);
        drawFooter(gc);
    }

    private void drawBackdrop(GraphicsContext gc) {
        gc.setFill(Color.color(0.02, 0.01, 0.05, 0.72));
        gc.fillRect(0, 0, 800, 720);
        for (int i = 0; i < 80; i++) {
            double x = i * 97 % 800;
            double y = i * 53 % 720;
            gc.setFill(Color.color(0.52, 0.83, 1, i % 2 == 0 ? 0.25 : 0.12));
            gc.fillRect(x, y, i % 8 == 0 ? 3 : 2, i % 8 == 0 ? 3 : 2);
        }
        gc.setFill(Color.color(1, 1, 1, 0.025));
        for (int y = 0; y < 720; y += 5) gc.fillRect(0, y, 800, 1);
    }

    private void drawHeader(GraphicsContext gc) {
        PixelFont.draw(gc, "STAGE " + (session.getStageNumber() - 1) + " CLEARED",
                40, 35, 2, ScaledRoot.ION_CYAN);
        gc.setFont(Font.font(PixelFonts.DISPLAY.getFamily(), 32));
        gc.setFill(ScaledRoot.FROST_WHITE);
        gc.fillText("Choose one module", 40, 92);
        PixelFont.draw(gc, "SCORE " + session.getScore(), 570, 40, 2, ScaledRoot.FROST_WHITE);
        PixelFont.draw(gc, "LIVES " + formatLives(session.getLives()), 570, 69, 1,
                ScaledRoot.FROST_WHITE);
        PixelFont.draw(gc, "STAGE " + (session.getStageNumber() + 1) + " NEXT", 570, 88, 1,
                ScaledRoot.ION_CYAN);
    }

    private void drawCards(GraphicsContext gc) {
        List<Upgrade> offers = selection.getOffers();
        double total = offers.size() * CARD_WIDTH + Math.max(0, offers.size() - 1) * CARD_GAP;
        double startX = (800 - total) / 2;
        double rerollScale = selection.isRerolling()
                ? Math.max(0.04, Math.abs(1 - selection.getRerollProgress() * 2)) : 1;
        for (int i = 0; i < offers.size(); i++) {
            boolean selected = i == selection.getSelectedIndex();
            double introProgress = REDUCE_MOTION ? 1 : selection.getIntroProgress();
            double delay = i * 0.17;
            double stagger = Math.max(0, Math.min(1,
                    (introProgress - delay) / (1 - delay)));
            double x = startX + i * (CARD_WIDTH + CARD_GAP);
            double y = 210 + (1 - stagger) * 150 - (selected ? 8 : 0);
            double scale = selected ? 1.10 : 1;
            double cx = x + CARD_WIDTH / 2;
            double cy = y + CARD_HEIGHT / 2;
            gc.save();
            gc.translate(cx, cy);
            gc.scale(scale * rerollScale, scale);
            gc.translate(-cx, -cy);
            gc.setGlobalAlpha(pendingConfirm != null && !selected
                    ? Math.max(0, 1 - confirmElapsed / 0.35) : selected ? 1 : 0.72);
            if (pendingConfirm != null && selected && (int) (confirmElapsed * 30) % 2 == 0) {
                gc.setGlobalAlpha(0.48);
            }
            drawCard(gc, offers.get(i), x, y, selected);
            gc.setGlobalAlpha(1);
            gc.restore();
        }
    }

    private void drawCard(GraphicsContext gc, Upgrade upgrade, double x, double y, boolean selected) {
        Color accent = accent(upgrade.getType());
        double right = x + CARD_WIDTH;
        double bottom = y + CARD_HEIGHT;
        double[] px = {x + 12, right - 12, right, right, right - 12, x + 12, x, x};
        double[] py = {y, y, y + 12, bottom - 18, bottom, bottom, bottom - 18, y + 12};
        gc.setFill(ScaledRoot.PANEL_DARK);
        gc.fillPolygon(px, py, px.length);
        gc.setStroke(selected ? ScaledRoot.SIGNAL_AMBER : accent);
        double pulse = REDUCE_MOTION ? 0 : 0.7 * Math.sin(selection.getElapsedSeconds() * 5);
        gc.setLineWidth(selected ? 3.5 + pulse : 2);
        gc.strokePolygon(px, py, px.length);
        gc.setFill(accent);
        gc.fillRect(x + 16, y + 15, CARD_WIDTH - 32, 4);
        drawIcon(gc, upgrade.getType(), x + CARD_WIDTH / 2, y + 86, accent);
        String name = upgrade.getName().toUpperCase();
        gc.setFont(Font.font(PixelFonts.DISPLAY.getFamily(), 24));
        gc.setFill(ScaledRoot.FROST_WHITE);
        gc.fillText(upgrade.getName(), x + 20, y + 150);
        drawWrappedEffect(gc, upgrade.getDescription(), x + 18, y + 186, accent);
        gc.setFill(accent);
        gc.fillRect(x + 18, bottom - 30, CARD_WIDTH - 36, 8);
        gc.setFill(ScaledRoot.VOID_VIOLET);
        for (int c = 0; c < 5; c++) gc.fillRect(x + 35 + c * 28, bottom - 30, 8, 8);
    }

    private void drawIcon(GraphicsContext gc, UpgradeType type, double centerX,
                          double centerY, Color accent) {
        gc.setFill(Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.18));
        gc.fillRect(centerX - 40, centerY - 38, 80, 76);
        gc.setFill(accent);
        switch (type) {
            case WIDE_PADDLE -> {
                gc.fillRect(centerX - 29, centerY - 5, 58, 12);
                gc.fillRect(centerX - 35, centerY - 2, 6, 6);
                gc.fillRect(centerX + 29, centerY - 2, 6, 6);
            }
            case EXTRA_LIFE -> drawHeart(gc, centerX, centerY, accent);
            case MULTI_BALL -> {
                gc.fillRect(centerX - 26, centerY - 8, 16, 16);
                gc.fillRect(centerX - 8, centerY - 20, 16, 16);
                gc.fillRect(centerX + 10, centerY - 8, 16, 16);
            }
            case POWER_BALL -> {
                gc.fillRect(centerX - 12, centerY - 18, 24, 36);
                gc.fillRect(centerX - 18, centerY - 12, 36, 24);
                gc.setFill(ScaledRoot.SIGNAL_AMBER);
                PixelFont.draw(gc, "S", centerX - 6, centerY - 10, 2, ScaledRoot.SIGNAL_AMBER);
            }
            case QUICK_BALL -> {
                gc.fillRect(centerX - 8, centerY - 8, 16, 16);
                gc.fillRect(centerX - 31, centerY - 14, 17, 4);
                gc.fillRect(centerX - 25, centerY - 3, 11, 4);
                gc.fillRect(centerX - 31, centerY + 8, 17, 4);
            }
            case PIERCING_BALL -> {
                gc.fillRect(centerX - 10, centerY - 10, 20, 20);
                gc.fillRect(centerX - 35, centerY - 2, 70, 4);
                gc.setFill(ScaledRoot.VOID_VIOLET);
                gc.fillRect(centerX - 2, centerY - 7, 4, 14);
            }
        }
    }

    private void drawHeart(GraphicsContext gc, double cx, double cy, Color accent) {
        gc.setFill(accent);
        gc.fillRect(cx - 20, cy - 14, 16, 16);
        gc.fillRect(cx + 4, cy - 14, 16, 16);
        gc.fillRect(cx - 26, cy - 8, 52, 18);
        gc.fillRect(cx - 20, cy + 10, 40, 8);
        gc.fillRect(cx - 12, cy + 18, 24, 8);
    }

    private void drawWrappedEffect(GraphicsContext gc, String effect,
                                   double x, double y, Color accent) {
        String[] words = effect.toUpperCase().split(" ");
        StringBuilder line = new StringBuilder();
        int row = 0;
        for (String word : words) {
            if (line.length() + word.length() > 22) {
                PixelFont.draw(gc, line.toString(), x, y + row++ * 24, 1, accent);
                line.setLength(0);
            }
            if (!line.isEmpty()) line.append(' ');
            line.append(word);
        }
        if (!line.isEmpty()) PixelFont.draw(gc, line.toString(), x, y + row * 24, 1, accent);
    }

    private void drawOwnedStrip(GraphicsContext gc) {
        Set<UpgradeType> owned = session.getAcquiredUpgrades();
        PixelFont.draw(gc, "YOUR UPGRADES", 440, 585, 1, ScaledRoot.FROST_WHITE);
        int index = 0;
        for (UpgradeType type : owned) {
            double x = 440 + index++ * 39;
            gc.setFill(ScaledRoot.PANEL_DARK);
            gc.fillRect(x, 606, 32, 32);
            gc.setStroke(accent(type));
            gc.setLineWidth(2);
            gc.strokeRect(x, 606, 32, 32);
            drawMiniIcon(gc, type, x, 606);
            PixelFont.draw(gc, "1", x + 25, 610, 1, ScaledRoot.SIGNAL_AMBER);
        }
        if (hoveredOwnedType != null) {
            double x = 432;
            double y = 520;
            gc.setFill(ScaledRoot.PANEL_PLUM);
            gc.fillRect(x, y, 350, 52);
            gc.setStroke(accent(hoveredOwnedType));
            gc.setLineWidth(2);
            gc.strokeRect(x, y, 350, 52);
            PixelFont.draw(gc, hoveredOwnedType.getDisplayName().toUpperCase(),
                    x + 10, y + 9, 1, ScaledRoot.FROST_WHITE);
            String effect = hoveredOwnedType.getDescription().toUpperCase();
            PixelFont.draw(gc, effect, x + 10, y + 29, 1, ScaledRoot.ION_CYAN);
        }
    }

    private UpgradeType ownedAt(double x, double y) {
        if (y < 606 || y > 638 || x < 440) return null;
        int index = (int) ((x - 440) / 39);
        List<UpgradeType> owned = session.getAcquiredUpgrades().stream().toList();
        return index >= 0 && index < owned.size() ? owned.get(index) : null;
    }

    private void drawMiniIcon(GraphicsContext gc, UpgradeType type, double x, double y) {
        gc.setFill(accent(type));
        switch (type) {
            case WIDE_PADDLE -> gc.fillRect(x + 5, y + 14, 22, 5);
            case EXTRA_LIFE -> {
                gc.fillRect(x + 7, y + 8, 7, 7);
                gc.fillRect(x + 18, y + 8, 7, 7);
                gc.fillRect(x + 6, y + 12, 20, 7);
                gc.fillRect(x + 10, y + 19, 12, 5);
            }
            case POWER_BALL -> gc.fillRect(x + 9, y + 8, 14, 14);
            case QUICK_BALL -> {
                gc.fillRect(x + 12, y + 12, 8, 8);
                gc.fillRect(x + 5, y + 8, 5, 3);
                gc.fillRect(x + 5, y + 18, 5, 3);
            }
            case MULTI_BALL -> {
                gc.fillRect(x + 5, y + 14, 6, 6);
                gc.fillRect(x + 13, y + 8, 6, 6);
                gc.fillRect(x + 21, y + 14, 6, 6);
            }
            case PIERCING_BALL -> {
                gc.fillRect(x + 13, y + 9, 7, 14);
                gc.fillRect(x + 5, y + 15, 23, 3);
            }
        }
    }

    private void drawFooter(GraphicsContext gc) {
        boolean enabled = selection.isInputEnabled();
        gc.setFill(enabled && session.getRerollsRemaining() > 0
                ? ScaledRoot.PANEL_PLUM : ScaledRoot.DISABLED_PANEL);
        gc.fillRect(42, 600, 188, 52);
        gc.setStroke(enabled && session.getRerollsRemaining() > 0
                ? ScaledRoot.ION_CYAN : ScaledRoot.DISABLED_EDGE);
        gc.setLineWidth(2);
        gc.strokeRect(42, 600, 188, 52);
        PixelFont.draw(gc, "REROLL (" + session.getRerollsRemaining() + " LEFT)",
                52, 619, 1, enabled ? ScaledRoot.FROST_WHITE : ScaledRoot.DISABLED_TEXT);
        drawKeyHint(gc, 42, 670, "A / D", "CHOOSE");
        drawKeyHint(gc, 266, 670, "SPACE", "PICK");
        drawKeyHint(gc, 470, 670, "R", "REROLL");
    }

    private void drawKeyHint(GraphicsContext gc, double x, double y, String key, String text) {
        gc.setFill(ScaledRoot.PANEL_PLUM);
        gc.fillRect(x, y, key.length() * 9 + 12, 24);
        gc.setStroke(ScaledRoot.SIGNAL_AMBER);
        gc.setLineWidth(1);
        gc.strokeRect(x, y, key.length() * 9 + 12, 24);
        PixelFont.draw(gc, key, x + 6, y + 7, 1, ScaledRoot.FROST_WHITE);
        PixelFont.draw(gc, text, x + key.length() * 9 + 20, y + 8, 1, ScaledRoot.FROST_WHITE);
    }

    private Color accent(UpgradeType type) {
        return switch (type) {
            case WIDE_PADDLE -> ScaledRoot.ION_CYAN;
            case EXTRA_LIFE -> ScaledRoot.EXTRA_LIFE_ACCENT;
            case POWER_BALL -> ScaledRoot.SIGNAL_AMBER;
            case QUICK_BALL -> ScaledRoot.QUICK_BALL_ACCENT;
            case MULTI_BALL -> ScaledRoot.MULTI_BALL_ACCENT;
            case PIERCING_BALL -> ScaledRoot.HOT_MAGENTA;
        };
    }

    private String formatLives(double lives) {
        return lives % 1 == 0 ? Integer.toString((int) lives) : String.format("%.1f", lives);
    }
}
