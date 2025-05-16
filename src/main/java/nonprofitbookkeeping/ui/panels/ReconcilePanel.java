package nonprofitbookkeeping.ui.panels;

import nonprofitbookkeeping.service.ReconciliationService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.List;

/**
 * Panel for reconciling entries using a reconciliation service.
 */
public class ReconcilePanel extends JPanel {

    private static final long serialVersionUID = 2420641949593920352L;
    private JTable table;
    private JComboBox<String> accountSelector;
    private JTextField fromField, toField;

    /**
     * Constructor for ReconcilePanel.
     * 
     * @param reconciliationService2 the reconciliation service for managing reconciliation
     */
    public ReconcilePanel(ReconciliationService reconciliationService2) {
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(new TitledBorder("Filter"));

        this.accountSelector = new JComboBox<>(new String[]{"Bank Checking", "Savings"}); // TODO: replace with dynamic account list
        this.fromField = new JTextField(10);
        this.toField = new JTextField(10);
        JButton filterBtn = new JButton("Search");

        filterBtn.addActionListener(e -> applyFilter());

        filterPanel.add(new JLabel("Account:"));
        filterPanel.add(this.accountSelector);
        filterPanel.add(new JLabel("From:"));
        filterPanel.add(this.fromField);
        filterPanel.add(new JLabel("To:"));
        filterPanel.add(this.toField);
        filterPanel.add(filterBtn);

        add(filterPanel, BorderLayout.NORTH);

        String[] cols = {"Date", "Amount", "Memo", "Status"};
        this.table = new JTable(new DefaultTableModel(cols, 0));
        add(new JScrollPane(this.table), BorderLayout.CENTER);

        JButton reconcileBtn = new JButton("Reconcile Selected");
        reconcileBtn.addActionListener(e -> reconcileSelected());
        add(reconcileBtn, BorderLayout.SOUTH);
    }

    /**
     * Applies filters for the search based on account and date range.
     */
    private void applyFilter() {
        String account = (String) this.accountSelector.getSelectedItem();
        String from = this.fromField.getText();
        String to = this.toField.getText();

        // Fetch unreconciled entries using the service
        List<String[]> entries = ReconciliationService.getUnreconciledEntries(account, from, to);
        DefaultTableModel model = (DefaultTableModel) this.table.getModel();
        model.setRowCount(0);

        for (String[] row : entries) {
            model.addRow(row);
        }
    }

    /**
     * Reconciles the selected transaction in the table.
     */
    private void reconcileSelected() {
        int row = this.table.getSelectedRow();

        if (row != -1) {
            String txnId = (String) this.table.getValueAt(row, 0); // Assuming ID is in first column
            boolean success = ReconciliationService.reconcileEntry(txnId);

            if (success) {
                JOptionPane.showMessageDialog(this, "Reconciled.");
                applyFilter(); // refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Reconciliation failed.");
            }
        }
    }
}
