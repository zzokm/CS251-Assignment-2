package masroofy.ui;

import masroofy.core.AuthManager;
import masroofy.core.CycleManager;
import masroofy.model.AppState;
import java.util.Scanner;

/** Console settings screen (privacy lock + reset cycle). */
public class SettingsUI {
  private final AuthManager authManager;
  private final CycleManager cycleManager;

  /** Creates settings UI with default managers. */
  public SettingsUI() {
    this(new AuthManager(), new CycleManager());
  }

  /**
   * Creates settings UI with injected managers.
   *
   * @param authManager privacy lock manager
   * @param cycleManager cycle manager
   */
  public SettingsUI(AuthManager authManager, CycleManager cycleManager) {
    if (authManager == null) throw new IllegalArgumentException("authManager cannot be null");
    if (cycleManager == null) throw new IllegalArgumentException("cycleManager cannot be null");
    this.authManager = authManager;
    this.cycleManager = cycleManager;
  }

  /**
   * Shows the settings menu.
   *
   * @param state application state
   * @param scanner console scanner
   */
  public void show(AppState state, Scanner scanner) {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
    if (scanner == null) throw new IllegalArgumentException("scanner cannot be null");

    boolean running = true;
    while (running) {
      System.out.println();
      System.out.println("Settings");
      System.out.println("--------");
      System.out.println("Privacy lock: " + (authManager.isLockEnabled(state) ? "ON" : "OFF"));
      System.out.println("1. Enable / set PIN");
      System.out.println("2. Disable lock");
      System.out.println("3. Reset cycle");
      System.out.println("0. Back");
      System.out.print("Choose: ");

      String choice = scanner.nextLine().trim();
      switch (choice) {
        case "1":
          enableLock(state, scanner);
          break;
        case "2":
          disableLock(state);
          break;
        case "3":
          resetCycle(state, scanner);
          break;
        case "0":
          running = false;
          break;
        default:
          System.out.println("Unknown option.");
      }
    }
  }

  private void enableLock(AppState state, Scanner scanner) {
    System.out.print("Enter new PIN: ");
    String pin = scanner.nextLine();
    System.out.print("Confirm PIN: ");
    String confirm = scanner.nextLine();
    try {
      authManager.enablePin(state, pin, confirm);
      System.out.println("Privacy lock enabled.");
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }
  }

  private void disableLock(AppState state) {
    authManager.disablePin(state);
    System.out.println("Privacy lock disabled.");
  }

  private void resetCycle(AppState state, Scanner scanner) {
    System.out.print("Confirm reset cycle? (y/n): ");
    String confirm = scanner.nextLine().trim().toLowerCase();
    if (!confirm.equals("y")) {
      System.out.println("Cancelled.");
      return;
    }
    cycleManager.resetCycle(state);
    System.out.println("Cycle reset.");
  }
}

