package masroofy.core;

import masroofy.model.AppState;
import masroofy.model.Cycle;
import masroofy.model.Expense;
import masroofy.storage.DatabaseHelper;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Handles budget cycle setup and cycle-based calculations. */
public class CycleManager {
  /** Creates a new cycle manager. */
  public CycleManager() {}

  /** Result returned from threshold checks (US #6). */
  public static final class ThresholdResult {
    private final boolean budgetExhausted;
    private final boolean show80Warning;

    /**
     * Creates a threshold result.
     *
     * @param budgetExhausted true if total spent >= allowance
     * @param show80Warning true if 80% warning should be shown now
     */
    public ThresholdResult(boolean budgetExhausted, boolean show80Warning) {
      this.budgetExhausted = budgetExhausted;
      this.show80Warning = show80Warning;
    }

    /**
     * Returns true when total spent is greater than or equal to allowance.
     *
     * @return true if the budget is exhausted
     */
    public boolean isBudgetExhausted() {
      return budgetExhausted;
    }

    /**
     * Returns true when 80% warning should be shown now (one-time).
     *
     * @return true if the warning should be printed now
     */
    public boolean shouldShow80Warning() {
      return show80Warning;
    }
  }

  /** Result returned after checking whether daily rollover changed the cycle calculation date. */
  public static final class RolloverResult {
    private final boolean rolloverApplied;
    private final double remainingBalance;
    private final long remainingDays;
    private final double safeDailyLimit;

    /**
     * Creates a rollover result.
     *
     * @param rolloverApplied true if lastCalculatedDate was advanced and saved
     * @param remainingBalance allowance minus spent
     * @param remainingDays remaining days used in limit calculation (>= 1)
     * @param safeDailyLimit remainingBalance / remainingDays
     */
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

    /**
     * Returns true when the saved calculation date was advanced to today.
     *
     * @return true if rollover was applied
     */
    public boolean isRolloverApplied() {
      return rolloverApplied;
    }

    /**
     * Returns the allowance remaining after saved expenses.
     *
     * @return remaining balance
     */
    public double getRemainingBalance() {
      return remainingBalance;
    }

    /**
     * Returns the number of days used in the safe daily limit calculation.
     *
     * @return remaining days
     */
    public long getRemainingDays() {
      return remainingDays;
    }

    /**
     * Returns the recalculated safe daily limit.
     *
     * @return safe daily limit
     */
    public double getSafeDailyLimit() {
      return safeDailyLimit;
    }

    /**
     * Returns true when spending is already higher than the cycle allowance.
     *
     * @return true if remainingBalance is negative
     */
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
        state,
        totalAllowance,
        DateFormats.parseFlexible(startDateIso, "Start date"),
        DateFormats.parseFlexible(endDateIso, "End date"));
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
    if (remainingBalance < 0.0 && !cycle.isOverspentShown()) {
      cycle.setOverspentShown(true);
      saveNow(state);
      Notifications.forState(state).overspent(remainingBalance);
    }
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
    return handleRolloverIfNeeded(state, DateFormats.parseFlexible(todayIso, "Today"));
  }

  /**
   * Returns the total number of days in the cycle.
   *
   * @param cycle active budget cycle
   * @return calendar-day distance between start and end dates
   */
  public long getTotalCycleDays(Cycle cycle) {
    validateCycleExists(cycle);
    LocalDate start = DateFormats.parseFlexible(cycle.getStartDate(), "Start date");
    LocalDate end = DateFormats.parseFlexible(cycle.getEndDate(), "End date");
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

    LocalDate start = DateFormats.parseFlexible(cycle.getStartDate(), "Start date");
    LocalDate end = DateFormats.parseFlexible(cycle.getEndDate(), "End date");
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

  /**
   * Checks spending thresholds for the active cycle (US #6).
   *
   * <p>Rules:
   * <ul>
   *   <li>If spent >= allowance → budget exhausted.</li>
   *   <li>If spent/allowance >= 0.80 and the warning has not been shown before → show warning once
   *       and persist the flag.</li>
   * </ul>
   *
   * <p>This method may save JSON if it needs to mark the 80% warning as shown.
   *
   * @param state application state containing active cycle and expenses
   * @return threshold result for the UI
   */
  public ThresholdResult checkThresholds(AppState state) {
    validateState(state);
    validateCycleExists(state.getActiveCycle());

    Cycle cycle = state.getActiveCycle();
    double allowance = cycle.getTotalAllowance();
    if (allowance <= 0.0) return new ThresholdResult(false, false);

    double spent = allowance - calculateRemainingBalance(state);
    boolean exhausted = spent >= allowance;

    boolean show80 = false;
    if (!cycle.isAlert80Shown() && spent / allowance >= 0.80) {
      show80 = true;
      cycle.setAlert80Shown(true);
      saveNow(state);
      Notifications.forState(state).budget80(allowance, spent);
    }

    if (exhausted && !cycle.isAlert100Shown()) {
      cycle.setAlert100Shown(true);
      saveNow(state);
      Notifications.forState(state).budgetExhausted(allowance, spent);
    }

    return new ThresholdResult(exhausted, show80);
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

  private static void validateState(AppState state) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
  }

  private static void validateCycleExists(Cycle cycle) {
    if (cycle == null) throw new IllegalArgumentException("No active cycle exists.");
  }

  private static LocalDate getLastCalculatedDateOrStartDate(Cycle cycle) {
    String lastCalculatedDate = cycle.getLastCalculatedDate();
    if (lastCalculatedDate == null || lastCalculatedDate.trim().isEmpty()) {
      LocalDate startDate = DateFormats.parseFlexible(cycle.getStartDate(), "Start date");
      cycle.setLastCalculatedDate(startDate.toString());
      return startDate;
    }
    return DateFormats.parseFlexible(lastCalculatedDate, "Last calculated date");
  }

  private static void saveNow(AppState state) {
    try {
      DatabaseHelper.saveState(state);
    } catch (SQLException e) {
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

    LocalDate start = DateFormats.parseFlexible(startDate, "Start date");
    LocalDate end = DateFormats.parseFlexible(endDate, "End date");

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
    Notifications.forState(state).cycleReset(startDate, endDate, newAllowance);
  }

  /**
   * Resets the current budget cycle (US #11).
   *
   * <p>Clears the active cycle and removes all expenses. Categories and privacy settings are kept.
   * Changes are saved immediately.
   *
   * @param state application state to update
   */
  public void resetCycle(AppState state) {
    validateState(state);
    state.setActiveCycle(null);
    state.getExpenses().clear();
    state.setNextExpenseId(1L);
    saveNow(state);
    Notifications.forState(state).send("Cycle reset", "Cycle was cleared (no active cycle).", "3", "info,money");
  }


}

