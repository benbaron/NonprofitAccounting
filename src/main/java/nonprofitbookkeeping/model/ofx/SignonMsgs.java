package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class SignonMsgs {
    @XmlElement(name = "SONRS")
    public SignonResponse sonrs;
}
