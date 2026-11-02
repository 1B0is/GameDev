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

    public Action selected() { return Action.values()[selectedIndex]; }
    public int getSelectedIndex() { return selectedIndex; }
}
