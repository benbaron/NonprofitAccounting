package nonprofitbookkeeping.ui.actions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color; // For verifying setColor calls if desired, not strictly needed for current test

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // For verify if we were checking Graphics calls

@ExtendWith(MockitoExtension.class)
class HelpActionTest {

    private HelpAction helpIcon;

    @Mock
    private Graphics mockGraphics;

    @Mock
    private Component mockComponent; // Component 'c' parameter for paintIcon

    @BeforeEach
    void setUp() {
        helpIcon = new HelpAction();
    }

    @Test
    @DisplayName("getIconWidth: Should return the fixed width of 16")
    void testGetIconWidth() {
        assertEquals(16, helpIcon.getIconWidth(), "Icon width should be 16.");
    }

    @Test
    @DisplayName("getIconHeight: Should return the fixed height of 16")
    void testGetIconHeight() {
        assertEquals(16, helpIcon.getIconHeight(), "Icon height should be 16.");
    }

    @Test
    @DisplayName("paintIcon: Basic call should not throw exceptions")
    void testPaintIcon_basicCall_noExceptions() {
        // This test ensures that paintIcon runs without errors with a mock Graphics object.
        // It does not verify the actual drawing output.
        assertDoesNotThrow(() -> helpIcon.paintIcon(mockComponent, mockGraphics, 0, 0),
            "paintIcon should execute without throwing exceptions with mock Graphics.");

        // Optionally, verify interactions with mockGraphics if specific drawing calls are critical to test
        // For example, to ensure a color is set or a rectangle is filled:
        // verify(mockGraphics).setColor(Color.BLUE);
        // verify(mockGraphics).fillRect(0, 0, 16, 16);
        // verify(mockGraphics).setColor(Color.WHITE);
        // verify(mockGraphics).drawString(eq("?"), anyInt(), anyInt());
        // For this subtask, "no exceptions" is the primary goal for basic sanity.
    }
}
