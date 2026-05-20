package masroofy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Root persisted state of the application.
 *
 * <p>This object is stored in {@code data/masroofy.db}. Any state-changing operation should save
 * immediately after updating this object.
 */
public class AppState {
  private Cycle activeCycle;
  private List<Expense> expenses;
  private List<Category> categories;
  private UserSettings settings;
  private long nextExpenseId;

  /** Creates a new empty state (first run). */
  public AppState() {
    this.expenses = new ArrayList<>();
    this.categories = new ArrayList<>();
    this.settings = new UserSettings();
    this.nextExpenseId = 1L;
  }

  /**
   * Returns the currently active cycle, or null if none exists.
   *
   * @return active cycle or null
   */
  public Cycle getActiveCycle() {
    return activeCycle;
  }

  /**
   * Sets the active cycle.
   *
   * @param activeCycle new active cycle (nullable)
   */
  public void setActiveCycle(Cycle activeCycle) {
    this.activeCycle = activeCycle;
  }

  /**
   * Returns the in-memory list of expenses.
   *
   * @return expense list (never null)
   */
  public List<Expense> getExpenses() {
    return expenses;
  }

  /**
   * Replaces the expense list.
   *
   * @param expenses new expenses list (null becomes empty)
   */
  public void setExpenses(List<Expense> expenses) {
    this.expenses = (expenses == null) ? new ArrayList<>() : expenses;
  }

  /**
   * Returns the category list.
   *
   * @return categories (never null)
   */
  public List<Category> getCategories() {
    return categories;
  }

  /**
   * Replaces the category list.
   *
   * @param categories new categories list (null becomes empty)
   */
  public void setCategories(List<Category> categories) {
    this.categories = (categories == null) ? new ArrayList<>() : categories;
  }

  /**
   * Returns user settings.
   *
   * @return settings (never null)
   */
  public UserSettings getSettings() {
    return settings;
  }

  /**
   * Replaces user settings.
   *
   * @param settings new settings (null becomes default settings)
   */
  public void setSettings(UserSettings settings) {
    this.settings = (settings == null) ? new UserSettings() : settings;
  }

  /**
   * Returns the next expense id that will be allocated.
   *
   * @return next id (>= 1)
   */
  public long getNextExpenseId() {
    return nextExpenseId;
  }

  /**
   * Sets the next expense id.
   *
   * @param nextExpenseId next id (values &lt; 1 become 1)
   */
  public void setNextExpenseId(long nextExpenseId) {
    this.nextExpenseId = Math.max(1L, nextExpenseId);
  }

  /**
   * Allocates a new unique expense id and increments the counter.
   *
   * @return allocated id
   */
  public long allocateExpenseId() {
    long id = nextExpenseId;
    nextExpenseId = id + 1;
    return id;
  }

  /** Ensures default categories exist (first run convenience). */
  public void ensureSeedCategories() {
    if (categories == null) categories = new ArrayList<>();
    if (!categories.isEmpty()) return;
    categories.add(new Category(1, "Food"));
    categories.add(new Category(2, "Transport"));
    categories.add(new Category(3, "Bills"));
    categories.add(new Category(4, "Shopping"));
    categories.add(new Category(5, "Entertainment"));
    categories.add(new Category(6, "Other"));
  }
}
