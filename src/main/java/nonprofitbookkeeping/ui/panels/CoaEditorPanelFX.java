
package nonprofitbookkeeping.ui.panels;

import java.util.function.Function;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.service.ChartOfAccountsService;
import java.math.BigDecimal;

public class CoaEditorPanelFX extends BorderPane
{
	
	private final ChartOfAccountsService svc;
	private final TreeTableView<Account> tree = new TreeTableView<>();
	private final TreeItem<Account> rootItem = new TreeItem<>();
	TextField numF = new TextField();
	TextField nameF = new TextField();
	TextField typeF = new TextField();
	TextField balF = new TextField();


	
	/**
	 * 
	 * Constructor CoaEditorPanelFX
	 * @param coa
	 */
	public CoaEditorPanelFX(ChartOfAccounts coa)
	{
		this.svc = new ChartOfAccountsService(coa);
		setPadding(new Insets(10));
		
		buildTree();
		setCenter(this.tree);
		setBottom(buildButtons());
		
		refresh(); // first load
	}
	/* same fields … */
	
	/* ------------------- UI scaffolding ---------------------- */
	@SuppressWarnings({ "deprecation", "unchecked" }) 
	private void buildTree()
	{
		this.tree.setShowRoot(false);
		this.tree.setRoot(this.rootItem);
		
		this.tree.getColumns().addAll(
			makeCol("Number", Account::getAccountNumber),
			makeCol("Name", Account::getName),
			makeCol("Type", Account::getAccountType),
			makeCol("Opening Balance", Account::getOpeningBalance));
		this.tree.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

	}
	
	/* ------------------ add / edit dialog -------------------- */
	private void dialog(Account parentContext)
	{
		Account editing = selected();
		boolean isEdit = editing != null;
		
		Dialog<Account> dlg = new Dialog<>();
		dlg.setTitle(isEdit ? "Edit Account" :
			(parentContext == null ? "Add Root Account" : "Add Sub-account"));
		dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		
		/* form fields */
		this.numF = new TextField(isEdit ? editing.getAccountNumber() : "");
		this.nameF = new TextField(isEdit ? editing.getName() : "");
		this.typeF = new TextField(isEdit ? editing.getAccountType() : "");
		this.balF = new TextField(isEdit ? editing.getOpeningBalance().toPlainString() : "0.00"); // NEW
		
		GridPane gp = new GridPane();
		gp.setHgap(10);
		gp.setVgap(8);
		gp.addRow(0, new Label("Number"), this.numF);
		gp.addRow(1, new Label("Name"), this.nameF);
		gp.addRow(2, new Label("Type"), this.typeF);
		gp.addRow(3, new Label("Opening Balance"), this.balF); // NEW
		dlg.getDialogPane().setContent(gp);
		
		dlg.setResultConverter(btn -> btn == ButtonType.OK ? build() : null);
		dlg.showAndWait().ifPresent(det -> {
			if (isEdit)
				ChartOfAccountsService.update(editing,
					det.getName(),
					det.getAccountType(),
					det.getOpeningBalance()); // pass balance
			else if (parentContext == null)
			{
				this.svc.addRoot(det);
			}
			else
			{
				this.svc.addChild(parentContext, det);
			}
			refresh();
		});
		
		
	}
	
	
	/**
	 * Make column
	 * @param <T> parameter
	 * @param name column name
	 * @param fn column function
	 * @return column
	 */
	private static <T> TreeTableColumn<Account, T> makeCol(	String name,
															Function<Account, T> fn)
	{
		TreeTableColumn<Account, T> col = new TreeTableColumn<>(name);
		col.setCellValueFactory(
			param -> new ReadOnlyObjectWrapper<>(fn.apply(param.getValue().getValue())));
		return col;
	}
	
	
	/**
	 * Build Buttons
	 * @return horizontal box
	 */
	private HBox buildButtons()
	{
		Button addRoot = new Button("Add Root");
		Button addSub = new Button("Add Sub");
		Button edit = new Button("Edit");
		Button del = new Button("Delete");
		
		addRoot.setOnAction(e -> dialog(null));
		addSub.setOnAction(e -> {
			var sel = selected();
			if (sel != null)
				dialog(sel);
		});
		edit.setOnAction(e -> {
			var sel = selected();
			if (sel != null)
				dialog(sel.getParentAccount()); // parent may be null
		});
		del.setOnAction(e -> {
			var sel = selected();
			
			if (sel != null)
			{
				this.svc.delete(sel);
				refresh();
			}
			
		});
		
		/**
		 * 
		 */
		return new HBox(8, addRoot, addSub, edit, del)
		{
			{
				setPadding(new Insets(6));
			}
			
		};
	}
	
	/**
	 * 
	 * @return
	 */
	private Account selected()
	{
		var sel = this.tree.getSelectionModel().getSelectedItem();
		return sel == null ? null : sel.getValue();
	}

	
	/* builds an Account instance from dialog values */
	Account build()
	{
		Account d = new Account();
		d.setAccountNumber(this.numF.getText().trim());
		d.setName(this.nameF.getText().trim());
		d.setAccountType(this.typeF.getText().trim());
		BigDecimal bal = BigDecimal.ZERO;
		
		try
		{
			bal = new BigDecimal(this.balF.getText().trim());
		}
		catch (NumberFormatException ignore)
		{
		}
		
		d.setOpeningBalance(bal); // NEW
		return d;
	}
	
	/**
	 * 
	 */
	private void refresh()
	{
		this.rootItem.getChildren().clear();
		this.svc.roots().forEach(r -> this.rootItem.getChildren().add(makeNode(r)));
		this.tree.refresh();
	}
	
	/**
	 * 
	 * @param acc
	 * @return
	 */
	private TreeItem<Account> makeNode(Account acc)
	{
		TreeItem<Account> item = new TreeItem<>(acc);
		this.svc.childrenOf(acc).forEach(child -> item.getChildren().add(makeNode(child)));
		item.setExpanded(true);
		return item;
	}
	
}
