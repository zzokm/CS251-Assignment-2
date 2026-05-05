  package masroofy.core;

  import masroofy.model.AppState;
  import masroofy.model.Category;
  import masroofy.model.Expense;
  import masroofy.storage.JsonStore;
  import java.io.IOException;
  import java.time.LocalDate;
  import java.time.format.DateTimeParseException;
  import java.util.Iterator;
  import java.util.ArrayList;
  import java.util.List;


  /**
   * Expense operations used by History flows (US #8).
   *
   * <p>Persistence rule: any edit/delete is saved immediately to JSON.
   */
  public class ExpenseManager {

    public Expense findExpenseById(AppState state, long expenseId) {
      if (state == null) throw new IllegalArgumentException("state cannot be null");
      for (Expense e : state.getExpenses()) {
        if (e.getId() == expenseId) return e;
      }
      return null;
    }


    /**
     * Returns all expenses sorted by timestamp, newest first (US #7).
     *
     * @param state current application state
     * @return a new list of expenses sorted descending by timestampMillis
     */
    public List<Expense> getExpensesSortedByDate(AppState state) { //Jana
      if (state == null) throw new IllegalArgumentException("state cannot be null");
      List<Expense> sorted = new ArrayList<>(state.getExpenses());
      sorted.sort((a, b) -> Long.compare(b.getTimestampMillis(), a.getTimestampMillis()));
      return sorted;
    }

    /**
     * Filters transaction history by optional category and optional inclusive date range (US #9).
     *
     * <p>Passing {@code null} for a filter means "all" for that field. Results are returned newest
     * first, matching the normal history order.
     *
     * @param state current application state
     * @param categoryId category id to match, or {@code null} for all categories
     * @param fromDate first accepted transaction date, or {@code null} for no lower bound
     * @param toDate last accepted transaction date, or {@code null} for no upper bound
     * @return matching transactions sorted descending by timestamp
     */
    public List<Expense> filterHistory(
        AppState state, Integer categoryId, LocalDate fromDate, LocalDate toDate) {
      if (state == null) throw new IllegalArgumentException("state cannot be null");
      if (categoryId != null && !categoryExists(state, categoryId)) {
        throw new IllegalArgumentException("Invalid category");
      }
      if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
        throw new IllegalArgumentException("From date must be before or equal to to date.");
      }

      List<Expense> filtered = new ArrayList<>();
      for (Expense e : state.getExpenses()) {
        if (e.matchesCategory(categoryId) && e.isWithinDateRange(fromDate, toDate)) {
          filtered.add(e);
        }
      }
      filtered.sort((a, b) -> Long.compare(b.getTimestampMillis(), a.getTimestampMillis()));
      return filtered;
    }

    /**
     * Parses optional ISO dates and filters transaction history by category/date range (US #9).
     *
     * @param state current application state
     * @param categoryId category id to match, or {@code null} for all categories
     * @param fromDateIso first accepted date in {@code yyyy-MM-dd}, blank/null for no lower bound
     * @param toDateIso last accepted date in {@code yyyy-MM-dd}, blank/null for no upper bound
     * @return matching transactions sorted descending by timestamp
     */
    public List<Expense> filterHistory(
        AppState state, Integer categoryId, String fromDateIso, String toDateIso) {
      return filterHistory(
          state,
          categoryId,
          parseOptionalDate(fromDateIso, "from date"),
          parseOptionalDate(toDateIso, "to date"));
    }

    public void editExpense(
        AppState state, long expenseId, double newAmount, int newCategoryId, String newNote) {
      if (state == null) throw new IllegalArgumentException("state cannot be null");
      if (newAmount <= 0) throw new IllegalArgumentException("Amount must be > 0");
      if (!categoryExists(state, newCategoryId)) throw new IllegalArgumentException("Invalid category");

      Expense e = findExpenseById(state, expenseId);
      if (e == null) throw new IllegalArgumentException("Expense not found: " + expenseId);

      e.setAmount(newAmount);
      e.setCategoryId(newCategoryId);
      e.setNote(newNote);

      saveNow(state);
    }

    public void deleteExpense(AppState state, long expenseId) {
      if (state == null) throw new IllegalArgumentException("state cannot be null");

      Iterator<Expense> it = state.getExpenses().iterator();
      while (it.hasNext()) {
        Expense e = it.next();
        if (e.getId() == expenseId) {
          it.remove();
          saveNow(state);
          return;
        }
      }
      throw new IllegalArgumentException("Expense not found: " + expenseId);
    }

    public boolean categoryExists(AppState state, int categoryId) {
      if (state == null) return false;
      for (Category c : state.getCategories()) {
        if (c.getId() == categoryId) return true;
      }
      return false;
    }

    public String categoryName(AppState state, int categoryId) {
      if (state == null) return "Unknown";
      for (Category c : state.getCategories()) {
        if (c.getId() == categoryId) return c.getName();
      }
      return "Unknown";
    }

    private static LocalDate parseOptionalDate(String value, String label) {
      if (value == null || value.trim().isEmpty()) return null;
      try {
        return LocalDate.parse(value.trim());
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException(label + " must use yyyy-MM-dd format.", e);
      }
    }

    private static void saveNow(AppState state) {
      try {
        JsonStore.saveState(state);
      } catch (IOException e) {
        throw new IllegalStateException("Failed to save state", e);
      }
    }
  }

