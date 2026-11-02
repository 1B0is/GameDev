package se233.gamedev.model;

/** JavaFX-free menu selection state with wraparound navigation. */
public final class MenuSelection {
    public enum Action { START, HOW_TO_PLAY, EXIT }

    private int selectedIndex;

    /** Moves selection by one item and wraps at either end of the menu. */
    public Action move(int direction) {
        selectedIndex = Math.floorMod(selectedIndex + Integer.signum(direction),
                Action.values().length);
        return selected();
    }

    /** Returns the selected menu action identifier. */
    public Action selected() { return Action.values()[selectedIndex]; }
    /** Returns the current zero-based selection index. */
    public int getSelectedIndex() { return selectedIndex; }
}
