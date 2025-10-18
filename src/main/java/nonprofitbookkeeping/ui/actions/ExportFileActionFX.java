package nonprofitbookkeeping.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ofx.OfxV2Writer;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.BankAccountInfo;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.CreditCardInfo;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.SignonData;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.TransactionData;

import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Exports ledger transactions to OFX/QFX files.
 */
public class ExportFileActionFX implements EventHandler<ActionEvent>
{
        private static final DateTimeFormatter OFX_DATE = DateTimeFormatter.BASIC_ISO_DATE;
        private static final DateTimeFormatter OFX_DATE_TIME =
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'.000'");
        private static final EnumSet<AccountType> BANK_ACCOUNT_TYPES = EnumSet.of(
                AccountType.ASSET,
                AccountType.BANK,
                AccountType.CASH,
                AccountType.CHECKING,
                AccountType.MONEYMKRT,
                AccountType.SIMPLEINVEST,
                AccountType.MUTUAL,
                AccountType.INVEST);
        private static final EnumSet<AccountType> CREDIT_CARD_TYPES = EnumSet.of(
                AccountType.CREDITCARD,
                AccountType.CREDIT);
        private static final EnumSet<AccountType> LIABILITY_TYPES = EnumSet.of(
                AccountType.CREDITCARD,
                AccountType.CREDIT,
                AccountType.LIABILITY,
                AccountType.LONG_TERM_LIABILITY);

        private final Stage ownerStage;

        public ExportFileActionFX(Stage primaryStage)
        {

                if (primaryStage == null)
                {
                        throw new IllegalArgumentException("Primary stage (owner stage) cannot be null.");
                }

                this.ownerStage = primaryStage;
        }

        @Override public void handle(ActionEvent event)
        {
                if (!CurrentCompany.isOpen())
                {
                        showError("No company open to export transactions from.");
                        return;
                }

                Company company = CurrentCompany.getCompany();

                if (company == null || company.getLedger() == null)
                {
                        showError("No ledger data available to export.");
                        return;
                }

                ChartOfAccounts chart = company.getChartOfAccounts();

                if (chart == null)
                {
                        showError("No chart of accounts available to select from.");
                        return;
                }

                Comparator<Account> accountComparator = Comparator.comparing(
                        accountEntry -> Optional.ofNullable(accountEntry.getName()).orElse(""),
                        String.CASE_INSENSITIVE_ORDER);

                List<Account> eligibleAccounts = chart.getAccounts().stream()
                        .filter(this::supportsOfxExport)
                        .sorted(accountComparator)
                        .collect(Collectors.toList());

                if (eligibleAccounts.isEmpty())
                {
                        showError("No bank or credit card accounts available for export.");
                        return;
                }

                Optional<Account> accountSelection = promptForAccount(eligibleAccounts);

                if (accountSelection.isEmpty())
                {
                        return;
                }

                Account account = accountSelection.get();
                ExportPayload payload = buildPayload(account, company.getLedger());

                if (payload.transactions().isEmpty())
                {
                        showInfo("No transactions found for " + account.getName() + " to export.");
                        return;
                }

                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Export Transactions");

                FileChooser.ExtensionFilter ofxFilter =
                        new FileChooser.ExtensionFilter("OFX files (*.ofx)", "*.ofx");
                FileChooser.ExtensionFilter qfxFilter =
                        new FileChooser.ExtensionFilter("QFX files (*.qfx)", "*.qfx");
                FileChooser.ExtensionFilter allFilter =
                        new FileChooser.ExtensionFilter("All files (*.*)", "*.*");

                fileChooser.getExtensionFilters().addAll(ofxFilter, qfxFilter, allFilter);
                fileChooser.setSelectedExtensionFilter(ofxFilter);

                File selectedFile = fileChooser.showSaveDialog(this.ownerStage);

                if (selectedFile == null)
                {
                        return;
                }

                FileChooser.ExtensionFilter selectedFilter = fileChooser.getSelectedExtensionFilter();
                boolean exportAsQfx = selectedFilter == qfxFilter ||
                        selectedFile.getName().toLowerCase(Locale.ROOT).endsWith(".qfx");
                String extension = exportAsQfx ? ".qfx" : ".ofx";
                File finalFile = ensureExtension(selectedFile, extension);

                try
                {
                        writeOfxFile(finalFile, account, company, payload, exportAsQfx);
                        showInfo("Exported " + payload.transactions().size() + " transactions to " +
                                finalFile.getAbsolutePath());
                }
                catch (Exception ex)
                {
                        showDetailedError("Could not export transactions", ex.getMessage());
                }
        }

        private Optional<Account> promptForAccount(List<Account> accounts)
        {
                Map<String, Account> labelToAccount = new LinkedHashMap<>();

                for (Account account : accounts)
                {
                        String label = formatAccountLabel(account);
                        labelToAccount.put(label, account);
                }

                List<String> choices = new ArrayList<>(labelToAccount.keySet());
                ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
                dialog.initOwner(this.ownerStage);
                dialog.setTitle("Select Account");
                dialog.setHeaderText("Choose the account to export:");

                Optional<String> selection = dialog.showAndWait();

                return selection.map(labelToAccount::get);
        }

        private boolean supportsOfxExport(Account account)
        {
                if (account == null)
                {
                        return false;
                }

                AccountType type = account.getAccountType();

                if (type == null)
                {
                        return true;
                }

                return BANK_ACCOUNT_TYPES.contains(type) || CREDIT_CARD_TYPES.contains(type);
        }

        private ExportPayload buildPayload(Account account, Ledger ledger)
        {
                if (ledger == null || ledger.getJournal() == null)
                {
                        return new ExportPayload(List.of(), null, null);
                }

                String targetAccountNumber = account.getAccountNumber();

                if (targetAccountNumber == null || targetAccountNumber.isBlank())
                {
                        return new ExportPayload(List.of(), null, null);
                }

                List<AccountingTransaction> journalTransactions =
                        ledger.getJournal().getJournalTransactions();

                if (journalTransactions == null || journalTransactions.isEmpty())
                {
                        return new ExportPayload(List.of(), null, null);
                }

                List<TransactionData> transactions = new ArrayList<>();
                List<LocalDate> dates = new ArrayList<>();

                for (AccountingTransaction txn : journalTransactions)
                {
                        if (txn == null || txn.getEntries() == null)
                        {
                                continue;
                        }

                        AccountingEntry entry = txn.getEntries().stream()
                                .filter(e -> Objects.equals(targetAccountNumber, e.getAccountNumber()))
                                .findFirst()
                                .orElse(null);

                        if (entry == null)
                        {
                                continue;
                        }

                        Optional<LocalDate> dateOpt = resolveTransactionDate(txn);
                        Optional<BigDecimal> amountOpt = determineSignedAmount(account, entry);

                        if (dateOpt.isEmpty() || amountOpt.isEmpty())
                        {
                                continue;
                        }

                        LocalDate date = dateOpt.get();
                        BigDecimal amount = amountOpt.get();
                        String checkNumber = blankToNull(txn.getCheckNumber());
                        String name = sanitizeName(txn);
                        String memo = coalesce(txn.getMemo(), "");
                        String type = determineTransactionType(txn, amount);

                        transactions.add(new TransactionData(type,
                                date.format(OFX_DATE),
                                amount.toPlainString(),
                                checkNumber,
                                name,
                                memo));
                        dates.add(date);
                }

                transactions.sort(Comparator.comparing(t -> t.date));
                Collections.sort(dates);

                LocalDate start = dates.isEmpty() ? null : dates.get(0);
                LocalDate end = dates.isEmpty() ? null : dates.get(dates.size() - 1);

                return new ExportPayload(transactions, start, end);
        }

        private Optional<BigDecimal> determineSignedAmount(Account account, AccountingEntry entry)
        {
                if (entry.getAmount() == null || entry.getAccountSide() == null)
                {
                        return Optional.empty();
                }

                AccountSide increaseSide = account.getIncreaseSide();

                if (increaseSide == null || increaseSide == AccountSide.UNKNOWN)
                {
                        increaseSide = inferDefaultIncreaseSide(account.getAccountType());
                }

                boolean entryIncreases = entry.getAccountSide() == increaseSide;
                BigDecimal signedAmount = entryIncreases ? entry.getAmount() : entry.getAmount().negate();

                if (isLiabilityLike(account.getAccountType()))
                {
                        signedAmount = signedAmount.negate();
                }

                return Optional.of(signedAmount);
        }

        private AccountSide inferDefaultIncreaseSide(AccountType type)
        {
                if (type == null)
                {
                        return AccountSide.DEBIT;
                }

                if (LIABILITY_TYPES.contains(type) || type == AccountType.INCOME || type == AccountType.EQUITY)
                {
                        return AccountSide.CREDIT;
                }

                return AccountSide.DEBIT;
        }

        private boolean isLiabilityLike(AccountType type)
        {
                return type != null && LIABILITY_TYPES.contains(type);
        }

        private Optional<LocalDate> resolveTransactionDate(AccountingTransaction txn)
        {
                if (txn.getDate() != null && !txn.getDate().isBlank())
                {
                        try
                        {
                                return Optional.of(LocalDate.parse(txn.getDate()));
                        }
                        catch (DateTimeParseException ignored)
                        {
                                // fall back to booking timestamp
                        }
                }

                Long bookingTimestamp = txn.getBookingDateTimestamp();

                if (bookingTimestamp != null && bookingTimestamp != 0L)
                {
                        return Optional.of(Instant.ofEpochMilli(bookingTimestamp)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate());
                }

                return Optional.empty();
        }

        private String determineTransactionType(AccountingTransaction txn, BigDecimal amount)
        {
                if (txn.getCheckNumber() != null && !txn.getCheckNumber().isBlank())
                {
                        return "CHECK";
                }

                if (amount.signum() > 0)
                {
                        return "CREDIT";
                }

                if (amount.signum() < 0)
                {
                        return "DEBIT";
                }

                return "OTHER";
        }

        private void writeOfxFile(File target,
                                  Account account,
                                  Company company,
                                  ExportPayload payload,
                                  boolean exportAsQfx) throws Exception
        {
                List<TransactionData> transactions = payload.transactions();
                boolean creditCardAccount = isCreditCardAccount(account);

                LocalDate startDate = Objects.requireNonNullElse(payload.startDate(), LocalDate.now());
                LocalDate endDate = Objects.requireNonNullElse(payload.endDate(), LocalDate.now());

                SignonData signon = new SignonData();
                signon.language = "ENG";
                signon.org = exportAsQfx ? "INTUIT" : "NONPROFIT";
                signon.fid = exportAsQfx ? "2000" : "0000";
                signon.message = "OK";
                signon.statusCode = "0";
                signon.severity = "INFO";
                signon.dtServer = LocalDateTime.now().format(OFX_DATE_TIME);

                BigDecimal balance = Optional.ofNullable(account.totalAccountBalance(company.getLedger()))
                        .orElse(BigDecimal.ZERO);
                String ledgerAsOf = endDate.format(OFX_DATE) + "T000000.000";

                BankAccountInfo bankInfo = null;
                CreditCardInfo ccInfo = null;

                String accountId = Optional.ofNullable(account.getAccountNumber())
                        .filter(id -> !id.isBlank())
                        .orElse(Optional.ofNullable(account.getName()).orElse("ACCOUNT"));

                if (creditCardAccount)
                {
                        ccInfo = new CreditCardInfo();
                        ccInfo.trnUid = generateUid();
                        ccInfo.accountId = accountId;
                        ccInfo.currency = resolveCurrency(account);
                        ccInfo.startDate = startDate.format(OFX_DATE);
                        ccInfo.endDate = endDate.format(OFX_DATE);
                        ccInfo.statusCode = "0";
                        ccInfo.severity = "INFO";
                }
                else
                {
                        bankInfo = new BankAccountInfo();
                        bankInfo.trnUid = generateUid();
                        bankInfo.bankId = resolveBankId(account);
                        bankInfo.accountId = accountId;
                        bankInfo.accountType = resolveOfxAccountType(account);
                        bankInfo.currency = resolveCurrency(account);
                        bankInfo.startDate = startDate.format(OFX_DATE);
                        bankInfo.endDate = endDate.format(OFX_DATE);
                        bankInfo.statusCode = "0";
                        bankInfo.severity = "INFO";
                }

                OfxV2Writer.writeOfxFile(target.getAbsolutePath(),
                        signon,
                        creditCardAccount ? null : transactions,
                        bankInfo,
                        creditCardAccount ? null : balance,
                        creditCardAccount ? null : ledgerAsOf,
                        creditCardAccount ? transactions : null,
                        ccInfo,
                        creditCardAccount ? balance : null,
                        creditCardAccount ? ledgerAsOf : null,
                        null);
        }

        private boolean isCreditCardAccount(Account account)
        {
                AccountType type = account.getAccountType();
                return type != null && CREDIT_CARD_TYPES.contains(type);
        }

        private String resolveOfxAccountType(Account account)
        {
                AccountType type = account.getAccountType();

                if (type == null)
                {
                        return "CHECKING";
                }

                return switch (type)
                {
                        case MONEYMKRT -> "MONEYMRKT";
                        case CHECKING -> "CHECKING";
                        case CASH -> "CASH";
                        case SIMPLEINVEST, INVEST, MUTUAL -> "SAVINGS";
                        default -> "CHECKING";
                };
        }

        private String resolveCurrency(Account account)
        {
                String currency = account.getCurrency();
                return (currency == null || currency.isBlank()) ? "USD" : currency.trim().toUpperCase(Locale.ROOT);
        }

        private String resolveBankId(Account account)
        {
                String code = account.getAccountCode();
                return (code == null || code.isBlank()) ? "000000000" : code.trim();
        }

        private String generateUid()
        {
                return UUID.randomUUID().toString().replaceAll("-", "");
        }

        private File ensureExtension(File file, String extension)
        {
                String name = file.getName().toLowerCase(Locale.ROOT);

                if (name.endsWith(extension))
                {
                        return file;
                }

                return new File(file.getParentFile(), file.getName() + extension);
        }

        private String sanitizeName(AccountingTransaction txn)
        {
                String name = coalesce(txn.getToFrom(), txn.getMemo(), "Transaction");
                return name.length() > 80 ? name.substring(0, 80) : name;
        }

        private String blankToNull(String value)
        {
                return (value == null || value.isBlank()) ? null : value.trim();
        }

        private String coalesce(String... values)
        {
                for (String value : values)
                {
                        if (value != null && !value.isBlank())
                        {
                                return value.trim();
                        }
                }

                return "";
        }

        private String formatAccountLabel(Account account)
        {
                String number = account.getAccountNumber();
                String name = Optional.ofNullable(account.getName())
                        .filter(n -> !n.isBlank())
                        .orElse(Optional.ofNullable(number).orElse("Account"));

                if (number == null || number.isBlank())
                {
                        return name;
                }

                return name + " (" + number + ")";
        }

        private void showError(String message)
        {
                Alert alert = new Alert(AlertType.ERROR, message);
                alert.initOwner(this.ownerStage);
                alert.setTitle("Export Error");
                alert.setHeaderText(null);
                alert.showAndWait();
        }

        private void showInfo(String message)
        {
                Alert alert = new Alert(AlertType.INFORMATION, message);
                alert.initOwner(this.ownerStage);
                alert.setTitle("Export Transactions");
                alert.setHeaderText(null);
                alert.showAndWait();
        }

        private void showDetailedError(String header, String message)
        {
                Alert alert = new Alert(AlertType.ERROR);
                alert.initOwner(this.ownerStage);
                alert.setTitle("Export Error");
                alert.setHeaderText(header);
                alert.setContentText(message == null || message.isBlank() ?
                        "Please check the application logs for more details." : message);
                alert.showAndWait();
        }

        private record ExportPayload(List<TransactionData> transactions,
                                     LocalDate startDate,
                                     LocalDate endDate)
        {
        }
}

