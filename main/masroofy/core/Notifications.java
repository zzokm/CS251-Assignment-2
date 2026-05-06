package masroofy.core;

import masroofy.model.AppState;
import masroofy.model.UserSettings;
import masroofy.storage.JsonStore;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

/**
 * Sends push notifications using ntfy (HTTP POST).
 *
 * <p>Defaults target: {@code https://ntfy.sh/masroofyXX} where {@code XX} is a 2-digit id stored in
 * settings (or derived from the machine when missing).
 *
 * <p>Configuration via environment variables:
 * <ul>
 *   <li>{@code NTFY_ENABLED} (default true)</li>
 *   <li>{@code NTFY_BASE_URL} (default https://ntfy.sh)</li>
 *   <li>{@code NTFY_TOPIC} (optional override)</li>
 * </ul>
 *
 * <p>Failures should never crash the app; sending is best-effort.
 */
public final class Notifications {
  private static final Duration TIMEOUT = Duration.ofSeconds(5);
  private static final String BASE_TOPIC = "masroofy";

  private final HttpClient http;
  private final boolean enabled;
  private final String baseUrl;
  private final String topic;

  /** Creates a notifier using environment-variable configuration. */
  public Notifications() {
    this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), readEnabled(), readBaseUrl(), readTopic());
  }

  /**
   * Creates a notifier using the per-machine topic saved in the application state.
   *
   * @param state application state
   * @return notifier targeting {@code https://ntfy.sh/masroofyXX} (by default)
   */
  public static Notifications forState(AppState state) {
    String baseUrl = readBaseUrl();
    String envTopic = readTopicOverride();
    if (envTopic != null) {
      return new Notifications(HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), readEnabled(), baseUrl, envTopic);
    }
    String suffix = ensureTopicSuffix(state);
    return new Notifications(
        HttpClient.newBuilder().connectTimeout(TIMEOUT).build(),
        readEnabled(),
        baseUrl,
        BASE_TOPIC + suffix);
  }

  /**
   * Returns the public ntfy link for this machine/topic, generating and saving the 2-digit suffix if
   * missing.
   *
   * @param state application state
   * @return public ntfy URL (e.g. {@code https://ntfy.sh/masroofy07})
   */
  public static String publicLink(AppState state) {
    String baseUrl = readBaseUrl();
    String envTopic = readTopicOverride();
    String topic = (envTopic != null) ? envTopic : (BASE_TOPIC + ensureTopicSuffix(state));
    return stripTrailingSlash(baseUrl) + "/" + topic;
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
    String start = DateFormats.formatDisplay(DateFormats.parseFlexible(startDateIso, "Start date"));
    String end = DateFormats.formatDisplay(DateFormats.parseFlexible(endDateIso, "End date"));
    return send(
        "Cycle reset",
        String.format("New cycle: %s to %s (allowance %.2f)", start, end, allowance),
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
    // Kept for backwards compatibility: if no override exists, use a stable machine-derived default.
    String override = readTopicOverride();
    return (override != null) ? override : (BASE_TOPIC + computeMachineSuffix());
  }

  private static String readTopicOverride() {
    String v = System.getenv("NTFY_TOPIC");
    if (v == null || v.isBlank()) return null;
    return v.trim();
  }

  private static String ensureTopicSuffix(AppState state) {
    if (state == null) {
      return computeMachineSuffix();
    }
    if (state.getSettings() == null) {
      state.setSettings(new UserSettings());
    }
    UserSettings s = state.getSettings();
    String current = (s.getNtfyTopicSuffix() == null) ? "" : s.getNtfyTopicSuffix().trim();
    if (isTwoDigits(current)) return current;

    String suffix = computeMachineSuffix();
    s.setNtfyTopicSuffix(suffix);
    try {
      JsonStore.saveState(state);
    } catch (Exception e) {
      // Best-effort: if saving fails, still return a stable suffix.
      System.err.println("Failed to persist ntfy topic suffix: " + e.getMessage());
    }
    return suffix;
  }

  private static boolean isTwoDigits(String s) {
    if (s == null || s.length() != 2) return false;
    return Character.isDigit(s.charAt(0)) && Character.isDigit(s.charAt(1));
  }

  private static String computeMachineSuffix() {
    // Stable per machine/user profile (simple, not security-sensitive).
    String seed =
        System.getProperty("os.name", "")
            + "|"
            + System.getProperty("os.arch", "")
            + "|"
            + System.getProperty("user.name", "")
            + "|"
            + System.getProperty("user.home", "");
    int n = Math.floorMod(sha256Int(seed), 100);
    return String.format("%02d", n);
  }

  private static int sha256Int(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] out = md.digest((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
      // Use first 4 bytes as int.
      int v = 0;
      for (int i = 0; i < 4 && i < out.length; i++) v = (v << 8) | (out[i] & 0xff);
      return v;
    } catch (Exception e) {
      return (s == null) ? 0 : s.hashCode();
    }
  }

  private static String stripTrailingSlash(String s) {
    if (s.endsWith("/")) return s.substring(0, s.length() - 1);
    return s;
  }
}

