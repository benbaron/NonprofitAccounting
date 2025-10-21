package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Data bean that mirrors the columns exposed by the Ledger Q1 Jasper template.
 */
public class LedgerQ1Bean
{
        private String col_0;
        private String col_1;
        private String col_2;
        private String col_3;
        private String col_4;
        private BigDecimal total;

        public String getCol_0()
        {
                return this.col_0;
        }

        public void setCol_0(String col_0)
        {
                this.col_0 = col_0;
        }

        public String getCol_1()
        {
                return this.col_1;
        }

        public void setCol_1(String col_1)
        {
                this.col_1 = col_1;
        }

        public String getCol_2()
        {
                return this.col_2;
        }

        public void setCol_2(String col_2)
        {
                this.col_2 = col_2;
        }

        public String getCol_3()
        {
                return this.col_3;
        }

        public void setCol_3(String col_3)
        {
                this.col_3 = col_3;
        }

        public String getCol_4()
        {
                return this.col_4;
        }

        public void setCol_4(String col_4)
        {
                this.col_4 = col_4;
        }

        public BigDecimal getTotal()
        {
                return this.total;
        }

        public void setTotal(BigDecimal total)
        {
                this.total = total;
        }
}
