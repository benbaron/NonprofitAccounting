package nonprofitbookkeeping.model.ofx;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import nonprofitbookkeeping.model.ofx.OfxV2Writer.InvestmentData;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.InvestmentPosition;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.InvestmentTransactionData;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.SecurityData;
import nonprofitbookkeeping.model.ofx.OfxV2Writer.SignonData;

class OfxV2WriterTest
{
        @TempDir
        Path tempDir;

        @Test
        void writeOfxFileIncludesInvestmentPositionsAndTransactions() throws Exception
        {
                Path output = this.tempDir.resolve("investment.ofx");

                SignonData signon = new SignonData();
                signon.language = "ENG";
                signon.org = "NonprofitAccounting";
                signon.fid = "NONPROFIT";
                signon.message = "OK";
                signon.dtServer = "20240131T000000.000";
                signon.statusCode = "0";
                signon.severity = "INFO";

                InvestmentData investment = new InvestmentData();
                investment.trnUid = "INV-001";
                investment.statusCode = "0";
                investment.severity = "INFO";
                investment.statusMessage = "Investment export";
                investment.brokerId = "BRK";
                investment.accountId = "ACC-123";
                investment.currency = "USD";
                investment.dtAsOf = "20240131T000000.000";
                investment.availableCash = new BigDecimal("1500.00");
                investment.cashAsOf = "20240131T000000.000";
                investment.marketValue = new BigDecimal("8200.50");
                investment.marketValueAsOf = "20240131T000000.000";
                investment.transactionStartDate = "20240101";
                investment.transactionEndDate = "20240131";

                SecurityData security = new SecurityData();
                security.uniqueId = "ABC123456";
                security.uniqueIdType = "CUSIP";
                security.name = "Example Growth Fund";
                security.ticker = "EGF";

                InvestmentPosition position = new InvestmentPosition();
                position.positionType = "POSSTOCK";
                position.security = security;
                position.heldInAccount = "CASH";
                position.positionTypeIndicator = "LONG";
                position.units = new BigDecimal("10");
                position.unitPrice = new BigDecimal("500.05");
                position.marketValue = new BigDecimal("5000.50");
                position.costBasis = new BigDecimal("4500.00");
                position.priceAsOf = "20240131";
                position.memo = "Long-term holding";
                investment.positions.add(position);

                InvestmentTransactionData transaction = new InvestmentTransactionData();
                transaction.transactionType = "BUYSTOCK";
                transaction.fitId = "FIT123";
                transaction.tradeDate = "20240115";
                transaction.settleDate = "20240117";
                transaction.memo = "Initial purchase";
                transaction.security = security;
                transaction.subAccountSecurity = "CASH";
                transaction.subAccountFund = "BROKERAGE";
                transaction.units = new BigDecimal("10");
                transaction.unitPrice = new BigDecimal("500.05");
                transaction.total = new BigDecimal("5000.50");
                transaction.commission = new BigDecimal("7.00");
                transaction.fees = new BigDecimal("3.00");
                transaction.taxes = BigDecimal.ZERO;
                investment.transactions.add(transaction);

                OfxV2Writer.writeOfxFile(output.toString(),
                        signon,
                        null,
                        null,
                        BigDecimal.ZERO,
                        "20240131",
                        null,
                        null,
                        BigDecimal.ZERO,
                        "20240131",
                        investment);

                String ofx = Files.readString(output, StandardCharsets.UTF_8);

                assertTrue(ofx.contains("<INVSTMTMSGSRSV1>"), "Investment section missing");
                assertTrue(ofx.contains("<INVPOSLIST>"), "Position list missing");
                assertTrue(ofx.contains("<POSSTOCK>"), "Stock position missing");
                assertTrue(ofx.contains("<UNIQUEID>ABC123456</UNIQUEID>"), "Security ID missing");
                assertTrue(ofx.contains("<INVTRANLIST>"), "Transaction list missing");
                assertTrue(ofx.contains("<BUYSTOCK>"), "Buy transaction missing");
                assertTrue(ofx.contains("<FITID>FIT123</FITID>"), "Transaction FITID missing");
                assertTrue(ofx.contains("<TOTAL>5000.5") || ofx.contains("<TOTAL>5000.50"), "Transaction total missing");
                assertTrue(ofx.contains("<MKTVAL>8200.5") || ofx.contains("<MKTVAL>8200.50"), "Market value missing");
        }
}
