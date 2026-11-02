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
import se233.gamedev.model.GameBounds;
import se233.gamedev.model.GameSession;
import se233.gamedev.model.GameState;
import se233.gamedev.model.Meteor;
import se233.gamedev.model.WeatherEvent;
import se233.gamedev.model.WeatherType;
import se233.gamedev.util.BeamGeometry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** วาดฉากและสไปรต์จากโมเดลโดยไม่แก้สถานะการเล่น */
public final class GameRenderer {
    public static final double CANVAS_WIDTH = GameBounds.FIELD_WIDTH;
    public static final double CANVAS_HEIGHT = GameBounds.FIELD_HEIGHT;
    public static final double HUD_HEIGHT = GameBounds.HUD_HEIGHT;
    private static final double FOG_BASE_OPACITY = 0.25;
    private static final double FOG_MIN_OPACITY = 0.16;
    private static final double FOG_MAX_OPACITY = 0.34;
    private static final double FOG_PULSE_AMPLITUDE = 0.045;
    private static final double FOG_FADE_SECONDS = 0.8;
    private static final double METEOR_INVULNERABILITY_BLINK_SECONDS = 0.12;

    private static final Image FIELDS = load("/se233/gamedev/images/Fields.png");
    private static final Image BLOCKS = load("/se233/gamedev/images/Blocks_Backgrounds.png");
    private static final Image FOG = keyBlack(load("/se233/gamedev/images/Fog.png"));
    private static final Image METEOR = keyBlack(load("/se233/gamedev/images/METEOR.png"));
    private static final int[] METEOR_BOUNDS = opaqueBounds(METEOR);
    private static final Image BLURRED_NORMAL_BRICK = blurTile(BLOCKS, 0, 16, 16, 8);
    private static final Image BLURRED_BONUS_BRICK = blurTile(BLOCKS, 0, 24, 16, 8);
    private static final Image CHARACTERS = keyGreen(
            load("/se233/gamedev/images/Characters.png"));
    private static final Image BEAM_ICON = load("/se233/gamedev/images/Beam_icon.jpg");
    private static final Color TEXT = Color.web("#f4f4f4");
    private static int previousFieldIndex = -1;

    private final int fieldIndex = chooseField();
    private long paddleAnimationElapsedNanos;
    private long previousAnimationTimestampNanos;
    private boolean animationClockStarted;
    private double weatherElapsedSeconds;
    private double scorePopSeconds;
    private double comboPopSeconds;
    private int previousScore;
    private int previousCombo;

    /** วาดพื้นหลัง HUD วัตถุเกม เอฟเฟกต์ และข้อความสถานะลงบน Canvas */
    public void draw(Canvas canvas, double viewportScale, GameSession session,
                     List<Ball> balls, double paddleX,
                     double paddleWidth, Map<Brick, Double> flashes, List<Point2D> beamDrops,
                     double beamSeconds, double gameOverAnimationSeconds, String status,
                     List<Meteor> meteors, WeatherEvent weatherEvent,
                     WarningPopupState weatherPopup, boolean paddleInvulnerable) {
        long now = System.nanoTime();
        double deltaSeconds = animationClockStarted
                ? Math.max(0, (now - previousAnimationTimestampNanos) / 1_000_000_000.0) : 0;
        if (animationClockStarted && session.getState() == GameState.PLAYING) {
            paddleAnimationElapsedNanos += now - previousAnimationTimestampNanos;
        }
        if (weatherEvent != null && weatherEvent.isActive()) weatherElapsedSeconds += deltaSeconds;
        scorePopSeconds = Math.max(0, scorePopSeconds - deltaSeconds);
        comboPopSeconds = Math.max(0, comboPopSeconds - deltaSeconds);
        if (session.getScore() != previousScore) {
            scorePopSeconds = 0.36;
            previousScore = session.getScore();
        }
        if (session.getComboStreak() > previousCombo) comboPopSeconds = 0.30;
        previousCombo = session.getComboStreak();
        previousAnimationTimestampNanos = now;
        animationClockStarted = true;

        WeatherType weather = weatherEvent != null && weatherEvent.isActive()
                ? weatherEvent.getType() : null;
        WeatherTheme theme = WeatherTheme.forWeather(weather);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);
        gc.scale(viewportScale, viewportScale);
        drawField(gc, theme);
        drawHud(gc, session, theme);

        for (Brick brick : session.getStage().getBricks()) {
            double flash = flashes.getOrDefault(brick, 0.0);
            if (!brick.isDestroyed() || flash > 0) {
                drawBrick(gc, brick, flash, theme, weather == WeatherType.FOG_VALLEY);
            }
        }

        if (weather == WeatherType.FOG_VALLEY) drawFog(gc);
        if (weather == WeatherType.METEOR_SHOWER) drawMeteors(gc, meteors, weatherElapsedSeconds);
        if (beamSeconds > 0) drawBeams(gc, paddleX, paddleWidth);
        if (gameOverAnimationSeconds < 0) {
            if (!paddleInvulnerable || (weatherElapsedSeconds % METEOR_INVULNERABILITY_BLINK_SECONDS)
                    < METEOR_INVULNERABILITY_BLINK_SECONDS / 2) {
                drawPaddle(gc, paddleX, paddleWidth);
            }
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
        if (weatherPopup != null && weatherPopup.isVisible() && weather != null) {
            WarningPopupRenderer.draw(gc, weather, theme, weatherPopup, FOG, METEOR, METEOR_BOUNDS);
        }

        if (status != null && !status.isBlank()
                && (weatherPopup == null || !weatherPopup.isVisible())) {
            gc.setFill(Color.color(0, 0, 0, 0.58));
            gc.fillRect(150, 300, 500, 48);
            PixelFont.draw(gc, status, 400 - status.length() * 6.0,
                    319, 2, Color.WHITE);
        }
    }

    /** เลือกภาพสนามที่สุ่มไว้ วาดพื้นที่เล่นและกรอบตกแต่ง */
    private void drawField(GraphicsContext gc, WeatherTheme theme) {
        gc.setFill(theme.background());
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

        gc.setFill(theme.fieldTint());
        gc.fillRect(0, HUD_HEIGHT, CANVAS_WIDTH, CANVAS_HEIGHT - HUD_HEIGHT);
        gc.setStroke(theme.accent());
        gc.setLineWidth(5);
        gc.strokeRect(3, HUD_HEIGHT + 2, CANVAS_WIDTH - 6, CANVAS_HEIGHT - HUD_HEIGHT - 5);
    }

    /** วาดคะแนน ชีวิต ด่าน และคำแนะนำปุ่มควบคุม */
    private void drawHud(GraphicsContext gc, GameSession session, WeatherTheme theme) {
        gc.setFill(theme.hud());
        gc.fillRect(0, 0, CANVAS_WIDTH, HUD_HEIGHT);
        gc.setFill(theme.accent());
        gc.fillRect(0, HUD_HEIGHT - 4, CANVAS_WIDTH, 4);

        Color text = Color.web("#f4f4f4");
        PixelFont.draw(gc, "LIVES", 20, 12, 2, theme.accent());
        PixelFont.draw(gc, formatLives(session.getLives()), 20, 39, 3, text);
        PixelFont.draw(gc, "STAGE", 140, 12, 2, theme.accent());
        PixelFont.draw(gc, session.getStageNumber() + "/" + session.getTotalStages(),
                140, 39, 2, text);
        PixelFont.draw(gc, "REROLL", 250, 12, 2, theme.accent());
        PixelFont.draw(gc, Integer.toString(session.getRerollsRemaining()), 250, 39, 3, text);
        PixelFont.draw(gc, "MOVE ARROWS A D", 20, 67, 1, text);

        int scoreScale = scorePopSeconds > 0.15 ? 3 : 2;
        String score = String.format("%06d", session.getScore());
        Color scoreColor = scorePopSeconds > 0.20 ? Color.web("#fff28a") : text;
        PixelFont.draw(gc, "SCORE", 400 - 5 * 6.0 * 2 / 2, 8, 2, theme.accent());
        PixelFont.draw(gc, score, 400 - score.length() * 6.0 * scoreScale / 2,
                30 - (scoreScale == 3 ? 3 : 0), scoreScale, scoreColor);

        gc.save();
        gc.translate(694, 31);
        gc.rotate(-10);
        PixelFont.draw(gc, "COMBO", 0, 0, 2, theme.accent());
        if (session.getComboStreak() >= 3) {
            int comboScale = comboPopSeconds > 0.12 ? 3 : 2;
            PixelFont.draw(gc, String.format("X%.1f", session.getComboMultiplier()),
                    4, 20, comboScale, Color.web("#7CFC98"));
        }
        gc.restore();
        PixelFont.draw(gc, "LAUNCH SPACE", 595, 68, 1, text);
    }

    /** วาดอิฐตามชนิด พร้อมเอฟเฟกต์แสดงพลังชีวิตและแสงเมื่อถูกชน */
    private void drawBrick(GraphicsContext gc, Brick brick, double flash,
                           WeatherTheme theme, boolean fogActive) {
        if (fogActive && (brick.getType() == BrickType.BONUS
                ? BLURRED_BONUS_BRICK : BLURRED_NORMAL_BRICK) != null) {
            Image blurred = brick.getType() == BrickType.BONUS
                    ? BLURRED_BONUS_BRICK : BLURRED_NORMAL_BRICK;
            gc.drawImage(blurred, brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
        } else if (BLOCKS != null) {
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
            PixelFont.draw(gc, Integer.toString(brick.getHitPoints()),
                    brick.getX() + brick.getWidth() - 12, brick.getY() + 6, 1, Color.WHITE);
        }

        if (theme.brickTint().getOpacity() > 0) {
            gc.setFill(theme.brickTint());
            gc.fillRect(brick.getX(), brick.getY(), brick.getWidth(), brick.getHeight());
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
                x + (width - 96) / 2, GameBounds.PADDLE_Y - 6, 96, 24);
        gc.restore();
    }

    /** วาดลำแสงสองแนวจากด้านข้างของไม้พายขึ้นไปถึงขอบบนสนาม */
    private void drawBeams(GraphicsContext gc, double paddleX, double paddleWidth) {
        double top = HUD_HEIGHT + 4;
        double emitterY = GameBounds.PADDLE_Y + 10;
        for (double x : BeamGeometry.emitterXPositions(paddleX, paddleWidth)) {
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
                paddleX + paddleWidth / 2 - 64, GameBounds.PADDLE_Y - 128, 128, 64);
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

    /** วาดชั้นหมอกที่เลื่อนช้า ๆ พร้อมความทึบที่แกว่งและบางลงใกล้ไม้พาย */
    private void drawFog(GraphicsContext gc) {
        if (FOG == null) return;
        double wave = Math.sin(weatherElapsedSeconds * 0.55) * FOG_PULSE_AMPLITUDE;
        double fadeIn = Math.min(1, weatherElapsedSeconds / FOG_FADE_SECONDS);
        double opacity = Math.max(FOG_MIN_OPACITY,
                Math.min(FOG_MAX_OPACITY, FOG_BASE_OPACITY + wave)) * fadeIn;
        double areaY = HUD_HEIGHT;
        double areaHeight = CANVAS_HEIGHT - HUD_HEIGHT;
        gc.save();
        gc.beginPath();
        gc.rect(0, areaY, CANVAS_WIDTH, areaHeight);
        gc.closePath();
        gc.clip();
        drawFogLayer(gc, opacity * 0.55, weatherElapsedSeconds * 7, 0,
                CANVAS_WIDTH * 1.25, areaHeight * 1.15);
        drawFogLayer(gc, opacity * 0.35, -weatherElapsedSeconds * 11, 18,
                CANVAS_WIDTH * 1.12, areaHeight * 1.05);
        drawFogLayer(gc, opacity * 0.22, weatherElapsedSeconds * 16, -12,
                CANVAS_WIDTH * 1.35, areaHeight * 1.20);
        gc.setGlobalAlpha(0.10);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, GameBounds.PADDLE_Y - 112, CANVAS_WIDTH, 112);
        gc.restore();
    }

    /** วาดภาพหมอกหนึ่งชั้นแบบเลื่อนวนในขอบเขตสนาม */
    private void drawFogLayer(GraphicsContext gc, double opacity, double offsetX,
                              double offsetY, double width, double height) {
        double x = -((offsetX % width + width) % width);
        gc.setGlobalAlpha(opacity);
        gc.drawImage(FOG, x, GameBounds.HUD_HEIGHT + offsetY, width, height);
        gc.drawImage(FOG, x + width, GameBounds.HUD_HEIGHT + offsetY, width, height);
        gc.setGlobalAlpha(1);
    }

    /** วาดอุกกาบาตพร้อมวงเรืองแสงเป็นจังหวะและรอยทางจางด้านหลัง */
    private void drawMeteors(GraphicsContext gc, List<Meteor> meteors, double elapsedSeconds) {
        if (METEOR == null || METEOR_BOUNDS == null) return;
        double pulse = 0.72 + Math.sin(elapsedSeconds * 8) * 0.12;
        for (Meteor meteor : meteors) {
            double centerX = meteor.getX() + meteor.getWidth() / 2;
            double centerY = meteor.getY() + meteor.getHeight() / 2;
            gc.setFill(Color.color(0.45, 0.12, 1, 0.10 * pulse));
            gc.fillOval(centerX - 32, centerY - 43, 64, 86);
            gc.setFill(Color.color(0.93, 0.20, 0.95, 0.18 * pulse));
            gc.fillOval(centerX - 21, centerY - 34, 42, 68);
            gc.setGlobalAlpha(0.13);
            drawMeteorImage(gc, meteor, meteor.getY() - 34, 0.48);
            gc.setGlobalAlpha(0.25);
            drawMeteorImage(gc, meteor, meteor.getY() - 17, 0.68);
            gc.setGlobalAlpha(1);
            drawMeteorImage(gc, meteor, meteor.getY(), 1);
        }
    }

    /** วาดส่วนที่ไม่โปร่งใสของภาพอุกกาบาตในกรอบโมเดล */
    private void drawMeteorImage(GraphicsContext gc, Meteor meteor, double y, double alpha) {
        gc.setGlobalAlpha(alpha);
        gc.drawImage(METEOR, METEOR_BOUNDS[0], METEOR_BOUNDS[1],
                METEOR_BOUNDS[2], METEOR_BOUNDS[3], meteor.getX(), y,
                meteor.getWidth(), meteor.getHeight());
        gc.setGlobalAlpha(1);
    }

    /** แปลงจำนวนชีวิตเป็นข้อความจำนวนเต็มหรือครึ่งหัวใจสำหรับ HUD */
    private String formatLives(double lives) {
        return lives % 1 == 0 ? Integer.toString((int) lives) : String.format("%.1f", lives);
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

    /** ทำให้พื้นหลังสีดำของภาพเอฟเฟกต์โปร่งใสเพื่อให้ใช้เป็น overlay ได้ */
    private static Image keyBlack(Image source) {
        if (source == null || source.isError()) return null;
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        PixelReader reader = source.getPixelReader();
        WritableImage keyed = new WritableImage(width, height);
        var writer = keyed.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = reader.getColor(x, y);
                boolean background = pixel.getOpacity() < 0.02
                        || (pixel.getRed() < 0.025 && pixel.getGreen() < 0.025
                        && pixel.getBlue() < 0.025);
                writer.setColor(x, y, background ? Color.TRANSPARENT : pixel);
            }
        }
        return keyed;
    }

    /** ค้นหากรอบพิกเซลที่มองเห็นได้ในภาพเพื่อไม่วาดพื้นที่โปร่งใสของ sprite */
    private static int[] opaqueBounds(Image image) {
        if (image == null || image.isError()) return null;
        PixelReader reader = image.getPixelReader();
        int minX = (int) image.getWidth();
        int minY = (int) image.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < (int) image.getHeight(); y++) {
            for (int x = 0; x < (int) image.getWidth(); x++) {
                if (reader.getColor(x, y).getOpacity() <= 0.02) continue;
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        return maxX < minX ? null : new int[]{minX, minY, maxX - minX + 1, maxY - minY + 1};
    }

    /** สร้างสำเนา tile อิฐที่เบลอไว้ล่วงหน้าเพื่อใช้เฉพาะในสภาพหมอก */
    private static Image blurTile(Image sheet, int sourceX, int sourceY, int width, int height) {
        if (sheet == null || sheet.isError()) return null;
        PixelReader reader = sheet.getPixelReader();
        WritableImage blurred = new WritableImage(width, height);
        var writer = blurred.getPixelWriter();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double red = 0;
                double green = 0;
                double blue = 0;
                double alpha = 0;
                int count = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int sx = Math.max(0, Math.min((int) sheet.getWidth() - 1, sourceX + x + dx));
                        int sy = Math.max(0, Math.min((int) sheet.getHeight() - 1, sourceY + y + dy));
                        Color pixel = reader.getColor(sx, sy);
                        red += pixel.getRed();
                        green += pixel.getGreen();
                        blue += pixel.getBlue();
                        alpha += pixel.getOpacity();
                        count++;
                    }
                }
                writer.setColor(x, y, new Color(red / count, green / count,
                        blue / count, alpha / count));
            }
        }
        return blurred;
    }
}
