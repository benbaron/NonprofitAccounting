package nonprofitbookkeeping.importer.sclx;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NonprofitBookkeepingSclxImportTargetTest
{
    @Test
    void mapTransaction_populatesToFromAndCheckNumberFromSclxFields() throws Exception
    {
        NonprofitBookkeepingSclxImportTarget target = new NonprofitBookkeepingSclxImportTarget();
        SclxDocument document = new SclxDocument(
            "SCLX",
            "1.3",
            null,
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(new SclxDocument.OutstandingItem(
                "oi-1",
                "PAYABLE",
                null,
                new SclxDocument.WorkbookLink("q4", 57),
                null,
                null,
                "CHK-1201",
                null,
                "Erin P.",
                null,
                null,
                null,
                BigDecimal.TEN,
                null,
                null,
                null,
                "OPEN",
                Map.of())),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            Map.of());

        target.beginImport(document, SclxImportOptions.defaults());

        SclxDocument.Transaction txn = new SclxDocument.Transaction(
            "txn-1",
            LocalDate.of(2026, 12, 31),
            LocalDate.of(2026, 12, 31),
            "NSF original",
            "CHK-1201",
            null,
            null,
            null,
            null,
            "POSTED",
            "MANUAL",
            "NOW",
            "NOW",
            null,
            new SclxDocument.WorkbookLink("q4", 57),
            null,
            List.of(),
            null,
            List.of(
                new SclxDocument.TransactionLine(
                    "l1",
                    "1000",
                    BigDecimal.TEN,
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    Map.of()),
                new SclxDocument.TransactionLine(
                    "l2",
                    "2000",
                    BigDecimal.ZERO,
                    BigDecimal.TEN,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    Map.of())),
            Map.of("workbook", Map.of("personOrBusinessName", "Erin P.")));

        Method method = NonprofitBookkeepingSclxImportTarget.class
            .getDeclaredMethod("mapTransaction", SclxDocument.Transaction.class);
        method.setAccessible(true);

        Object mappedTxn = method.invoke(target, txn);
        Method getToFrom = mappedTxn.getClass().getMethod("getToFrom");
        Method getCheckNumber = mappedTxn.getClass().getMethod("getCheckNumber");

        assertEquals("Erin P.", getToFrom.invoke(mappedTxn));
        assertEquals("CHK-1201", getCheckNumber.invoke(mappedTxn));
    }
}
