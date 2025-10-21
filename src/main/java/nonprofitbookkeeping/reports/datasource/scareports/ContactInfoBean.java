package nonprofitbookkeeping.reports.datasource.scareports;

/**
 * Simple JavaBean backing the Contact Info report with semantic field names.
 */
public class ContactInfoBean
{
        private String primaryLegalName;
        private String primaryStreetAddress;
        private String primaryCity;
        private String primaryStateOrProvince;
        private String primaryPostalCode;
        private String primaryHomeTelephone;

        public String getPrimaryLegalName()
        {
                return this.primaryLegalName;
        }

        public void setPrimaryLegalName(String primaryLegalName)
        {
                this.primaryLegalName = primaryLegalName;
        }

        public String getPrimaryStreetAddress()
        {
                return this.primaryStreetAddress;
        }

        public void setPrimaryStreetAddress(String primaryStreetAddress)
        {
                this.primaryStreetAddress = primaryStreetAddress;
        }

        public String getPrimaryCity()
        {
                return this.primaryCity;
        }

        public void setPrimaryCity(String primaryCity)
        {
                this.primaryCity = primaryCity;
        }

        public String getPrimaryStateOrProvince()
        {
                return this.primaryStateOrProvince;
        }

        public void setPrimaryStateOrProvince(String primaryStateOrProvince)
        {
                this.primaryStateOrProvince = primaryStateOrProvince;
        }

        public String getPrimaryPostalCode()
        {
                return this.primaryPostalCode;
        }

        public void setPrimaryPostalCode(String primaryPostalCode)
        {
                this.primaryPostalCode = primaryPostalCode;
        }

        public String getPrimaryHomeTelephone()
        {
                return this.primaryHomeTelephone;
        }

        public void setPrimaryHomeTelephone(String primaryHomeTelephone)
        {
                this.primaryHomeTelephone = primaryHomeTelephone;
        }
}
