package nonprofitbookkeeping.model.ofx;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link OfxV2Writer} to ensure the investment section includes
 * positions and transactions.
 */
class OfxV2WriterTest
{

        @TempDir
        Path tempDir;

        @Test
        void writeInvestmentSectionIncludesPositionsAndTransactions() throws Exception
        {
                Path output = tempDir.resolve("investment.ofx");

                OfxV2Writer.SignonData signon = new OfxV2Writer.SignonData();
                signon.language = "ENG";
                signon.org = "DemoFI";
                signon.fid = "12345";
                signon.message = "OK";
                signon.dtServer = "20240101T000000.000";
                signon.statusCode = "0";
                signon.severity = "INFO";

                OfxV2Writer.InvestmentData investment = new OfxV2Writer.InvestmentData();
                investment.trnUid = "INV-1";
                investment.brokerId = "MyBroker";
                investment.accountId = "ACC-123";
                investment.currency = "USD";
                investment.dtAsOf = "20240131";
                investment.statusCode = "0";
                investment.severity = "INFO";
                investment.availableCash = new BigDecimal("2500.00");
                investment.marketValue = new BigDecimal("1502.50");
                investment.transactionDateStart = "20240101";
                investment.transactionDateEnd = "20240131";

                SecurityNode security = new SecurityNode("US1234567890", "AAPL");

                OfxV2Writer.InvestmentPosition position = new OfxV2Writer.InvestmentPosition();
                position.security = security;
                position.units = new BigDecimal("10");
                position.unitPrice = new BigDecimal("150.25");
                position.marketValue = new BigDecimal("1502.50");
                position.priceAsOf = "20240131";
                position.positionType = "LONG";
                investment.positions.add(position);

                InvestmentTransaction transaction = new InvestmentTransaction("FIT123", "20240105",
                        "Buy shares", "AAPL Purchase", "BUY",
                        security, new BigDecimal("5"), new BigDecimal("150.25"), new BigDecimal("10.00"));
                investment.transactions.add(transaction);

                OfxV2Writer.writeOfxFile(output.toString(), signon,
                        null, null, null, null,
                        null, null, null, null,
                        investment);

                String content = Files.readString(output);

                assertTrue(content.contains("<INVPOSLIST>"));
                assertTrue(content.contains("<POSSTOCK>"));
                assertTrue(content.contains("<UNITS>10</UNITS>"));
                assertTrue(content.contains("<UNITPRICE>150.25</UNITPRICE>"));
                assertTrue(content.contains("<MKTVAL>1502.5</MKTVAL>"));
                assertTrue(content.contains("<INVTRANLIST>"));
                assertTrue(content.contains("<INVBUY>"));
                assertTrue(content.contains("<DTSTART>20240101</DTSTART>"));
                assertTrue(content.contains("<DTEND>20240131</DTEND>"));
                assertTrue(content.contains("<TOTAL>761.25</TOTAL>"));
                assertTrue(content.contains("<FEES>10</FEES>"));
                assertTrue(content.contains("<UNIQUEID>US1234567890</UNIQUEID>"));
                assertTrue(content.contains("<DTTRADE>20240105</DTTRADE>"));
        }
}
