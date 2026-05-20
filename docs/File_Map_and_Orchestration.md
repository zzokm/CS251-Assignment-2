# File Map + Orchestration (US #1–#12) — Simple Version

This file tells you **where code goes** (model/core/ui/storage) and **how each user story is implemented** (what UI calls what core, what state changes, and when we save to SQLite).

## Folder/package meaning (no extra theory)

- **`main/masroofy/model/`**: classes that represent the saved data (loaded into `AppState` from SQLite)
- **`main/masroofy/storage/`**: reading/writing SQLite (`data/masroofy.db`)
- **`main/masroofy/core/`**: business logic (validations + calculations + editing state + saving)
- **`main/masroofy/ui/`**: console menus and input/output (calls core methods)
- **`main/masroofy/Main.java`**: program entry

## Persistence (SQLite) rule (important)

- On startup:
  - load `AppState` from `data/masroofy.db` via `DatabaseHelper.loadState()`
- When something changes:
  - update `AppState`
  - **save immediately** via `DatabaseHelper.saveState(state)`

Example save call:

- `DatabaseHelper.saveState(state);`

## Data handling (who reads/writes what, and what gets saved)

### Where data lives

- **In memory**: inside one object, `AppState state`
- **On disk**: `data/masroofy.db` (SQLite)

### What is saved (minimum)

Everything in `AppState` is saved. That means:

- `state.activeCycle`
- `state.expenses`
- `state.categories`
- `state.settings`
- `state.nextExpenseId`

### What each layer does with data

#### UI (`main/masroofy/ui/*`)

- **Reads**: user input (strings/numbers) from console
- **Reads**: `state` values to display them (example: list expenses)
- **Writes**: nothing directly in the database
- **May write**: small direct changes to `state` only if you want, but better: UI calls core and core mutates state

Example:

- UI reads “expenseId=12, newAmount=60”
- UI calls `ExpenseManager.editExpense(state, 12, 60, categoryId, note)`

#### Core (`main/masroofy/core/*`)

- **Reads**: `state` to make decisions and calculations
- **Writes**: updates `state` (this is where state changes happen)
- **Saves**: calls `DatabaseHelper.saveState(state)` after any state change

Examples:

- `ExpenseManager.deleteExpense(...)`:
  - removes one element from `state.expenses`
  - saves to SQLite
- `AuthManager.verifyPin(...)`:
  - increments `state.settings.failedAttempts` on failure
  - sets `lockoutUntilMillis` when needed
  - saves to SQLite
- `ReportManager.getSpendingInsights(...)`:
  - only reads state and returns calculated values
  - **does not save**

#### Model (`main/masroofy/model/*`)

- **Purpose**: defines what the saved data looks like in memory
- **Reads/writes**: fields inside objects (`Expense.amount`, `UserSettings.failedAttempts`, etc.)

#### Storage (`main/masroofy/storage/*`)

- **Reads**: `data/masroofy.db` via JDBC
- **Writes**: `data/masroofy.db` in a transaction
- **Classes**:
  - `Paths` — `DATA_DIR`, `APP_DB`
  - `DatabaseHelper` — `loadState()`, `saveState()`, schema creation

### What triggers a save (simple rule list)

Save after any operation that changes `AppState`, for example:

- **Expenses**:
  - add expense (US #2)
  - edit expense (US #8)
  - delete expense (US #8)
  - reset cycle clears expenses (US #11)
- **Settings / Auth**:
  - enable lock / set PIN (US #12)
  - disable lock (US #12)
  - failed PIN attempt counter changes (US #12)
  - lockout time changes (US #12)
- **Cycle**:
  - initialize cycle (US #1)
  - reset cycle (US #11)
  - rollover updates lastCalculatedDate (US #5) if you store it
- **Threshold flags**:
  - “80% alert shown” flag (US #6)

## Files (what each file is for)

### Entry point

- **`main/masroofy/Main.java`**
  - Starts the program.
  - Loads state from SQLite, seeds on first run, starts `MenuUI`.

### Storage

- **`main/masroofy/storage/Paths.java`**
  - Path constants: `data/`, `data/masroofy.db`

- **`main/masroofy/storage/DatabaseHelper.java`**
  - `loadState()` → returns `AppState`
  - `saveState(AppState)` → persists full state in a transaction

### Model (data in AppState)

- **`main/masroofy/model/AppState.java`** — root in-memory state
- **`main/masroofy/model/Expense.java`** — one transaction
- **`main/masroofy/model/Category.java`** — one category
- **`main/masroofy/model/UserSettings.java`** — privacy lock settings
- **`main/masroofy/model/Cycle.java`** — cycle info and alert flags

### Core (business logic)

- **`main/masroofy/core/ExpenseManager.java`** — add/edit/delete; saves after mutation
- **`main/masroofy/core/ReportManager.java`** — read-only dashboard calculations
- **`main/masroofy/core/AuthManager.java`** — PIN lock; saves after settings change
- **`main/masroofy/core/CycleManager.java`** — cycle, rollover, thresholds; saves after mutation

### UI (console)

UI should: read input, call core methods, print output.

UI should NOT: calculate business rules or write to the database directly.

## Orchestration pattern (how calls flow)

For any story, the flow is:

1. UI reads input
2. UI calls core method
3. Core validates + updates `AppState`
4. Core saves to SQLite immediately
5. UI prints result

## User stories (US #1–#12) — save triggers

| Story | Save after change? |
|-------|-------------------|
| US #1 Initialize cycle | Yes |
| US #2 Add expense | Yes |
| US #3 Daily limit view | No (read-only) |
| US #4 Spending insights | No |
| US #5 Rollover | Yes (if date advances) |
| US #6 Threshold alert | Yes (when flag set) |
| US #7 History list | No |
| US #8 Edit/delete | Yes |
| US #9 Filter history | No |
| US #10 Offline persistence | Yes (rule across app) |
| US #11 Reset cycle | Yes |
| US #12 Privacy lock | Yes |
