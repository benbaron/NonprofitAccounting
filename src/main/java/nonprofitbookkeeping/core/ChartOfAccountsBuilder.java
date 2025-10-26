package nonprofitbookkeeping.core;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.ChartOfAccounts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Builder used to assemble a {@link ChartOfAccounts} instance from staged
 * {@link Account} definitions.
 */
public class ChartOfAccountsBuilder
{
        private final Map<String, Account> stagedAccounts = new LinkedHashMap<>();

        /**
         * Constructs a ChartOfAccountsBuilder.
         */
        public ChartOfAccountsBuilder()
        {
        }

        /**
         * Creates a new ChartOfAccountsBuilder.
         *
         * @return A new ChartOfAccountsBuilder instance.
         */
        public static ChartOfAccountsBuilder create()
        {
                return new ChartOfAccountsBuilder();
        }

        /**
         * Adds a new account to the staged chart of accounts.
         *
         * @param accountNumber The account number.
         * @param name The name of the account.
         * @param increaseSide The side where the account increases (Debit or Credit).
         * @return This ChartOfAccountsBuilder instance for chaining.
         */
        public ChartOfAccountsBuilder addAccount(
                String accountNumber,
                String name,
                AccountSide increaseSide)
        {
                Objects.requireNonNull(accountNumber, "accountNumber");
                Objects.requireNonNull(name, "name");
                Objects.requireNonNull(increaseSide, "increaseSide");

                Account account = new Account();
                account.setAccountNumber(accountNumber);
                account.setName(name);
                account.setIncreaseSide(increaseSide);
                return addAccount(account);
        }

        /**
         * Adds an existing {@link Account} to the staged chart of accounts.
         *
         * @param account The account to stage.
         * @return This ChartOfAccountsBuilder instance for chaining.
         */
        public ChartOfAccountsBuilder addAccount(Account account)
        {
                Account copy = copyAccount(Objects.requireNonNull(account, "account"));
                String number = copy.getAccountNumber();

                if (number == null || number.isBlank())
                {
                        throw new IllegalArgumentException("Account must have a non-blank account number.");
                }

                this.stagedAccounts.put(number, copy);
                return this;
        }

        /**
         * Adds the provided accounts to the staged chart of accounts.
         *
         * @param accounts Collection of accounts to add. Null and empty collections are ignored.
         * @return This ChartOfAccountsBuilder instance for chaining.
         */
        public ChartOfAccountsBuilder addAccounts(Collection<? extends Account> accounts)
        {
                if (accounts == null || accounts.isEmpty())
                {
                        return this;
                }

                accounts.forEach(this::addAccount);
                return this;
        }

        /**
         * Builds a ChartOfAccounts populated with the staged accounts.
         *
         * @return A new ChartOfAccounts instance containing the staged accounts.
         */
        public ChartOfAccounts build()
        {
                Map<String, Account> copies = this.stagedAccounts.values().stream()
                        .map(ChartOfAccountsBuilder::copyAccount)
                        .collect(Collectors.toMap(Account::getAccountNumber,
                                account -> account,
                                (existing, replacement) -> replacement,
                                LinkedHashMap::new));

                ChartOfAccounts chart = new ChartOfAccounts();

                // Add root accounts first so children can attach to their parents later.
                copies.values().stream()
                        .filter(account -> !account.hasParent())
                        .forEach(chart::addAccount);

                // Attach sub accounts. Any child without a staged parent becomes a root account.
                copies.values().stream()
                        .filter(Account::hasParent)
                        .forEach(account -> {
                                Account parent = chart.getAccount(account.getParentAccountId());

                                if (parent == null)
                                {
                                        chart.addAccount(account);
                                }
                                else
                                {
                                        chart.addSubAccount(parent, account);
                                }
                        });

                return chart;
        }

        /**
         * Returns an immutable snapshot of the staged accounts.
         *
         * @return An immutable list containing copies of the staged accounts.
         */
        public List<Account> getAccounts()
        {
                return this.stagedAccounts.values().stream()
                        .map(ChartOfAccountsBuilder::copyAccount)
                        .collect(Collectors.toUnmodifiableList());
        }

        /**
         * Clears all staged accounts.
         */
        public void clear()
        {
                this.stagedAccounts.clear();
        }

        private static Account copyAccount(Account original)
        {
                Account copy = new Account();
                copy.setAccountNumber(original.getAccountNumber());
                copy.setName(original.getName());
                copy.setIncreaseSide(original.getIncreaseSide());
                copy.setAccountCode(original.getAccountCode());
                copy.setAccountType(original.getAccountType());
                copy.setParentAccountId(original.getParentAccountId());
                copy.setCurrency(original.getCurrency());
                copy.setOpeningBalance(original.getOpeningBalance());
                copy.setAssociatedFundIds(new ArrayList<>(original.getAssociatedFundIds()));
                return copy;
        }
}
