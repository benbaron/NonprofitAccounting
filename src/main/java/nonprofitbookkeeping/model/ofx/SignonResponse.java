package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class SignonResponse {
    @XmlElement(name = "STATUS")
    public Status status;

    @XmlElement(name = "DTSERVER")
    public String dtServer;

    @XmlElement(name = "LANGUAGE")
    public String language;

    @XmlElement(name = "FI")
    public FinancialInstitution fi;
}
