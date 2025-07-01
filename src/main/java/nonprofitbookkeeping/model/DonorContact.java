package nonprofitbookkeeping.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Simple contact information for a donor. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonorContact implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String email;
    private String phone;
}
