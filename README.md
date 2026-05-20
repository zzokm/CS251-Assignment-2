# Masroofy

**Masroofy** is a Java console budget-management application for CS251 Assignment 2. It helps you set a spending cycle, log expenses, track a safe daily limit, view category breakdowns, and keep all data stored locally in SQLite, no internet required.

## Team

| Name | ID | GitHub |
|------|-----|--------|
| Yehia Hassan | 20242447 | [@zzokm](https://github.com/zzokm) |
| Hana Khaled | 20240650 | [@hanakapony06](https://github.com/hanakapony06) |
| Jana Ahmed | 20240759 | [@Jana-Farahat](https://github.com/Jana-Farahat) |
| Noha Mohamed | 20240794 | [@nohamohamed2006](https://github.com/nohamohamed2006) |

## Features

- Initialize a budget cycle with allowance and date range
- Log, edit, delete, and filter expenses by category and date
- Dashboard with safe daily limit and spending insights by category
- Daily rollover and budget threshold warnings (80% / exhausted)
- Privacy PIN lock with lockout after failed attempts
- Optional [ntfy](https://ntfy.sh) push notifications for key events
- Full offline persistence in `data/masroofy.db`

## Tech stack

- **Language:** Java (JDK 17+)
- **UI:** Console (menus and prompts)
- **Persistence:** SQLite via JDBC (`lib/sqlite-jdbc.jar`)
- **Docs:** Javadoc (`documentation/`)

## Requirements

- Java JDK 17 or newer
- `lib/sqlite-jdbc.jar` (included in this repository)

## Build and run

From the project root:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run.ps1
```

Or use the VS Code / Cursor task **Java: Run Masroofy** (`Ctrl+Shift+B`).

On first run, the app creates `data/masroofy.db` and walks you through initial setup if no cycle exists yet.

## Project structure

```
main/masroofy/
├── Main.java          # Entry point
├── ui/                # Console screens (menu, setup, dashboard, history, settings)
├── core/              # Business logic (cycle, expenses, auth, reports, notifications)
├── model/             # Domain objects (AppState, Cycle, Expense, …)
└── storage/           # SQLite (DatabaseHelper, Paths)

data/masroofy.db       # Runtime database (created locally, gitignored)
lib/sqlite-jdbc.jar    # SQLite JDBC driver
scripts/run.ps1        # Compile and run helper
docs/                  # SRS, SDS, task plan, file map
documentation/         # Generated Javadoc HTML
```

## Persistence

All application state is stored in **`data/masroofy.db`**:

- `DatabaseHelper.loadState()` on startup
- `DatabaseHelper.saveState(state)` after every state change

The `data/` folder is gitignored so each machine keeps its own database.

## Documentation

| Resource | Location |
|----------|----------|
| Implementation plan | `docs/TASK.md` |
| File map & user-story flow | `docs/File_Map_and_Orchestration.md` |
| Work allocation | `docs/WORK_ALLOCATION.md` |
| SRS / SDS | `docs/Masroofy_SRS.md`, `docs/Masroofy_SDS_Full.md` |
| Generated API docs | `documentation/index.html` |

### Regenerate Javadoc

```powershell
javadoc -encoding UTF-8 -d documentation -sourcepath main -subpackages masroofy -classpath "lib/sqlite-jdbc.jar"
```

Use the `javadoc` executable from your JDK `bin` folder if it is not on your PATH.

## Course

CS251 — Software Engineering (Assignment 2: Budget Management System)
