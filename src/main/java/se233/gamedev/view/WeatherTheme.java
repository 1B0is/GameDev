package se233.gamedev.view;

import javafx.scene.paint.Color;
import se233.gamedev.model.WeatherType;

/** ชุดสีหลักสำหรับปรับบรรยากาศของฉากตามสภาพอากาศ */
public record WeatherTheme(Color background, Color fieldTint, Color hud,
                           Color accent, Color brickTint, Color popup,
                           Color popupText, Color popupBorder, Color popupSecondary,
                           Color popupStripeLight, Color popupStripeDark,
                           Color popupGlow, Color popupOverlay) {
    public static final WeatherTheme NORMAL = new WeatherTheme(
            Color.web("#050b1e"), Color.color(0.01, 0.02, 0.08, 0.20),
            Color.web("#080d25"), Color.web("#aeb6c4"),
            Color.color(1, 1, 1, 0), Color.web("#111a32"),
            Color.web("#f4f4f4"), Color.web("#aeb6c4"), Color.web("#777f8d"),
            Color.web("#c8cbd0"), Color.web("#353941"), Color.web("#d7dce2"),
            Color.web("#000000"));
    public static final WeatherTheme FOG = new WeatherTheme(
            Color.web("#11151c"), Color.color(0.5, 0.52, 0.56, 0.30),
            Color.web("#20242b"), Color.web("#d7dce2"),
            Color.color(0.8, 0.83, 0.88, 0.30), Color.web("#252a31"),
            Color.web("#f2f2f2"), Color.web("#d7dce2"), Color.web("#aeb4bc"),
            Color.web("#d7dce2"), Color.web("#34383e"), Color.web("#f2f2f2"),
            Color.web("#050609"));
    public static final WeatherTheme METEOR = new WeatherTheme(
            Color.web("#080d2c"), Color.color(0.25, 0.15, 0.60, 0.25),
            Color.web("#11183e"), Color.web("#ed70ff"),
            Color.color(0.45, 0.24, 0.95, 0.38), Color.web("#1a1644"),
            Color.web("#f7ecff"), Color.web("#438bff"), Color.web("#8551df"),
            Color.web("#ff79d9"), Color.web("#552f9b"), Color.web("#ec63ff"),
            Color.web("#030413"));

    /** เลือกชุดสีของสภาพอากาศที่ทำงาน หรือคืนธีมปกติเมื่อไม่มีเหตุการณ์ */
    public static WeatherTheme forWeather(WeatherType weather) {
        if (weather == WeatherType.FOG_VALLEY) return FOG;
        if (weather == WeatherType.METEOR_SHOWER) return METEOR;
        return NORMAL;
    }
}
