package nonprofitbookkeeping.core;

import nonprofitbookkeeping.api.AccountDetails;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.ChartOfAccounts;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Builder that collects {@link Account} definitions and produces a populated
 * {@link ChartOfAccounts} instance.
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
         * @return A new ChartOfAccountsBuilder instance.
         */
        public static ChartOfAccountsBuilder create()
        {
                return new ChartOfAccountsBuilder();
        }

        /**
         * Adds a new top-level account to the builder.
         * @param accountNumber The account number. Must not be null or blank.
         * @param name The display name of the account.
         * @param increaseSide The side where the account increases (Debit or Credit).
         * @return This ChartOfAccountsBuilder instance for chaining.
         * @throws IllegalArgumentException if {@code accountNumber} is null/blank or
         *         already staged.
         */
        public ChartOfAccountsBuilder addAccount(
                String accountNumber,
                String name,
                AccountSide increaseSide)
        {
                Account acc = new Account();
                acc.setAccountNumber(accountNumber);
                acc.setName(name);
                acc.setIncreaseSide(increaseSide);
                return addAccount(acc);
        }

        /**
         * Adds an {@link AccountDetails} description to the builder.
         * @param details the account description to add.
         * @return This builder for chaining.
         * @throws NullPointerException if {@code details} is null.
         * @throws IllegalArgumentException if the account number is blank or already staged.
         */
        public ChartOfAccountsBuilder addAccount(AccountDetails details)
        {
                Objects.requireNonNull(details, "details");
                Account account = fromDetails(details);
                return addAccount(account);
        }

        /**
         * Adds an {@link Account} definition to the builder.
         * @param account the account to add.
         * @return this builder for chaining.
         * @throws NullPointerException if {@code account} is null.
         * @throws IllegalArgumentException if the account number is blank or already staged.
         */
        public ChartOfAccountsBuilder addAccount(Account account)
        {
                Objects.requireNonNull(account, "account");
                String accountNumber = Objects.requireNonNull(account.getAccountNumber(),
                        "accountNumber");

                if (accountNumber.isBlank())
                {
                        throw new IllegalArgumentException("accountNumber must not be blank");
                }

                if (this.stagedAccounts.containsKey(accountNumber))
                {
                        throw new IllegalArgumentException("Duplicate account number staged: "
                                + accountNumber);
                }

                this.stagedAccounts.put(accountNumber, copyAccount(account));
                return this;
        }

        /**
         * Adds multiple accounts described by {@link AccountDetails} instances.
         * @param accounts collection of account descriptions.
         * @return this builder for chaining.
         */
        public ChartOfAccountsBuilder addAccounts(Collection<? extends AccountDetails> accounts)
        {
                if (accounts == null || accounts.isEmpty())
                {
                        return this;
                }

                for (AccountDetails details : accounts)
                {
                        addAccount(details);
                }

                return this;
        }

        /**
         * Builds the ChartOfAccounts using the staged account definitions.
         * @return A new ChartOfAccounts instance populated with the staged accounts.
         * @throws IllegalStateException if a child account references a parent that was not staged.
         */
        public ChartOfAccounts build()
        {
                ChartOfAccounts chart = new ChartOfAccounts();
                Map<String, Account> inserted = new LinkedHashMap<>();
                List<Account> pendingChildren = new ArrayList<>();

                for (Account prototype : this.stagedAccounts.values())
                {
                        Account candidate = copyAccount(prototype);

                        if (!candidate.hasParent())
                        {
                                chart.addAccount(candidate);
                                inserted.put(candidate.getAccountNumber(), candidate);
                        }
                        else
                        {
                                pendingChildren.add(candidate);
                        }
                }

                if (!pendingChildren.isEmpty())
                {
                        resolveChildren(chart, pendingChildren, inserted);
                }

                return chart;
        }

        private static void resolveChildren(ChartOfAccounts chart,
                List<Account> pendingChildren,
                Map<String, Account> inserted)
        {
                List<Account> remaining = new ArrayList<>(pendingChildren);

                while (!remaining.isEmpty())
                {
                        boolean progress = false;
                        Iterator<Account> iterator = remaining.iterator();

                        while (iterator.hasNext())
                        {
                                Account child = iterator.next();
                                String parentNumber = child.getParentAccountId();
                                Account parent = parentNumber == null ? null : chart.getAccount(parentNumber);

                                if (parent != null)
                                {
                                        chart.addSubAccount(parent, child);
                                        inserted.put(child.getAccountNumber(), child);
                                        iterator.remove();
                                        progress = true;
                                }
                        }

                        if (!progress)
                        {
                                String unresolved = remaining.stream()
                                        .map(Account::getAccountNumber)
                                        .collect(Collectors.joining(", "));
                                throw new IllegalStateException(
                                        "Unable to resolve parent accounts for: " + unresolved);
                        }
                }
        }

        private static Account fromDetails(AccountDetails details)
        {
                Account account = new Account();
                account.setAccountNumber(details.getAccountNumber());

                String name = details.getName();

                if (name == null || name.isBlank())
                {
                        name = details.getAccountName();
                }

                account.setName(name);
                account.setIncreaseSide(details.getIncreaseSide());
                account.setAccountCode(details.getAccountCode());

                String parent = details.getParentAccount();

                if (parent != null && !parent.isBlank())
                {
                        account.setParentAccountId(parent);
                }

                account.setCurrency(details.getCurrency());

                String type = details.getAccountType();

                if (type != null && !type.isBlank())
                {
                        AccountType parsed = AccountType.fromString(
                                type.trim().toUpperCase(Locale.ROOT).replace(' ', '_'));

                        if (parsed != AccountType.UNKNOWN)
                        {
                                account.setAccountType(parsed);
                        }
                }

                BigDecimal opening = details.getOpeningBalance();

                if (opening != null)
                {
                        account.setOpeningBalance(opening);
                }

                return account;
        }

        private static Account copyAccount(Account source)
        {
                Account copy = new Account();
                List<String> funds = source.getAssociatedFundIds();
                copy.setAssociatedFundIds(funds == null ? new ArrayList<>() : new ArrayList<>(funds));
                copy.setAccountNumber(source.getAccountNumber());
                copy.setIncreaseSide(source.getIncreaseSide());
                copy.setName(source.getName());
                copy.setAccountCode(source.getAccountCode());
                copy.setAccountType(source.getAccountType());
                copy.setParentAccountId(source.getParentAccountId());
                copy.setCurrency(source.getCurrency());
                copy.setOpeningBalance(source.getOpeningBalance());
                return copy;
        }
}
