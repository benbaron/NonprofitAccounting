package nonprofitbookkeeping.model.ofx;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OfxV2WriterTest
{
        @TempDir
        Path tempDir;

        @Test
        void writeOfxFile_includesInvestmentPositionsAndTransactions() throws Exception
        {
                Path output = this.tempDir.resolve("investment.ofx");

                OfxV2Writer.SignonData signon = new OfxV2Writer.SignonData();
                signon.language = "ENG";
                signon.org = "TestBroker";
                signon.fid = "12345";
                signon.message = "OK";
                signon.dtServer = "20240101T000000.000";
                signon.statusCode = "0";
                signon.severity = "INFO";

                OfxV2Writer.SecurityData security = new OfxV2Writer.SecurityData();
                security.uniqueId = "XYZ123456";
                security.uniqueIdType = "CUSIP";
                security.name = "Example Index";
                security.ticker = "EXIDX";

                OfxV2Writer.InvestmentPosition position = new OfxV2Writer.InvestmentPosition();
                position.positionType = "POSSTOCK";
                position.security = security;
                position.heldInAccount = "CASH";
                position.positionTypeIndicator = "LONG";
                position.units = new BigDecimal("10");
                position.unitPrice = new BigDecimal("150.25");
                position.marketValue = new BigDecimal("1502.50");
                position.costBasis = new BigDecimal("1200.00");
                position.priceAsOf = "20240101";
                position.memo = "Core holding";

                OfxV2Writer.InvestmentTransactionData txn = new OfxV2Writer.InvestmentTransactionData();
                txn.transactionType = "BUYSTOCK";
                txn.fitId = "INV-0001";
                txn.tradeDate = "20231215";
                txn.settleDate = "20231218";
                txn.memo = "Purchase";
                txn.security = security;
                txn.subAccountSecurity = "CASH";
                txn.subAccountFund = "CASH";
                txn.units = new BigDecimal("10");
                txn.unitPrice = new BigDecimal("150.25");
                txn.total = new BigDecimal("1502.50");
                txn.commission = new BigDecimal("10.00");
                txn.fees = new BigDecimal("2.00");
                txn.taxes = new BigDecimal("1.50");

                OfxV2Writer.InvestmentData investment = new OfxV2Writer.InvestmentData();
                investment.trnUid = "7890";
                investment.brokerId = "BROKER";
                investment.accountId = "INV-001";
                investment.currency = "USD";
                investment.dtAsOf = "20240101T000000.000";
                investment.statusCode = "0";
                investment.severity = "INFO";
                investment.availableCash = new BigDecimal("2500.00");
                investment.marketValue = new BigDecimal("5000.00");
                investment.cashAsOf = "20240101";
                investment.marketValueAsOf = "20240101";
                investment.transactionStartDate = "20231201";
                investment.transactionEndDate = "20240101";
                investment.positions.add(position);
                investment.transactions.add(txn);

                OfxV2Writer.writeOfxFile(output.toString(),
                        signon,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        investment);

                String content = Files.readString(output);
                assertTrue(content.contains("<INVPOSLIST>"));
                assertTrue(content.contains("<POSSTOCK>"));
                assertTrue(content.contains("<INVPOS>"));
                assertTrue(content.contains("<UNITS>10</UNITS>"));
                assertTrue(content.contains("<INVTRANLIST>"));
                assertTrue(content.contains("<BUYSTOCK>"));
                assertTrue(content.contains("<INVBUY>"));
                assertTrue(content.contains("<INVTRAN>"));
                assertTrue(content.contains("<FITID>INV-0001</FITID>"));
                assertTrue(content.contains("<SECID>"));
                assertTrue(content.contains("<UNIQUEID>XYZ123456</UNIQUEID>"));
                assertTrue(content.contains("<COMMISSION>10.00</COMMISSION>"));
        }
}
