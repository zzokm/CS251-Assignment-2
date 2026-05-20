package masroofy.storage;

import masroofy.model.AppState;
import masroofy.model.Category;
import masroofy.model.Cycle;
import masroofy.model.Expense;
import masroofy.model.UserSettings;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves application state in {@link Paths#APP_DB} using SQLite.
 *
 * <p>Any state-changing operation should call {@link #saveState(AppState)} immediately so the app
 * restarts correctly.
 */
public final class DatabaseHelper {
  private static final String JDBC_URL = "jdbc:sqlite:" + Paths.APP_DB;

  private DatabaseHelper() {}

  static {
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      throw new ExceptionInInitializerError(
          new IllegalStateException(
              "SQLite JDBC driver not found. Add lib/sqlite-jdbc.jar to the classpath.", e));
    }
  }

  /**
   * Opens a connection to the SQLite database, creating schema if needed.
   *
   * @return open JDBC connection
   * @throws SQLException if connecting fails
   */
  public static Connection openConnection() throws SQLException {
    try {
      Files.createDirectories(Paths.DATA_DIR);
    } catch (IOException e) {
      throw new SQLException("Could not create data directory", e);
    }
    Connection conn = DriverManager.getConnection(JDBC_URL);
    conn.setAutoCommit(false);
    ensureSchema(conn);
    conn.commit();
    conn.setAutoCommit(true);
    return conn;
  }

  /**
   * Loads application state from the database.
   *
   * <p>If the database is empty or missing tables, returns a new state with seeded categories.
   *
   * @return loaded application state
   * @throws SQLException if loading fails
   */
  public static AppState loadState() throws SQLException {
    try (Connection conn = openConnection()) {
      AppState state = new AppState();
      loadMeta(conn, state);
      state.setCategories(loadCategories(conn));
      state.setExpenses(loadExpenses(conn));
      state.setSettings(loadSettings(conn));
      state.setActiveCycle(loadActiveCycle(conn));
      state.ensureSeedCategories();
      return state;
    }
  }

  /**
   * Persists the full application state in a single transaction.
   *
   * @param state state to persist
   * @throws SQLException if saving fails
   */
  public static void saveState(AppState state) throws SQLException {
    if (state == null) throw new IllegalArgumentException("state cannot be null");

    try (Connection conn = openConnection()) {
      conn.setAutoCommit(false);
      try {
        saveMeta(conn, state);
        saveCategories(conn, state.getCategories());
        saveExpenses(conn, state.getExpenses());
        saveSettings(conn, state.getSettings());
        saveActiveCycle(conn, state.getActiveCycle());
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    }
  }

  private static void ensureSchema(Connection conn) throws SQLException {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate(
          """
          CREATE TABLE IF NOT EXISTS app_meta (
            key TEXT PRIMARY KEY NOT NULL,
            value TEXT NOT NULL
          )
          """);
      st.executeUpdate(
          """
          CREATE TABLE IF NOT EXISTS cycles (
            id INTEGER PRIMARY KEY,
            total_allowance REAL NOT NULL,
            start_date TEXT,
            end_date TEXT,
            last_calculated_date TEXT,
            alert80_shown INTEGER NOT NULL DEFAULT 0,
            alert100_shown INTEGER NOT NULL DEFAULT 0,
            overspent_shown INTEGER NOT NULL DEFAULT 0,
            is_active INTEGER NOT NULL DEFAULT 0
          )
          """);
      st.executeUpdate(
          """
          CREATE TABLE IF NOT EXISTS categories (
            id INTEGER PRIMARY KEY,
            name TEXT NOT NULL
          )
          """);
      st.executeUpdate(
          """
          CREATE TABLE IF NOT EXISTS expenses (
            id INTEGER PRIMARY KEY,
            category_id INTEGER NOT NULL,
            amount REAL NOT NULL,
            timestamp_millis INTEGER NOT NULL,
            note TEXT
          )
          """);
      st.executeUpdate(
          """
          CREATE TABLE IF NOT EXISTS user_settings (
            id INTEGER PRIMARY KEY CHECK (id = 1),
            privacy_lock_enabled INTEGER NOT NULL DEFAULT 0,
            pin_salt_base64 TEXT,
            pin_hash_hex TEXT,
            failed_attempts INTEGER NOT NULL DEFAULT 0,
            lockout_until_millis INTEGER NOT NULL DEFAULT 0,
            ntfy_topic_suffix TEXT
          )
          """);
    }
  }

  private static void loadMeta(Connection conn, AppState state) throws SQLException {
    try (PreparedStatement ps =
        conn.prepareStatement("SELECT value FROM app_meta WHERE key = 'next_expense_id'")) {
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          state.setNextExpenseId(Long.parseLong(rs.getString("value")));
        }
      }
    }
  }

  private static List<Category> loadCategories(Connection conn) throws SQLException {
    List<Category> list = new ArrayList<>();
    try (Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT id, name FROM categories ORDER BY id")) {
      while (rs.next()) {
        list.add(new Category(rs.getInt("id"), rs.getString("name")));
      }
    }
    return list;
  }

  private static List<Expense> loadExpenses(Connection conn) throws SQLException {
    List<Expense> list = new ArrayList<>();
    try (Statement st = conn.createStatement();
        ResultSet rs =
            st.executeQuery(
                "SELECT id, category_id, amount, timestamp_millis, note FROM expenses ORDER BY id")) {
      while (rs.next()) {
        list.add(
            new Expense(
                rs.getLong("id"),
                rs.getInt("category_id"),
                rs.getDouble("amount"),
                rs.getLong("timestamp_millis"),
                rs.getString("note")));
      }
    }
    return list;
  }

  private static UserSettings loadSettings(Connection conn) throws SQLException {
    UserSettings settings = new UserSettings();
    try (Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM user_settings WHERE id = 1")) {
      if (!rs.next()) return settings;
      settings.setPrivacyLockEnabled(rs.getInt("privacy_lock_enabled") != 0);
      settings.setPinSaltBase64(rs.getString("pin_salt_base64"));
      settings.setPinHashHex(rs.getString("pin_hash_hex"));
      settings.setFailedAttempts(rs.getInt("failed_attempts"));
      settings.setLockoutUntilMillis(rs.getLong("lockout_until_millis"));
      settings.setNtfyTopicSuffix(rs.getString("ntfy_topic_suffix"));
    }
    return settings;
  }

  private static Cycle loadActiveCycle(Connection conn) throws SQLException {
    try (PreparedStatement ps =
        conn.prepareStatement("SELECT * FROM cycles WHERE is_active = 1 LIMIT 1")) {
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return rowToCycle(rs);
      }
    }
  }

  private static Cycle rowToCycle(ResultSet rs) throws SQLException {
    Cycle c = new Cycle();
    c.setId(rs.getLong("id"));
    c.setTotalAllowance(rs.getDouble("total_allowance"));
    c.setStartDate(rs.getString("start_date"));
    c.setEndDate(rs.getString("end_date"));
    c.setLastCalculatedDate(rs.getString("last_calculated_date"));
    c.setAlert80Shown(rs.getInt("alert80_shown") != 0);
    c.setAlert100Shown(rs.getInt("alert100_shown") != 0);
    c.setOverspentShown(rs.getInt("overspent_shown") != 0);
    return c;
  }

  private static void saveMeta(Connection conn, AppState state) throws SQLException {
    try (PreparedStatement ps =
        conn.prepareStatement(
            """
            INSERT INTO app_meta (key, value) VALUES ('next_expense_id', ?)
            ON CONFLICT(key) DO UPDATE SET value = excluded.value
            """)) {
      ps.setString(1, Long.toString(state.getNextExpenseId()));
      ps.executeUpdate();
    }
  }

  private static void saveCategories(Connection conn, List<Category> categories) throws SQLException {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate("DELETE FROM categories");
    }
    if (categories == null || categories.isEmpty()) return;

    try (PreparedStatement ps =
        conn.prepareStatement("INSERT INTO categories (id, name) VALUES (?, ?)")) {
      for (Category c : categories) {
        ps.setInt(1, c.getId());
        ps.setString(2, c.getName());
        ps.addBatch();
      }
      ps.executeBatch();
    }
  }

  private static void saveExpenses(Connection conn, List<Expense> expenses) throws SQLException {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate("DELETE FROM expenses");
    }
    if (expenses == null || expenses.isEmpty()) return;

    try (PreparedStatement ps =
        conn.prepareStatement(
            """
            INSERT INTO expenses (id, category_id, amount, timestamp_millis, note)
            VALUES (?, ?, ?, ?, ?)
            """)) {
      for (Expense e : expenses) {
        ps.setLong(1, e.getId());
        ps.setInt(2, e.getCategoryId());
        ps.setDouble(3, e.getAmount());
        ps.setLong(4, e.getTimestampMillis());
        ps.setString(5, e.getNote());
        ps.addBatch();
      }
      ps.executeBatch();
    }
  }

  private static void saveSettings(Connection conn, UserSettings settings) throws SQLException {
    if (settings == null) settings = new UserSettings();

    try (Statement st = conn.createStatement()) {
      st.executeUpdate("DELETE FROM user_settings");
    }

    try (PreparedStatement ps =
        conn.prepareStatement(
            """
            INSERT INTO user_settings (
              id, privacy_lock_enabled, pin_salt_base64, pin_hash_hex,
              failed_attempts, lockout_until_millis, ntfy_topic_suffix
            ) VALUES (1, ?, ?, ?, ?, ?, ?)
            """)) {
      ps.setInt(1, settings.isPrivacyLockEnabled() ? 1 : 0);
      ps.setString(2, settings.getPinSaltBase64());
      ps.setString(3, settings.getPinHashHex());
      ps.setInt(4, settings.getFailedAttempts());
      ps.setLong(5, settings.getLockoutUntilMillis());
      ps.setString(6, settings.getNtfyTopicSuffix());
      ps.executeUpdate();
    }
  }

  private static void saveActiveCycle(Connection conn, Cycle active) throws SQLException {
    try (Statement st = conn.createStatement()) {
      st.executeUpdate("DELETE FROM cycles");
    }
    if (active == null) return;

    try (PreparedStatement ps =
        conn.prepareStatement(
            """
            INSERT INTO cycles (
              id, total_allowance, start_date, end_date, last_calculated_date,
              alert80_shown, alert100_shown, overspent_shown, is_active
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)
            """)) {
      ps.setLong(1, active.getId());
      ps.setDouble(2, active.getTotalAllowance());
      ps.setString(3, active.getStartDate());
      ps.setString(4, active.getEndDate());
      ps.setString(5, active.getLastCalculatedDate());
      ps.setInt(6, active.isAlert80Shown() ? 1 : 0);
      ps.setInt(7, active.isAlert100Shown() ? 1 : 0);
      ps.setInt(8, active.isOverspentShown() ? 1 : 0);
      ps.executeUpdate();
    }
  }
}
