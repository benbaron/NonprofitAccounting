package nonprofitbookkeeping.ui.javafx.supplemental;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;



/**
 * The Class SupplementalLinesEditor.
 */
public class SupplementalLinesEditor extends VBox
{
	
	/** The config. */
	private final SupplementalLineConfig config;
	
	/** The rows. */
	private final ObservableList<SupplementalLineRow> rows =
		FXCollections.observableArrayList();
	
	/** The table. */
	private final TableView<SupplementalLineRow> table =
		new TableView<>(rows);
	
	/** The validation label. */
	private final Label validationLabel = new Label();
	
	/** The entry refs. */
	private final ObservableList<EntryRef> entryRefs =
		FXCollections.observableArrayList();
	
	/** The person refs. */
	private final ObservableList<PersonRef> personRefs =
		FXCollections.observableArrayList();

	/**
	 * Instantiates a new supplemental lines editor.
	 *
	 * @param config the config
	 */
	public SupplementalLinesEditor(SupplementalLineConfig config)
	{
		this.config = config;

		setSpacing(8);
		setPadding(new Insets(8));

		this.table.setEditable(true);
		this.table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		buildColumns();

		HBox buttons = new HBox(8);
		Button add = new Button("Add");
		Button remove = new Button("Remove");
		add.setOnAction(event -> this.rows.add(new SupplementalLineRow()));
		remove.setOnAction(event ->
		{
			SupplementalLineRow selected =
				this.table.getSelectionModel().getSelectedItem();
			if (selected != null)
			{
				this.rows.remove(selected);
			}
		});
		buttons.getChildren().addAll(add, remove);

		this.validationLabel.getStyleClass().addAll("validation-label",
			"state-inline", "state-error");
		this.validationLabel.setWrapText(true);

		getChildren().addAll(buttons, this.table, this.validationLabel);
		VBox.setVgrow(this.table, Priority.ALWAYS);
	}

	/**
	 * Sets the entry refs.
	 *
	 * @param refs the new entry refs
	 */
	public void setEntryRefs(List<EntryRef> refs)
	{
		this.entryRefs.setAll(refs);
	}

	/**
	 * Sets the person refs.
	 *
	 * @param refs the new person refs
	 */
	public void setPersonRefs(List<PersonRef> refs)
	{
		this.personRefs.setAll(refs);
	}

	/**
	 * Gets the rows.
	 *
	 * @return the rows
	 */
	public ObservableList<SupplementalLineRow> getRows()
	{
		return this.rows;
	}

	/**
	 * Gets the config.
	 *
	 * @return the config
	 */
	public SupplementalLineConfig getConfig()
	{
		return this.config;
	}

	/**
	 * Sets the rows.
	 *
	 * @param newRows the new rows
	 */
	public void setRows(Collection<SupplementalLineRow> newRows)
	{
		this.rows.setAll(newRows);
	}

	/**
	 * Validate rows.
	 *
	 * @return the list
	 */
	public List<String> validateRows()
	{
		List<String> errors = new ArrayList<>();

		for (int i = 0; i < this.rows.size(); i++)
		{
			SupplementalLineRow row = this.rows.get(i);
			int rowNo = i + 1;

			if (row.getDescription() == null || row.getDescription().trim().isEmpty())
			{
				errors.add(this.config.tabTitle + " row " + rowNo +
					": Description is required.");
			}

			if (row.getAmount() == null)
			{
				errors.add(this.config.tabTitle + " row " + rowNo +
					": Amount is required.");
			}
			else if (row.getAmount().compareTo(BigDecimal.ZERO) < 0)
			{
				errors.add(this.config.tabTitle + " row " + rowNo +
					": Amount must be >= 0.");
			}

			if (this.config.showStartEnd)
			{
				LocalDate start = row.getStartDate();
				LocalDate end = row.getEndDate();
				if ((start != null && end == null) || (start == null && end != null))
				{
					errors.add(this.config.tabTitle + " row " + rowNo +
						": Start and End date must both be set.");
				}
				if (start != null && end != null && start.isAfter(end))
				{
					errors.add(this.config.tabTitle + " row " + rowNo +
						": Start date must be <= End date.");
				}
			}
		}

		return errors;
	}

	/**
	 * Validate and display.
	 *
	 * @return true, if successful
	 */
	public boolean validateAndDisplay()
	{
		List<String> errors = validateRows();
		if (errors.isEmpty())
		{
			this.validationLabel.setText("");
			this.validationLabel.getStyleClass().remove("state-error");
			if (!this.validationLabel.getStyleClass().contains("state-valid"))
			{
				this.validationLabel.getStyleClass().add("state-valid");
			}
			return true;
		}
		this.validationLabel.getStyleClass().remove("state-valid");
		if (!this.validationLabel.getStyleClass().contains("state-error"))
		{
			this.validationLabel.getStyleClass().add("state-error");
		}
		this.validationLabel.setText(String.join("\n", errors));
		return false;
	}

	/**
	 * Validate entry link sums.
	 *
	 * @param entryAmountLookup the entry amount lookup
	 * @return the list
	 */
	public List<String> validateEntryLinkSums(Function<Long, BigDecimal> entryAmountLookup)
	{
		Map<Long, BigDecimal> sums = new HashMap<>();
		for (SupplementalLineRow row : this.rows)
		{
			Long entryId = row.getEntryId();
			if (entryId == null)
			{
				continue;
			}
			BigDecimal amount = row.getAmount() == null ? BigDecimal.ZERO : row.getAmount();
			sums.merge(entryId, amount, BigDecimal::add);
		}

		List<String> errors = new ArrayList<>();
		for (Map.Entry<Long, BigDecimal> entry : sums.entrySet())
		{
			long entryId = entry.getKey();
			BigDecimal expected = entryAmountLookup.apply(entryId);
			if (expected == null)
			{
				continue;
			}
			BigDecimal actual = entry.getValue();
			if (actual.compareTo(expected) != 0)
			{
				errors.add(this.config.tabTitle + ": Entry " + entryId +
					" schedule sum " + actual +
					" does not match entry amount " + expected + ".");
			}
		}

		return errors;
	}

	/**
	 * Builds the columns.
	 */
	private void buildColumns()
	{
		TableColumn<SupplementalLineRow, Long> entryCol =
			new TableColumn<>("Link to Entry");
		entryCol.setCellValueFactory(cd -> cd.getValue().entryIdProperty());
		entryCol.setCellFactory(col -> new EntryLinkCell(this.entryRefs));
		entryCol.setEditable(true);

		TableColumn<SupplementalLineRow, String> descCol =
			new TableColumn<>("Description");
		descCol.setCellValueFactory(cd -> cd.getValue().descriptionProperty());
		descCol.setCellFactory(TextFieldTableCell.forTableColumn());
		descCol.setOnEditCommit(event ->
			event.getRowValue().setDescription(event.getNewValue()));

		TableColumn<SupplementalLineRow, String> refCol =
			new TableColumn<>("Reference");
		refCol.setCellValueFactory(cd -> cd.getValue().referenceProperty());
		refCol.setCellFactory(TextFieldTableCell.forTableColumn());
		refCol.setOnEditCommit(event ->
			event.getRowValue().setReference(event.getNewValue()));

		TableColumn<SupplementalLineRow, BigDecimal> amtCol =
			new TableColumn<>("Amount");
		amtCol.setCellValueFactory(cd -> cd.getValue().amountProperty());
		amtCol.setCellFactory(col -> new BigDecimalEditingCell());
		amtCol.setOnEditCommit(event ->
			event.getRowValue().setAmount(event.getNewValue()));

		TableColumn<SupplementalLineRow, Long> personCol =
			new TableColumn<>("Counterparty");
		personCol.setCellValueFactory(cd -> cd.getValue().counterpartyPersonIdProperty());
		personCol.setCellFactory(col -> new PersonLinkCell(this.personRefs));
		personCol.setEditable(true);

		this.table.getColumns().addAll(entryCol, personCol, descCol, refCol, amtCol);

		if (this.config.showDueDate)
		{
			TableColumn<SupplementalLineRow, LocalDate> dueCol =
				new TableColumn<>("Due Date");
			dueCol.setCellValueFactory(cd -> cd.getValue().dueDateProperty());
			dueCol.setCellFactory(col -> new DatePickerCell());
			dueCol.setOnEditCommit(event ->
				event.getRowValue().setDueDate(event.getNewValue()));
			this.table.getColumns().add(dueCol);
		}

		if (this.config.showStartEnd)
		{
			TableColumn<SupplementalLineRow, LocalDate> startCol =
				new TableColumn<>("Start Date");
			startCol.setCellValueFactory(cd -> cd.getValue().startDateProperty());
			startCol.setCellFactory(col -> new DatePickerCell());
			startCol.setOnEditCommit(event ->
				event.getRowValue().setStartDate(event.getNewValue()));

			TableColumn<SupplementalLineRow, LocalDate> endCol =
				new TableColumn<>("End Date");
			endCol.setCellValueFactory(cd -> cd.getValue().endDateProperty());
			endCol.setCellFactory(col -> new DatePickerCell());
			endCol.setOnEditCommit(event ->
				event.getRowValue().setEndDate(event.getNewValue()));

			this.table.getColumns().addAll(startCol, endCol);
		}

		TableColumn<SupplementalLineRow, String> notesCol =
			new TableColumn<>("Notes");
		notesCol.setCellValueFactory(cd -> cd.getValue().notesProperty());
		notesCol.setCellFactory(TextFieldTableCell.forTableColumn());
		notesCol.setOnEditCommit(event ->
			event.getRowValue().setNotes(event.getNewValue()));
		this.table.getColumns().add(notesCol);
	}

	/**
	 * The Class EntryLinkCell.
	 */
	private static class EntryLinkCell extends TableCell<SupplementalLineRow, Long>
	{
		
		/** The combo. */
		private final ComboBox<EntryRef> combo;

		/**
		 * Instantiates a new entry link cell.
		 *
		 * @param entryRefs the entry refs
		 */
		EntryLinkCell(ObservableList<EntryRef> entryRefs)
		{
			this.combo = new ComboBox<>(entryRefs);
			this.combo.setMaxWidth(Double.MAX_VALUE);
			this.combo.setConverter(new StringConverter<>()
			{
				@Override
				public String toString(EntryRef ref)
				{
					return ref == null ? "" : ref.toString();
				}

				@Override
				public EntryRef fromString(String value)
				{
					return null;
				}
			});

			this.combo.valueProperty().addListener((obs, oldValue, newValue) ->
			{
				if (isEditing())
				{
					commitEdit(newValue == null ? null : newValue.getEntryId());
				}
			});
		}

		/**
		 * Override @see javafx.scene.control.TableCell#startEdit() 
		 */
		@Override
		public void startEdit()
		{
			super.startEdit();
			if (!isEmpty())
			{
				Long entryId = getItem();
				if (entryId == null)
				{
					this.combo.getSelectionModel().clearSelection();
				}
				else
				{
					for (EntryRef ref : this.combo.getItems())
					{
						if (ref.getEntryId() == entryId)
						{
							this.combo.getSelectionModel().select(ref);
							break;
						}
					}
				}
				setGraphic(this.combo);
				setText(null);
			}
		}

		/**
		 * Override @see javafx.scene.control.TableCell#cancelEdit() 
		 */
		@Override
		public void cancelEdit()
		{
			super.cancelEdit();
			setGraphic(null);
			setText(displayText(getItem()));
		}

		/**
		 * Override @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean) 
		 */
		@Override
		protected void updateItem(Long item, boolean empty)
		{
			super.updateItem(item, empty);
			if (empty)
			{
				setText(null);
				setGraphic(null);
				return;
			}

			if (isEditing())
			{
				setText(null);
				setGraphic(this.combo);
			}
			else
			{
				setGraphic(null);
				setText(displayText(item));
			}
		}

		/**
		 * Display text.
		 *
		 * @param entryId the entry id
		 * @return the string
		 */
		private String displayText(Long entryId)
		{
			if (entryId == null)
			{
				return "";
			}
			return "Entry #" + entryId;
		}
	}

	/**
	 * The Class PersonLinkCell.
	 */
	private static class PersonLinkCell extends TableCell<SupplementalLineRow, Long>
	{
		
		/** The combo. */
		private final ComboBox<PersonRef> combo;

		/**
		 * Instantiates a new person link cell.
		 *
		 * @param personRefs the person refs
		 */
		PersonLinkCell(ObservableList<PersonRef> personRefs)
		{
			this.combo = new ComboBox<>(personRefs);
			this.combo.setMaxWidth(Double.MAX_VALUE);
			this.combo.setConverter(new StringConverter<>()
			{
				@Override
				public String toString(PersonRef ref)
				{
					return ref == null ? "" : ref.toString();
				}

				@Override
				public PersonRef fromString(String value)
				{
					return null;
				}
			});

			this.combo.valueProperty().addListener((obs, oldValue, newValue) ->
			{
				if (isEditing())
				{
					commitEdit(newValue == null ? null : newValue.getPersonId());
				}
			});
		}

		/**
		 * Override @see javafx.scene.control.TableCell#startEdit() 
		 */
		@Override
		public void startEdit()
		{
			super.startEdit();
			if (!isEmpty())
			{
				Long personId = getItem();
				if (personId == null)
				{
					this.combo.getSelectionModel().clearSelection();
				}
				else
				{
					for (PersonRef ref : this.combo.getItems())
					{
						if (ref.getPersonId() == personId)
						{
							this.combo.getSelectionModel().select(ref);
							break;
						}
					}
				}
				setGraphic(this.combo);
				setText(null);
			}
		}

		/**
		 * Override @see javafx.scene.control.TableCell#cancelEdit() 
		 */
		@Override
		public void cancelEdit()
		{
			super.cancelEdit();
			setGraphic(null);
			setText(displayText(getItem()));
		}

		/**
		 * Override @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean) 
		 */
		@Override
		protected void updateItem(Long item, boolean empty)
		{
			super.updateItem(item, empty);
			if (empty)
			{
				setText(null);
				setGraphic(null);
				return;
			}

			if (isEditing())
			{
				setText(null);
				setGraphic(this.combo);
			}
			else
			{
				setGraphic(null);
				setText(displayText(item));
			}
		}

		/**
		 * Display text.
		 *
		 * @param personId the person id
		 * @return the string
		 */
		private String displayText(Long personId)
		{
			if (personId == null)
			{
				return "";
			}
			return "Person #" + personId;
		}
	}

	/**
	 * The Class BigDecimalEditingCell.
	 */
	private static class BigDecimalEditingCell
		extends TableCell<SupplementalLineRow, BigDecimal>
	{
		
		/** The field. */
		private final TextField field = new TextField();

		/**
		 * Instantiates a new big decimal editing cell.
		 */
		BigDecimalEditingCell()
		{
			this.field.setOnAction(event -> commitFromText());
			this.field.focusedProperty().addListener((obs, was, isNow) ->
			{
				if (!isNow)
				{
					commitFromText();
				}
			});
		}

		/**
		 * Override @see javafx.scene.control.TableCell#startEdit() 
		 */
		@Override
		public void startEdit()
		{
			super.startEdit();
			this.field.setText(getItem() == null ? "" : getItem().toPlainString());
			setGraphic(this.field);
			setText(null);
		}

		/**
		 * Override @see javafx.scene.control.TableCell#cancelEdit() 
		 */
		@Override
		public void cancelEdit()
		{
			super.cancelEdit();
			setGraphic(null);
			setText(getItem() == null ? "" : getItem().toPlainString());
		}

		/**
		 * Override @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean) 
		 */
		@Override
		protected void updateItem(BigDecimal item, boolean empty)
		{
			super.updateItem(item, empty);
			if (empty)
			{
				setText(null);
				setGraphic(null);
				return;
			}
			if (isEditing())
			{
				setText(null);
				setGraphic(this.field);
			}
			else
			{
				setGraphic(null);
				setText(item == null ? "" : item.toPlainString());
			}
		}

		/**
		 * Commit from text.
		 */
		private void commitFromText()
		{
			if (!isEditing())
			{
				return;
			}
			String text = this.field.getText() == null ? "" : this.field.getText().trim();
			if (text.isEmpty())
			{
				commitEdit(null);
				return;
			}
			try
			{
				commitEdit(new BigDecimal(text));
			}
			catch (NumberFormatException ex)
			{
				cancelEdit();
			}
		}
	}

	/**
	 * The Class DatePickerCell.
	 */
	private static class DatePickerCell extends TableCell<SupplementalLineRow, LocalDate>
	{
		
		/** The picker. */
		private final javafx.scene.control.DatePicker picker =
			new javafx.scene.control.DatePicker();

		/**
		 * Instantiates a new date picker cell.
		 */
		DatePickerCell()
		{
			this.picker.setOnAction(event -> commitEdit(this.picker.getValue()));
			this.picker.focusedProperty().addListener((obs, was, isNow) ->
			{
				if (!isNow)
				{
					commitEdit(this.picker.getValue());
				}
			});
		}

		/**
		 * Override @see javafx.scene.control.TableCell#startEdit() 
		 */
		@Override
		public void startEdit()
		{
			super.startEdit();
			this.picker.setValue(getItem());
			setGraphic(this.picker);
			setText(null);
		}

		/**
		 * Override @see javafx.scene.control.TableCell#cancelEdit() 
		 */
		@Override
		public void cancelEdit()
		{
			super.cancelEdit();
			setGraphic(null);
			setText(getItem() == null ? "" : getItem().toString());
		}

		/**
		 * Override @see javafx.scene.control.Cell#updateItem(java.lang.Object, boolean) 
		 */
		@Override
		protected void updateItem(LocalDate item, boolean empty)
		{
			super.updateItem(item, empty);
			if (empty)
			{
				setText(null);
				setGraphic(null);
				return;
			}
			if (isEditing())
			{
				setText(null);
				setGraphic(this.picker);
			}
			else
			{
				setGraphic(null);
				setText(item == null ? "" : item.toString());
			}
		}
	}
}
