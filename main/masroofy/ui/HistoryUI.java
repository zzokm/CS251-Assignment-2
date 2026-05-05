package masroofy.ui;

import masroofy.core.ExpenseManager;
import masroofy.model.AppState;
import masroofy.model.Category;
import masroofy.model.Expense;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/** Console history screen for US #9: Filter Transaction History. */
public class HistoryUI {
  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final ExpenseManager expenseManager;

  /** Creates the history screen with the default expense manager. */
  public HistoryUI() {
    this(new ExpenseManager());
  }

  /** Creates the history screen with an injected expense manager. */
  public HistoryUI(ExpenseManager expenseManager) {
    if (expenseManager == null) throw new IllegalArgumentException("expenseManager cannot be null");
    this.expenseManager = expenseManager;
  }

  /**
   * Runs the transaction filter flow.
   *
   * <p>All inputs are optional. Blank category means all categories, and blank date fields mean no
   * lower or upper date bound.
   *
   * @param state current application state
   * @param scanner console input scanner
   */
  public void showFilteredHistory(AppState state, Scanner scanner) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
    if (scanner == null) throw new IllegalArgumentException("scanner cannot be null");

    System.out.println();
    System.out.println("Filter Transaction History");
    System.out.println("--------------------------");
    printCategories(state);

    try {
      Integer categoryId = readOptionalCategory(scanner);

      System.out.print("From date (yyyy-MM-dd, blank for any): ");
      String fromDate = scanner.nextLine().trim();

      System.out.print("To date (yyyy-MM-dd, blank for any): ");
      String toDate = scanner.nextLine().trim();

      List<Expense> results = expenseManager.filterHistory(state, categoryId, fromDate, toDate);
      printExpenses(state, results);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void printCategories(AppState state) {
    System.out.println("Categories:");
    for (Category category : state.getCategories()) {
      System.out.printf("%d. %s%n", category.getId(), category.getName());
    }
  }

  private Integer readOptionalCategory(Scanner scanner) {
    System.out.print("Category id (blank for all): ");
    String rawCategory = scanner.nextLine().trim();
    if (rawCategory.isEmpty()) return null;
    try {
      return Integer.parseInt(rawCategory);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Category id must be a number.");
    }
  }

  private void printExpenses(AppState state, List<Expense> expenses) {
    if (expenses.isEmpty()) {
      System.out.println("No transactions found for the selected filter.");
      return;
    }

    System.out.println();
    System.out.println("Matching Transactions");
    System.out.println("---------------------");
    for (Expense expense : expenses) {
      String timestamp =
          Instant.ofEpochMilli(expense.getTimestampMillis())
              .atZone(ZoneId.systemDefault())
              .format(DATE_TIME_FORMAT);
      String note = expense.getNote() == null || expense.getNote().isBlank()
          ? ""
          : " | " + expense.getNote();

      System.out.printf(
          "#%d | %s | %s | %.2f EGP%s%n",
          expense.getId(),
          timestamp,
          expenseManager.categoryName(state, expense.getCategoryId()),
          expense.getAmount(),
          note);
    }
  }
}

