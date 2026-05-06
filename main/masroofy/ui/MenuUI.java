package masroofy.ui;

import masroofy.core.AuthManager;
import masroofy.core.CycleManager;
import masroofy.core.ExpenseManager;
import masroofy.model.AppState;
import masroofy.model.Category;
import masroofy.model.Expense;
import java.util.Scanner;

/** Main console menu and startup router. */
public class MenuUI {
  private final AuthManager authManager;
  private final CycleManager cycleManager;
  private final ExpenseManager expenseManager;
  private final SetupUI setupUI;
  private final DashboardUI dashboardUI;
  private final HistoryUI historyUI;
  private final SettingsUI settingsUI;

  /** Creates the menu with default dependencies. */
  public MenuUI() {
    this(
        new AuthManager(),
        new CycleManager(),
        new ExpenseManager(),
        new SetupUI(),
        new DashboardUI(),
        new HistoryUI(),
        new SettingsUI());
  }

  /** Creates the menu with injected dependencies. */
  public MenuUI(CycleManager cycleManager, SetupUI setupUI, HistoryUI historyUI) {
    this(
        new AuthManager(),
        cycleManager,
        new ExpenseManager(),
        setupUI,
        new DashboardUI(),
        historyUI,
        new SettingsUI());
  }

  /** Creates the menu with injected dependencies. */
  public MenuUI(
      AuthManager authManager,
      CycleManager cycleManager,
      ExpenseManager expenseManager,
      SetupUI setupUI,
      DashboardUI dashboardUI,
      HistoryUI historyUI) {
    this(authManager, cycleManager, expenseManager, setupUI, dashboardUI, historyUI, new SettingsUI());
  }

  /** Creates the menu with injected dependencies. */
  public MenuUI(
      AuthManager authManager,
      CycleManager cycleManager,
      ExpenseManager expenseManager,
      SetupUI setupUI,
      DashboardUI dashboardUI,
      HistoryUI historyUI,
      SettingsUI settingsUI) {
    if (authManager == null) throw new IllegalArgumentException("authManager cannot be null");
    if (cycleManager == null) throw new IllegalArgumentException("cycleManager cannot be null");
    if (expenseManager == null) throw new IllegalArgumentException("expenseManager cannot be null");
    if (setupUI == null) throw new IllegalArgumentException("setupUI cannot be null");
    if (dashboardUI == null) throw new IllegalArgumentException("dashboardUI cannot be null");
    if (historyUI == null) throw new IllegalArgumentException("historyUI cannot be null");
    if (settingsUI == null) throw new IllegalArgumentException("settingsUI cannot be null");
    this.authManager = authManager;
    this.cycleManager = cycleManager;
    this.expenseManager = expenseManager;
    this.setupUI = setupUI;
    this.dashboardUI = dashboardUI;
    this.historyUI = historyUI;
    this.settingsUI = settingsUI;
  }

  /**
   * Starts the console flow.
   *
   * <p>US #5 is checked on startup because this console app has no background midnight scheduler.
   * If the saved calculation date is older than today, the cycle's safe daily limit is recalculated
   * from remaining balance and remaining days.
   *
   * @param state current application state loaded from JSON
   */
  public void start(AppState state) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");

    Scanner scanner = new Scanner(System.in);

    if (!runPrivacyGate(state, scanner)) {
      return;
    }

    if (!cycleManager.hasActiveCycle(state)) {
      setupUI.startSetup(state, scanner);
    }

    showMainMenu(state, scanner);
  }

  private void showMainMenu(AppState state, Scanner scanner) {
    boolean running = true;
    while (running) {
      System.out.println();
      System.out.println("Menu");
      System.out.println("1. Dashboard");
      System.out.println("2. Add expense");
      System.out.println("3. History");
      System.out.println("4. Filter history");
      System.out.println("5. Edit transaction");
      System.out.println("6. Delete transaction");
      System.out.println("7. Settings");
      System.out.println("0. Exit");
      System.out.print("Choose: ");

      String choice = scanner.nextLine().trim();
      switch (choice) {
        case "1":
          dashboardUI.show(state);
          break;
        case "2":
          showAddExpense(state, scanner);
          dashboardUI.show(state);
          break;
        case "3":
          historyUI.showHistory(state);
          break;
        case "4":
          historyUI.showFilteredHistory(state, scanner);
          break;
        case "5":
          historyUI.editTransaction(state, scanner);
          break;
        case "6":
          historyUI.deleteTransaction(state, scanner);
          break;
        case "7":
          settingsUI.show(state, scanner);
          if (!cycleManager.hasActiveCycle(state)) {
            setupUI.startSetup(state, scanner);
          }
          break;
        case "0":
          running = false;
          break;
        default:
          System.out.println("Unknown option.");
      }
    }
  }

  private void showAddExpense(AppState state, Scanner scanner) {
    System.out.println();
    System.out.println("Add Expense");
    System.out.println("-----------");
    printCategories(state);

    try {
      System.out.print("Category id: ");
      int categoryId = Integer.parseInt(scanner.nextLine().trim());

      System.out.print("Amount (EGP): ");
      double amount = Double.parseDouble(scanner.nextLine().trim());

      System.out.print("Note (optional): ");
      String note = scanner.nextLine();

      Expense expense = expenseManager.addExpense(state, categoryId, amount, note);
      System.out.printf("Expense #%d saved.%n", expense.getId());
    } catch (NumberFormatException e) {
      System.out.println("Category id and amount must be valid numbers.");
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

  private boolean runPrivacyGate(AppState state, Scanner scanner) {
    if (!authManager.isLockEnabled(state)) return true;

    while (true) {
      long now = System.currentTimeMillis();
      if (authManager.isLockedOut(state, now)) {
        long sec = (authManager.lockoutRemainingMillis(state, now) + 999) / 1000;
        System.out.println("Locked out. Try again in " + sec + " seconds.");
        return false;
      }

      System.out.print("Enter PIN: ");
      String pin = scanner.nextLine();
      boolean ok = authManager.verifyPin(state, pin);
      if (ok) return true;
      System.out.println("Wrong PIN.");
    }
  }
}
