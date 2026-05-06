package masroofy.ui;

import masroofy.core.CycleManager;
import masroofy.core.DateFormats;
import masroofy.model.AppState;
import masroofy.model.Cycle;
import java.time.LocalDate;
import java.util.Scanner;

/** Console setup screen for initial budget cycle setup. */
public class SetupUI {
  private final CycleManager cycleManager;

  /** Creates the setup screen with the default cycle manager. */
  public SetupUI() {
    this(new CycleManager());
  }

  /**
   * Creates the setup screen with an injected cycle manager.
   *
   * @param cycleManager cycle manager
   */
  public SetupUI(CycleManager cycleManager) {
    if (cycleManager == null) throw new IllegalArgumentException("cycleManager cannot be null");
    this.cycleManager = cycleManager;
  }

  /**
   * Runs the initial budget setup flow.
   *
   * <p>User chooses a cycle period (weekly/biweekly/monthly) and a start day (today / first day of
   * this month / custom). Custom start date cannot be in the future.
   *
   * <p>Validation and persistence are handled by {@link CycleManager#initializeCycle(AppState,
   * double, String, String)}.
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

        LocalDate startDate = readStartDate(scanner);
        LocalDate endDate = calculateEndDate(startDate, readPeriod(scanner));

        Cycle cycle =
            cycleManager.initializeCycle(state, allowance, startDate.toString(), endDate.toString());
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

  private Period readPeriod(Scanner scanner) {
    while (true) {
      System.out.println();
      System.out.println("Choose period:");
      System.out.println("1. Weekly");
      System.out.println("2. Biweekly");
      System.out.println("3. Monthly");
      System.out.print("Choose: ");
      String choice = scanner.nextLine().trim();
      switch (choice) {
        case "1":
          return Period.WEEKLY;
        case "2":
          return Period.BIWEEKLY;
        case "3":
          return Period.MONTHLY;
        default:
          System.out.println("Unknown option.");
      }
    }
  }

  private LocalDate readStartDate(Scanner scanner) {
    while (true) {
      System.out.println();
      System.out.println("Choose start day:");
      System.out.println("1. Today");
      System.out.println("2. First day of this month");
      System.out.println("3. Custom (not in future)");
      System.out.print("Choose: ");
      String choice = scanner.nextLine().trim();
      LocalDate today = LocalDate.now();
      switch (choice) {
        case "1":
          return today;
        case "2":
          return today.withDayOfMonth(1);
        case "3":
          System.out.print("Custom start date (DD MM YYYY): ");
          String raw = scanner.nextLine().trim();
          LocalDate d = DateFormats.parseFlexible(raw, "Start date");
          if (d.isAfter(today)) {
            System.out.println("Start date cannot be in the future.");
            continue;
          }
          return d;
        default:
          System.out.println("Unknown option.");
      }
    }
  }

  private LocalDate calculateEndDate(LocalDate startDate, Period period) {
    if (startDate == null) throw new IllegalArgumentException("startDate cannot be null");
    if (period == null) throw new IllegalArgumentException("period cannot be null");
    switch (period) {
      case WEEKLY:
        return startDate.plusWeeks(1);
      case BIWEEKLY:
        return startDate.plusWeeks(2);
      case MONTHLY:
        return startDate.plusMonths(1);
      default:
        throw new IllegalArgumentException("Unknown period");
    }
  }

  private enum Period {
    WEEKLY,
    BIWEEKLY,
    MONTHLY
  }
}

