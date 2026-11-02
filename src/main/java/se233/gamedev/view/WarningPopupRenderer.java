package se233.gamedev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import se233.gamedev.model.GameBounds;
import se233.gamedev.model.WeatherType;

/** วาดหน้าต่างเตือนสภาพอากาศแบบพิกเซลโดยใช้ชุดสีของอากาศปัจจุบัน */
public final class WarningPopupRenderer {
    public static final double WIDTH_RATIO = 0.60;
    public static final double HEIGHT_RATIO = 0.36;
    public static final double OVERLAY_ALPHA = 0.55;
    public static final double STRIPE_SCROLL_PIXELS_PER_SECOND = 12;
    public static final double HEADER_BLINK_HERTZ = 2;
    public static final double GLITCH_INTERVAL_SECONDS = 1.8;
    public static final double FLICKER_MIN_INTERVAL_SECONDS = 0.5;
    public static final double FLICKER_MAX_INTERVAL_SECONDS = 2.0;

    private static final double WIDTH = GameBounds.FIELD_WIDTH * WIDTH_RATIO;
    private static final double HEIGHT = GameBounds.FIELD_HEIGHT * HEIGHT_RATIO;
    private static final double STRIPE_HEIGHT = 9;
    private static final double STRIPE_WIDTH = 18;

    private WarningPopupRenderer() {
    }

    /** วาดฉากมืด หน้าต่างเตือน รายละเอียด และเอฟเฟกต์ตามเวลาจำลอง */
    public static void draw(GraphicsContext gc, WeatherType weather, WeatherTheme theme,
                            WarningPopupState state, Image fogImage, Image meteorImage,
                            int[] meteorBounds) {
        if (state == null || !state.isVisible()) return;
        double elapsed = state.getElapsedSeconds();
        double progress = state.progress();
        double scale = state.getPhase() == WarningPopupState.Phase.ENTER
                ? 0.70 + 0.30 * easeOutBack(progress)
                : state.getPhase() == WarningPopupState.Phase.EXIT
                ? 0.76 + 0.24 * progress : 1;
        double alpha = state.getPhase() == WarningPopupState.Phase.ENTER
                ? progress : state.getPhase() == WarningPopupState.Phase.EXIT ? progress : 1;
        double shakeX = state.getPhase() == WarningPopupState.Phase.ENTER && elapsed < 0.12
                ? Math.sin(elapsed * 105) * 1.5 : 0;
        double shakeY = state.getPhase() == WarningPopupState.Phase.ENTER && elapsed < 0.12
                ? Math.cos(elapsed * 91) : 0;
        double x = (GameBounds.FIELD_WIDTH - WIDTH) / 2;
        double y = (GameBounds.HUD_HEIGHT + GameBounds.FIELD_HEIGHT - HEIGHT) / 2;

        gc.save();
        gc.setImageSmoothing(false);
        gc.setFill(withAlpha(theme.popupOverlay(), OVERLAY_ALPHA));
        gc.fillRect(0, GameBounds.HUD_HEIGHT, GameBounds.FIELD_WIDTH,
                GameBounds.FIELD_HEIGHT - GameBounds.HUD_HEIGHT);
        if (state.getPhase() == WarningPopupState.Phase.ENTER && elapsed < 0.075) {
            gc.setFill(withAlpha(theme.popupText(), 0.16 * (1 - elapsed / 0.075)));
            gc.fillRect(0, GameBounds.HUD_HEIGHT, GameBounds.FIELD_WIDTH,
                    GameBounds.FIELD_HEIGHT - GameBounds.HUD_HEIGHT);
        }

        gc.translate(GameBounds.FIELD_WIDTH / 2 + shakeX, GameBounds.FIELD_HEIGHT / 2 + shakeY);
        gc.scale(scale, scale);
        gc.translate(-GameBounds.FIELD_WIDTH / 2, -GameBounds.FIELD_HEIGHT / 2);
        gc.setGlobalAlpha(alpha);
        drawPanel(gc, x, y, theme, elapsed, weather, fogImage, meteorImage,
                meteorBounds, alpha);
        gc.restore();
    }

    /** วาดพื้นหลัง กรอบ แถบเตือน ข้อความ และรายละเอียดของหน้าต่าง */
    private static void drawPanel(GraphicsContext gc, double x, double y, WeatherTheme theme,
                                  double elapsed, WeatherType weather, Image fogImage,
                                  Image meteorImage, int[] meteorBounds, double panelAlpha) {
        boolean fog = weather == WeatherType.FOG_VALLEY;
        boolean flicker = fog && isFlicker(elapsed);
        double flickerAlpha = flicker ? 0.48 : 1;
        gc.save();
        gc.setFill(theme.popup());
        gc.fillRect(x, y, WIDTH, HEIGHT);
        gc.setStroke(withAlpha(theme.popupBorder(), flickerAlpha));
        gc.setLineWidth(3);
        gc.strokeRect(x + 2, y + 2, WIDTH - 4, HEIGHT - 4);

        drawPanelAtmosphere(gc, x, y, theme, elapsed, fog, fogImage,
                meteorImage, meteorBounds, panelAlpha);
        drawHazardStripes(gc, x, y, theme, elapsed, fog);
        drawHeader(gc, x, y, theme, elapsed, flickerAlpha);
        drawWeatherTitle(gc, x, y, theme, elapsed, weather, flickerAlpha, panelAlpha);
        drawEffect(gc, x, y, theme, fog, flickerAlpha);
        drawScoreBadge(gc, x, y, theme, elapsed);
        drawFooter(gc, x, y, theme, elapsed, flickerAlpha);
        drawScanlines(gc, x, y, theme);
        if (fog) drawVignette(gc, x, y, theme);
        gc.restore();
    }

    /** วาดหมอกหรือดาวและอุกกาบาตภายในกรอบหน้าต่าง */
    private static void drawPanelAtmosphere(GraphicsContext gc, double x, double y,
                                            WeatherTheme theme, double elapsed, boolean fog,
                                            Image fogImage, Image meteorImage,
                                            int[] meteorBounds, double panelAlpha) {
        gc.save();
        gc.beginPath();
        gc.rect(x + 5, y + 7, WIDTH - 10, HEIGHT - 14);
        gc.closePath();
        gc.clip();
        if (fog) {
            if (fogImage != null && !fogImage.isError()) {
                double drift = (elapsed * 10) % (WIDTH + 120);
                gc.setGlobalAlpha(panelAlpha * 0.14);
                gc.drawImage(fogImage, x - 90 + drift, y + 68, 210, 95);
                gc.drawImage(fogImage, x + WIDTH - drift, y + 125, 190, 80);
                gc.setGlobalAlpha(panelAlpha);
            }
            drawFogEyes(gc, x, y, theme, elapsed);
        } else {
            drawStars(gc, x, y, theme, elapsed);
            drawPopupMeteors(gc, x, y, theme, elapsed, meteorImage, meteorBounds);
        }
        gc.restore();
    }

    /** วาดดวงตาคู่ที่ปรากฏและกะพริบเป็นช่วงในหมอก */
    private static void drawFogEyes(GraphicsContext gc, double x, double y,
                                    WeatherTheme theme, double elapsed) {
        double cycle = elapsed % 8.5;
        double opacity = cycle < 3.8 || cycle > 4.65 ? 0
                : Math.min(1, Math.min((cycle - 3.8) / 0.18, (4.65 - cycle) / 0.25));
        if (cycle > 4.25 && cycle < 4.38) opacity = 0;
        if (opacity <= 0) return;
        int cycleIndex = (int) (elapsed / 8.5);
        int corner = (cycleIndex * 17 + cycleIndex * cycleIndex * 13 + 7) & 3;
        double eyeX = corner % 2 == 0 ? x + 38 : x + WIDTH - 48;
        double eyeY = corner < 2 ? y + 90 : y + HEIGHT - 85;
        gc.setFill(withAlpha(theme.popupText(), opacity * 0.9));
        gc.fillRect(eyeX, eyeY, 3, 3);
        gc.fillRect(eyeX + 9, eyeY, 3, 3);
    }

    /** วาดดาวพิกเซลที่เปลี่ยนความสว่างเป็นจังหวะ */
    private static void drawStars(GraphicsContext gc, double x, double y,
                                  WeatherTheme theme, double elapsed) {
        for (int i = 0; i < 24; i++) {
            double sx = x + 14 + (i * 73 % (int) (WIDTH - 28));
            double sy = y + 18 + (i * 41 % (int) (HEIGHT - 36));
            double twinkle = 0.25 + 0.65 * (0.5 + 0.5 * Math.sin(elapsed * 4 + i * 1.7));
            gc.setFill(withAlpha(theme.popupText(), twinkle));
            int size = i % 5 == 0 ? 3 : 2;
            gc.fillRect(sx, sy, size, size);
        }
    }

    /** วาดอุกกาบาตขนาดเล็กพร้อมเส้นทางพุ่งเฉียงในหน้าต่าง */
    private static void drawPopupMeteors(GraphicsContext gc, double x, double y,
                                         WeatherTheme theme, double elapsed, Image image,
                                         int[] bounds) {
        for (int i = 0; i < 3; i++) {
            double cycle = (elapsed * (0.42 + i * 0.08) + i * 0.39) % 1;
            double mx = x + 34 + cycle * (WIDTH - 100);
            double my = y + 40 + (cycle * 115) % (HEIGHT - 60);
            gc.setStroke(withAlpha(theme.popupGlow(), 0.65));
            gc.setLineWidth(3);
            gc.strokeLine(mx - 22, my - 22, mx - 3, my - 3);
            if (image != null && !image.isError() && bounds != null) {
                gc.drawImage(image, bounds[0], bounds[1], bounds[2], bounds[3],
                        mx - 5, my - 5, 12, 12);
            } else {
                gc.setFill(theme.popupStripeLight());
                gc.fillPolygon(new double[]{mx, mx + 7, mx + 3, mx - 2},
                        new double[]{my - 5, my, my + 6, my + 2}, 4);
            }
        }
    }

    /** วาดแถบเตือนบนและล่างที่เลื่อนตามเวลา */
    private static void drawHazardStripes(GraphicsContext gc, double x, double y,
                                          WeatherTheme theme, double elapsed, boolean fog) {
        double scroll = (elapsed * STRIPE_SCROLL_PIXELS_PER_SECOND) % STRIPE_WIDTH;
        for (double offset = -STRIPE_WIDTH; offset < WIDTH; offset += STRIPE_WIDTH) {
            double left = x + offset - scroll;
            Color stripe = ((int) Math.floor((offset + scroll) / STRIPE_WIDTH) & 1) == 0
                    ? theme.popupStripeLight() : theme.popupStripeDark();
            gc.setFill(stripe);
            gc.fillPolygon(new double[]{left, left + STRIPE_WIDTH / 2, left + STRIPE_WIDTH,
                            left + STRIPE_WIDTH / 2},
                    new double[]{y + 3, y + 3, y + STRIPE_HEIGHT, y + STRIPE_HEIGHT},
                    4);
            gc.fillPolygon(new double[]{left, left + STRIPE_WIDTH / 2, left + STRIPE_WIDTH,
                            left + STRIPE_WIDTH / 2},
                    new double[]{y + HEIGHT - STRIPE_HEIGHT, y + HEIGHT - 3,
                            y + HEIGHT - 3, y + HEIGHT - STRIPE_HEIGHT},
                    4);
        }
        if (!fog) {
            Color[] bands = {theme.popupBorder(), theme.popupSecondary(), theme.accent()};
            double bandWidth = (WIDTH - 16) / bands.length;
            for (int i = 0; i < bands.length; i++) {
                gc.setFill(bands[i]);
                gc.fillRect(x + 8 + i * bandWidth, y + 12, bandWidth, 3);
                gc.fillRect(x + 8 + i * bandWidth, y + HEIGHT - 15, bandWidth, 3);
            }
        }
    }

    /** วาดแถบหัวเรื่องและไอคอนเตือนที่กะพริบ */
    private static void drawHeader(GraphicsContext gc, double x, double y,
                                   WeatherTheme theme, double elapsed, double flickerAlpha) {
        double blink = 0.55 + 0.45 * Math.abs(Math.sin(elapsed * Math.PI * 2 * HEADER_BLINK_HERTZ));
        Color header = withAlpha(theme.popupBorder(), blink * flickerAlpha);
        double centerY = y + 35;
        drawWarningIcon(gc, x + 30, centerY - 7, theme, blink * flickerAlpha);
        drawWarningIcon(gc, x + WIDTH - 42, centerY - 7, theme, blink * flickerAlpha);
        String text = "WEATHER REPORT";
        PixelFont.draw(gc, text, x + (WIDTH - text.length() * 12) / 2,
                y + 24, 2, header);
        gc.setFill(withAlpha(theme.popupSecondary(), flickerAlpha));
        gc.fillRect(x + 24, y + 49, WIDTH - 48, 2);
    }

    /** วาดไอคอนสามเหลี่ยมเตือนพร้อมเครื่องหมายตกใจแบบพิกเซล */
    private static void drawWarningIcon(GraphicsContext gc, double x, double y,
                                        WeatherTheme theme, double alpha) {
        gc.setFill(withAlpha(theme.popupBorder(), alpha));
        gc.fillPolygon(new double[]{x + 8, x + 16, x, x + 8},
                new double[]{y, y + 15, y + 15, y}, 4);
        gc.setFill(withAlpha(theme.popup(), alpha));
        gc.fillRect(x + 7, y + 5, 2, 5);
        gc.fillRect(x + 7, y + 12, 2, 2);
    }

    /** วาดชื่อสภาพอากาศพร้อมแสง เงา glitch และการสั่นเล็กน้อย */
    private static void drawWeatherTitle(GraphicsContext gc, double x, double y,
                                         WeatherTheme theme, double elapsed, WeatherType weather,
                                         double flickerAlpha, double panelAlpha) {
        String title = weather == WeatherType.FOG_VALLEY ? "FOG VALLEY" : "METEOR SHOWER";
        int size = 3;
        double titleWidth = title.length() * 6.0 * size;
        double titleX = x + (WIDTH - titleWidth) / 2;
        double titleY = y + 72;
        boolean glitch = elapsed % GLITCH_INTERVAL_SECONDS < 0.13;
        double jitterX = weather == WeatherType.FOG_VALLEY
                ? (((int) (elapsed * 2.3) % 3) == 0 ? 1.5 : -1) : 0;
        if (weather == WeatherType.METEOR_SHOWER) {
            double glow = 0.24 + 0.16 * (0.5 + 0.5 * Math.sin(elapsed * 5));
            gc.setGlobalAlpha(glow * panelAlpha);
            PixelFont.draw(gc, title, titleX - 2, titleY, size, theme.popupGlow());
            gc.setGlobalAlpha(panelAlpha * glow);
            PixelFont.draw(gc, title, titleX + 2, titleY, size, theme.popupSecondary());
            gc.setGlobalAlpha(panelAlpha);
        }
        if (glitch) {
            gc.setGlobalAlpha(0.45 * panelAlpha);
            PixelFont.draw(gc, title, titleX - 2, titleY, size, theme.popupSecondary());
            gc.setGlobalAlpha(0.45 * panelAlpha);
            PixelFont.draw(gc, title, titleX + 2, titleY, size, theme.popupGlow());
            gc.setGlobalAlpha(panelAlpha);
            drawGlitchSlices(gc, title, titleX, titleY, size,
                    withAlpha(theme.popupText(), flickerAlpha), elapsed);
        } else {
            PixelFont.draw(gc, title, titleX + jitterX, titleY, size,
                    withAlpha(theme.popupText(), flickerAlpha));
        }
    }

    /** วาดชื่อเรื่องแยกเป็นแถบเพื่อสร้างอาการภาพคลาดเคลื่อน */
    private static void drawGlitchSlices(GraphicsContext gc, String title, double x, double y,
                                         int size, Color color, double elapsed) {
        double width = title.length() * 6.0 * size;
        for (int slice = 0; slice < 3; slice++) {
            double sliceY = y + slice * 8;
            double offset = ((int) (elapsed * 1000) + slice * 7) % 2 == 0 ? 2 : -2;
            gc.save();
            gc.beginPath();
            gc.rect(x - 4, sliceY, width + 8, 8);
            gc.closePath();
            gc.clip();
            PixelFont.draw(gc, title, x + offset, y, size, color);
            gc.restore();
        }
    }

    /** แสดงคำอธิบายผลของสภาพอากาศหนึ่งบรรทัด */
    private static void drawEffect(GraphicsContext gc, double x, double y,
                                   WeatherTheme theme, boolean fog, double flickerAlpha) {
        gc.setFill(withAlpha(theme.popupSecondary(), flickerAlpha));
        gc.fillRect(x + 42, y + 133, 5, 5);
        String line = fog ? "THICK FOG HIDES THE BRICKS. STAY SHARP."
                : "METEORS FALL EVERY SECOND. A HIT COSTS 0.5 LIFE.";
        PixelFont.draw(gc, line, x + 55, y + 132, 1,
                withAlpha(theme.popupText(), flickerAlpha));
    }

    /** วาดป้ายตัวคูณคะแนนที่ขยายและยุบเป็นจังหวะ */
    private static void drawScoreBadge(GraphicsContext gc, double x, double y,
                                      WeatherTheme theme, double elapsed) {
        double pulse = 1 + 0.05 * Math.sin(elapsed * 5);
        double badgeWidth = 160 * pulse;
        double badgeHeight = 28 * pulse;
        double badgeX = x + (WIDTH - badgeWidth) / 2;
        double badgeY = y + 158 + (28 - badgeHeight) / 2;
        gc.setFill(theme.popupStripeDark());
        gc.fillRect(badgeX - 3, badgeY - 3, badgeWidth + 6, badgeHeight + 6);
        gc.setStroke(theme.popupStripeLight());
        gc.setLineWidth(2);
        gc.strokeRect(badgeX, badgeY, badgeWidth, badgeHeight);
        gc.setFill(theme.popup());
        gc.fillRect(badgeX + 2, badgeY + 2, badgeWidth - 4, badgeHeight - 4);
        String badge = "SCORE X1.25";
        PixelFont.draw(gc, badge, x + (WIDTH - badge.length() * 12) / 2,
                badgeY + (badgeHeight - 14) / 2, 2, theme.popupStripeLight());
    }

    /** วาดข้อความคำสั่ง Space พร้อมกรอบปุ่มแบบคีย์แคป */
    private static void drawFooter(GraphicsContext gc, double x, double y,
                                   WeatherTheme theme, double elapsed, double flickerAlpha) {
        double blink = (0.45 + 0.55 * (0.5 + 0.5 * Math.sin(elapsed * Math.PI * 4)))
                * flickerAlpha;
        Color footerText = withAlpha(theme.popupText(), blink);
        double start = x + (WIDTH - 150) / 2;
        double baseline = y + HEIGHT - 25;
        PixelFont.draw(gc, "PRESS", start, baseline + 3, 1, footerText);
        double keyX = start + 35;
        gc.setFill(withAlpha(theme.popupStripeDark(), blink));
        gc.fillRect(keyX - 3, baseline, 40, 16);
        gc.setStroke(withAlpha(theme.popupBorder(), blink));
        gc.setLineWidth(2);
        gc.strokeRect(keyX - 3, baseline, 40, 16);
        PixelFont.draw(gc, "SPACE", keyX + 1, baseline + 5, 1, footerText);
        PixelFont.draw(gc, "TO LAUNCH", keyX + 44, baseline + 3, 1, footerText);
    }

    /** เพิ่มเส้นสแกนจาง ๆ ทับบริเวณภายในหน้าต่าง */
    private static void drawScanlines(GraphicsContext gc, double x, double y, WeatherTheme theme) {
        gc.setFill(withAlpha(theme.popupText(), 0.035));
        for (double lineY = y + 56; lineY < y + HEIGHT - 18; lineY += 4) {
            gc.fillRect(x + 5, lineY, WIDTH - 10, 1);
        }
    }

    /** ทำมุมหน้าต่างหมอกให้มืดลงด้วยบล็อกสีแบบขั้น */
    private static void drawVignette(GraphicsContext gc, double x, double y, WeatherTheme theme) {
        Color shadow = withAlpha(theme.popupOverlay(), 0.20);
        gc.setFill(shadow);
        for (int i = 0; i < 5; i++) {
            double inset = i * 4;
            gc.fillRect(x + inset, y + inset, 3, 3);
            gc.fillRect(x + WIDTH - inset - 3, y + inset, 3, 3);
            gc.fillRect(x + inset, y + HEIGHT - inset - 3, 3, 3);
            gc.fillRect(x + WIDTH - inset - 3, y + HEIGHT - inset - 3, 3, 3);
        }
    }

    /** คำนวณจังหวะไฟกะพริบของธีมหมอกแบบทำซ้ำได้ */
    private static boolean isFlicker(double elapsed) {
        double cursor = 0;
        for (int cycle = 0; cursor <= elapsed; cycle++) {
            long mixed = (cycle * 1103515245L + 12345L) & 0x7fffffffL;
            double interval = FLICKER_MIN_INTERVAL_SECONDS
                    + (mixed % 1501) / 1000.0;
            if (elapsed >= cursor && elapsed < cursor + 0.065) return true;
            cursor += interval;
        }
        return false;
    }

    /** คำนวณการขยายเข้าแบบมีจังหวะเด้งเกินเล็กน้อย */
    private static double easeOutBack(double progress) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        double shifted = progress - 1;
        return 1 + c3 * shifted * shifted * shifted + c1 * shifted * shifted;
    }

    /** สร้างสีจากพาเลตต์เดิมพร้อมค่าความทึบที่กำหนด */
    private static Color withAlpha(Color color, double alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                Math.max(0, Math.min(1, alpha)));
    }
}
