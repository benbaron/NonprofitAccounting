
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.ContactInfoBean;

public class ContactInfoJasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
                return "jrxml/sca-reports/CONTACT_INFO_1_fixed_labeled.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "ContactInfo";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<ContactInfoBean> getReportData()
        {
                ContactInfoBean bean = new ContactInfoBean();
                bean.setPrimaryLegalName("John Doe");
                bean.setPrimaryStreetAddress("123 Main St");
                bean.setPrimaryCity("Anytown");
                bean.setPrimaryStateOrProvince("CA");
                bean.setPrimaryPostalCode("12345");
                bean.setPrimaryHomeTelephone("555-1111");
                return java.util.Collections.singletonList(bean);

        }
	
	
	
}
