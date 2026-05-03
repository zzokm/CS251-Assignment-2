package masroofy.storage;

import java.nio.file.Path;

public final class Paths {
  private Paths() {}

  /** Project runtime data directory (created automatically). */
  public static final Path DATA_DIR = Path.of("data");

  /** Main persisted state file for the whole application. */
  public static final Path APP_STATE_JSON = DATA_DIR.resolve("app-state.json");
}

