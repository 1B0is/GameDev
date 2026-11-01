package se233.gamedev.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import se233.gamedev.model.Ball;
import se233.gamedev.model.Brick;
import se233.gamedev.model.BrickType;
import se233.gamedev.model.GameSession;
import se233.gamedev.model.GameState;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** วาดฉากและสไปรต์จากโมเดลโดยไม่แก้สถานะการเล่น */
public final class GameRenderer {
    public static final double CANVAS_WIDTH = 800;
    public static final double CANVAS_HEIGHT = 600;
    public static final double HUD_HEIGHT = 46;

    private static final Image FIELDS = load("/se233/gamedev/images/Fields.png");
    private static final Image BLOCKS = load("/se233/gamedev/images/Blocks_Backgrounds.png");
    private static final Image CHARACTERS = keyGreen(
            load("/se233/gamedev/images/Characters.png"));
    private static final Image BEAM_ICON = load("/se233/gamedev/images/Beam_icon.jpg");
    private static final Color TEXT = Color.web("#f4f4f4");
    private static int previousFieldIndex = -1;

    private final int fieldIndex = chooseField();
    private long paddleAnimationElapsedNanos;
    private long previousAnimationTimestampNanos;
    private boolean animationClockStarted;

    /** วาดพื้นหลัง HUD วัตถุเกม เอฟเฟกต์ และข้อความสถานะลงบน Canvas */
    public void draw(Canvas canvas, GameSession session, List<Ball> balls, double paddleX,
                     double paddleWidth, Map<Brick, Double> flashes, List<Point2D> beamDrops,
                     double beamSeconds, double gameOverAnimationSeconds, String status) {
        long now = System.nanoTime();
        if (animationClockStarted && session.getState() == GameState.PLAYING) {
            paddleAnimationElapsedNanos += now - previousAnimationTimestampNanos;
        }
        previousAnimationTimestampNanos = now;
        animationClockStarted = true;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawField(gc);
        drawHud(gc, session);

        for (Brick brick : session.getStage().getBricks()) {
            double flash = flashes.getOrDefault(brick, 0.0);
            if (!brick.isDestroyed() || flash > 0) drawBrick(gc, brick, flash);
        }

        if (beamSeconds > 0) drawBeams(gc, paddleX, paddleWidth);
        if (gameOverAnimationSeconds < 0) {
            drawPaddle(gc, paddleX, paddleWidth);
            for (Ball ball : balls) drawBall(gc, ball);
        }
        for (Point2D drop : beamDrops) {
            if (BEAM_ICON != null) gc.drawImage(BEAM_ICON, drop.getX(), drop.getY(), 28, 28);
            else {
                gc.setFill(Color.DEEPSKYBLUE);
                gc.fillOval(drop.getX(), drop.getY(), 28, 28);
            }
        }
        if (gameOverAnimationSeconds >= 0) {
            drawExplosion(gc, paddleX, paddleWidth, gameOverAnimationSeconds);
        }

        if (status != null && !status.isBlank()) {
            gc.setFill(Color.color(0, 0, 0, 0.58));
            gc.fillRoundRect(180, 285, 440, 54, 14, 14);
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Monospaced", 18));
            gc.fillText(status, 400 - gc.getFont().getSize() * status.length() * 0.3, 318);
        }
    }

    /** เลือกภาพสนามที่สุ่มไว้ วาดพื้นที่เล่นและกรอบตกแต่ง */
    private void drawField(GraphicsContext gc) {
        gc.setFill(Color.web("#050b1e"));
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        if (FIELDS != null) {
            int column = fieldIndex % 5;
            int row = fieldIndex / 5;
            gc.drawImage(FIELDS, column * 232 + 8, row * 256 + 8, 208, 232,
                    4, HUD_HEIGHT + 4, CANVAS_WIDTH - 8, CANVAS_HEIGHT - HUD_HEIGHT - 8);
        } else {
            gc.setFill(Color.web("#09184c"));
            gc.fillRect(0, HUD_HEIGHT, CANVAS_WIDTH, CANVAS_HEIGHT - HUD_HEIGHT);
        }

        gc.setFill(Color.color(0.01, 0.02, 0.08, 0.2));
        gc.fillRect(0, HUD_HEIGHT, CANVAS_WIDTH, CANVAS_HEIGHT - HUD_HEIGHT);
        gc.setStroke(Color.web("#aeb6c4"));
        gc.setLineWidth(4);
        gc.strokeRect(3, HUD_HEIGHT + 2, CANVAS_WIDTH - 6, CANVAS_HEIGHT - HUD_HEIGHT - 5);
    }

    /** วาดคะแนน ชีวิต ด่าน และคำแนะนำปุ่มควบคุม */
    private void drawHud(GraphicsContext gc, GameSession session) {
        gc.setFill(Color.color(0.02, 0.03, 0.08, 0.92));
        gc.fillRect(0, 0, CANVAS_WIDTH, HUD_HEIGHT);
        gc.setFill(TEXT);
        gc.setFont(javafx.scene.text.Font.font("Monospaced", 14));
        gc.fillText("SCORE  " + String.format("%06d", session.getScore()), 18, 29);
        gc.fillText("LIVES  " + session.getLives(), 230, 29);
        gc.fillText("STAGE  " + session.getStageNumber() + "/" + session.getTotalStages(), 355, 29);
        gc.setFont(javafx.scene.text.Font.font("Monospaced", 12));
        gc.fillText("REROLL " + session.getRerollsRemaining(), 460, 29);
        gc.setFont(javafx.scene.text.Font.font("Monospaced", 9));
        gc.fillText("MOVE: ARROWS / A,D", 530, 19);
        gc.fillText("LAUNCH: SPACE", 530, 35);
    }

    /** วาดอิฐตามชนิด พร้อมเอฟเฟกต์แสดงพลังชีวิตและแสงเมื่อถูกชน */
    private void drawBrick(GraphicsContext gc, Brick brick, double flash) {
        if (BLOCKS != null) {
            double sourceY = brick.getType() == BrickType.BONUS ? 24 : 16;
            gc.drawImage(BLOCKS, 0, sourceY, 16, 8,
                    brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
        } else {
            gc.setFill(Color.web("#707c92"));
            gc.fillRoundRect(brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight(), 6, 6);
        }

        if (brick.getType() == BrickType.STRONG) {
            gc.setFill(Color.color(0.35, 0.12, 0.75, 0.42));
            gc.fillRoundRect(brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight(), 5, 5);
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Monospaced", 12));
            gc.fillText(Integer.toString(brick.getHitPoints()),
                    brick.getX() + brick.getWidth() - 13, brick.getY() + 16);
        }

        if (flash > 0) {
            gc.setGlobalAlpha(Math.min(1, flash * 5));
            gc.setFill(Color.WHITE);
            gc.fillRoundRect(brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight(), 5, 5);
            gc.setGlobalAlpha(1);
        }
    }

    /** วาดเฟรมไม้พายตามเวลา โดยคงขนาดและตำแหน่งตามโมเดล */
    private void drawPaddle(GraphicsContext gc, double x, double width) {
        if (CHARACTERS == null) return;
        int frame = SpriteAnimation.frameAt(paddleAnimationElapsedNanos);
        SpriteSheet.SourceRect source = SpriteSheet.sourceRect(SpriteSheet.VAUS_ROW, frame);
        gc.save();
        gc.setImageSmoothing(false);
        gc.drawImage(CHARACTERS, source.x(), source.y(), source.width(), source.height(),
                x + (width - 96) / 2, 546, 96, 24);
        gc.restore();
    }

    /** วาดลำแสงสองแนวจากด้านข้างของไม้พายขึ้นไปถึงขอบบนสนาม */
    private void drawBeams(GraphicsContext gc, double paddleX, double paddleWidth) {
        double top = HUD_HEIGHT + 4;
        double emitterY = 558;
        for (double fraction : new double[]{0.08, 0.92}) {
            double x = paddleX + paddleWidth * fraction;
            gc.setFill(Color.web("#168dff"));
            gc.fillRect(x - 5, top, 10, emitterY - top);
            gc.setFill(Color.WHITE);
            gc.fillRect(x - 2, top, 4, emitterY - top);
        }
    }

    /** แสดงเฟรมระเบิดตามเวลาที่ผ่านไปหลังผู้เล่นแพ้ */
    private void drawExplosion(GraphicsContext gc, double paddleX, double paddleWidth,
                               double elapsedSeconds) {
        if (CHARACTERS == null) return;
        int frame = Math.min(2, (int) (elapsedSeconds / 0.18));
        gc.drawImage(CHARACTERS, frame * 32, 5 * 16, 32, 16,
                paddleX + paddleWidth / 2 - 64, 532, 128, 64);
    }

    /** วาดลูกบอลพร้อมเงาเรืองแสงและไฮไลต์ */
    private void drawBall(GraphicsContext gc, Ball ball) {
        gc.setFill(Color.color(0.1, 0.9, 1, 0.25));
        gc.fillOval(ball.getX() - 5, ball.getY() - 5,
                ball.getWidth() + 10, ball.getHeight() + 10);
        gc.setFill(Color.web("#fff3a6"));
        gc.fillOval(ball.getX(), ball.getY(), ball.getWidth(), ball.getHeight());
        gc.setFill(Color.WHITE);
        gc.fillOval(ball.getX() + 3, ball.getY() + 2, 4, 4);
    }

    /** โหลดรูปภาพจากทรัพยากรใน classpath และคืน null หากไม่พบไฟล์ */
    private static Image load(String resource) {
        var url = GameRenderer.class.getResource(resource);
        return url == null ? null : new Image(url.toExternalForm());
    }

    /** สุ่มเลือกหนึ่งในเก้าฉากที่มีภาพ โดยไม่เลือกซ้ำกับฉากก่อนหน้า */
    private static synchronized int chooseField() {
        int next;
        do {
            next = ThreadLocalRandom.current().nextInt(9);
        } while (next == previousFieldIndex);
        previousFieldIndex = next;
        return next;
    }

    /** ทำให้พิกเซลฉากสีเขียวในภาพสไปรต์โปร่งใสก่อนนำไปวาด */
    private static Image keyGreen(Image source) {
        if (source == null || source.isError()) return null;
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        PixelReader reader = source.getPixelReader();
        WritableImage keyed = new WritableImage(width, height);
        var writer = keyed.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = reader.getColor(x, y);
                boolean greenScreen = pixel.getGreen() < 0.65
                        && pixel.getGreen() > pixel.getRed() * 1.35
                        && pixel.getGreen() > pixel.getBlue() * 1.3
                        && pixel.getGreen() > 0.22;
                writer.setColor(x, y, greenScreen ? Color.TRANSPARENT : pixel);
            }
        }
        return keyed;
    }
}
