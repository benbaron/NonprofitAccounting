package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import nonprofitbookkeeping.ui.panels.JournalEntryWorkspaceFX;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

public class GeneralJournalEntryPanelFXTest extends JavaFXTestBase {

    private GeneralJournalEntryPanelFX panel;
    private AccountingTransaction saved;

    @Start
    public void start(Stage stage) {
        ChartOfAccounts coa = new ChartOfAccounts();
        Account cash = new Account("100", "Cash", AccountType.ASSET, BigDecimal.ZERO);
        Account rev = new Account("400", "Revenue", AccountType.INCOME, BigDecimal.ZERO);
        coa.addAccount(cash);
        coa.addAccount(rev);
        Company c = new Company();
        c.setChartOfAccounts(coa);
        CurrentCompany.forceCompanyLoad(c);

        Set<AccountingEntry> entries = new LinkedHashSet<>();
        entries.add(new AccountingEntry(new BigDecimal("10"), "100", AccountSide.DEBIT, "Cash"));
        entries.add(new AccountingEntry(new BigDecimal("10"), "400", AccountSide.CREDIT, "Revenue"));
        AccountingTransaction tx = new AccountingTransaction(new Account(), entries, null, 12345L);
        tx.setId(1);
        tx.setDate("2024-07-01");
        tx.setDescription("Memo");

        this.panel = new GeneralJournalEntryPanelFX(tx, t -> this.saved = t);
        Scene scene = new Scene(this.panel, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testPersistKeepsIdAndTimestamp() {
        Platform.runLater(() -> this.panel.getSaveButton().fire());
        WaitForAsyncUtils.waitForFxEvents();
        assertNotNull(this.saved);
        assertEquals(1, this.saved.getId());
        assertEquals(12345L, this.saved.getBookingDateTimestamp());
    }

    @Test
    public void testMissingAccountShowsError() {
        Platform.runLater(() -> {
            this.panel.getLines().clear();
            JournalEntryWorkspaceFX.Line bad = new JournalEntryWorkspaceFX.Line();
            bad.account.set("Bad");
            bad.debit.set(BigDecimal.TEN);
            this.panel.getLines().add(bad);
        });
        WaitForAsyncUtils.waitForFxEvents();

        try (MockedStatic<AlertBox> mocked = mockStatic(AlertBox.class)) {
            Platform.runLater(() -> {
                this.panel.getSaveButton().fire();
            });
            WaitForAsyncUtils.waitForFxEvents();
            mocked.verify(() -> AlertBox.showError(any(), contains("Account not found")), times(1));
        }
    }
}
