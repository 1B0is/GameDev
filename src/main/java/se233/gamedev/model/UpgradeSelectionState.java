package se233.gamedev.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** JavaFX-free selection, intro gate, confirmation guard, and reroll timing. */
public final class UpgradeSelectionState {
    public static final double INTRO_DURATION_SECONDS = 0.40;
    public static final double REROLL_DURATION_SECONDS = 0.25;
    private List<Upgrade> offers;
    private List<Upgrade> pendingOffers;
    private int selectedIndex;
    private double elapsedSeconds;
    private double rerollElapsedSeconds;
    private boolean confirmed;

    /** Creates selection state for the currently offered upgrades. */
    public UpgradeSelectionState(List<Upgrade> offers) {
        this.offers = distinctOffers(offers);
    }

    /** Advances intro and shuffle animations using elapsed simulation time. */
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

    /** Moves selection by one card, wrapping at both ends when input is enabled. */
    public void move(int direction) {
        if (!isInputEnabled() || offers.isEmpty()) return;
        selectedIndex = Math.floorMod(selectedIndex + Integer.signum(direction), offers.size());
    }

    /** Selects a specific card index when input is enabled. */
    public void select(int index) {
        if (!isInputEnabled() || offers.isEmpty()) return;
        selectedIndex = Math.floorMod(index, offers.size());
    }

    /** Confirms the highlighted upgrade once, or returns null when input is gated. */
    public Upgrade confirm() {
        if (!isInputEnabled() || confirmed || offers.isEmpty()) return null;
        confirmed = true;
        return offers.get(selectedIndex);
    }

    /** Begins a short reroll animation and installs distinct replacement offers at its end. */
    public boolean reroll(List<Upgrade> replacements) {
        if (!isInputEnabled() || confirmed || pendingOffers != null) return false;
        pendingOffers = distinctOffers(replacements);
        rerollElapsedSeconds = 0;
        return true;
    }

    /** Returns the current offers, retaining old cards while a reroll animation plays. */
    public List<Upgrade> getOffers() { return offers; }
    /** Returns the current highlighted card index. */
    public int getSelectedIndex() { return selectedIndex; }
    /** Returns whether the intro is complete and no transition is running. */
    public boolean isInputEnabled() {
        return elapsedSeconds >= INTRO_DURATION_SECONDS && pendingOffers == null && !confirmed;
    }
    /** Returns normalized progress through the card entrance animation. */
    public double getIntroProgress() {
        return Math.max(0, Math.min(1, elapsedSeconds / INTRO_DURATION_SECONDS));
    }
    /** Returns the elapsed screen time for ambient selection-card effects. */
    public double getElapsedSeconds() { return elapsedSeconds; }
    /** Returns whether a reroll animation is in progress. */
    public boolean isRerolling() { return pendingOffers != null; }
    /** Returns progress through the reroll animation from zero to one. */
    public double getRerollProgress() {
        return pendingOffers == null ? 0
                : Math.max(0, Math.min(1, rerollElapsedSeconds / REROLL_DURATION_SECONDS));
    }

    private static List<Upgrade> distinctOffers(List<Upgrade> offered) {
        List<Upgrade> copy = List.copyOf(Objects.requireNonNull(offered, "offers"));
        if (new HashSet<>(copy).size() != copy.size()) {
            throw new IllegalArgumentException("Upgrade choices must be distinct");
        }
        return copy;
    }
}
