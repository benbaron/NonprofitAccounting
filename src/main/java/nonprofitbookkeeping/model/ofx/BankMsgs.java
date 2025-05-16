package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class BankMsgs {
    @XmlElement(name = "STMTTRNRS")
    public StatementTransactionResponse stmtTrnrs;
}
