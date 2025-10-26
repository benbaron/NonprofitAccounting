package nonprofitbookkeeping.core;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.ChartOfAccounts;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChartOfAccountsBuilderTest
{
        @Test
        void buildCreatesChartWithStagedAccounts()
        {
                ChartOfAccountsBuilder builder = new ChartOfAccountsBuilder()
                        .addAccount("100", "Cash", AccountSide.DEBIT)
                        .addAccount("200", "Revenue", AccountSide.CREDIT);

                ChartOfAccounts chart = builder.build();

                Account cash = chart.getAccount("100");
                Account revenue = chart.getAccount("200");

                assertNotNull(cash, "Cash account should be present in the chart");
                assertNotNull(revenue, "Revenue account should be present in the chart");
                assertEquals("Cash", cash.getName());
                assertEquals(AccountSide.DEBIT, cash.getIncreaseSide());
                assertEquals("Revenue", revenue.getName());
                assertEquals(AccountSide.CREDIT, revenue.getIncreaseSide());
        }

        @Test
        void buildAttachesChildAccountsWhenParentExists()
        {
                Account parent = new Account();
                parent.setAccountNumber("300");
                parent.setName("Expenses");
                parent.setIncreaseSide(AccountSide.DEBIT);
                parent.setAccountType(AccountType.EXPENSE);

                Account child = new Account();
                child.setAccountNumber("301");
                child.setName("Supplies");
                child.setIncreaseSide(AccountSide.DEBIT);
                child.setParentAccountId("300");
                child.setOpeningBalance(new BigDecimal("42.00"));

                ChartOfAccounts chart = new ChartOfAccountsBuilder()
                        .addAccount(parent)
                        .addAccount(child)
                        .build();

                Account storedParent = chart.getAccount("300");
                Account storedChild = chart.getAccount("301");

                assertNotNull(storedParent, "Parent account should be present");
                assertNotNull(storedChild, "Child account should be present");
                assertTrue(storedChild.hasParent(), "Child account should retain its parent");
                assertEquals("300", storedChild.getParentAccountId());
                assertEquals(new BigDecimal("42.00"), storedChild.getOpeningBalance());
                assertEquals(List.of(storedChild), chart.getChildren(storedParent));
        }

        @Test
        void buildReturnsFreshChartEachTime()
        {
                ChartOfAccountsBuilder builder = new ChartOfAccountsBuilder()
                        .addAccount("400", "Equity", AccountSide.CREDIT);

                ChartOfAccounts first = builder.build();
                first.addAccount(new Account("999", "Temp", AccountSide.DEBIT));

                ChartOfAccounts second = builder.build();

                assertNull(second.getAccount("999"), "Temporary account should not leak into subsequent builds");
        }

        @Test
        void getAccountsReturnsDefensiveCopies()
        {
                ChartOfAccountsBuilder builder = new ChartOfAccountsBuilder()
                        .addAccount("500", "Checking", AccountSide.DEBIT);

                List<Account> snapshot = builder.getAccounts();
                Account staged = snapshot.get(0);

                assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new Account()));

                staged.setName("Mutated");

                ChartOfAccounts chart = builder.build();

                assertEquals("Checking", chart.getAccount("500").getName(),
                        "Mutating the snapshot should not impact the builder state");
        }
}
