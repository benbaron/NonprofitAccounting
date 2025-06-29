package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.ui.JavaFXTestBase;
import nonprofitbookkeeping.ui.helpers.AlertBox;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

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
    public void testPersistKeepsIdAndTimestamp() throws Exception {
        Method persist = GeneralJournalEntryPanelFX.class.getDeclaredMethod("persist");
        persist.setAccessible(true);
        Platform.runLater(() -> {
            try { persist.invoke(this.panel); } catch (Exception e) { throw new RuntimeException(e); }
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertNotNull(this.saved);
        assertEquals(1, this.saved.getId());
        assertEquals(12345L, this.saved.getBookingDateTimestamp());
    }

    @Test
    public void testMissingAccountShowsError() throws Exception {
        Field linesField = GeneralJournalEntryPanelFX.class.getDeclaredField("lines");
        linesField.setAccessible(true);
        @SuppressWarnings("unchecked") ObservableList<GeneralJournalEntryPanelFX.Line> lines =
                (ObservableList<GeneralJournalEntryPanelFX.Line>) linesField.get(this.panel);
        Platform.runLater(() -> {
            lines.clear();
            GeneralJournalEntryPanelFX.Line bad = new GeneralJournalEntryPanelFX.Line();
            bad.account.set("Bad");
            bad.debit.set(BigDecimal.TEN);
            lines.add(bad);
        });
        WaitForAsyncUtils.waitForFxEvents();

        Method persist = GeneralJournalEntryPanelFX.class.getDeclaredMethod("persist");
        persist.setAccessible(true);
        try (MockedStatic<AlertBox> mocked = mockStatic(AlertBox.class)) {
            Platform.runLater(() -> {
                try { persist.invoke(this.panel); } catch (Exception e) { throw new RuntimeException(e); }
            });
            WaitForAsyncUtils.waitForFxEvents();
            mocked.verify(() -> AlertBox.showError(any(), contains("Account not found")), times(1));
        }
    }
}
