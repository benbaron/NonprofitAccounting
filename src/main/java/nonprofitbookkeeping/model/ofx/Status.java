package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Status {
    @XmlElement(name = "CODE")
    public int code;

    @XmlElement(name = "SEVERITY")
    public String severity;

    @XmlElement(name = "MESSAGE")
    public String message;
}
