package se233.gamedev.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Map;

/** วาดข้อความอักษรละ 5x7 พิกเซล เพื่อให้ HUD ไม่พึ่งฟอนต์เรียบของระบบ */
public final class PixelFont {
    private static final Map<Character, String[]> GLYPHS = Map.ofEntries(
            Map.entry('A', glyph("01110", "10001", "10001", "11111", "10001", "10001", "10001")),
            Map.entry('B', glyph("11110", "10001", "10001", "11110", "10001", "10001", "11110")),
            Map.entry('C', glyph("01111", "10000", "10000", "10000", "10000", "10000", "01111")),
            Map.entry('D', glyph("11110", "10001", "10001", "10001", "10001", "10001", "11110")),
            Map.entry('E', glyph("11111", "10000", "10000", "11110", "10000", "10000", "11111")),
            Map.entry('F', glyph("11111", "10000", "10000", "11110", "10000", "10000", "10000")),
            Map.entry('G', glyph("01111", "10000", "10000", "10111", "10001", "10001", "01111")),
            Map.entry('H', glyph("10001", "10001", "10001", "11111", "10001", "10001", "10001")),
            Map.entry('I', glyph("11111", "00100", "00100", "00100", "00100", "00100", "11111")),
            Map.entry('J', glyph("00111", "00010", "00010", "00010", "10010", "10010", "01100")),
            Map.entry('K', glyph("10001", "10010", "10100", "11000", "10100", "10010", "10001")),
            Map.entry('L', glyph("10000", "10000", "10000", "10000", "10000", "10000", "11111")),
            Map.entry('M', glyph("10001", "11011", "10101", "10101", "10001", "10001", "10001")),
            Map.entry('N', glyph("10001", "11001", "10101", "10011", "10001", "10001", "10001")),
            Map.entry('O', glyph("01110", "10001", "10001", "10001", "10001", "10001", "01110")),
            Map.entry('P', glyph("11110", "10001", "10001", "11110", "10000", "10000", "10000")),
            Map.entry('Q', glyph("01110", "10001", "10001", "10001", "10101", "10010", "01101")),
            Map.entry('R', glyph("11110", "10001", "10001", "11110", "10100", "10010", "10001")),
            Map.entry('S', glyph("01111", "10000", "10000", "01110", "00001", "00001", "11110")),
            Map.entry('T', glyph("11111", "00100", "00100", "00100", "00100", "00100", "00100")),
            Map.entry('U', glyph("10001", "10001", "10001", "10001", "10001", "10001", "01110")),
            Map.entry('V', glyph("10001", "10001", "10001", "10001", "10001", "01010", "00100")),
            Map.entry('W', glyph("10001", "10001", "10001", "10101", "10101", "10101", "01010")),
            Map.entry('X', glyph("10001", "10001", "01010", "00100", "01010", "10001", "10001")),
            Map.entry('Y', glyph("10001", "10001", "01010", "00100", "00100", "00100", "00100")),
            Map.entry('Z', glyph("11111", "00001", "00010", "00100", "01000", "10000", "11111")),
            Map.entry('0', glyph("01110", "10001", "10011", "10101", "11001", "10001", "01110")),
            Map.entry('1', glyph("00100", "01100", "00100", "00100", "00100", "00100", "01110")),
            Map.entry('2', glyph("01110", "10001", "00001", "00010", "00100", "01000", "11111")),
            Map.entry('3', glyph("11110", "00001", "00001", "01110", "00001", "00001", "11110")),
            Map.entry('4', glyph("00010", "00110", "01010", "10010", "11111", "00010", "00010")),
            Map.entry('5', glyph("11111", "10000", "10000", "11110", "00001", "00001", "11110")),
            Map.entry('6', glyph("01110", "10000", "10000", "11110", "10001", "10001", "01110")),
            Map.entry('7', glyph("11111", "00001", "00010", "00100", "01000", "01000", "01000")),
            Map.entry('8', glyph("01110", "10001", "10001", "01110", "10001", "10001", "01110")),
            Map.entry('9', glyph("01110", "10001", "10001", "01111", "00001", "00001", "01110")),
            Map.entry(':', glyph("00000", "00100", "00100", "00000", "00100", "00100", "00000")),
            Map.entry('/', glyph("00001", "00010", "00010", "00100", "01000", "01000", "10000")),
            Map.entry('-', glyph("00000", "00000", "00000", "11111", "00000", "00000", "00000")),
            Map.entry('+', glyph("00000", "00100", "00100", "11111", "00100", "00100", "00000")),
            Map.entry('.', glyph("00000", "00000", "00000", "00000", "00000", "00110", "00110")),
            Map.entry('%', glyph("11001", "11010", "00100", "01000", "10110", "00110", "00000")),
            Map.entry(' ', glyph("00000", "00000", "00000", "00000", "00000", "00000", "00000"))
    );

    /** ป้องกันการสร้างออบเจ็กต์จากคลาสวาดตัวอักษร */
    private PixelFont() {
    }

    /** วาดข้อความด้วยกริดพิกเซลและระยะห่างคงที่ โดยไม่ interpolate รูปทรง */
    public static void draw(GraphicsContext gc, String text, double x, double y,
                            int pixelSize, Color color) {
        gc.setFill(color);
        double cursor = x;
        for (char raw : text.toUpperCase().toCharArray()) {
            String[] rows = GLYPHS.getOrDefault(raw, GLYPHS.get(' '));
            for (int row = 0; row < rows.length; row++) {
                for (int column = 0; column < rows[row].length(); column++) {
                    if (rows[row].charAt(column) == '1') {
                        gc.fillRect(cursor + column * pixelSize, y + row * pixelSize,
                                pixelSize, pixelSize);
                    }
                }
            }
            cursor += 6.0 * pixelSize;
        }
    }

    /** วาดตัวอักษรโดยแทนทุกพิกเซลที่ติดสว่างด้วยก้อนอิฐที่ตกลงมาเป็นลำดับ */
    public static void drawBricked(GraphicsContext gc, String text, double x, double y,
                                   int brickSize, double introProgress,
                                   Color face, Color shadow) {
        double progress = Math.max(0, Math.min(1, introProgress));
        for (int character = 0; character < text.length(); character++) {
            String[] rows = GLYPHS.getOrDefault(Character.toUpperCase(text.charAt(character)),
                    GLYPHS.get(' '));
            double delay = (character * 37 % 9) / 36.0;
            double local = Math.max(0, Math.min(1, (progress - delay) / (1 - delay)));
            double fall = (1 - local) * 52;
            double characterX = x + character * 6.0 * brickSize;
            for (int row = 0; row < rows.length; row++) {
                for (int column = 0; column < rows[row].length(); column++) {
                    if (rows[row].charAt(column) != '1') continue;
                    double bx = characterX + column * brickSize;
                    double by = y + row * brickSize + fall;
                    gc.setFill(shadow);
                    gc.fillRect(bx - 1, by - 1, brickSize, brickSize);
                    gc.setFill(face);
                    gc.fillRect(bx, by, brickSize - 1, brickSize - 1);
                }
            }
        }
    }

    /** สร้างข้อมูลบิตของตัวอักษรหนึ่งตัวจากแถวกว้างห้าพิกเซล */
    /** คืนรูปแบบแถวพิกเซลสำหรับตัวอักษรหนึ่งตัว */
    private static String[] glyph(String... rows) {
        return rows;
    }
}
