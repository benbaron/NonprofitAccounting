package nonprofitbookkeeping.importer.sclx.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.importer.sclx.mapping.BudgetRecordMapper;
import nonprofitbookkeeping.model.impex.BudgetRecord;

import java.io.IOException;

/**
 * Jackson adapter for BudgetRecord.
 */
public final class BudgetRecordDeserializer extends JsonDeserializer<BudgetRecord> {

    @Override
    public BudgetRecord deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        return new BudgetRecordMapper(mapper).fromSclx(JacksonJsonNodeSupport.readTree(parser));
    }
}
