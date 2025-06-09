package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

/**
 * Represents bank account information, commonly found in OFX data.
 * This includes identifiers for the bank, the account itself, and the type of account.
 * This class is designed for JAXB marshalling and unmarshalling of OFX XML elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BankAccountInfo {
    /**
     * The bank identifier (e.g., routing number).
     * Corresponds to the OFX tag {@code <BANKID>}.
     */
    @XmlElement(name = "BANKID")
    public String bankId;

    /**
     * The account identifier (i.e., account number).
     * Corresponds to the OFX tag {@code <ACCTID>}.
     */
    @XmlElement(name = "ACCTID")
    public String acctId;

    /**
     * The type of account (e.g., CHECKING, SAVINGS, MONEYMRKT, CREDITLINE).
     * Corresponds to the OFX tag {@code <ACCTTYPE>}.
     */
    @XmlElement(name = "ACCTTYPE")
    public String acctType;
}
