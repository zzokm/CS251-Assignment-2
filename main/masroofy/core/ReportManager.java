package masroofy.core;

import masroofy.model.AppState;
import masroofy.model.Category;
import masroofy.model.Expense;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reporting / dashboard calculations.
 *
 * <p>US #4: Visual Spending Insights (console). Computes totals per category and percentage share of
 * total spending.
 */
public class ReportManager {
  /** Creates a new report manager. */
  public ReportManager() {}

  /** A stable structure for the UI to print. */
  public static final class SpendingInsight {
    private final int categoryId;
    private final String categoryName;
    private final double total;
    private final double percent;

    /**
     * Creates an insight row.
     *
     * @param categoryId category id
     * @param categoryName category display name
     * @param total total spent in that category
     * @param percent percent share of total spending (0..100)
     */
    public SpendingInsight(int categoryId, String categoryName, double total, double percent) {
      this.categoryId = categoryId;
      this.categoryName = categoryName;
      this.total = total;
      this.percent = percent;
    }

    /** @return category id */
    public int getCategoryId() {
      return categoryId;
    }

    /** @return category name */
    public String getCategoryName() {
      return categoryName;
    }

    /** @return total amount spent in this category */
    public double getTotal() {
      return total;
    }

    /** @return percent share of total spending */
    public double getPercent() {
      return percent;
    }
  }

  /**
   * Aggregates expenses by category and computes percent share.
   *
   * <p>Percent formula: {@code percent = (categoryTotal / totalSpent) * 100}. If totalSpent is zero,
   * all percents are 0.
   * @param state application state
   * @return list of insight rows
   */
  public List<SpendingInsight> getSpendingInsights(AppState state) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");

    Map<Integer, String> catNames = new LinkedHashMap<>();
    for (Category c : state.getCategories()) catNames.put(c.getId(), c.getName());

    Map<Integer, Double> totals = new LinkedHashMap<>();
    double totalSpent = 0.0;
    for (Expense e : state.getExpenses()) {
      double amt = e.getAmount();
      totalSpent += amt;
      totals.put(e.getCategoryId(), totals.getOrDefault(e.getCategoryId(), 0.0) + amt);
    }

    List<SpendingInsight> out = new ArrayList<>();
    for (Map.Entry<Integer, Double> entry : totals.entrySet()) {
      int catId = entry.getKey();
      double catTotal = entry.getValue();
      double percent = (totalSpent <= 0.0) ? 0.0 : (catTotal / totalSpent) * 100.0;
      String name = catNames.getOrDefault(catId, "Unknown");
      out.add(new SpendingInsight(catId, name, catTotal, percent));
    }
    return out;
  }

  /**
   * Builds a simple ASCII bar using only keyboard characters.
   *
   * <p>Example: {@code [######--------------]}
   *
   * @param percent value between 0 and 100
   * @param width bar width (commonly 20)
   * @return bar string
   */
  public String bar(double percent, int width) {
    int w = Math.max(1, width);
    double p = Math.max(0.0, Math.min(100.0, percent));
    int filled = (int) Math.round((p / 100.0) * w);
    return "[" + "#".repeat(filled) + "-".repeat(w - filled) + "]";
  }
}

