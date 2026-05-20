package masroofy.storage;

import java.nio.file.Path;

/** Central file-system paths used by the application. */
public final class Paths {
  private Paths() {}

  /** Project runtime data directory (created automatically). */
  public static final Path DATA_DIR = Path.of("data");

  /** SQLite database file for all persisted application state. */
  public static final Path APP_DB = DATA_DIR.resolve("masroofy.db");
}
