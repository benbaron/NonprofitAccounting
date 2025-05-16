package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class BankAccountInfo {
    @XmlElement(name = "BANKID")
    public String bankId;

    @XmlElement(name = "ACCTID")
    public String acctId;

    @XmlElement(name = "ACCTTYPE")
    public String acctType;
}
