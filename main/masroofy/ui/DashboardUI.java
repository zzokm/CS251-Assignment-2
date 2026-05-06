package masroofy.ui;

import masroofy.core.CycleManager;
import masroofy.core.ReportManager;
import masroofy.core.ReportManager.SpendingInsight;
import masroofy.model.AppState;
import java.time.LocalDate;
import java.util.List;

/** Console dashboard screen (US #3, #4, #5, #6). */
public class DashboardUI {
  private final CycleManager cycleManager;
  private final ReportManager reportManager;

  /** Creates a dashboard UI with default managers. */
  public DashboardUI() {
    this(new CycleManager(), new ReportManager());
  }

  /**
   * Creates a dashboard UI with injected managers.
   *
   * @param cycleManager cycle logic
   * @param reportManager reporting logic
   */
  public DashboardUI(CycleManager cycleManager, ReportManager reportManager) {
    if (cycleManager == null) throw new IllegalArgumentException("cycleManager cannot be null");
    if (reportManager == null) throw new IllegalArgumentException("reportManager cannot be null");
    this.cycleManager = cycleManager;
    this.reportManager = reportManager;
  }

  /**
   * Shows dashboard numbers and spending insights.
   *
   * <p>Also triggers rollover check (US #5) and threshold messages (US #6).
   *
   * @param state application state
   */
  public void show(AppState state) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");

    System.out.println();
    System.out.println("Dashboard");
    System.out.println("---------");

    CycleManager.RolloverResult rollover =
        cycleManager.handleRolloverIfNeeded(state, LocalDate.now());

    System.out.printf("Remaining balance: %.2f EGP%n", rollover.getRemainingBalance());
    System.out.printf("Remaining days: %d%n", rollover.getRemainingDays());
    System.out.printf("Safe daily limit: %.2f EGP%n", rollover.getSafeDailyLimit());

    if (rollover.isOverspent()) {
      System.out.println("Warning: spending is above the cycle allowance.");
    }

    CycleManager.ThresholdResult threshold = cycleManager.checkThresholds(state);
    if (threshold.isBudgetExhausted()) {
      System.out.println("Budget exhausted.");
    } else if (threshold.shouldShow80Warning()) {
      System.out.println("Warning: you reached 80% of your budget.");
    }

    printInsights(state);
  }

  private void printInsights(AppState state) {
    List<SpendingInsight> insights = reportManager.getSpendingInsights(state);
    if (insights.isEmpty()) {
      System.out.println();
      System.out.println("No expenses yet.");
      return;
    }

    double total = 0.0;
    for (SpendingInsight i : insights) total += i.getTotal();

    System.out.println();
    System.out.println("Spending by category");
    System.out.println("--------------------");
    for (SpendingInsight i : insights) {
      System.out.printf(
          "%s: %.2f (%.1f%%) %s%n",
          i.getCategoryName(),
          i.getTotal(),
          i.getPercent(),
          reportManager.bar(i.getPercent(), 20));
    }
    System.out.printf("Total spent: %.2f%n", total);
  }
}
