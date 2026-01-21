package nonprofitbookkeeping.ui.javafx.supplemental;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;

public class SupplementalLinesTabs extends TabPane
{
	private final Map<SupplementalLineKind, SupplementalLinesEditor> editors =
		new EnumMap<>(SupplementalLineKind.class);
	private final Map<SupplementalLineKind, Tab> tabs =
		new EnumMap<>(SupplementalLineKind.class);

	public SupplementalLinesTabs()
	{
		setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

		for (SupplementalLineKind kind : List.of(
			SupplementalLineKind.RECEIVABLE,
			SupplementalLineKind.PAYABLE,
			SupplementalLineKind.PREPAID_EXPENSE,
			SupplementalLineKind.DEFERRED_REVENUE,
			SupplementalLineKind.OTHER_ASSET,
			SupplementalLineKind.OTHER_LIABILITY))
		{
			SupplementalLineConfig config = SupplementalLineConfig.forKind(kind);
			SupplementalLinesEditor editor = new SupplementalLinesEditor(config);
			this.editors.put(kind, editor);

			Tab tab = new Tab(config.tabTitle, editor);
			this.tabs.put(kind, tab);
			getTabs().add(tab);
		}
	}

	public void setEntryRefs(List<EntryRef> entryRefs)
	{
		for (SupplementalLinesEditor editor : this.editors.values())
		{
			editor.setEntryRefs(entryRefs);
		}
	}

	public void setPersonRefs(List<PersonRef> personRefs)
	{
		for (SupplementalLinesEditor editor : this.editors.values())
		{
			editor.setPersonRefs(personRefs);
		}
	}

	public SupplementalLinesEditor editor(SupplementalLineKind kind)
	{
		return this.editors.get(kind);
	}

	public void setEnabled(SupplementalLineKind kind, boolean enabled)
	{
		Tab tab = this.tabs.get(kind);
		if (tab != null)
		{
			tab.setDisable(!enabled);
		}
	}
}
