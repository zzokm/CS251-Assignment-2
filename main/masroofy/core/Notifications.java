package masroofy.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Sends push notifications using ntfy (HTTP POST).
 *
 * <p>Defaults target: {@code https://ntfy.sh/masroofyapp}.
 *
 * <p>Configuration via environment variables:
 * <ul>
 *   <li>{@code NTFY_ENABLED} (default true)</li>
 *   <li>{@code NTFY_BASE_URL} (default https://ntfy.sh)</li>
 *   <li>{@code NTFY_TOPIC} (default masroofyapp)</li>
 * </ul>
 *
 * <p>Failures should never crash the app; sending is best-effort.
 */
public final class Notifications {
  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private final HttpClient http;
  private final boolean enabled;
  private final String baseUrl;
  private final String topic;

  /** Creates a notifier using environment-variable configuration. */
  public Notifications() {
    this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), readEnabled(), readBaseUrl(), readTopic());
  }

  /**
   * Creates a notifier with explicit configuration (useful for testing).
   *
   * @param http http client
   * @param enabled whether sending is enabled
   * @param baseUrl ntfy base url (e.g. https://ntfy.sh)
   * @param topic ntfy topic (path segment)
   */
  public Notifications(HttpClient http, boolean enabled, String baseUrl, String topic) {
    if (http == null) throw new IllegalArgumentException("http cannot be null");
    if (baseUrl == null || baseUrl.isBlank()) throw new IllegalArgumentException("baseUrl cannot be blank");
    if (topic == null || topic.isBlank()) throw new IllegalArgumentException("topic cannot be blank");
    this.http = http;
    this.enabled = enabled;
    this.baseUrl = stripTrailingSlash(baseUrl.trim());
    this.topic = topic.trim();
  }

  /**
   * Sends a notification.
   *
   * @param title short title (header)
   * @param message message body
   * @param priority ntfy priority (e.g. {@code 3}..{@code 5}) or null
   * @param tagsCsv comma-separated tags (e.g. {@code warning,money}) or null
   * @return true if the POST was attempted and returned a 2xx status, otherwise false
   */
  public boolean send(String title, String message, String priority, String tagsCsv) {
    if (!enabled) return false;

    String body = (message == null) ? "" : message;
    URI uri = URI.create(baseUrl + "/" + topic);

    try {
      HttpRequest.Builder b =
          HttpRequest.newBuilder()
              .uri(uri)
              .timeout(TIMEOUT)
              .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

      if (title != null && !title.isBlank()) b.header("Title", title.trim());
      if (priority != null && !priority.isBlank()) b.header("Priority", priority.trim());
      if (tagsCsv != null && !tagsCsv.isBlank()) b.header("Tags", tagsCsv.trim());

      HttpResponse<Void> resp = http.send(b.build(), HttpResponse.BodyHandlers.discarding());
      int code = resp.statusCode();
      return code >= 200 && code < 300;
    } catch (Exception e) {
      System.err.println("ntfy send failed: " + e.getMessage());
      return false;
    }
  }

  /** Sends an 80% budget warning notification. */
  public boolean budget80(double allowance, double spent) {
    return send(
        "Budget warning",
        String.format("You reached 80%% of your budget. Spent %.2f / %.2f", spent, allowance),
        "4",
        "warning,money");
  }

  /** Sends a budget exhausted notification. */
  public boolean budgetExhausted(double allowance, double spent) {
    return send(
        "Budget exhausted",
        String.format("Budget exhausted. Spent %.2f / %.2f", spent, allowance),
        "5",
        "warning,money");
  }

  /** Sends an overspent notification. */
  public boolean overspent(double remainingBalance) {
    return send(
        "Overspent",
        String.format("Spending is above allowance. Remaining balance: %.2f", remainingBalance),
        "5",
        "warning,money");
  }

  /** Sends a PIN lockout notification. */
  public boolean pinLockout(long seconds) {
    return send(
        "PIN lockout",
        String.format("Too many failed PIN attempts. Locked for %d seconds.", Math.max(0, seconds)),
        "5",
        "warning,lock");
  }

  /** Sends a cycle reset notification. */
  public boolean cycleReset(String startDateIso, String endDateIso, double allowance) {
    return send(
        "Cycle reset",
        String.format("New cycle: %s to %s (allowance %.2f)", startDateIso, endDateIso, allowance),
        "3",
        "info,money");
  }

  /** Sends an expense added notification. */
  public boolean expenseAdded(String categoryName, double amount) {
    return send(
        "Expense added",
        String.format("Added %.2f in %s", amount, categoryName == null ? "Unknown" : categoryName),
        "3",
        "money");
  }

  private static boolean readEnabled() {
    String v = System.getenv("NTFY_ENABLED");
    if (v == null || v.isBlank()) return true;
    return v.trim().equalsIgnoreCase("true") || v.trim().equals("1") || v.trim().equalsIgnoreCase("yes");
  }

  private static String readBaseUrl() {
    String v = System.getenv("NTFY_BASE_URL");
    return (v == null || v.isBlank()) ? "https://ntfy.sh" : v.trim();
  }

  private static String readTopic() {
    String v = System.getenv("NTFY_TOPIC");
    return (v == null || v.isBlank()) ? "masroofyapp" : v.trim();
  }

  private static String stripTrailingSlash(String s) {
    if (s.endsWith("/")) return s.substring(0, s.length() - 1);
    return s;
  }
}

