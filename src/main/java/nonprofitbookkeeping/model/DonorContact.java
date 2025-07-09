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
	/**  
	 * Constructor DonorContact
	 * @param object
	 * @param text
	 * @param text2
	 * @param text3
	 */
	public DonorContact(Object object, String text, String text2, String text3)
	{
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the name
	 */
	public String getName()
	{
		
		return this.name;
		
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		
		this.name = name;
		
	}
	/**
	 * @return
	 */
	public String getEmail()
	{
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @return
	 */
	public String getId()
	{
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @return
	 */
	public String getPhone()
	{
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @param email2
	 */
	public void setEmail(Object email2)
	{
		// TODO Auto-generated method stub
		
	}
	/**
	 * @param phone2
	 */
	public void setPhone(Object phone2)
	{
		// TODO Auto-generated method stub
		
	}
}
