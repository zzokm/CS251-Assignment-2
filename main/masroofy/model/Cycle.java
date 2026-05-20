package masroofy.model;

/**
 * Budget cycle metadata persisted in SQLite.
 *
 * <p>US #1 needs the total allowance plus a start and end date. Dates are stored as ISO strings
 * ({@code yyyy-MM-dd}) in the database.
 */
public class Cycle {
  private long id;
  private double totalAllowance;
  private String startDate;
  private String endDate;
  private String lastCalculatedDate;
  private boolean alert80Shown;
  private boolean alert100Shown;
  private boolean overspentShown;

  /** Creates an empty cycle (used when loading from the database). */
  public Cycle() {}

  /**
   * Creates a cycle with id and allowance (dates can be set later).
   *
   * @param id cycle id
   * @param totalAllowance allowance amount
   */
  public Cycle(long id, double totalAllowance) {
    this.id = id;
    this.totalAllowance = totalAllowance;
  }

  /**
   * Creates a full cycle with allowance and dates.
   *
   * @param id cycle id
   * @param totalAllowance allowance amount
   * @param startDate cycle start date (yyyy-MM-dd)
   * @param endDate cycle end date (yyyy-MM-dd)
   */
  public Cycle(long id, double totalAllowance, String startDate, String endDate) {
    this.id = id;
    this.totalAllowance = totalAllowance;
    this.startDate = startDate;
    this.endDate = endDate;
    this.lastCalculatedDate = startDate;
    this.alert80Shown = false;
    this.alert100Shown = false;
    this.overspentShown = false;
  }

  /** @return the saved cycle id */
  public long getId() {
    return id;
  }

  /** @param id the saved cycle id */
  public void setId(long id) {
    this.id = id;
  }

  /** @return the starting allowance amount for this cycle */
  public double getTotalAllowance() {
    return totalAllowance;
  }

  /** @param totalAllowance the starting allowance amount for this cycle */
  public void setTotalAllowance(double totalAllowance) {
    this.totalAllowance = totalAllowance;
  }

  /** @return the cycle start date in {@code yyyy-MM-dd} format */
  public String getStartDate() {
    return startDate;
  }

  /** @param startDate the cycle start date in {@code yyyy-MM-dd} format */
  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  /** @return the cycle end date in {@code yyyy-MM-dd} format */
  public String getEndDate() {
    return endDate;
  }

  /** @param endDate the cycle end date in {@code yyyy-MM-dd} format */
  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  /** @return last date rollover calculations were checked */
  public String getLastCalculatedDate() {
    return lastCalculatedDate;
  }

  /** @param lastCalculatedDate last date rollover calculations were checked */
  public void setLastCalculatedDate(String lastCalculatedDate) {
    this.lastCalculatedDate = lastCalculatedDate;
  }

  /**
   * Returns whether the 80% warning was already shown for this cycle (US #6).
   *
   * <p>Persisted in SQLite to avoid spamming the user with repeated warnings.
   *
   * @return true if warning already shown
   */
  public boolean isAlert80Shown() {
    return alert80Shown;
  }

  /** @param alert80Shown whether the 80% warning was already shown for this cycle (US #6) */
  public void setAlert80Shown(boolean alert80Shown) {
    this.alert80Shown = alert80Shown;
  }

  /**
   * Returns whether the budget exhausted notification was already sent for this cycle.
   *
   * @return true if exhausted notification already sent
   */
  public boolean isAlert100Shown() {
    return alert100Shown;
  }

  /** @param alert100Shown whether the exhausted notification was already sent for this cycle */
  public void setAlert100Shown(boolean alert100Shown) {
    this.alert100Shown = alert100Shown;
  }

  /**
   * Returns whether the overspent notification was already sent for this cycle.
   *
   * @return true if overspent notification already sent
   */
  public boolean isOverspentShown() {
    return overspentShown;
  }

  /** @param overspentShown whether the overspent notification was already sent for this cycle */
  public void setOverspentShown(boolean overspentShown) {
    this.overspentShown = overspentShown;
  }
}
