package nonprofitbookkeeping.service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ofx.OfxV2Writer;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.BankAccountInfo;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.CreditCardInfo;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.SignonData;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.TransactionData;

/**
 * Handles exporting ledger data to OFX/QFX statement files.
 */
public final class FileExportService
{

private static final Logger LOGGER =
LoggerFactory.getLogger(FileExportService.class);

private static final DateTimeFormatter OFX_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

private static final EnumSet<AccountType> BANK_ACCOUNT_TYPES = EnumSet.of(AccountType.ASSET,
AccountType.BANK,
AccountType.CASH,
AccountType.CHECKING,
AccountType.MONEYMKRT);

private static final EnumSet<AccountType> CREDIT_CARD_ACCOUNT_TYPES = EnumSet.of(AccountType.CREDIT,
AccountType.CREDITCARD,
AccountType.LIABILITY,
AccountType.LONG_TERM_LIABILITY);

private FileExportService()
{
}

/** Supported statement formats. */
public enum StatementFormat
{
/** Standard OFX 2.x XML. */
OFX,
/** Intuit QFX (handled identically to OFX but preserved for clarity). */
QFX
}

/**
 * Result object describing the outcome of an export attempt.
 */
public static final class ExportResult
{

private final boolean success;
private final int transactionsWritten;
private final String message;

ExportResult(boolean success, int transactionsWritten, String message)
{
this.success = success;
this.transactionsWritten = transactionsWritten;
this.message = message;
}

/** @return {@code true} when the export succeeded. */
public boolean success()
{
return this.success;
}

/** @return number of transactions written to the export file. */
public int transactionsWritten()
{
return this.transactionsWritten;
}

/**
 * @return human readable status message. Always non-null.
 */
public String message()
{
return this.message;
}
}

/**
 * Determines whether an account is eligible for statement export.
 *
 * @param account account to inspect
 * @return {@code true} if the account represents a bank or credit-card style ledger
 */
public static boolean isSupportedStatementAccount(Account account)
{
if (account == null)
{
return false;
}

AccountType type = account.getAccountType();

if (type == null)
{
return false;
}

return BANK_ACCOUNT_TYPES.contains(type) || CREDIT_CARD_ACCOUNT_TYPES.contains(type);
}

/**
 * Exports all transactions for the supplied account to an OFX/QFX file.
 *
 * @param destination the file to write (will be replaced if it already exists)
 * @param company     active company providing ledger/chart context
 * @param account     target account to export
 * @param format      desired statement format (OFX or QFX)
 * @return {@link ExportResult} describing success/failure details
 */
public static ExportResult exportAccountStatement(File destination,
Company company,
Account account,
StatementFormat format)
{
if (destination == null)
{
return new ExportResult(false, 0, "Destination file must be provided.");
}

if (company == null)
{
return new ExportResult(false, 0, "No company context available for export.");
}

if (account == null)
{
return new ExportResult(false, 0, "No account selected for export.");
}

if (format == null)
{
return new ExportResult(false, 0, "Export format must be specified.");
}

if (!isSupportedStatementAccount(account))
{
return new ExportResult(false,
0,
"Account type '" + Optional.ofNullable(account.getAccountType()).orElse(AccountType.UNKNOWN)
+ "' is not supported for OFX/QFX export.");
}

Ledger ledger = company.getLedger();

if (ledger == null)
{
return new ExportResult(false, 0, "Company ledger is not available.");
}

List<AccountingTransaction> transactions = ledger.getTransactions();

if (transactions == null || transactions.isEmpty())
{
return new ExportResult(false, 0, "No transactions available to export for this account.");
}

List<AccountingTransaction> sorted = transactions.stream()
.filter(Objects::nonNull)
.sorted(transactionComparator())
.toList();

List<TransactionData> statementEntries = new ArrayList<>();
LocalDate startDate = null;
LocalDate endDate = null;
BigDecimal runningBalance = Optional.ofNullable(account.getOpeningBalance()).orElse(BigDecimal.ZERO);

for (AccountingTransaction tx : sorted)
{
AccountingEntry entry = findEntryForAccount(tx, account.getAccountNumber());

if (entry == null)
{
continue;
}

LocalDate txDate = parseDate(tx.getDate());

if (txDate == null)
{
LOGGER.debug("Skipping transaction without valid date for OFX export: {}",
tx.getMemo());
continue;
}

BigDecimal signedAmount = determineSignedAmount(entry, account);
runningBalance = runningBalance.add(signedAmount);

String ofxDate = txDate.format(OFX_DATE);
String checkNumber = clean(tx.getCheckNumber());
String name = firstNonBlank(tx.getToFrom(), tx.getMemo(), "Transaction");
String memo = firstNonBlank(tx.getMemo(), tx.getToFrom());
String type = determineTransactionType(checkNumber, signedAmount);
String amountString = formatAmount(signedAmount);

statementEntries.add(new TransactionData(type,
ofxDate,
amountString,
checkNumber,
name,
memo));

if (startDate == null || txDate.isBefore(startDate))
{
startDate = txDate;
}

if (endDate == null || txDate.isAfter(endDate))
{
endDate = txDate;
}
}

if (statementEntries.isEmpty())
{
return new ExportResult(false, 0, "No dated transactions found for account '" + account.getName() + "'.");
}

BigDecimal ledgerBalance = Optional.ofNullable(account.totalAccountBalance(ledger)).orElse(BigDecimal.ZERO);

if (account.getIncreaseSide() != AccountSide.DEBIT)
{
ledgerBalance = ledgerBalance.negate();
}

ledgerBalance = ledgerBalance.setScale(Math.max(ledgerBalance.scale(), 2), RoundingMode.HALF_UP);

String start = (startDate != null ? startDate : endDate).format(OFX_DATE);
String end = (endDate != null ? endDate : startDate).format(OFX_DATE);
String asOf = end;

SignonData signon = buildSignon(company, format);
CompanyProfileModel profile = company.getCompanyProfile();

try
{
if (CREDIT_CARD_ACCOUNT_TYPES.contains(account.getAccountType()))
{
CreditCardInfo ccInfo = buildCreditCardInfo(account, profile, start, end);
OfxV2Writer.writeOfxFile(destination.getAbsolutePath(),
signon,
null,
null,
BigDecimal.ZERO,
asOf,
statementEntries,
ccInfo,
ledgerBalance,
asOf,
null);
}
else
{
BankAccountInfo bankInfo = buildBankInfo(account, profile, start, end);
OfxV2Writer.writeOfxFile(destination.getAbsolutePath(),
signon,
statementEntries,
bankInfo,
ledgerBalance,
asOf,
null,
null,
BigDecimal.ZERO,
asOf,
null);
}
}
catch (Exception ex)
{
LOGGER.error("Failed to export OFX/QFX file", ex);
return new ExportResult(false, 0, "Failed to write OFX/QFX file: " + ex.getMessage());
}

return new ExportResult(true,
statementEntries.size(),
String.format(Locale.US,
"Exported %d transactions to %s",
statementEntries.size(),
destination.getName()));
}

private static Comparator<AccountingTransaction> transactionComparator()
{
return Comparator.comparing((AccountingTransaction tx) -> parseDate(tx.getDate()),
Comparator.nullsLast(Comparator.naturalOrder()))
.thenComparing(tx -> Optional.ofNullable(tx.getBookingDateTimestamp()).orElse(0L));
}

private static AccountingEntry findEntryForAccount(AccountingTransaction tx, String accountNumber)
{
if (tx == null || tx.getEntries() == null || accountNumber == null)
{
return null;
}

for (AccountingEntry entry : tx.getEntries())
{
if (entry != null && accountNumber.equals(entry.getAccountNumber()))
{
return entry;
}
}

return null;
}

private static LocalDate parseDate(String raw)
{
if (raw == null || raw.isBlank())
{
return null;
}

try
{
return LocalDate.parse(raw.trim());
}
catch (DateTimeParseException ex)
{
LOGGER.debug("Unable to parse transaction date '{}': {}",
raw,
ex.getMessage());
return null;
}
}

private static BigDecimal determineSignedAmount(AccountingEntry entry, Account account)
{
BigDecimal amount = Optional.ofNullable(entry.getAmount()).orElse(BigDecimal.ZERO);

if (entry.getAccountSide() != account.getIncreaseSide())
{
amount = amount.negate();
}

if (account.getIncreaseSide() != AccountSide.DEBIT)
{
amount = amount.negate();
}

return amount;
}

private static String determineTransactionType(String checkNumber, BigDecimal amount)
{
if (checkNumber != null && !checkNumber.isBlank())
{
return "CHECK";
}

return amount.signum() >= 0 ? "CREDIT" : "DEBIT";
}

private static String formatAmount(BigDecimal amount)
{
    int scale = Math.max(amount.scale(), 2);
    BigDecimal scaled = amount.setScale(scale, RoundingMode.HALF_UP);
    return scaled.toPlainString();
}

private static String clean(String value)
{
if (value == null)
{
return null;
}

String trimmed = value.trim();
return trimmed.isEmpty() ? null : trimmed;
}

private static String firstNonBlank(String... values)
{
if (values == null)
{
return "";
}

for (String value : values)
{
if (value != null)
{
String trimmed = value.trim();

if (!trimmed.isEmpty())
{
return trimmed;
}
}
}

return "";
}

private static SignonData buildSignon(Company company, StatementFormat format)
{
SignonData signon = new SignonData();
CompanyProfileModel profile = company.getCompanyProfile();
String companyName = (profile != null) ? clean(profile.getCompanyName()) : null;

signon.language = "ENG";
signon.org = Optional.ofNullable(companyName).orElse("NonprofitAccounting");
signon.fid = (format == StatementFormat.QFX) ? "INTUIT" : Optional.ofNullable(companyName)
.orElse("NONPROFIT");
signon.message = (format == StatementFormat.QFX) ? "QFX Export" : "OFX Export";
signon.dtServer = LocalDate.now().format(OFX_DATE) + "T000000.000";
signon.statusCode = "0";
signon.severity = "INFO";
return signon;
}

private static BankAccountInfo buildBankInfo(Account account,
CompanyProfileModel profile,
String startDate,
String endDate)
{
BankAccountInfo info = new BankAccountInfo();
info.trnUid = UUID.randomUUID().toString();
info.accountId = Optional.ofNullable(account.getAccountNumber()).orElse("0000");
info.bankId = determineBankId(account, profile);
info.accountType = resolveBankAccountType(account);
info.currency = resolveCurrency(account, profile);
info.startDate = startDate;
info.endDate = endDate;
info.statusCode = "0";
info.severity = "INFO";
return info;
}

private static CreditCardInfo buildCreditCardInfo(Account account,
CompanyProfileModel profile,
String startDate,
String endDate)
{
CreditCardInfo info = new CreditCardInfo();
info.trnUid = UUID.randomUUID().toString();
info.accountId = Optional.ofNullable(account.getAccountNumber()).orElse("0000");
info.currency = resolveCurrency(account, profile);
info.startDate = startDate;
info.endDate = endDate;
info.statusCode = "0";
info.severity = "INFO";
return info;
}

private static String resolveBankAccountType(Account account)
{
AccountType type = account.getAccountType();

if (type == null)
{
return "CHECKING";
}

return switch (type)
{
case MONEYMKRT -> "MONEYMRKT";
case CASH -> "CASH";
case CHECKING, BANK -> "CHECKING";
default -> "CHECKING";
};
}

private static String resolveCurrency(Account account, CompanyProfileModel profile)
{
String accountCurrency = clean(account.getCurrency());

if (accountCurrency != null && accountCurrency.length() == 3)
{
return accountCurrency.toUpperCase(Locale.US);
}

if (profile != null && profile.getBaseCurrency() != null && profile.getBaseCurrency().length() == 3)
{
return profile.getBaseCurrency().toUpperCase(Locale.US);
}

return "USD";
}

private static String determineBankId(Account account, CompanyProfileModel profile)
{
if (account == null)
{
return "000000000";
}

String code = clean(account.getAccountCode());

if (code != null && !code.isEmpty())
{
return code;
}

if (profile != null)
{
String defaultBank = clean(profile.getDefaultBankAccount());

if (defaultBank != null)
{
return defaultBank;
}
}

return "000000000";
}
}
