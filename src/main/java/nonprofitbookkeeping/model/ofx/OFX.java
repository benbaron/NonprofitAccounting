package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "OFX")
@XmlAccessorType(XmlAccessType.FIELD)
public class OFX {
    @XmlElement(name = "SIGNONMSGSRSV1")
    public SignonMsgs signonMsgs;

    @XmlElement(name = "BANKMSGSRSV1")
    public BankMsgs bankMsgs;
}
