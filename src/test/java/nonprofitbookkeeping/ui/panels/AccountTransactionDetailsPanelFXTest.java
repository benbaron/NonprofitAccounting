package nonprofitbookkeeping.ui.panels;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.Start;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTransactionDetailsPanelFXTest extends JavaFXTestBase {

    private AccountTransactionDetailsPanelFX panel;

    @Start
    @Override
    public void start(Stage stage) throws Exception {
        super.start(stage);
        getStage().setScene(new Scene(new BorderPane(), 800, 600));
    }

    @BeforeEach
    public void createPanel() {
        for (CurrentCompany.CompanyChangeListener l : CurrentCompany.CompanyListener.getListeners()) {
            CurrentCompany.CompanyListener.removeCompanyListener(l);
        }

        interact(() -> {
            this.panel = new AccountTransactionDetailsPanelFX();
            getStage().setScene(new Scene(this.panel, 800, 600));
        });
    }

    @AfterEach
    public void disposePanel() {
        if (this.panel != null) {
            this.panel.dispose();
            this.panel = null;
        }

        for (CurrentCompany.CompanyChangeListener l : CurrentCompany.CompanyListener.getListeners()) {
            CurrentCompany.CompanyListener.removeCompanyListener(l);
        }
    }

    @Test
    public void testSetupListenerRegistersOnlyOnce() throws Exception {
        assertEquals(1, CurrentCompany.CompanyListener.getListeners().size());

        Method setup = AccountTransactionDetailsPanelFX.class.getDeclaredMethod("setupCompanyChangeListener");
        setup.setAccessible(true);
        setup.invoke(this.panel);
        setup.invoke(this.panel);

        assertEquals(1, CurrentCompany.CompanyListener.getListeners().size());
    }

    @Test
    public void testDisposeRemovesListener() {
        assertFalse(CurrentCompany.CompanyListener.getListeners().isEmpty());
        this.panel.dispose();
        assertTrue(CurrentCompany.CompanyListener.getListeners().isEmpty());
    }

    @Test
    public void testRecreatingPanelAfterDisposeRegistersListenerOnlyOnce() {
        assertEquals(1, CurrentCompany.CompanyListener.getListeners().size());

        this.panel.dispose();
        assertTrue(CurrentCompany.CompanyListener.getListeners().isEmpty());

        interact(() -> {
            AccountTransactionDetailsPanelFX newPanel = new AccountTransactionDetailsPanelFX();
            getStage().setScene(new Scene(newPanel, 800, 600));
            this.panel = newPanel;
        });

        assertEquals(1, CurrentCompany.CompanyListener.getListeners().size());
    }
}
