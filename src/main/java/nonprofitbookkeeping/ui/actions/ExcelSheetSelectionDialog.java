package nonprofitbookkeeping.ui.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
		List<String> ledgerPages = resolveLedgerPages(sheetNames);

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
		dialog.setHeaderText(
			"Choose a specific sheet to import, or import all ledger pages.");
		ButtonType importButton = new ButtonType("Import Selected Sheet",
			ButtonType.OK.getButtonData());
		ButtonType allLedgerPagesButton = ledgerPages.size() == 4 ?
			new ButtonType("Import All Ledger Pages (Q1-Q4)",
				ButtonType.APPLY.getButtonData()) : null;
		if (allLedgerPagesButton != null)
		{
			dialog.getDialogPane().getButtonTypes().addAll(allLedgerPagesButton,
				importButton, ButtonType.CANCEL);
		}
		else
		{
			dialog.getDialogPane().getButtonTypes().addAll(importButton,
				ButtonType.CANCEL);
		}

		ListView<String> listView =
			new ListView<>(javafx.collections.FXCollections
				.observableArrayList(sheetNames));
		listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		listView.getSelectionModel().selectFirst();

		VBox content = new VBox(8, new Label("Ledger sheets:"), listView);
		content.setPrefHeight(300);
		dialog.getDialogPane().setContent(content);

		dialog.setResultConverter(button -> {
			if (button == importButton)
			{
				return new ArrayList<>(
					listView.getSelectionModel().getSelectedItems());
			}
			if (button == allLedgerPagesButton)
			{
				return new ArrayList<>(ledgerPages);
			}
			return null;
		});

		Optional<List<String>> result = dialog.showAndWait();
		return result.orElse(Collections.emptyList());
	}

	private static List<String> resolveLedgerPages(List<String> sheetNames)
	{
		if (sheetNames == null || sheetNames.isEmpty())
		{
			return Collections.emptyList();
		}

		List<String> expected = Arrays.asList(
			"Ledger_Q1",
			"Ledger_Q2",
			"Ledger_Q3",
			"Ledger_Q4");

		return expected.stream()
			.filter(sheetNames::contains)
			.collect(Collectors.toList());
	}
}
