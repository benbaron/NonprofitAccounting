
package nonprofitbookkeeping.importer.sclx.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import nonprofitbookkeeping.model.impex.BankStatementRecord;
import nonprofitbookkeeping.model.impex.BankingItemRecord;
import nonprofitbookkeeping.model.impex.BudgetRecord;

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
