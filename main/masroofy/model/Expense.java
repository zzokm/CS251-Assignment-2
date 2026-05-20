package masroofy.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

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

  /** Creates an empty expense (used when loading from the database). */
  public Expense() {}

  /**
   * Creates an expense transaction.
   *
   * @param id expense id
   * @param categoryId category id
   * @param amount amount spent
   * @param timestampMillis timestamp in epoch millis
   * @param note optional note
   */
  public Expense(long id, int categoryId, double amount, long timestampMillis, String note) {
    this.id = id;
    this.categoryId = categoryId;
    this.amount = amount;
    this.timestampMillis = timestampMillis;
    this.note = note;
  }

  /** @return expense id */
  public long getId() {
    return id;
  }

  /** @param id expense id */
  public void setId(long id) {
    this.id = id;
  }

  /** @return category id */
  public int getCategoryId() {
    return categoryId;
  }

  /** @param categoryId category id */
  public void setCategoryId(int categoryId) {
    this.categoryId = categoryId;
  }

  /** @return amount spent */
  public double getAmount() {
    return amount;
  }

  /** @param amount amount spent */
  public void setAmount(double amount) {
    this.amount = amount;
  }

  /** @return timestamp in epoch millis */
  public long getTimestampMillis() {
    return timestampMillis;
  }

  /** @param timestampMillis timestamp in epoch millis */
  public void setTimestampMillis(long timestampMillis) {
    this.timestampMillis = timestampMillis;
  }

  /** @return optional note (may be blank) */
  public String getNote() {
    return note;
  }

  /** @param note optional note */
  public void setNote(String note) {
    this.note = note;
  }

  /**
   * Returns the transaction date in the computer's local time zone.
   *
   * <p>US #9 uses this date for inclusive date-range filtering.
   */
  /** @return transaction date in the local time zone */
  public LocalDate getLocalDate() {
    return Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()).toLocalDate();
  }

  /**
   * Checks whether this transaction belongs to the requested category.
   *
   * @param requestedCategoryId category id to match, or {@code null} to accept any category
   * @return true if this expense matches the requested category filter
   */
  public boolean matchesCategory(Integer requestedCategoryId) {
    return requestedCategoryId == null || categoryId == requestedCategoryId;
  }

  /**
   * Checks whether this transaction date is inside an inclusive date range.
   *
   * @param fromDate first accepted date, or {@code null} for no lower bound
   * @param toDate last accepted date, or {@code null} for no upper bound
   * @return true if this expense falls inside the requested date range
   */
  public boolean isWithinDateRange(LocalDate fromDate, LocalDate toDate) {
    LocalDate transactionDate = getLocalDate();
    if (fromDate != null && transactionDate.isBefore(fromDate)) return false;
    if (toDate != null && transactionDate.isAfter(toDate)) return false;
    return true;
  }
}
