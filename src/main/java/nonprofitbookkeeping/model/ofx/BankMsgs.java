package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

/**
 * Represents the banking messages section within an OFX response,
 * specifically containing a statement transaction response.
 * This class is designed for JAXB marshalling and unmarshalling of OFX XML elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BankMsgs {
    /**
     * The statement transaction response, which includes details about
     * account statements and transactions.
     * Corresponds to the OFX aggregate {@code <STMTTRNRS>}.
     */
    @XmlElement(name = "STMTTRNRS")
    public StatementTransactionResponse stmtTrnrs;
}
