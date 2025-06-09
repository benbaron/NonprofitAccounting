package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

/**
 * Represents the Statement Response (STMTRS) aggregate in an OFX banking message.
 * This typically includes the default currency for the statement, account information,
 * a list of transactions, and the ledger balance.
 * This class is designed for JAXB marshalling and unmarshalling of OFX XML elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StatementResponse {
    /**
     * The default currency for the amounts in this statement.
     * Corresponds to the OFX tag {@code <CURDEF>}. (e.g., "USD")
     */
    @XmlElement(name = "CURDEF")
    public String curDef;

    /**
     * Information about the bank account from which the statement is generated.
     * Corresponds to the OFX aggregate {@code <BANKACCTFROM>}.
     */
    @XmlElement(name = "BANKACCTFROM")
    public BankAccountInfo bankAcctFrom;

    /**
     * The list of bank transactions included in this statement.
     * Corresponds to the OFX aggregate {@code <BANKTRANLIST>}.
     */
    @XmlElement(name = "BANKTRANLIST")
    public BankTransactionList bankTranList;

    /**
     * The ledger balance of the account.
     * Corresponds to the OFX aggregate {@code <LEDGERBAL>}.
     */
    @XmlElement(name = "LEDGERBAL")
    public Balance ledgerBal;
}
