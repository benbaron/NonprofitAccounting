package nonprofitbookkeeping.model.budget;

import lombok.Data;
import lombok.NoArgsConstructor; // For Jackson
// No @AllArgsConstructor to allow custom constructor logic for budgetId

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Represents a budget for a specific fiscal year.
 * A budget consists of a name, fiscal year, an optional description, a list of budget lines,
 * currency, and an optional associated fund ID.
 * Lombok's {@code @Data} and {@code @NoArgsConstructor} are used for boilerplate code generation.
 */
@Data
@NoArgsConstructor // Keep for Jackson, but ensure budgetId is handled if object created this way
public class Budget {
    /** The unique identifier for the budget. Typically a UUID. */
    private String budgetId;
    /** The name of the budget (e.g., "Annual Operational Budget"). */
    private String budgetName;
    /** The fiscal year to which this budget applies (e.g., 2024). */
    private int fiscalYear;
    /** An optional description for the budget. */
    private String description; // Optional
    /** A list of {@link BudgetLine} items that make up this budget. Initialized to an empty ArrayList. */
    private List<BudgetLine> budgetLines = new ArrayList<>();
    /** The currency code for the amounts in this budget (e.g., "USD"). Should ideally match the company's base currency. */
    private String currency; // Should match company currency
    /** The identifier of a specific fund to which this budget applies, if any. Optional. */
    private String applicableFundId; // Optional

    /**
     * Constructs a Budget with a given name and fiscal year.
     * A unique {@code budgetId} is automatically generated using UUID.
     * The list of budget lines is initialized as empty.
     * @param budgetName The name for the budget.
     * @param fiscalYear The fiscal year for the budget.
     */
    public Budget(String budgetName, int fiscalYear) {
        this.budgetId = UUID.randomUUID().toString(); // Generate new ID
        this.budgetName = budgetName;
        this.fiscalYear = fiscalYear;
        // budgetLines is initialized by field declaration
    }
    
    /**
     * Gets the unique identifier for the budget.
     * If the {@code budgetId} is currently null (e.g., if the object was created via
     * the no-args constructor and not yet populated), a new UUID will be generated and assigned.
     * @return The budget ID string.
     */
    public String getBudgetId() {
        if (this.budgetId == null) {
            this.budgetId = UUID.randomUUID().toString();
        }
        return this.budgetId;
    }
    
    // Note: The following comment block from original code is retained for context,
    // but the primary mechanism for ID generation is now in the constructor and the custom getter.
    // Ensure budgetId is set upon construction if using @NoArgsConstructor path,
    // though this is better handled by having Jackson populate it or using a factory.
    // For robust handling with @NoArgsConstructor, budgetId might need to be checked/set
    // upon first access or through a specific init method if not deserialized.
    // The getter approach above is a common pattern.

    /**
     * Adds a {@link BudgetLine} to this budget.
     * If the internal list of budget lines is null (which shouldn't happen with current field initialization),
     * it will be initialized.
     * @param line The budget line to add.
     */
    public void addBudgetLine(BudgetLine line) {
        if (this.budgetLines == null) { // Defensive check, though current initialization prevents this
            this.budgetLines = new ArrayList<>();
        }
        this.budgetLines.add(line);
    }

    /**
     * Removes a specific {@link BudgetLine} from this budget.
     * If the budget lines list is null or the line is not found, no action occurs.
     * @param line The budget line to remove.
     */
    public void removeBudgetLine(BudgetLine line) {
        if (this.budgetLines != null) {
            this.budgetLines.remove(line);
        }
    }

    // Explicit getters and setters below are mostly redundant due to Lombok @Data,
    // but are documented as they exist.

	/**
	 * Gets the name of the budget.
	 * @return The budget name.
	 */
	public String getBudgetName()
	{
		return this.budgetName;
	}

	/**
	 * Sets the name of the budget.
	 * @param budgetName The budget name to set.
	 */
	public void setBudgetName(String budgetName)
	{
		this.budgetName = budgetName;
	}

	/**
	 * Gets the fiscal year of the budget.
	 * @return The fiscal year.
	 */
	public int getFiscalYear()
	{
		return this.fiscalYear;
	}

	/**
	 * Sets the fiscal year of the budget.
	 * @param fiscalYear The fiscal year to set.
	 */
	public void setFiscalYear(int fiscalYear)
	{
		this.fiscalYear = fiscalYear;
	}

	/**
	 * Gets the optional description of the budget.
	 * @return The budget description, or null if not set.
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 * Sets the optional description of the budget.
	 * @param description The budget description to set.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * Gets the list of budget lines for this budget.
	 * @return A list of {@link BudgetLine} objects.
	 */
	public List<BudgetLine> getBudgetLines()
	{
		return this.budgetLines;
	}

	/**
	 * Sets the list of budget lines for this budget.
	 * @param budgetLines A list of {@link BudgetLine} objects to set.
	 */
	public void setBudgetLines(List<BudgetLine> budgetLines)
	{
		this.budgetLines = budgetLines;
	}

	/**
	 * Gets the currency code for this budget.
	 * @return The currency code (e.g., "USD").
	 */
	public String getCurrency()
	{
		return this.currency;
	}

	/**
	 * Sets the currency code for this budget.
	 * @param currency The currency code to set.
	 */
	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	/**
	 * Gets the ID of the fund to which this budget applies, if any.
	 * @return The applicable fund ID, or null if not set.
	 */
	public String getApplicableFundId()
	{
		return this.applicableFundId;
	}

	/**
	 * Sets the ID of the fund to which this budget applies.
	 * @param applicableFundId The fund ID to set.
	 */
	public void setApplicableFundId(String applicableFundId)
	{
		this.applicableFundId = applicableFundId;
	}

	/**
	 * Sets the unique identifier for the budget.
	 * This is typically generated automatically but can be set manually if needed (e.g., during deserialization).
	 * @param budgetId The budget ID to set.
	 */
	public void setBudgetId(String budgetId)
	{
		this.budgetId = budgetId;
	}

}
