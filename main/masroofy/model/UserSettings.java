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

  public boolean isPrivacyLockEnabled() {
    return privacyLockEnabled;
  }

  public void setPrivacyLockEnabled(boolean privacyLockEnabled) {
    this.privacyLockEnabled = privacyLockEnabled;
  }

  public String getPinSaltBase64() {
    return pinSaltBase64;
  }

  public void setPinSaltBase64(String pinSaltBase64) {
    this.pinSaltBase64 = pinSaltBase64;
  }

  public String getPinHashHex() {
    return pinHashHex;
  }

  public void setPinHashHex(String pinHashHex) {
    this.pinHashHex = pinHashHex;
  }

  public int getFailedAttempts() {
    return failedAttempts;
  }

  public void setFailedAttempts(int failedAttempts) {
    this.failedAttempts = Math.max(0, failedAttempts);
  }

  public long getLockoutUntilMillis() {
    return lockoutUntilMillis;
  }

  public void setLockoutUntilMillis(long lockoutUntilMillis) {
    this.lockoutUntilMillis = Math.max(0L, lockoutUntilMillis);
  }

  public Map<String, Object> toJsonObject() {
    Map<String, Object> o = new LinkedHashMap<>();
    o.put("privacyLockEnabled", privacyLockEnabled);
    o.put("pinSaltBase64", pinSaltBase64);
    o.put("pinHashHex", pinHashHex);
    o.put("failedAttempts", failedAttempts);
    o.put("lockoutUntilMillis", lockoutUntilMillis);
    return o;
  }

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
    return s;
  }
}

