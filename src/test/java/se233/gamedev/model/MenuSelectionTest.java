package se233.gamedev.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuSelectionTest {
    @Test
    void navigationWrapsAndReturnsActionIdentifiers() {
        MenuSelection menu = new MenuSelection();
        assertEquals(MenuSelection.Action.START, menu.selected());
        assertEquals(MenuSelection.Action.EXIT, menu.move(-1));
        assertEquals(MenuSelection.Action.START, menu.move(1));
        assertEquals(MenuSelection.Action.HOW_TO_PLAY, menu.move(1));
    }
}
