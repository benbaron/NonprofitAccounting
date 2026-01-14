package nonprofitbookkeeping.ui.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import nonprofitbookkeeping.service.ExcelLedgerImportService;

/**
 * Utility for selecting ledger sheets from an Excel workbook.
 */
public final class ExcelSheetSelectionDialog
{
	private ExcelSheetSelectionDialog()
	{
	}

	public static List<String> selectSheets(Stage owner, File workbook)
		throws IOException
	{
		List<String> sheetNames =
			ExcelLedgerImportService.listSheetNames(workbook);

		if (sheetNames.isEmpty())
		{
			return Collections.emptyList();
		}

		if (sheetNames.size() == 1)
		{
			return sheetNames;
		}

		Dialog<List<String>> dialog = new Dialog<>();
		dialog.initOwner(owner);
		dialog.setTitle("Select Ledger Sheets");
		dialog.setHeaderText("Choose one or more sheets to import.");
		ButtonType importButton = new ButtonType("Import",
			ButtonType.OK.getButtonData());
		dialog.getDialogPane().getButtonTypes().addAll(importButton,
			ButtonType.CANCEL);

		ListView<String> listView =
			new ListView<>(javafx.collections.FXCollections
				.observableArrayList(sheetNames));
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listView.getSelectionModel().selectAll();

		VBox content = new VBox(8, new Label("Ledger sheets:"), listView);
		content.setPrefHeight(300);
		dialog.getDialogPane().setContent(content);

		dialog.setResultConverter(button -> {
			if (button == importButton)
			{
				return new ArrayList<>(
					listView.getSelectionModel().getSelectedItems());
			}
			return null;
		});

		Optional<List<String>> result = dialog.showAndWait();
		return result.orElse(Collections.emptyList());
	}
}
