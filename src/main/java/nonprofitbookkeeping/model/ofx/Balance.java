package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlAccessorType(XmlAccessType.FIELD)
public class Balance {
    @XmlElement(name = "BALAMT")
    public BigDecimal balAmt;

    @XmlElement(name = "DTASOF")
    public String dtAsOf;
}
