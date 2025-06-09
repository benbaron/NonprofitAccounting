package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

/**
 * Represents the Statement Transaction Response (STMTTRNRS) aggregate in an OFX file.
 * This is a wrapper that includes a transaction UID, the status of the response,
 * and the actual statement response (STMTRS) containing account and transaction details.
 * This class is designed for JAXB marshalling and unmarshalling of OFX XML elements.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StatementTransactionResponse {
    /**
     * The transaction UID, a unique identifier for this response.
     * Corresponds to the OFX tag {@code <TRNUID>}.
     */
    @XmlElement(name = "TRNUID")
    public String trnUid;

    /**
     * The status of processing the statement request.
     * Corresponds to the OFX aggregate {@code <STATUS>}.
     */
    @XmlElement(name = "STATUS")
    public Status status;

    /**
     * The actual statement response containing account details and transactions.
     * Corresponds to the OFX aggregate {@code <STMTRS>}.
     */
    @XmlElement(name = "STMTRS")
    public StatementResponse stmtRs;
}
