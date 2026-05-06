package masroofy.core;

import masroofy.model.AppState;
import masroofy.model.UserSettings;
import masroofy.storage.JsonStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Privacy lock (PIN) manager for US #12.
 *
 * <p>Lockout rule: after 3 consecutive failures, lock for 30 seconds.
 *
 * <p>PIN storage is intentionally simple: {@code pinHashHex = SHA-256(pin)}.
 *
 * <p>This is not meant to be production-grade security; it's a straightforward local lock that
 * avoids storing the PIN in plain text.
 */
public class AuthManager {
  /** Maximum consecutive failed PIN attempts before lockout. */
  public static final int MAX_FAILED_ATTEMPTS = 3;

  /** Lockout duration in milliseconds after reaching the failure limit. */
  public static final long LOCKOUT_WINDOW_MILLIS = 30_000L;

  /** Creates a new auth manager. */
  public AuthManager() {}

  /**
   * Returns whether privacy lock is enabled in settings.
   *
   * @param state application state
   * @return true if lock enabled
   */
  public boolean isLockEnabled(AppState state) {
    return state != null
        && state.getSettings() != null
        && state.getSettings().isPrivacyLockEnabled();
  }

  /**
   * Returns whether the user is currently locked out.
   *
   * @param state application state
   * @param nowMillis current time in epoch millis
   * @return true if lock is enabled and now is before lockout end
   */
  public boolean isLockedOut(AppState state, long nowMillis) {
    UserSettings s = requireSettings(state);
    return s.isPrivacyLockEnabled() && nowMillis < s.getLockoutUntilMillis();
  }

  /**
   * Returns remaining lockout time in milliseconds.
   *
   * @param state application state
   * @param nowMillis current time in epoch millis
   * @return remaining time, or 0 if not locked out
   */
  public long lockoutRemainingMillis(AppState state, long nowMillis) {
    UserSettings s = requireSettings(state);
    long remaining = s.getLockoutUntilMillis() - nowMillis;
    return Math.max(0L, remaining);
  }

  /**
   * Enables privacy lock and sets the PIN.
   *
   * <p>Stores {@code SHA-256(pin)} and resets failed attempts/lockout. Saves JSON immediately.
   *
   * @param state application state
   * @param pin PIN to set
   * @param confirmPin confirmation PIN (must match)
   */
  public void enablePin(AppState state, String pin, String confirmPin) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
    if (pin == null || pin.isBlank()) throw new IllegalArgumentException("PIN cannot be empty");
    if (!pin.equals(confirmPin)) throw new IllegalArgumentException("PIN confirmation does not match");

    String hashHex = sha256Hex(pin);

    UserSettings s = requireSettings(state);
    s.setPinSaltBase64(null);
    s.setPinHashHex(hashHex);
    s.setPrivacyLockEnabled(true);
    s.setFailedAttempts(0);
    s.setLockoutUntilMillis(0L);
    saveNow(state);
  }

  /**
   * Disables privacy lock and clears lockout counters.
   *
   * @param state application state
   */
  public void disablePin(AppState state) {
    UserSettings s = requireSettings(state);
    s.setPrivacyLockEnabled(false);
    s.setFailedAttempts(0);
    s.setLockoutUntilMillis(0L);
    saveNow(state);
  }

  /**
   * Verifies a PIN. On success, resets lockout counters. On failure, increments attempts and may
   * trigger lockout.
   *
   * <p>This method persists settings after any counter/lockout change.
   *
   * @param state application state
   * @param pin entered PIN
   * @return true if the PIN is correct (or lock is disabled), otherwise false
   */
  public boolean verifyPin(AppState state, String pin) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
    UserSettings s = requireSettings(state);
    if (!s.isPrivacyLockEnabled()) return true;

    long now = System.currentTimeMillis();
    if (now < s.getLockoutUntilMillis()) return false;

    String expected = s.getPinHashHex();
    if (expected == null) return false;

    String actual = sha256Hex(pin == null ? "" : pin);
    if (expected.equalsIgnoreCase(actual)) {
      s.setFailedAttempts(0);
      s.setLockoutUntilMillis(0L);
      saveNow(state);
      return true;
    }

    int next = s.getFailedAttempts() + 1;
    s.setFailedAttempts(next);
    if (next >= MAX_FAILED_ATTEMPTS) {
      s.setLockoutUntilMillis(now + LOCKOUT_WINDOW_MILLIS);
    }
    saveNow(state);
    return false;
  }

  private static UserSettings requireSettings(AppState state) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
    if (state.getSettings() == null) state.setSettings(new UserSettings());
    return state.getSettings();
  }

  private static String sha256Hex(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] out = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(out.length * 2);
      for (byte b : out) hex.append(String.format("%02x", b));
      return hex.toString();
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }

  private static void saveNow(AppState state) {
    try {
      JsonStore.saveState(state);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to save state", e);
    }
  }
}

