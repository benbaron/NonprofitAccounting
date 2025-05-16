
package nonprofitbookkeeping.ui.panels;

import javax.swing.*;

import nonprofitbookkeeping.api.SalesCOGServiceIntf;

import java.awt.*;

public class SalesAndCOGPanel extends JPanel
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 5589297853893342276L;
	private final SalesCOGServiceIntf service;
	
	public SalesAndCOGPanel(SalesCOGServiceIntf service)
	{
		this.service = service;
		buildUI();
	}
	
	/**
	 * 
	 */
	private void buildUI()
	{
		setLayout(new BorderLayout());
		
		JPanel inputPanel = new JPanel(new FlowLayout());
		JTextField fromField = new JTextField(10);
		JTextField toField = new JTextField(10);
		JButton calculateBtn = new JButton("Calculate");
		
		JLabel salesLabel = new JLabel("Sales: $0.00");
		JLabel cogLabel = new JLabel("COGS: $0.00");
		
		calculateBtn.addActionListener(e -> {
			String from = fromField.getText();
			String to = toField.getText();
			double sales = this.service.getTotalSales(from, to);
			double cogs = this.service.calculateCOGSForPeriod(from, to);
			salesLabel.setText("Sales: $" + String.format("%.2f", sales));
			cogLabel.setText("COGS: $" + String.format("%.2f", cogs));
		});
		
		inputPanel.add(new JLabel("From:"));
		inputPanel.add(fromField);
		inputPanel.add(new JLabel("To:"));
		inputPanel.add(toField);
		inputPanel.add(calculateBtn);
		
		JPanel resultPanel = new JPanel(new GridLayout(2, 1));
		resultPanel.add(salesLabel);
		resultPanel.add(cogLabel);
		
		add(inputPanel, BorderLayout.NORTH);
		add(resultPanel, BorderLayout.CENTER);
	}
	
}
