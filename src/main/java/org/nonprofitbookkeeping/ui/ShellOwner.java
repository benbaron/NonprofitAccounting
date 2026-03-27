package org.nonprofitbookkeeping.ui;

/**
 * Canonical shell owner contract for navigation, panel hosting, and inspector composition.
 */
public interface ShellOwner
{
    NavigationPane navigationPane();
    PanelHost panelHost();
    InspectorPane inspectorPane();
}
