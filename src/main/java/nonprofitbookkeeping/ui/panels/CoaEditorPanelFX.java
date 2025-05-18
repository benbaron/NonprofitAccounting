/* ────────────────────────────────────────────────────────────── */
/* CoaEditorPanelFX.java – ladder-view editor for the chart */
/* ────────────────────────────────────────────────────────────── */

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

public class CoaEditorPanelFX extends BorderPane
{
	
	private final ChartOfAccountsService svc;
	private final TreeTableView<Account> tree = new TreeTableView<>();
	private final TreeItem<Account> rootItem = new TreeItem<>();
	
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
	
	/**
	 * 
	 */
	/* ------------------- UI scaffolding ---------------------- */
	@SuppressWarnings({ "unchecked", "deprecation" }) 
	private void buildTree()
	{
		this.tree.setShowRoot(false);
		this.tree.setRoot(this.rootItem);
		
		this.tree.getColumns().addAll(
			makeCol("Number", Account::getAccountNumber),
			makeCol("Name", Account::getName),
			makeCol("Type", Account::getAccountType));
		
		this.tree.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
	}
	
	/**
	 * Make column
	 * @param <T> parameter
	 * @param name column name
	 * @param fn column function
	 * @return column
	 */
	private static <T> TreeTableColumn<Account, T> makeCol(String name,
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
	
	/* ------------------ add / edit dialog -------------------- */
	private void dialog(Account parentContext) {
        Account editing = selected();
        boolean isEdit  = editing != null;

        Dialog<Account> dlg = new Dialog<>();
        dlg.setTitle(isEdit ? "Edit Account" :
                     (parentContext == null ? "Add Root Account" : "Add Sub-account"));
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField numF  = new TextField(isEdit ? editing.getAccountNumber() : "");
        TextField nameF = new TextField(isEdit ? editing.getName()  : "");
        TextField typeF = new TextField(isEdit ? editing.getAccountType()  : "");

        GridPane gp = new GridPane();
        gp.setHgap(10); gp.setVgap(8);
        gp.addRow(0, new Label("Number:"), numF);
        gp.addRow(1, new Label("Name:"),   nameF);
        gp.addRow(2, new Label("Type:"),   typeF);
        dlg.getDialogPane().setContent(gp);

        dlg.setResultConverter(btn -> btn == ButtonType.OK ? build() : null);

        dlg.showAndWait().ifPresent(det -> {
            if (isEdit) ChartOfAccountsService.update(editing, det.getName(), det.getAccountType());
            else if (parentContext == null) this.svc.addRoot(det);
            else this.svc.addChild(parentContext, det);
            refresh();
        });

    }
	
	/**
	 * @return
	 */
	private static Account build()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* -------------------- load → TreeItems ------------------- */
	private void refresh()
	{
		this.rootItem.getChildren().clear();
		this.svc.roots().forEach(r -> this.rootItem.getChildren().add(makeNode(r)));
		this.tree.refresh();
	}
	
	private TreeItem<Account> makeNode(Account acc)
	{
		TreeItem<Account> item = new TreeItem<>(acc);
		this.svc.childrenOf(acc).forEach(child -> item.getChildren().add(makeNode(child)));
		item.setExpanded(true);
		return item;
	}
	
}
