package nonprofitbookkeeping.importer.sclx.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.importer.sclx.mapping.BankStatementRecordMapper;
import nonprofitbookkeeping.model.impex.BankStatementRecord;

import java.io.IOException;

/**
 * Jackson adapter for BankStatementRecord.
 */
public final class BankStatementRecordDeserializer extends JsonDeserializer<BankStatementRecord> {

    @Override
    public BankStatementRecord deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        return new BankStatementRecordMapper(mapper).fromSclx(JacksonJsonNodeSupport.readTree(parser));
    }
}
