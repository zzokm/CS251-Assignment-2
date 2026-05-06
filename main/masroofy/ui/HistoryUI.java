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

/** Console history screen (US #7, #8, #9). */
public class HistoryUI {
  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("dd MM uuuu HH:mm");

  private final ExpenseManager expenseManager;

  /** Creates the history screen with the default expense manager. */
  public HistoryUI() {
    this(new ExpenseManager());
  }

  /**
   * Creates the history screen with an injected expense manager.
   *
   * @param expenseManager expense manager
   */
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

      System.out.print("From date (DD MM YYYY, blank for any): ");
      String fromDate = scanner.nextLine().trim();

      System.out.print("To date (DD MM YYYY, blank for any): ");
      String toDate = scanner.nextLine().trim();

      List<Expense> results = expenseManager.filterHistory(state, categoryId, fromDate, toDate);
      printExpenses(state, results);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Shows the full history list (newest first) (US #7).
   *
   * @param state current application state
   */
  public void showHistory(AppState state) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
    List<Expense> expenses = expenseManager.getExpensesSortedByDate(state);
    printExpenses(state, expenses);
  }

  /**
   * Prompts the user to edit an existing transaction (US #8).
   *
   * @param state current application state
   * @param scanner console input scanner
   */
  public void editTransaction(AppState state, Scanner scanner) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
    if (scanner == null) throw new IllegalArgumentException("scanner cannot be null");

    System.out.print("Transaction id to edit: ");
    String raw = scanner.nextLine().trim();
    long id;
    try {
      id = Long.parseLong(raw);
    } catch (NumberFormatException e) {
      System.out.println("Id must be a number.");
      return;
    }

    System.out.print("New amount (EGP): ");
    double amount;
    try {
      amount = Double.parseDouble(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      System.out.println("Amount must be a valid number.");
      return;
    }

    printCategories(state);
    System.out.print("New category id: ");
    int categoryId;
    try {
      categoryId = Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException e) {
      System.out.println("Category id must be a number.");
      return;
    }

    System.out.print("New note (optional): ");
    String note = scanner.nextLine();

    try {
      expenseManager.editExpense(state, id, amount, categoryId, note);
      System.out.println("Transaction updated.");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Prompts the user to delete a transaction with confirmation (US #8).
   *
   * @param state current application state
   * @param scanner console input scanner
   */
  public void deleteTransaction(AppState state, Scanner scanner) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
    if (scanner == null) throw new IllegalArgumentException("scanner cannot be null");

    System.out.print("Transaction id to delete: ");
    String raw = scanner.nextLine().trim();
    long id;
    try {
      id = Long.parseLong(raw);
    } catch (NumberFormatException e) {
      System.out.println("Id must be a number.");
      return;
    }

    System.out.print("Confirm delete? (y/n): ");
    String confirm = scanner.nextLine().trim().toLowerCase();
    if (!confirm.equals("y")) {
      System.out.println("Cancelled.");
      return;
    }

    try {
      expenseManager.deleteExpense(state, id);
      System.out.println("Transaction deleted.");
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

