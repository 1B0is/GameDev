package se233.gamedev.view;

/** กำหนดสัญญาให้หน้าจอที่มี Canvas ปรับขนาดตามสเกล viewport ได้ */
public interface ScaleAwareScreen {
    /** รับค่าสเกลระหว่างหน้าต่างจริงกับหน้าจอเชิงตรรกะ */
    void setViewportScale(double scale);
}
