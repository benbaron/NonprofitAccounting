package nonprofitbookkeeping.core;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountDetailsImpl;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.ChartOfAccounts;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChartOfAccountsBuilderTest
{
        @Test
        void buildIncludesAccountsAddedByNumber()
        {
                ChartOfAccounts chart = ChartOfAccountsBuilder.create()
                        .addAccount("1000", "Operating Cash", AccountSide.DEBIT)
                        .addAccount("2000", "Accounts Payable", AccountSide.CREDIT)
                        .build();

                assertNotNull(chart.getAccount("1000"));
                assertNotNull(chart.getAccount("2000"));
                assertEquals(2, chart.getAccounts().size());
        }

        @Test
        void buildCopiesAccountDetailsMetadata()
        {
                AccountDetailsImpl details = new AccountDetailsImpl("3000",
                        "Unrestricted Net Assets",
                        AccountSide.CREDIT);
                details.setAccountCode("EQ-3000");
                details.setAccountType(AccountType.EQUITY.name());
                details.setCurrency("USD");
                details.setOpeningBalance(new BigDecimal("125.75"));

                ChartOfAccounts chart = ChartOfAccountsBuilder.create()
                        .addAccount(details)
                        .build();

                Account account = chart.getAccount("3000");

                assertNotNull(account);
                assertEquals("Unrestricted Net Assets", account.getName());
                assertEquals("EQ-3000", account.getAccountCode());
                assertEquals(AccountType.EQUITY, account.getAccountType());
                assertEquals("USD", account.getCurrency());
                assertEquals(new BigDecimal("125.75"), account.getOpeningBalance());
        }

        @Test
        void buildResolvesChildAccounts()
        {
                Account parent = new Account();
                parent.setAccountNumber("1000");
                parent.setName("Cash");
                parent.setIncreaseSide(AccountSide.DEBIT);

                Account child = new Account();
                child.setAccountNumber("1001");
                child.setName("Petty Cash");
                child.setIncreaseSide(AccountSide.DEBIT);
                child.setParentAccountId("1000");

                ChartOfAccounts chart = ChartOfAccountsBuilder.create()
                        .addAccount(parent)
                        .addAccount(child)
                        .build();

                Account root = chart.getAccount("1000");
                assertNotNull(root);

                List<Account> children = chart.getChildren(root);
                assertEquals(1, children.size());
                assertEquals("1001", children.get(0).getAccountNumber());
                assertEquals("1000", children.get(0).getParentAccountId());
        }

        @Test
        void addingDuplicateAccountNumberThrows()
        {
                ChartOfAccountsBuilder builder = ChartOfAccountsBuilder.create()
                        .addAccount("1000", "Operating Cash", AccountSide.DEBIT);

                assertThrows(IllegalArgumentException.class,
                        () -> builder.addAccount("1000", "Petty Cash", AccountSide.DEBIT));
        }
}
