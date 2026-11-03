package se233.gamedev.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** จัดการการเลือกอัปเกรด เวลาแอนิเมชัน และเงื่อนไขการยืนยันโดยไม่พึ่ง JavaFX */
public final class UpgradeSelectionState {
    public static final double INTRO_DURATION_SECONDS = 0.40;
    public static final double REROLL_DURATION_SECONDS = 0.25;
    private List<Upgrade> offers;
    private List<Upgrade> pendingOffers;
    private int selectedIndex;
    private double elapsedSeconds;
    private double rerollElapsedSeconds;
    private boolean confirmed;

    /** สร้างสถานะการเลือกจากรายการอัปเกรดที่เสนอ */
    public UpgradeSelectionState(List<Upgrade> offers) {
        this.offers = distinctOffers(offers);
    }

    /** อัปเดตเวลาการแสดงการ์ดและแอนิเมชันสุ่มใหม่ */
    public void update(double dtSeconds) {
        if (!Double.isFinite(dtSeconds) || dtSeconds < 0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        elapsedSeconds += dtSeconds;
        if (pendingOffers != null) {
            rerollElapsedSeconds += dtSeconds;
            if (rerollElapsedSeconds >= REROLL_DURATION_SECONDS) {
                offers = pendingOffers;
                pendingOffers = null;
                rerollElapsedSeconds = REROLL_DURATION_SECONDS;
                selectedIndex = 0;
            }
        }
    }

    /** เลื่อนการเลือกหนึ่งการ์ดและวนกลับเมื่อถึงขอบรายการ */
    public void move(int direction) {
        if (!isInputEnabled() || offers.isEmpty()) return;
        selectedIndex = Math.floorMod(selectedIndex + Integer.signum(direction), offers.size());
    }

    /** เลือกการ์ดตามดัชนีเมื่อเปิดรับการควบคุม */
    public void select(int index) {
        if (!isInputEnabled() || offers.isEmpty()) return;
        selectedIndex = Math.floorMod(index, offers.size());
    }

    /** ยืนยันอัปเกรดที่เลือกได้ครั้งเดียวเมื่อเปิดรับการควบคุม */
    public Upgrade confirm() {
        if (!isInputEnabled() || confirmed || offers.isEmpty()) return null;
        confirmed = true;
        return offers.get(selectedIndex);
    }

    /** เริ่มแอนิเมชันสุ่มใหม่และเตรียมข้อเสนอทดแทนที่ไม่ซ้ำกัน */
    public boolean reroll(List<Upgrade> replacements) {
        if (!isInputEnabled() || confirmed || pendingOffers != null) return false;
        pendingOffers = distinctOffers(replacements);
        rerollElapsedSeconds = 0;
        return true;
    }

    /** คืนรายการอัปเกรดที่แสดงอยู่ */
    public List<Upgrade> getOffers() { return offers; }
    /** คืนดัชนีการ์ดที่ถูกเลือก */
    public int getSelectedIndex() { return selectedIndex; }
    /** ตรวจว่าสามารถรับคำสั่งจากผู้เล่นได้หรือไม่ */
    public boolean isInputEnabled() {
        return elapsedSeconds >= INTRO_DURATION_SECONDS && pendingOffers == null && !confirmed;
    }
    /** คืนความคืบหน้าการปรากฏของการ์ดในช่วงศูนย์ถึงหนึ่ง */
    public double getIntroProgress() {
        return Math.max(0, Math.min(1, elapsedSeconds / INTRO_DURATION_SECONDS));
    }
    /** คืนเวลาที่ผ่านไปนับจากเริ่มแสดงหน้าจอ */
    public double getElapsedSeconds() { return elapsedSeconds; }
    /** ตรวจว่ากำลังแสดงแอนิเมชันสุ่มข้อเสนอใหม่หรือไม่ */
    public boolean isRerolling() { return pendingOffers != null; }
    /** คืนความคืบหน้าการสุ่มใหม่ในช่วงศูนย์ถึงหนึ่ง */
    public double getRerollProgress() {
        return pendingOffers == null ? 0
                : Math.max(0, Math.min(1, rerollElapsedSeconds / REROLL_DURATION_SECONDS));
    }

    /** ตรวจและคัดลอกรายการข้อเสนอ พร้อมปฏิเสธอัปเกรดที่ซ้ำกัน */
    private static List<Upgrade> distinctOffers(List<Upgrade> offered) {
        List<Upgrade> copy = List.copyOf(Objects.requireNonNull(offered, "offers"));
        if (new HashSet<>(copy).size() != copy.size()) {
            throw new IllegalArgumentException("Upgrade choices must be distinct");
        }
        return copy;
    }
}
