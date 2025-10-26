package nonprofitbookkeeping.ui.panels;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.math.BigDecimal;

@ExtendWith(ApplicationExtension.class)
public class JournalEntryWorkspaceFocusCommitTest
{
        private JournalEntryWorkspaceFX panel;
        private TableView<JournalEntryWorkspaceFX.Line> table;
        private TextArea memoArea;

        @Start
        private void start(Stage stage)
        {
                ChartOfAccounts coa = new ChartOfAccounts();
                Account bank = new Account("1010", "Bank", AccountType.ASSET, BigDecimal.ZERO);
                bank.setIncreaseSide(AccountSide.DEBIT);
                coa.addAccount(bank);

                Company company = new Company();
                company.setChartOfAccounts(coa);
                CurrentCompany.forceCompanyLoad(company);

                this.panel = new JournalEntryWorkspaceFX(tx -> { });
                Scene scene = new Scene(this.panel, 800, 600);
                stage.setScene(scene);
                stage.show();
                stage.toFront();

                this.table = this.panel.getTable();
                this.memoArea = (TextArea) this.panel.lookup("#memoArea");
        }

        @Test
        void editingDebitCellCommitsValue(FxRobot robot)
        {
                WaitForAsyncUtils.waitForFxEvents();

                Platform.runLater(() -> this.panel.getLines().get(0).account.set("Bank"));
                WaitForAsyncUtils.waitForFxEvents();

                Node row = robot.lookup(".table-row-cell").nth(0).query();
                Node debitCell = robot.lookup(".table-row-cell").nth(0).lookup(".table-cell").nth(1).query();
                robot.clickOn(row);
                robot.doubleClickOn(debitCell);
                robot.write("100.00");
                robot.clickOn(this.memoArea);
                WaitForAsyncUtils.waitForFxEvents();

                BigDecimal amount = this.panel.getLines().get(0).debit.get();
                Assertions.assertThat(amount).isEqualByComparingTo("100.00");
        }
}

