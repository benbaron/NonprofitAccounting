package nonprofitbookkeeping.ui.panels;

import javafx.scene.Scene;
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

    @BeforeEach
    public void clearListeners() {
        for (CurrentCompany.CompanyChangeListener l : CurrentCompany.CompanyListener.getListeners()) {
            CurrentCompany.CompanyListener.removeCompanyListener(l);
        }
    }

    @Start
    @Override
    public void start(Stage stage) {
        this.panel = new AccountTransactionDetailsPanelFX();
        Scene scene = new Scene(this.panel, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @AfterEach
    public void disposePanel() {
        if (this.panel != null) {
            this.panel.dispose();
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
}
