package masroofy.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Budget cycle metadata.
 *
 * <p>This project keeps cycle data minimal; calculations are performed by managers from persisted
 * expenses.
 */
public class Cycle {
  private long id;
  private double totalAllowance;

  public Cycle() {}

  public Cycle(long id, double totalAllowance) {
    this.id = id;
    this.totalAllowance = totalAllowance;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public double getTotalAllowance() {
    return totalAllowance;
  }

  public void setTotalAllowance(double totalAllowance) {
    this.totalAllowance = totalAllowance;
  }

  public Map<String, Object> toJsonObject() {
    Map<String, Object> o = new LinkedHashMap<>();
    o.put("id", id);
    o.put("totalAllowance", totalAllowance);
    return o;
  }

  public static Cycle fromJsonObject(Map<String, Object> o) {
    Cycle c = new Cycle();
    if (o == null) return c;
    Object id = o.get("id");
    if (id instanceof Number) c.setId(((Number) id).longValue());
    Object t = o.get("totalAllowance");
    if (t instanceof Number) c.setTotalAllowance(((Number) t).doubleValue());
    return c;
  }
}

