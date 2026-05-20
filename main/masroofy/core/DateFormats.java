package masroofy.core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Date parsing/formatting utilities for console input/output. */
public final class DateFormats {
  private DateFormats() {}

  /** Display format used in UI: {@code DD MM YYYY}. */
  public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd MM uuuu");

  /** Returns date formatted as {@code DD MM YYYY}. */
  public static String formatDisplay(LocalDate d) {
    if (d == null) throw new IllegalArgumentException("date cannot be null");
    return d.format(DISPLAY_DATE);
  }

  /**
   * Parses a date string, accepting:
   * <ul>
   *   <li>{@code DD MM YYYY} (preferred)</li>
   *   <li>{@code DD-MM-YYYY}</li>
   *   <li>{@code DD/MM/YYYY}</li>
   *   <li>Legacy persisted format {@code YYYY-MM-DD}</li>
   * </ul>
   *
   * @param raw date input
   * @param label label for error messages
   * @return parsed LocalDate
   */
  public static LocalDate parseFlexible(String raw, String label) {
    if (raw == null || raw.trim().isEmpty()) {
      throw new IllegalArgumentException(label + " cannot be empty.");
    }

    String s = raw.trim();

    // Backward compatibility: persisted cycle dates use ISO yyyy-MM-dd.
    if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
      return LocalDate.parse(s);
    }

    // Accept DD MM YYYY with space, dash or slash separators.
    s = s.replace('-', ' ').replace('/', ' ');
    s = s.replaceAll("\\s+", " ");
    String[] parts = s.split(" ");
    if (parts.length != 3) {
      throw new IllegalArgumentException(label + " must use DD MM YYYY format.");
    }

    try {
      int dd = Integer.parseInt(parts[0]);
      int mm = Integer.parseInt(parts[1]);
      int yyyy = Integer.parseInt(parts[2]);
      return LocalDate.of(yyyy, mm, dd);
    } catch (Exception e) {
      throw new IllegalArgumentException(label + " must be a valid date in DD MM YYYY format.", e);
    }
  }
}

