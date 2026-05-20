package masroofy;

import masroofy.model.AppState;
import masroofy.storage.DatabaseHelper;
import masroofy.ui.MenuUI;
import java.sql.SQLException;

/** Application entry point. Loads persisted state and starts the console UI. */
public class Main {
  /** Utility class; no instances needed. */
  public Main() {}

  /**
   * Program entry point.
   *
   * @param args ignored
   * @throws SQLException if reading or writing the SQLite database fails
   */
  public static void main(String[] args) throws SQLException {
    AppState state = DatabaseHelper.loadState();
    DatabaseHelper.saveState(state);
    MenuUI menu = new MenuUI();
    menu.start(state);
  }
}