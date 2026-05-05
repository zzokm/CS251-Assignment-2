package masroofy.ui;

import masroofy.core.CycleManager;
import masroofy.model.AppState;
import java.time.LocalDate;
import java.util.Scanner;

/** Main console menu and startup router. */
public class MenuUI {
  private final CycleManager cycleManager;
  private final SetupUI setupUI;
  private final HistoryUI historyUI;

  /** Creates the menu with default dependencies. */
  public MenuUI() {
    this(new CycleManager(), new SetupUI(), new HistoryUI());
  }

  /** Creates the menu with injected dependencies. */
  public MenuUI(CycleManager cycleManager, SetupUI setupUI, HistoryUI historyUI) {
    if (cycleManager == null) throw new IllegalArgumentException("cycleManager cannot be null");
    if (setupUI == null) throw new IllegalArgumentException("setupUI cannot be null");
    if (historyUI == null) throw new IllegalArgumentException("historyUI cannot be null");
    this.cycleManager = cycleManager;
    this.setupUI = setupUI;
    this.historyUI = historyUI;
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
    if (!cycleManager.hasActiveCycle(state)) {
      setupUI.startSetup(state, scanner);
    }

    showRolloverStatus(state);
    showMainMenu(state, scanner);
  }

  private void showMainMenu(AppState state, Scanner scanner) {
    boolean running = true;
    while (running) {
      System.out.println();
      System.out.println("Menu");
      System.out.println("1. Filter transaction history");
      System.out.println("0. Exit");
      System.out.print("Choose: ");

      String choice = scanner.nextLine().trim();
      switch (choice) {
        case "1":
          historyUI.showFilteredHistory(state, scanner);
          break;
        case "0":
          running = false;
          break;
        default:
          System.out.println("Unknown option.");
      }
    }
  }

  private void showRolloverStatus(AppState state) {
    CycleManager.RolloverResult result =
        cycleManager.handleRolloverIfNeeded(state, LocalDate.now());

    System.out.println();
    if (result.isRolloverApplied()) {
      System.out.println("Daily rollover applied for today.");
    } else {
      System.out.println("Daily rollover already up to date.");
    }

    if (result.isOverspent()) {
      System.out.println("Warning: spending is above the cycle allowance.");
    }

    System.out.printf("Remaining balance: %.2f EGP%n", result.getRemainingBalance());
    System.out.printf("Remaining days: %d%n", result.getRemainingDays());
    System.out.printf("Safe daily limit: %.2f EGP%n", result.getSafeDailyLimit());
  }
}
