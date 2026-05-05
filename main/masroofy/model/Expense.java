package masroofy.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A single spending transaction.
 *
 * <p>Timestamps are stored as epoch millis (UTC-based) for simplicity.
 */
public class Expense {
  private long id;
  private int categoryId;
  private double amount;
  private long timestampMillis;
  private String note;

  public Expense() {}

  public Expense(long id, int categoryId, double amount, long timestampMillis, String note) {
    this.id = id;
    this.categoryId = categoryId;
    this.amount = amount;
    this.timestampMillis = timestampMillis;
    this.note = note;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public int getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(int categoryId) {
    this.categoryId = categoryId;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public long getTimestampMillis() {
    return timestampMillis;
  }

  public void setTimestampMillis(long timestampMillis) {
    this.timestampMillis = timestampMillis;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public Map<String, Object> toJsonObject() {
    Map<String, Object> o = new LinkedHashMap<>();
    o.put("id", id);
    o.put("categoryId", categoryId);
    o.put("amount", amount);
    o.put("timestampMillis", timestampMillis);
    o.put("note", note);
    return o;
  }

  public static Expense fromJsonObject(Map<String, Object> o) {
    Expense e = new Expense();
    if (o == null) return e;
    Object id = o.get("id");
    if (id instanceof Number) e.setId(((Number) id).longValue());
    Object cat = o.get("categoryId");
    if (cat instanceof Number) e.setCategoryId(((Number) cat).intValue());
    Object amt = o.get("amount");
    if (amt instanceof Number) e.setAmount(((Number) amt).doubleValue());
    Object ts = o.get("timestampMillis");
    if (ts instanceof Number) e.setTimestampMillis(((Number) ts).longValue());
    Object note = o.get("note");
    if (note instanceof String) e.setNote((String) note);
    return e;
  }
}

