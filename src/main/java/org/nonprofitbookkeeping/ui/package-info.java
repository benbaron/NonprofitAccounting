/**
 * Canonical home for application-shell UI composition is {@code org.nonprofitbookkeeping.ui}.
 *
 * <p>Panel inventory and routing entry points in this namespace include:
 * {@link org.nonprofitbookkeeping.ui.PanelHost},
 * {@link org.nonprofitbookkeeping.ui.MainWindowAlternate},
 * {@link org.nonprofitbookkeeping.ui.MainWindow}, and
 * {@link org.nonprofitbookkeeping.ui.routing.WorkspaceRouter}.
 *
 * <p>Legacy JavaFX implementations under {@code nonprofitbookkeeping.ui.panels} are consumed
 * through adapter/wrapper boundaries in this package (for example
 * {@link org.nonprofitbookkeeping.ui.FundsPanel},
 * {@link org.nonprofitbookkeeping.ui.InventoryPanel}, and
 * {@link org.nonprofitbookkeeping.ui.ReportLibraryPanel}). This adapter layer is intentional to
 * avoid cross-namespace leakage into new shell orchestration code.
 */
package org.nonprofitbookkeeping.ui;
