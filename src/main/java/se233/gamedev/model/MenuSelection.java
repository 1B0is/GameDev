package se233.gamedev.model;

/** จัดการรายการเมนูที่เลือกและการเลื่อนวนโดยไม่พึ่ง JavaFX */
public final class MenuSelection {
    public enum Action { START, HOW_TO_PLAY, EXIT }

    private int selectedIndex;

    /** เลื่อนรายการที่เลือกหนึ่งตำแหน่งและวนกลับเมื่อถึงปลายรายการ */
    public Action move(int direction) {
        selectedIndex = Math.floorMod(selectedIndex + Integer.signum(direction),
                Action.values().length);
        return selected();
    }

    /** คืนคำสั่งของรายการเมนูที่เลือกอยู่ */
    public Action selected() { return Action.values()[selectedIndex]; }
    /** คืนดัชนีของรายการเมนูที่เลือกอยู่ */
    public int getSelectedIndex() { return selectedIndex; }
}
