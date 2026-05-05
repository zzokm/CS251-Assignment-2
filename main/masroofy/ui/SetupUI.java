package masroofy.ui;

import masroofy.core.CycleManager;
import masroofy.model.AppState;
import masroofy.model.Cycle;
import java.util.Scanner;

/** Console setup screen for US #1: Set Initial Budget Cycle. */
public class SetupUI {
  private final CycleManager cycleManager;

  /** Creates the setup screen with the default cycle manager. */
  public SetupUI() {
    this(new CycleManager());
  }

  /** Creates the setup screen with an injected cycle manager. */
  public SetupUI(CycleManager cycleManager) {
    if (cycleManager == null) throw new IllegalArgumentException("cycleManager cannot be null");
    this.cycleManager = cycleManager;
  }

  /**
   * Runs the initial budget setup flow.
   *
   * <p>The user enters total allowance, start date, and end date. Validation and persistence are
   * handled by {@link CycleManager#initializeCycle(AppState, double, String, String)}. The flow
   * repeats until valid input is saved.
   *
   * @param state application state to update
   * @param scanner console input scanner
   * @return the newly created active cycle
   */
  public Cycle startSetup(AppState state, Scanner scanner) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
    if (scanner == null) throw new IllegalArgumentException("scanner cannot be null");

    System.out.println();
    System.out.println("Initial Setup");
    System.out.println("-------------");

    while (true) {
      try {
        System.out.print("Total allowance (EGP): ");
        double allowance = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("Start date (yyyy-MM-dd): ");
        String startDate = scanner.nextLine().trim();

        System.out.print("End date (yyyy-MM-dd): ");
        String endDate = scanner.nextLine().trim();

        Cycle cycle = cycleManager.initializeCycle(state, allowance, startDate, endDate);
        double initialDailyLimit = cycleManager.calculateInitialSafeDailyLimit(cycle);
        System.out.printf("Cycle saved. Initial safe daily limit: %.2f EGP%n", initialDailyLimit);
        return cycle;
      } catch (NumberFormatException e) {
        System.out.println("Allowance must be a valid number.");
      } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage());
      }

      System.out.println("Please try again.");
      System.out.println();
    }
  }
}

