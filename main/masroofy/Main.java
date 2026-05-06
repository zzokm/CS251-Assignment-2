package masroofy;

import masroofy.model.AppState;
import masroofy.storage.JsonStore;
import masroofy.ui.MenuUI;
import java.io.IOException;

/** Application entry point. Loads persisted state and starts the console UI. */
public class Main {
  /** Utility class; no instances needed. */
  public Main() {}

  /**
   * Program entry point.
   *
   * @param args ignored
   * @throws IOException if reading or writing the persisted JSON fails
   */
  public static void main(String[] args) throws IOException {
    AppState state = JsonStore.loadState();
    JsonStore.saveState(state);
    MenuUI menu = new MenuUI();
    menu.start(state);
  }
}