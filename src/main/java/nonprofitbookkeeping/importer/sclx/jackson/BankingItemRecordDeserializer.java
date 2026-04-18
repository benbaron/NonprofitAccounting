package nonprofitbookkeeping.importer.sclx.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import nonprofitbookkeeping.importer.sclx.mapping.BankingItemRecordMapper;
import nonprofitbookkeeping.model.records.BankingItemRecord;

import java.io.IOException;

/**
 * Jackson adapter for BankingItemRecord.
 */
public final class BankingItemRecordDeserializer extends JsonDeserializer<BankingItemRecord> {

    @Override
    public BankingItemRecord deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        return new BankingItemRecordMapper(mapper).fromSclx(JacksonJsonNodeSupport.readTree(parser));
    }
}
