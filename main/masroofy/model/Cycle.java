package masroofy.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Budget cycle metadata saved in JSON.
 *
 * <p>US #1 needs the total allowance plus a start and end date. Dates are stored as ISO strings
 * ({@code yyyy-MM-dd}) so the JSON file stays simple and readable.
 */
public class Cycle {
  private long id;
  private double totalAllowance;
  private String startDate;
  private String endDate;
  private String lastCalculatedDate;
  private boolean alert80Shown;

  public Cycle() {}

  public Cycle(long id, double totalAllowance) {
    this.id = id;
    this.totalAllowance = totalAllowance;
  }

  public Cycle(long id, double totalAllowance, String startDate, String endDate) {
    this.id = id;
    this.totalAllowance = totalAllowance;
    this.startDate = startDate;
    this.endDate = endDate;
    this.lastCalculatedDate = startDate;
    this.alert80Shown = false;
  }

  /** Returns the saved cycle id. */
  public long getId() {
    return id;
  }

  /** Sets the saved cycle id. */
  public void setId(long id) {
    this.id = id;
  }

  /** Returns the starting allowance amount for this cycle. */
  public double getTotalAllowance() {
    return totalAllowance;
  }

  /** Sets the starting allowance amount for this cycle. */
  public void setTotalAllowance(double totalAllowance) {
    this.totalAllowance = totalAllowance;
  }

  /** Returns the cycle start date in {@code yyyy-MM-dd} format. */
  public String getStartDate() {
    return startDate;
  }

  /** Sets the cycle start date in {@code yyyy-MM-dd} format. */
  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  /** Returns the cycle end date in {@code yyyy-MM-dd} format. */
  public String getEndDate() {
    return endDate;
  }

  /** Sets the cycle end date in {@code yyyy-MM-dd} format. */
  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  /** Returns the last date rollover calculations were checked. */
  public String getLastCalculatedDate() {
    return lastCalculatedDate;
  }

  /** Sets the last date rollover calculations were checked. */
  public void setLastCalculatedDate(String lastCalculatedDate) {
    this.lastCalculatedDate = lastCalculatedDate;
  }

  /**
   * Returns whether the 80% warning was already shown for this cycle (US #6).
   *
   * <p>This is saved in JSON to avoid spamming the user with repeated warnings.
   */
  public boolean isAlert80Shown() {
    return alert80Shown;
  }

  /** Sets whether the 80% warning was already shown for this cycle (US #6). */
  public void setAlert80Shown(boolean alert80Shown) {
    this.alert80Shown = alert80Shown;
  }

  /** Converts this cycle to a JSON-friendly object. */
  public Map<String, Object> toJsonObject() {
    Map<String, Object> o = new LinkedHashMap<>();
    o.put("id", id);
    o.put("totalAllowance", totalAllowance);
    o.put("startDate", startDate);
    o.put("endDate", endDate);
    o.put("lastCalculatedDate", lastCalculatedDate);
    o.put("alert80Shown", alert80Shown);
    return o;
  }

  /** Creates a cycle from a JSON-friendly object. */
  public static Cycle fromJsonObject(Map<String, Object> o) {
    Cycle c = new Cycle();
    if (o == null) return c;
    Object id = o.get("id");
    if (id instanceof Number) c.setId(((Number) id).longValue());
    Object t = o.get("totalAllowance");
    if (t instanceof Number) c.setTotalAllowance(((Number) t).doubleValue());
    Object start = o.get("startDate");
    if (start instanceof String) c.setStartDate((String) start);
    Object end = o.get("endDate");
    if (end instanceof String) c.setEndDate((String) end);
    Object last = o.get("lastCalculatedDate");
    if (last instanceof String) c.setLastCalculatedDate((String) last);
    Object shown = o.get("alert80Shown");
    if (shown instanceof Boolean) c.setAlert80Shown((Boolean) shown);
    return c;
  }
}

