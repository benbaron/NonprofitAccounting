
package nonprofitbookkeeping.importer.sclx.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import nonprofitbookkeeping.model.records.BankStatementRecord;
import nonprofitbookkeeping.model.records.BankingItemRecord;
import nonprofitbookkeeping.model.records.BudgetRecord;

/**
 * Registers Jackson adapters for the final impex records derived from SCLX.
 */
public final class SclxImpexJacksonModule extends SimpleModule
{
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 6433368221907844841L;

	public SclxImpexJacksonModule()
	{
		super("SclxImpexJacksonModule");
		addDeserializer(BankStatementRecord.class,
			new BankStatementRecordDeserializer());
		addDeserializer(BudgetRecord.class, new BudgetRecordDeserializer());
		addDeserializer(BankingItemRecord.class,
			new BankingItemRecordDeserializer());
		
	}
	
}
