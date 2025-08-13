package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;
import java.util.List;

/**
 * Represents a list of bank account transactions for a specified period,
 * as typically found in OFX financial data. This includes the start and end dates
 * of the transaction list and the transactions themselves.
 * This class is designed for JAXB marshalling and unmarshalling of OFX XML elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BankTransactionList 
{
    /**
     * The start date of the transaction list.
     * Corresponds to the OFX tag {@code <DTSTART>}.
     * The format is typically a string like "YYYYMMDDHHMMSS[.mmm][Timezone]" or "YYYYMMDD".
     */
    @XmlElement(name = "DTSTART")
    public String dtStart;

    /**
     * The end date of the transaction list.
     * Corresponds to the OFX tag {@code <DTEND>}.
     * The format is typically a string like "YYYYMMDDHHMMSS[.mmm][Timezone]" or "YYYYMMDD".
     */
    @XmlElement(name = "DTEND")
    public String dtEnd;

    /**
     * A list of statement transactions ({@link Transaction}) included in this list.
     * Corresponds to one or more OFX {@code <STMTTRN>} aggregates.
     */
    @XmlElement(name = "STMTTRN")
    public List<Transaction> stmtTrns;
}
