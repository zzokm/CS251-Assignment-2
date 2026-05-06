package masroofy.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User settings persisted in {@link AppState}.
 *
 * <p>Privacy lock stores a simple hash:
 * {@code pinHashHex = SHA-256(pin)}.
 */
public class UserSettings {
  private boolean privacyLockEnabled;
  private String pinSaltBase64;
  private String pinHashHex;
  private int failedAttempts;
  private long lockoutUntilMillis;
  private String ntfyTopicSuffix;

  /** Creates default settings (privacy lock off). */
  public UserSettings() {}

  /** @return true if privacy lock is enabled */
  public boolean isPrivacyLockEnabled() {
    return privacyLockEnabled;
  }

  /** @param privacyLockEnabled true to enable privacy lock */
  public void setPrivacyLockEnabled(boolean privacyLockEnabled) {
    this.privacyLockEnabled = privacyLockEnabled;
  }

  /**
   * Returns the stored salt in base64.
   *
   * <p>This implementation does not require a salt and typically stores null.
   *
   * @return salt (nullable)
   */
  public String getPinSaltBase64() {
    return pinSaltBase64;
  }

  /** @param pinSaltBase64 stored salt (nullable) */
  public void setPinSaltBase64(String pinSaltBase64) {
    this.pinSaltBase64 = pinSaltBase64;
  }

  /** @return stored PIN hash hex (nullable if not set) */
  public String getPinHashHex() {
    return pinHashHex;
  }

  /** @param pinHashHex stored PIN hash hex */
  public void setPinHashHex(String pinHashHex) {
    this.pinHashHex = pinHashHex;
  }

  /** @return number of consecutive failed attempts */
  public int getFailedAttempts() {
    return failedAttempts;
  }

  /** @param failedAttempts number of consecutive failed attempts */
  public void setFailedAttempts(int failedAttempts) {
    this.failedAttempts = Math.max(0, failedAttempts);
  }

  /** @return epoch millis until which PIN entry is locked */
  public long getLockoutUntilMillis() {
    return lockoutUntilMillis;
  }

  /** @param lockoutUntilMillis epoch millis until which PIN entry is locked */
  public void setLockoutUntilMillis(long lockoutUntilMillis) {
    this.lockoutUntilMillis = Math.max(0L, lockoutUntilMillis);
  }

  /**
   * Returns the saved 2-digit notification topic suffix (e.g., {@code 07}).
   *
   * @return suffix (nullable if not set yet)
   */
  public String getNtfyTopicSuffix() {
    return ntfyTopicSuffix;
  }

  /**
   * Sets the saved 2-digit notification topic suffix.
   *
   * @param ntfyTopicSuffix suffix (e.g., {@code 07})
   */
  public void setNtfyTopicSuffix(String ntfyTopicSuffix) {
    this.ntfyTopicSuffix = ntfyTopicSuffix;
  }

  /**
   * Converts settings to a JSON-friendly object.
   *
   * @return map containing settings fields
   */
  public Map<String, Object> toJsonObject() {
    Map<String, Object> o = new LinkedHashMap<>();
    o.put("privacyLockEnabled", privacyLockEnabled);
    o.put("pinSaltBase64", pinSaltBase64);
    o.put("pinHashHex", pinHashHex);
    o.put("failedAttempts", failedAttempts);
    o.put("lockoutUntilMillis", lockoutUntilMillis);
    o.put("ntfyTopicSuffix", ntfyTopicSuffix);
    return o;
  }

  /**
   * Builds settings from a JSON-friendly object.
   *
   * @param o parsed JSON object map
   * @return settings
   */
  public static UserSettings fromJsonObject(Map<String, Object> o) {
    UserSettings s = new UserSettings();
    if (o == null) return s;
    Object en = o.get("privacyLockEnabled");
    if (en instanceof Boolean) s.setPrivacyLockEnabled((Boolean) en);
    Object salt = o.get("pinSaltBase64");
    if (salt instanceof String) s.setPinSaltBase64((String) salt);
    Object hash = o.get("pinHashHex");
    if (hash instanceof String) s.setPinHashHex((String) hash);
    Object fa = o.get("failedAttempts");
    if (fa instanceof Number) s.setFailedAttempts(((Number) fa).intValue());
    Object lo = o.get("lockoutUntilMillis");
    if (lo instanceof Number) s.setLockoutUntilMillis(((Number) lo).longValue());
    Object suf = o.get("ntfyTopicSuffix");
    if (suf instanceof String) s.setNtfyTopicSuffix((String) suf);
    return s;
  }
}

