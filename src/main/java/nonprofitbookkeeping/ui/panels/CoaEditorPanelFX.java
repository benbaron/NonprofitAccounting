
package nonprofitbookkeeping.ui.panels;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.service.ChartOfAccountsIOService;
import nonprofitbookkeeping.service.ChartOfAccountsService;
import nonprofitbookkeeping.ui.helpers.AlertBox;

/**
 * Interactive ladder view for editing a company's Chart of Accounts.
 *
 * <p>Usage:</p>
 * <pre>{@code
 * CoaEditorPanelFX editor = new CoaEditorPanelFX(
 *         company.getChartOfAccounts(),
 *         chart -> {                       // on-save callback
 *             company.setChartOfAccounts(chart);
 *             CompanyLoaderService.saveCompanyFile(companyFile, company);
 *         },
 *         () -> root.setCenter(previousView)   // on-close callback
 * );
 * root.setCenter(editor);
 * }</pre>
 */
public class CoaEditorPanelFX extends BorderPane
{
	
	/* ------------------------------------------------------------------ */
	private final ChartOfAccountsService svc;
	private final Consumer<ChartOfAccounts> onSave;
	private final Runnable onClose;
	
	private final TreeTableView<Account> tree = new TreeTableView<>();
	private final TreeItem<Account> rootItem = new TreeItem<>();
	
	private final ChartOfAccountsIOService ioSvc = new ChartOfAccountsIOService();
	
	/* dialog fields (re-used so we can read them in build()) */
	private TextField numF, nameF, typeF, balF;
	
	
	/** 
	 * convenience when no callbacks are needed 
	 * 
	 * Constructor CoaEditorPanelFX
	 * @param chart
	 */
	public CoaEditorPanelFX(ChartOfAccounts chart)
	{
		this(chart, null, null);
	}
	
	/**
	 * Constructor CoaEditorPanelFX
	 * @param chart
	 * @param onSave  	CALLBACK
	 * @param onClose	CALLBACK
	 */
	public CoaEditorPanelFX(ChartOfAccounts chart,
		Consumer<ChartOfAccounts> onSave, //
		Runnable onClose)
	{
		this.svc = new ChartOfAccountsService(chart);
		this.onSave = onSave;
		this.onClose = onClose;
		
		setPadding(new Insets(10));
		
		// build the tree
		buildTree();
		
		setCenter(this.tree);
		setBottom(buildButtons());
		
		refresh();
	}
	
	/* ------------------------------------------------------------------ */
	@SuppressWarnings("unchecked") private void buildTree()
	{
		this.tree.setShowRoot(false);
		this.tree.setRoot(this.rootItem);
		
		this.tree.getColumns().addAll(
			makeCol("Number", Account::getAccountNumber),
			makeCol("Name", Account::getName),
			makeCol("Type", a -> a.getAccountType().toString()),
			makeCol("Opening Balance", Account::getOpeningBalance));
		this.tree.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
	}
	
	/* ------------------------------------------------------------------ */
	private HBox buildButtons()
	{
		Button addRoot = new Button("Add Root");
		Button addSub = new Button("Add Sub");
		Button edit = new Button("Edit");
		Button del = new Button("Delete");
		Button saveBtn = new Button("Save");
		Button importBtn = new Button("Import JSON…");
		Button exportBtn = new Button("Export JSON…");
		Button cancel = new Button("Cancel");
		
		// button actions
		addRoot.setOnAction(e -> showDialog(null, null));
		addSub.setOnAction(e -> {
			Account parent = selected();
			
			if (parent != null)
			{
				showDialog(parent, null);
			}
			
		});
		edit.setOnAction(e -> {
			Account sel = selected();
			
			if (sel != null)
			{
				showDialog(sel.getParentAccount(), sel);
			}
			
		});
		del.setOnAction(e -> deleteSelected());
		importBtn.setOnAction(e -> importJson());
		exportBtn.setOnAction(e -> exportJson());
		saveBtn.setOnAction(e -> {
			
			if (this.onSave != null)
			{
				// On save, do this.
				this.onSave.accept(this.svc.asChart());
			}
			
			closePanel();
		});
		cancel.setOnAction(e -> closePanel());
		
		//
		return new HBox(8, addRoot, addSub, edit, del,
			importBtn, exportBtn, saveBtn, cancel)
		{
			{
				setPadding(new Insets(6));
			}
			
		};
	}
	
	/* ------------------------------------------------------------------ */
	private void showDialog(Account parent, Account editing)
	{
		boolean isEdit = editing != null;
		
		Dialog<Account> dlg = new Dialog<>();
		dlg.setTitle(
			isEdit ? "Edit Account" : (parent == null ? "Add Root Account" : "Add Sub-account"));
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		this.numF = new TextField(isEdit ? editing.getAccountNumber() : "");
		this.nameF = new TextField(isEdit ? editing.getName() : "");
		this.typeF = new TextField(isEdit ? editing.getAccountType().toString() : "");
		this.balF = new TextField(isEdit ? editing.getOpeningBalance().toPlainString() : "0.00");
		
		GridPane gp = new GridPane();
		gp.setHgap(10);
		gp.setVgap(8);
		gp.addRow(0, new Label("Number"), this.numF);
		gp.addRow(1, new Label("Name"), this.nameF);
		gp.addRow(2, new Label("Type"), this.typeF);
		gp.addRow(3, new Label("Opening Balance"), this.balF);
		
		dlg.getDialogPane().setContent(gp);
		dlg.setResultConverter(btn -> btn == ButtonType.OK ? buildAccount() : null);
		
		dlg.showAndWait().ifPresent(det -> {
			
			if (isEdit)
			{
				ChartOfAccountsService.update(editing,
					det.getName(), det.getAccountType(), det.getOpeningBalance());
				this.tree.refresh();
			}
			else
			{
				
				if (parent == null)
				{
					this.svc.addRoot(det);
				}
				else
				{
					this.svc.addChild(parent, det);
				}
				
				insertIntoTree(det, parent);
			}
			
		});
	}
	
	/* ------------------------------------------------------------------ */
	private void deleteSelected()
	{
		Account sel = selected();
		
		if (sel == null)
		{
			return;
		}
		
		this.svc.delete(sel);
		TreeItem<Account> node = this.tree.getSelectionModel().getSelectedItem();
		
		if (node != null)
		{
			node.getParent().getChildren().remove(node);
		}
		
	}
	
	/* ------------------------------------------------------------------ */
	private void importJson()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Import Chart of Accounts");
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
		File f = fc.showOpenDialog(getScene().getWindow());
		
		if (f == null)
		{
			return;
		}
		
		try
		{
			ChartOfAccounts imported = this.ioSvc.importFromJson(f.toPath());
			this.svc.replaceChart(imported);
			refresh();
		}
		catch (IOException ex)
		{
			AlertBox.showError(null, "Import failed: " + ex.getMessage());
		}
		
	}
	
	/**
	 * 
	 */
	private void exportJson()
	{
		FileChooser fc = new FileChooser();
		fc.setTitle("Export Chart of Accounts");
		fc.setInitialFileName("chart-of-accounts.json");
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
		File f = fc.showSaveDialog(getScene().getWindow());
		
		if (f == null)
		{
			return;
		}
		
		try
		{
			this.ioSvc.exportToJson(this.svc.asChart(), f.toPath());
			AlertBox.showInfo(null, "Exported to " + f);
		}
		catch (IOException ex)
		{
			AlertBox.showError(null, "Export failed: " + ex.getMessage());
		}
		
	}
	
	/* ------------------------------------------------------------------ */
	private void closePanel()
	{
		
		if (this.onClose != null)
		{
			this.onClose.run(); // caller handles UI restore
		}
		else
		{
			Stage s = (Stage) getScene().getWindow();
			
			if (s != null)
			{
				s.close(); // standalone stage case
			}
			
		}
		
	}
	
	/* ------------------------------------------------------------------ */
	private void refresh()
	{
		this.rootItem.getChildren().clear();
		this.svc.roots().forEach(r -> this.rootItem.getChildren().add(makeNode(r)));
		this.tree.refresh();
	}
	
	private Account selected()
	{
		TreeItem<Account> ti = this.tree.getSelectionModel().getSelectedItem();
		return ti == null ? null : ti.getValue();
	}
	
	/* ------------------------------------------------------------------ */
	private Account buildAccount()
	{
		Account a = new Account();
		a.setAccountNumber(this.numF.getText().trim());
		a.setName(this.nameF.getText().trim());
		a.setAccountType(AccountType.fromString(this.typeF.getText().trim()));
		
		try
		{
			a.setOpeningBalance(new BigDecimal(this.balF.getText().trim()));
		}
		catch (NumberFormatException ignore)
		{
			a.setOpeningBalance(BigDecimal.ZERO);
		}
		
		return a;
	}
	
	/* ------------------------------------------------------------------ */
	private TreeItem<Account> makeNode(Account acc)
	{
		TreeItem<Account> ti = new TreeItem<>(acc);
		this.svc.childrenOf(acc).forEach(child -> ti.getChildren().add(makeNode(child)));
		ti.setExpanded(true);
		return ti;
	}
	
	private <T> TreeTableColumn<Account, T> makeCol(String name,
													Function<Account, T> fn)
	{
		TreeTableColumn<Account, T> c = new TreeTableColumn<>(name);
		c.setCellValueFactory(
			cd -> new ReadOnlyObjectWrapper<>(fn.apply(cd.getValue().getValue())));
		return c;
	}
	
	/* insert new node --------------------------------------------------- */
	private void insertIntoTree(Account det, Account parent)
	{
		
		if (parent == null)
		{
			this.rootItem.getChildren().add(makeNode(det));
		}
		else
		{
			TreeItem<Account> parentItem = find(this.rootItem, parent);
			
			if (parentItem != null)
			{
				parentItem.getChildren().add(makeNode(det));
			}
			
		}
		
	}
	
	private TreeItem<Account> find(TreeItem<Account> n, Account acc)
	{
		
		if (n.getValue() == acc)
		{
			return n;
		}
		
		for (TreeItem<Account> c : n.getChildren())
		{
			TreeItem<Account> hit = find(c, acc);
			
			if (hit != null)
			{
				return hit;
			}
			
		}
		
		return null;
	}
	
}
