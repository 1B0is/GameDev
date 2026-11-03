package se233.gamedev.view;

import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;

/** โหลดแบบอักษรพิกเซลที่แนบมากับเกมสำหรับชื่อและข้อความ */
public final class PixelFonts {
    public static final Font DISPLAY = load("/fonts/Jersey10-Regular.ttf", 28);
    public static final Font BODY = load("/fonts/VT323-Regular.ttf", 24);

    /** ป้องกันการสร้างออบเจ็กต์จากคลาสรวมแบบอักษร */
    private PixelFonts() {
    }

    /** โหลดแบบอักษรจากทรัพยากรและกำหนดขนาดที่ต้องการ */
    private static Font load(String path, double size) {
        try (InputStream stream = PixelFonts.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing bundled pixel font: " + path);
            Font font = Font.loadFont(stream, size);
            if (font == null) throw new IllegalStateException("Unable to load bundled pixel font: " + path);
            return font;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read bundled pixel font: " + path, exception);
        }
    }
}
