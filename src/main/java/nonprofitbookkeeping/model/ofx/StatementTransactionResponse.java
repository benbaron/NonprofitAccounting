package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class StatementTransactionResponse {
    @XmlElement(name = "TRNUID")
    public String trnUid;

    @XmlElement(name = "STATUS")
    public Status status;

    @XmlElement(name = "STMTRS")
    public StatementResponse stmtRs;
}
