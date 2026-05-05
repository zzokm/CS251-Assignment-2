package masroofy.core;

import masroofy.model.AppState;
import masroofy.model.Cycle;
import masroofy.model.Expense;
import masroofy.storage.JsonStore;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/** Handles budget cycle setup and cycle-based calculations. */
public class CycleManager {

  /** Result returned after checking whether daily rollover changed the cycle calculation date. */
  public static final class RolloverResult {
    private final boolean rolloverApplied;
    private final double remainingBalance;
    private final long remainingDays;
    private final double safeDailyLimit;

    public RolloverResult(
        boolean rolloverApplied,
        double remainingBalance,
        long remainingDays,
        double safeDailyLimit) {
      this.rolloverApplied = rolloverApplied;
      this.remainingBalance = remainingBalance;
      this.remainingDays = remainingDays;
      this.safeDailyLimit = safeDailyLimit;
    }

    /** Returns true when the saved calculation date was advanced to today. */
    public boolean isRolloverApplied() {
      return rolloverApplied;
    }

    /** Returns the allowance remaining after saved expenses. */
    public double getRemainingBalance() {
      return remainingBalance;
    }

    /** Returns the number of days used in the safe daily limit calculation. */
    public long getRemainingDays() {
      return remainingDays;
    }

    /** Returns the recalculated safe daily limit. */
    public double getSafeDailyLimit() {
      return safeDailyLimit;
    }

    /** Returns true when spending is already higher than the cycle allowance. */
    public boolean isOverspent() {
      return remainingBalance < 0.0;
    }
  }

  /**
   * Creates the first active budget cycle for US #1 and saves it immediately.
   *
   * <p>Validation rules: allowance must be greater than zero, and end date must be strictly after
   * start date.
   *
   * @param state application state to update
   * @param totalAllowance user's starting cash allowance
   * @param startDate cycle start date
   * @param endDate cycle end date
   * @return the newly created active cycle
   */
  public Cycle initializeCycle(
      AppState state, double totalAllowance, LocalDate startDate, LocalDate endDate) {
    validateState(state);
    validateCycleInput(totalAllowance, startDate, endDate);

    Cycle cycle =
        new Cycle(System.currentTimeMillis(), totalAllowance, startDate.toString(), endDate.toString());
    cycle.setLastCalculatedDate(startDate.toString());
    state.setActiveCycle(cycle);
    saveNow(state);
    return cycle;
  }

  /**
   * Parses date strings, creates the active budget cycle, and saves it immediately.
   *
   * @param state application state to update
   * @param totalAllowance user's starting cash allowance
   * @param startDateIso cycle start date in {@code yyyy-MM-dd} format
   * @param endDateIso cycle end date in {@code yyyy-MM-dd} format
   * @return the newly created active cycle
   */
  public Cycle initializeCycle(
      AppState state, double totalAllowance, String startDateIso, String endDateIso) {
    return initializeCycle(
        state, totalAllowance, parseDate(startDateIso, "start date"), parseDate(endDateIso, "end date"));
  }

  /**
   * Calculates the initial safe daily limit for a new cycle.
   *
   * <p>Formula: {@code totalAllowance / numberOfDays}. The number of days is the calendar-day
   * distance from start date to end date, so 2026-05-05 to 2026-06-04 is 30 days.
   *
   * @param cycle active budget cycle
   * @return initial safe daily limit
   */
  public double calculateInitialSafeDailyLimit(Cycle cycle) {
    validateCycleExists(cycle);
    long totalDays = getTotalCycleDays(cycle);
    return cycle.getTotalAllowance() / totalDays;
  }

  /**
   * Calculates remaining balance from allowance minus all saved expenses.
   *
   * @param state application state containing active cycle and expenses
   * @return remaining cycle balance
   */
  public double calculateRemainingBalance(AppState state) {
    validateState(state);
    validateCycleExists(state.getActiveCycle());

    double spent = 0.0;
    for (Expense expense : state.getExpenses()) {
      spent += expense.getAmount();
    }
    return state.getActiveCycle().getTotalAllowance() - spent;
  }

  /**
   * Calculates safe daily limit for the current date using remaining balance and remaining days.
   *
   * <p>Formula: {@code remainingBalance / remainingDays}. If the date is on or after the end date,
   * this method uses one remaining day so the dashboard can still show a final-day amount.
   *
   * @param state application state containing active cycle and expenses
   * @param today current date used for the calculation
   * @return safe daily limit for today
   */
  public double calculateSafeDailyLimit(AppState state, LocalDate today) {
    validateState(state);
    validateCycleExists(state.getActiveCycle());
    if (today == null) throw new IllegalArgumentException("today cannot be null");

    long remainingDays = getRemainingDays(state.getActiveCycle(), today);
    return calculateRemainingBalance(state) / remainingDays;
  }

  /**
   * Handles US #5 rollover when the app starts or the dashboard opens.
   *
   * <p>No background scheduler is used. If {@code today} is after the cycle's
   * {@code lastCalculatedDate}, this method updates that date, saves JSON immediately, and returns
   * the recalculated balance, remaining days, and safe daily limit. Unspent money naturally rolls
   * into the new limit because the formula uses current remaining balance over current remaining
   * days.
   *
   * @param state application state containing the active cycle and expenses
   * @param today current date used for rollover detection
   * @return rollover calculation result for the UI to display
   */
  public RolloverResult handleRolloverIfNeeded(AppState state, LocalDate today) {
    validateState(state);
    validateCycleExists(state.getActiveCycle());
    if (today == null) throw new IllegalArgumentException("today cannot be null");

    Cycle cycle = state.getActiveCycle();
    boolean missingLastCalculatedDate =
        cycle.getLastCalculatedDate() == null || cycle.getLastCalculatedDate().trim().isEmpty();
    LocalDate lastCalculatedDate = getLastCalculatedDateOrStartDate(cycle);
    boolean shouldApply = missingLastCalculatedDate || today.isAfter(lastCalculatedDate);

    if (shouldApply) {
      cycle.setLastCalculatedDate(today.toString());
      saveNow(state);
    }

    double remainingBalance = calculateRemainingBalance(state);
    long remainingDays = getRemainingDays(cycle, today);
    double safeDailyLimit = remainingBalance / remainingDays;
    return new RolloverResult(shouldApply, remainingBalance, remainingDays, safeDailyLimit);
  }

  /**
   * Parses date strings and handles US #5 rollover when needed.
   *
   * @param state application state containing the active cycle and expenses
   * @param todayIso current date in {@code yyyy-MM-dd} format
   * @return rollover calculation result for the UI to display
   */
  public RolloverResult handleRolloverIfNeeded(AppState state, String todayIso) {
    return handleRolloverIfNeeded(state, parseDate(todayIso, "today"));
  }

  /**
   * Returns the total number of days in the cycle.
   *
   * @param cycle active budget cycle
   * @return calendar-day distance between start and end dates
   */
  public long getTotalCycleDays(Cycle cycle) {
    validateCycleExists(cycle);
    LocalDate start = parseDate(cycle.getStartDate(), "start date");
    LocalDate end = parseDate(cycle.getEndDate(), "end date");
    validateCycleInput(cycle.getTotalAllowance(), start, end);
    return ChronoUnit.DAYS.between(start, end);
  }

  /**
   * Returns remaining days from the supplied date until the cycle end date.
   *
   * @param cycle active budget cycle
   * @param today current date used for the calculation
   * @return at least one day
   */
  public long getRemainingDays(Cycle cycle, LocalDate today) {
    validateCycleExists(cycle);
    if (today == null) throw new IllegalArgumentException("today cannot be null");

    LocalDate start = parseDate(cycle.getStartDate(), "start date");
    LocalDate end = parseDate(cycle.getEndDate(), "end date");
    validateCycleInput(cycle.getTotalAllowance(), start, end);

    if (today.isBefore(start)) return ChronoUnit.DAYS.between(start, end);
    long days = ChronoUnit.DAYS.between(today, end);
    return Math.max(1L, days);
  }

  /**
   * Checks whether the app already has an active budget cycle.
   *
   * @param state application state to inspect
   * @return true if an active cycle exists
   */
  public boolean hasActiveCycle(AppState state) {
    return state != null && state.getActiveCycle() != null;
  }

  private static void validateCycleInput(
      double totalAllowance, LocalDate startDate, LocalDate endDate) {
    if (totalAllowance <= 0.0) {
      throw new IllegalArgumentException("Allowance must be a positive number.");
    }
    if (startDate == null) {
      throw new IllegalArgumentException("Start date cannot be empty.");
    }
    if (endDate == null) {
      throw new IllegalArgumentException("End date cannot be empty.");
    }
    if (!endDate.isAfter(startDate)) {
      throw new IllegalArgumentException("End date must be after start date.");
    }
  }

  private static LocalDate parseDate(String value, String label) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(label + " cannot be empty.");
    }
    try {
      return LocalDate.parse(value.trim());
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(label + " must use yyyy-MM-dd format.", e);
    }
  }

  private static void validateState(AppState state) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
  }

  private static void validateCycleExists(Cycle cycle) {
    if (cycle == null) throw new IllegalArgumentException("No active cycle exists.");
  }

  private static LocalDate getLastCalculatedDateOrStartDate(Cycle cycle) {
    String lastCalculatedDate = cycle.getLastCalculatedDate();
    if (lastCalculatedDate == null || lastCalculatedDate.trim().isEmpty()) {
      LocalDate startDate = parseDate(cycle.getStartDate(), "start date");
      cycle.setLastCalculatedDate(startDate.toString());
      return startDate;
    }
    return parseDate(lastCalculatedDate, "last calculated date");
  }

  private static void saveNow(AppState state) {
    try {
      JsonStore.saveState(state);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to save state", e);
    }
  }


  /**
   * Resets the current budget cycle (US #11). Jana
   *
   * <p>Clears all expenses and creates a new cycle with the given allowance and dates.
   * Changes are saved immediately.
   *
   * @param state application state
   * @param newAllowance new cycle allowance
   * @param startDate start date (yyyy-MM-dd)
   * @param endDate end date (yyyy-MM-dd)
   */
  public void resetCycle(AppState state, double newAllowance, String startDate, String endDate) {
    validateState(state);

    LocalDate start = parseDate(startDate, "start date");
    LocalDate end = parseDate(endDate, "end date");

    validateCycleInput(newAllowance, start, end);

    // Clear all existing expenses
    state.getExpenses().clear();

    // Create new cycle
    Cycle newCycle = new Cycle(
            System.currentTimeMillis(),
            newAllowance,
            startDate,
            endDate
    );

    newCycle.setLastCalculatedDate(startDate);

    state.setActiveCycle(newCycle);

    saveNow(state);
  }


}

