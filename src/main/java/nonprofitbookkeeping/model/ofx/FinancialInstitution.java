package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class FinancialInstitution {
    @XmlElement(name = "ORG")
    public String org;

    @XmlElement(name = "FID")
    public String fid;
}
