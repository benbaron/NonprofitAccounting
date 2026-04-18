
package nonprofitbookkeeping.ui.actions.scaledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.junit.jupiter.api.Test;

import nonprofitbookkeeping.model.records.ExcelLedgerRow;

class ImportFromExcelActionFXTest
{
	@Test
	void buildTableModelAddsAllocationColumns()
	{
		ExcelLedgerRow row = new ExcelLedgerRow();
		row.setBalance(new BigDecimal("100.25"));
		row.setDate(LocalDate.of(2024, 5, 10));
		row.setCheckNumber("1234");
		row.setClearBank("Bank A");
		row.setToFrom("Vendor");
		row.setMemoNotes("Office supplies");
		row.setBudgetTracking("Ops");
		row.setNetTotal(new BigDecimal("100.25"));
		
		ExcelLedgerRow.Allocation alloc1 = new ExcelLedgerRow.Allocation();
		alloc1.setAmount(new BigDecimal("75.00"));
		alloc1.setAssetLiabilityAccount("1000");
		alloc1.setIncomeCategory("4000");
		alloc1.setExpenseCategory("5000");
		alloc1.setFund("General");
		
		ExcelLedgerRow.Allocation alloc2 = new ExcelLedgerRow.Allocation();
		alloc2.setAmount(new BigDecimal("25.25"));
		alloc2.setAssetLiabilityAccount("1001");
		alloc2.setIncomeCategory("4001");
		alloc2.setExpenseCategory("5001");
		alloc2.setFund("Restricted");
		
		row.getAllocations().add(alloc1);
		row.getAllocations().add(alloc2);
		
		DefaultTableModel model =
			ImportFromOutlandsLedgerActionFX.buildTableModel(List.of(row));
		
		assertEquals(1, model.getRowCount());
		assertEquals(8 + (2 * 5), model.getColumnCount());
		assertEquals(new BigDecimal("100.25"), model.getValueAt(0, 0));
		assertEquals(LocalDate.of(2024, 5, 10), model.getValueAt(0, 1));
		assertEquals("1234", model.getValueAt(0, 2));
		assertEquals("Bank A", model.getValueAt(0, 3));
		assertEquals("Vendor", model.getValueAt(0, 4));
		assertEquals("Office supplies", model.getValueAt(0, 5));
		assertEquals("Ops", model.getValueAt(0, 6));
		assertEquals(new BigDecimal("100.25"), model.getValueAt(0, 7));
		
		assertEquals(new BigDecimal("75.00"), model.getValueAt(0, 8));
		assertEquals("1000", model.getValueAt(0, 9));
		assertEquals("4000", model.getValueAt(0, 10));
		assertEquals("5000", model.getValueAt(0, 11));
		assertEquals("General", model.getValueAt(0, 12));
		
		assertEquals(new BigDecimal("25.25"), model.getValueAt(0, 13));
		assertEquals("1001", model.getValueAt(0, 14));
		assertEquals("4001", model.getValueAt(0, 15));
		assertEquals("5001", model.getValueAt(0, 16));
		assertEquals("Restricted", model.getValueAt(0, 17));
		
		assertFalse(model.isCellEditable(0, 0));
		
	}
	
	@Test
	void buildTableModelHandlesMissingAllocations()
	{
		ExcelLedgerRow row = new ExcelLedgerRow();
		row.setBalance(null);
		row.setDate(null);
		row.setCheckNumber(null);
		row.setClearBank(null);
		row.setToFrom(null);
		row.setMemoNotes(null);
		row.setBudgetTracking(null);
		row.setNetTotal(null);
		
		row.getAllocations().add(null);
		
		DefaultTableModel model =
			ImportFromOutlandsLedgerActionFX.buildTableModel(List.of(row));
		
		assertEquals(1, model.getRowCount());
		assertEquals(13, model.getColumnCount());
		assertNull(model.getValueAt(0, 8));
		assertNull(model.getValueAt(0, 12));
		
	}
	
}
