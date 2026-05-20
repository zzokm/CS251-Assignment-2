# Masroofy — CS251 Assignment 2

Java console budget app with **SQLite** persistence (`data/masroofy.db`).

## Requirements

- Java JDK 17+ (tested with recent JDK)
- `lib/sqlite-jdbc.jar` (included in this repo)

## Build and run

From the project root:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run.ps1
```

Or use the VS Code task **Java: Run Masroofy** (`Ctrl+Shift+B`).

## Persistence

- All data is stored in `data/masroofy.db` (gitignored).
- `DatabaseHelper.loadState()` on startup; `DatabaseHelper.saveState(state)` after every state change.
- No JSON persistence files are used.

## Documentation

- Team plan: `docs/TASK.md`
- File map: `docs/File_Map_and_Orchestration.md`
- Generated Javadoc: `documentation/` (regenerate with `javadoc` — see below)

### Regenerate Javadoc

```powershell
javadoc -encoding UTF-8 -d documentation -sourcepath main -subpackages masroofy -classpath "lib/sqlite-jdbc.jar"
```

Use the `javadoc` from your JDK `bin` folder if it is not on PATH.
