package nonprofitbookkeeping.ui.panels;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Base class for report-related actions in the application.
 * Although named with "Interface", this is currently implemented as an abstract base class
 * extending {@link AbstractAction}. Subclasses are expected to provide concrete implementations
 * for generating specific reports.
 * <p>
 * The {@link #actionPerformed(ActionEvent)} method is a stub and should be overridden by
 * subclasses to define the specific reporting logic.
 * </p>
 */
public abstract class ReportActionInterface extends AbstractAction
{

       /**
        * {@inheritDoc}
        *
        * <p>
        * Classes extending this base are expected to implement their own
        * reporting logic. Making this method abstract enforces that contract at
        * compile time and removes the previous no-op stub implementation.
        * </p>
        *
        * @param e the triggering event
        */
       @Override public abstract void actionPerformed(ActionEvent e);
	
}
