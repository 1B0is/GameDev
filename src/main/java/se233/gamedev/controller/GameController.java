package se233.gamedev.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;
import javafx.scene.transform.Scale;
import se233.gamedev.model.Ball;
import se233.gamedev.model.Brick;
import se233.gamedev.model.GameSession;
import se233.gamedev.model.GameState;
import se233.gamedev.model.GameBounds;
import se233.gamedev.model.Meteor;
import se233.gamedev.model.MeteorHitGuard;
import se233.gamedev.model.MeteorSpawner;
import se233.gamedev.model.Paddle;
import se233.gamedev.model.UpgradeType;
import se233.gamedev.model.WeatherEvent;
import se233.gamedev.model.WeatherType;
import se233.gamedev.util.BeamGeometry;
import se233.gamedev.util.AudioManager;
import se233.gamedev.util.GameErrorHandler;
import se233.gamedev.util.GameLogger;
import se233.gamedev.view.GameRenderer;
import se233.gamedev.view.WarningPopupState;
import se233.gamedev.view.ScaleAwareScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
/** รับอินพุตและอัปเดตสถานะเกม โดยแยกการวาดภาพไว้ใน GameRenderer */
public final class GameController extends BorderPane implements ScaleAwareScreen {
    private static final double FIELD_WIDTH = GameBounds.FIELD_WIDTH;
    private static final double FIELD_HEIGHT = GameBounds.FIELD_HEIGHT;
    private static final double GAME_OVER_EXPLOSION_SECONDS = 0.54;
    private static final double BEAM_DAMAGE_SECONDS = 1.5;
    private static final double BEAM_TICK_SECONDS = BEAM_DAMAGE_SECONDS / 10;
    private static final double BEAM_DROP_CHANCE = 0.15;

    private final GameSession session;
    private final Consumer<GameSession> onStateChange;
    private final Canvas canvas = new Canvas(FIELD_WIDTH, FIELD_HEIGHT);
    private final GameRenderer renderer = new GameRenderer();
    private final Set<KeyCode> keysDown = new HashSet<>();
    private final Map<Brick, Double> hitFlashes = new HashMap<>();
    private final Map<Ball, Set<Brick>> piercingContacts = new HashMap<>();
    private final List<Ball> balls = new ArrayList<>();
    private final List<Point2D> beamDrops = new ArrayList<>();
    private final List<Meteor> meteors = new ArrayList<>();
    private final MeteorSpawner meteorSpawner = new MeteorSpawner(new Random());
    private final Paddle paddle;
    private final AnimationTimer loop;
    private long previousFrame;
    private double beamSeconds;
    private double beamTickTimer;
    private int beamTicksRemaining;
    private double statusSeconds;
    private final MeteorHitGuard meteorHitGuard = new MeteorHitGuard();
    private double gameOverAnimationSeconds = -1;
    private String status = "PRESS SPACE TO LAUNCH";
    private final WarningPopupState weatherPopup;
    private boolean stopped;
    private double viewportScale = 1;
    private final Scale canvasCounterScale = new Scale(1, 1, 0, 0);
    /** เตรียมสถานะและวัตถุของเกม พร้อมเริ่มลูปแอนิเมชัน */
    public GameController(GameSession session, Consumer<GameSession> onStateChange) {
        this.session = session;
        this.onStateChange = onStateChange;
        double paddleWidth = 108 * session.getPaddleWidthMultiplier();
        paddle = new Paddle((FIELD_WIDTH - paddleWidth) / 2, GameBounds.PADDLE_Y,
                paddleWidth, 20, FIELD_WIDTH, 7.5);
        weatherPopup = session.getWeatherEvent()
                .filter(WeatherEvent::isActive)
                .map(ignored -> new WarningPopupState())
                .orElse(null);
        resetBalls();
        setCenter(canvas);
        BorderPane.setAlignment(canvas, Pos.TOP_LEFT);
        canvas.getTransforms().add(canvasCounterScale);
        setStyle("-fx-background-color: #050b1e;");
        setFocusTraversable(true);
        canvas.setFocusTraversable(true);

        loop = new AnimationTimer() {
            /** อัปเดตและวาดเฟรมเกมตามเวลาที่ JavaFX ส่งมา */
            @Override
            public void handle(long now) {
                if (previousFrame == 0) {
                    previousFrame = now;
                    render();
                    return;
                }

                double frameScale = Math.min((now - previousFrame) / 16_666_667.0, 2.0);
                previousFrame = now;
                update(frameScale);
                render();
            }
        };
        installKeyboard();
        loop.start();
        Platform.runLater(canvas::requestFocus);
    }
    /** ปรับขนาด Canvas โดยคงพิกัดเชิงตรรกะของสนามเกม */
    @Override
    public void setViewportScale(double scale) {
        if (!Double.isFinite(scale) || scale <= 0) return;
        viewportScale = scale;
        canvas.setWidth(FIELD_WIDTH * scale);
        canvas.setHeight(FIELD_HEIGHT * scale);
        canvasCounterScale.setX(1 / scale);
        canvasCounterScale.setY(1 / scale);
    }
    /** ผูกปุ่มควบคุมและปุ่มปล่อยบอลกับ Canvas */
    private void installKeyboard() {
        canvas.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene == null) return;
            Platform.runLater(canvas::requestFocus);
        });
        canvas.setOnKeyPressed(event -> {
            keysDown.add(event.getCode());
            if (event.getCode() == KeyCode.SPACE && session.getState() == GameState.PLAYING
                    && gameOverAnimationSeconds < 0 && !balls.isEmpty()
                    && !balls.get(0).isLaunched()) {
                if (weatherPopup != null) weatherPopup.dismiss();
                launchBalls();
                status = "";
                GameLogger.playerAction("Player launched the ball");
            }
        });
        canvas.setOnKeyReleased(event -> keysDown.remove(event.getCode()));
    }
    /** ปล่อยลูกบอลตามความเร็วที่ได้จากอัปเกรดปัจจุบัน */
    private void launchBalls() {
        double speed = session.getBallSpeedMultiplier();
        for (Ball ball : balls) ball.launch(0, -6.4 * speed);
    }
    /** อัปเดตสถานะเกม การชน ไอเทม และการเปลี่ยนด่านในหนึ่งเฟรม */
    private void update(double frameScale) {
        double deltaSeconds = frameScale / 60.0;
        if (statusSeconds > 0) {
            statusSeconds = Math.max(0, statusSeconds - deltaSeconds);
            if (statusSeconds == 0) status = "";
        }
        if (gameOverAnimationSeconds >= 0) {
            gameOverAnimationSeconds += deltaSeconds;
            if (gameOverAnimationSeconds >= GAME_OVER_EXPLOSION_SECONDS) {
                stop();
                onStateChange.accept(session);
            }
            return;
        }
        if (session.getState() != GameState.PLAYING) return;
        if (weatherPopup != null && weatherPopup.isVisible()) {
            weatherPopup.update(deltaSeconds);
        }

        if (keysDown.contains(KeyCode.LEFT) || keysDown.contains(KeyCode.A)) {
            paddle.moveLeft(frameScale);
        }
        if (keysDown.contains(KeyCode.RIGHT) || keysDown.contains(KeyCode.D)) {
            paddle.moveRight(frameScale);
        }
        if (!balls.isEmpty() && !balls.get(0).isLaunched()) {
            positionDockedBalls();
        } else {
            updateBalls(frameScale);
        }

        updateBeamDrops(frameScale);
        updateActiveBeam(deltaSeconds);
        updateMeteors(deltaSeconds);
        if (gameOverAnimationSeconds >= 0) return;
        if (session.getState() == GameState.PLAYING && session.getStage().isCleared()) {
            clearMeteors();
            if (GameErrorHandler.run(session::clearStage)) {
                stop();
                onStateChange.accept(session);
            } else {
                stop();
                status = "GAME ERROR - SEE LOG";
            }
            return;
        }
        if (balls.isEmpty()) loseBallLife();
        hitFlashes.replaceAll((brick, time) -> Math.max(0, time - deltaSeconds));
        hitFlashes.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }
    /** จัดบอลที่ยังไม่ปล่อยให้อยู่เหนือไม้พาย */
    private void positionDockedBalls() {
        double centerX = paddle.getX() + paddle.getWidth() / 2 - 7;
        for (int i = 0; i < balls.size(); i++) {
            double offset = (i - (balls.size() - 1) / 2.0) * 18;
            balls.get(i).reset(centerX + offset, GameBounds.BALL_START_Y);
        }
    }
    /** อัปเดตบอลทั้งหมดและนำบอลที่หลุดสนามออก */
    private void updateBalls(double frameScale) {
        Iterator<Ball> iterator = balls.iterator();
        while (iterator.hasNext()) {
            Ball ball = iterator.next();
            updateBall(ball, frameScale);
            if (ball.getY() > FIELD_HEIGHT) {
                piercingContacts.remove(ball);
                iterator.remove();
            }
            if (session.getState() != GameState.PLAYING) return;
        }

    }
    /** เคลื่อนบอลและจัดการการชนกับขอบสนาม ไม้พาย และอิฐ */
    private void updateBall(Ball ball, double frameScale) {
        ball.move(frameScale);
        ball.bounceOffWalls(8, GameRenderer.HUD_HEIGHT + 8, FIELD_WIDTH - 8, FIELD_HEIGHT);
        if (ball.bounceOffPaddle(paddle)) {
            session.resetCombo();
            GameLogger.playerAction("Ball bounced off paddle");
        }

        Set<Brick> contacts = piercingContacts.computeIfAbsent(ball, ignored -> new HashSet<>());
        contacts.removeIf(brick -> !ball.intersects(brick));
        boolean piercing = session.hasUpgrade(UpgradeType.PIERCING_BALL);
        for (Brick brick : session.getStage().getBricks()) {
            if (brick.isDestroyed() || !ball.intersects(brick)) continue;
            if (piercing) {
                if (!contacts.add(brick)) continue;
            } else if (!ball.bounceOffBrick(brick)) {
                continue;
            }
            session.recordBrickHit();
            damageBrick(brick, true);
            break;
        }
    }
    /** ทำให้อิฐเสียพลังชีวิต เพิ่มคะแนนเมื่อแตก และอาจสร้างไอเทมลำแสง */
    private void damageBrick(Brick brick, boolean canDropPowerUp) {
        hitFlashes.put(brick, 0.16);
        AudioManager.getInstance().playSfx(AudioManager.BRICK_HIT);
        boolean destroyed = brick.hit();
        GameLogger.playerAction(destroyed ? "Brick destroyed" : "Brick hit");
        if (!destroyed) return;
        session.getStage().recordBrickDestroyed();
        if (!GameErrorHandler.run(() -> session.addScore(brick.getScoreValue()))) {
            stop();
            status = "GAME ERROR - SEE LOG";
            return;
        }
        if (canDropPowerUp && !session.getStage().isCleared()
                && Math.random() < BEAM_DROP_CHANCE) {
            beamDrops.add(new Point2D(brick.getX() + brick.getWidth() / 2 - 14, brick.getY()));
        }
    }
    /** เคลื่อนไอเทมลำแสงและตรวจจับการเก็บด้วยไม้พาย */
    private void updateBeamDrops(double frameScale) {
        for (int i = beamDrops.size() - 1; i >= 0; i--) {
            Point2D drop = beamDrops.get(i).add(0, 2.2 * frameScale);
            beamDrops.set(i, drop);
            boolean caught = drop.getY() + 28 >= paddle.getY()
                    && drop.getY() <= paddle.getY() + paddle.getHeight()
                    && drop.getX() + 28 >= paddle.getX()
                    && drop.getX() <= paddle.getX() + paddle.getWidth();
            if (caught) {
                beamSeconds = BEAM_DAMAGE_SECONDS;
                beamTickTimer = 0;
                beamTicksRemaining = 10;
                beamDrops.remove(i);
                status = "LASER BEAM!";
                statusSeconds = 1.5;
                GameLogger.playerAction("Collected beam power-up; 10 laser damage pulses activated");
            } else if (drop.getY() > FIELD_HEIGHT) {
                beamDrops.remove(i);
            }
        }
    }
    /** อัปเดตอุกกาบาตและให้ช่วงคุ้มกันชั่วคราวเมื่อชนไม้พาย */
    private void updateMeteors(double deltaSeconds) {
        meteorHitGuard.update(deltaSeconds);
        WeatherEvent event = session.getWeatherEvent().orElse(null);
        if (event == null || !event.isActive() || event.getType() != WeatherType.METEOR_SHOWER) {
            clearMeteors();
            return;
        }

        boolean gameLaunched = !balls.isEmpty() && balls.get(0).isLaunched();
        Meteor spawned = meteorSpawner.update(deltaSeconds, FIELD_WIDTH, gameLaunched);
        if (spawned != null) {
            meteors.add(spawned);
            GameLogger.playerAction("Meteor spawned at x=" + Math.round(spawned.getX()));
        }
        for (int i = meteors.size() - 1; i >= 0; i--) {
            Meteor meteor = meteors.get(i);
            meteor.update(deltaSeconds);
            if (meteor.isBelowField(FIELD_HEIGHT)) {
                meteors.remove(i);
            } else if (meteorHitGuard.canBeHit() && meteor.intersects(paddle)) {
                clearMeteors();
                meteorHitGuard.start();
                GameLogger.warning("Meteor hit the paddle; invulnerable for one second");
                if (!GameErrorHandler.run(session::loseHalfLife)) {
                    stop();
                    status = "GAME ERROR - SEE LOG";
                } else if (session.getState() == GameState.GAME_OVER) {
                    gameOverAnimationSeconds = 0;
                    status = "";
                } else {
                    status = "METEOR HIT - HALF LIFE LOST";
                    statusSeconds = 1.5;
                }
                return;
            }
        }
    }
    /** ล้างอุกกาบาตที่กำลังแสดงและเริ่มเวลาปล่อยใหม่ */
    private void clearMeteors() {
        meteors.clear();
        meteorSpawner.reset();
    }
    /** อัปเดตเวลาลำแสงและสร้างจังหวะโจมตีตามกำหนด */
    private void updateActiveBeam(double deltaSeconds) {
        if (beamSeconds <= 0) return;
        beamSeconds = Math.max(0, beamSeconds - deltaSeconds);
        if (beamTicksRemaining == 0) return;
        if (beamTickTimer == 0) {
            fireBeamTick();
            beamTicksRemaining--;
            beamTickTimer = BEAM_TICK_SECONDS;
        }
        beamTickTimer -= deltaSeconds;
        while (beamTickTimer <= 0 && beamTicksRemaining > 0) {
            fireBeamTick();
            beamTicksRemaining--;
            beamTickTimer += BEAM_TICK_SECONDS;
        }
    }
    /** ทำให้อิฐที่อยู่ใต้หัวปล่อยลำแสงเสียพลังชีวิต */
    private void fireBeamTick() {
        double[] beamXs = BeamGeometry.emitterXPositions(paddle.getX(), paddle.getWidth());
        for (Brick brick : session.getStage().getBricks()) {
            if (brick.isDestroyed()) continue;
            boolean hitByBeam = false;
            for (double x : beamXs) {
                if (x >= brick.getX() && x <= brick.getX() + brick.getWidth()) {
                    hitByBeam = true;
                    break;
                }
            }
            if (hitByBeam) {
                damageBrick(brick, false);
            }
        }
    }
    /** ลดชีวิตเมื่อบอลหมดและเริ่มรอบบอลใหม่หรือจบเกม */
    private void loseBallLife() {
        if (!GameErrorHandler.run(session::loseLife)) {
            stop();
            status = "GAME ERROR - SEE LOG";
        } else if (session.getState() == GameState.GAME_OVER) {
            clearMeteors();
            gameOverAnimationSeconds = 0;
            status = "";
        } else {
            clearMeteors();
            resetBalls();
            status = "LIFE LOST - PRESS SPACE";
        }
    }
    /** สร้างบอลใหม่ตามจำนวนที่อัปเกรดมัลติบอลกำหนด */
    private void resetBalls() {
        balls.clear();
        int count = 1 + session.getAdditionalBallCount();
        double width = 108 * session.getPaddleWidthMultiplier();
        double centerX = paddle.getX() + paddle.getWidth() / 2;
        for (int i = 0; i < count; i++) {
            double offset = (i - (count - 1) / 2.0) * 18;
            balls.add(new Ball(centerX + offset - 7, GameBounds.BALL_START_Y, 14));
        }
    }
    /** ส่งข้อมูลปัจจุบันให้ renderer วาดลงบน Canvas */
    private void render() {
        renderer.draw(canvas, viewportScale, session, balls, paddle.getX(), paddle.getWidth(),
                hitFlashes, beamDrops, beamSeconds, gameOverAnimationSeconds,
                status, meteors, session.getWeatherEvent().orElse(null),
                weatherPopup, meteorHitGuard.isInvulnerable());
    }
    /** หยุดลูปแอนิเมชันโดยป้องกันการหยุดซ้ำ */
    public void stop() {
        if (!stopped) {
            loop.stop();
            stopped = true;
        }
    }
    /** จับภาพเฟรมเกมปัจจุบันเพื่อใช้เป็นพื้นหลังหน้าอัปเกรด */
    public WritableImage snapshotGameplay() {
        return canvas.snapshot(null, null);
    }
}
