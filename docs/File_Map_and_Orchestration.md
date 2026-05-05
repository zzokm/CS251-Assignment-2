# File Map + Orchestration (US #1–#12) — Simple Version

This file tells you **where code goes** (model/core/ui/storage) and **how each user story is implemented** (what UI calls what core, what state changes, and when we save JSON).

## Folder/package meaning (no extra theory)

- **`main/masroofy/model/`**: classes that represent the saved data (what goes inside `data/app-state.json`)
- **`main/masroofy/storage/`**: reading/writing JSON (`data/app-state.json`)
- **`main/masroofy/core/`**: business logic (validations + calculations + editing state + saving)
- **`main/masroofy/ui/`**: console menus and input/output (calls core methods)
- **`main/masroofy/Main.java`**: program entry

## Persistence (JSON) rule (important)

- On startup:
  - load `AppState` from `data/app-state.json`
- When something changes:
  - update `AppState`
  - **save immediately** to `data/app-state.json`

Example save call:

- `JsonStore.saveState(state);`

## Data handling (who reads/writes what, and what gets saved)

### Where data lives

- **In memory**: inside one object, `AppState state`
- **On disk**: `data/app-state.json`

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
- **Writes**: nothing directly in JSON and should not modify JSON files
- **May write**: small direct changes to `state` only if you want, but better: UI calls core and core mutates state

Example:

- UI reads “expenseId=12, newAmount=60”
- UI calls `ExpenseManager.editExpense(state, 12, 60, categoryId, note)`

#### Core (`main/masroofy/core/*`)

- **Reads**: `state` to make decisions and calculations
- **Writes**: updates `state` (this is where state changes happen)
- **Saves**: calls `JsonStore.saveState(state)` after any state change

Examples:

- `ExpenseManager.deleteExpense(...)`:
  - removes one element from `state.expenses`
  - saves JSON
- `AuthManager.verifyPin(...)`:
  - increments `state.settings.failedAttempts` on failure
  - sets `lockoutUntilMillis` when needed
  - saves JSON
- `ReportManager.getSpendingInsights(...)`:
  - only reads state and returns calculated values
  - **does not save**

#### Model (`main/masroofy/model/*`)

- **Purpose**: defines what the saved data looks like
- **Reads/writes**: fields inside objects (`Expense.amount`, `UserSettings.failedAttempts`, etc.)
- **Converts**:
  - `toJsonObject()` → turn objects into `Map/List` JSON-friendly structures
  - `fromJsonObject(...)` → load objects from `Map/List`

#### Storage (`main/masroofy/storage/*`)

- **Reads**: file `data/app-state.json`
- **Writes**: file `data/app-state.json`
- **How it saves**:
  - write to a temp file
  - atomic replace (so file corruption risk is lower)

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

### Example JSON shape (simplified)

This is what `data/app-state.json` roughly looks like:

```json
{
  "nextExpenseId": 3,
  "activeCycle": { "id": 1, "totalAllowance": 2000.0 },
  "expenses": [
    { "id": 1, "categoryId": 1, "amount": 50.0, "timestampMillis": 1760000000000, "note": "lunch" },
    { "id": 2, "categoryId": 2, "amount": 20.0, "timestampMillis": 1760000500000, "note": "" }
  ],
  "categories": [
    { "id": 1, "name": "Food" },
    { "id": 2, "name": "Transport" }
  ],
  "settings": {
    "privacyLockEnabled": true,
    "pinSaltBase64": null,
    "pinHashHex": "a4c3... (sha256 hex)",
    "failedAttempts": 1,
    "lockoutUntilMillis": 0
  }
}
```

## Files (what each file is for)

### Entry point

- **`main/masroofy/Main.java`**
  - Starts the program.
  - Usually creates `new MenuUI()` (or another UI runner).

### Storage

- **`main/masroofy/storage/Paths.java`**
  - Has the path constants:
    - `data/`
    - `data/app-state.json`

- **`main/masroofy/storage/JsonStore.java`**
  - Reads and writes JSON:
    - `loadState()` → returns `AppState`
    - `saveState(AppState)` → saves state (atomic save)

### Model (data stored in JSON)

- **`main/masroofy/model/AppState.java`**
  - The root object we save/load.
  - Contains:
    - `activeCycle`
    - `expenses`
    - `categories`
    - `settings`
    - `nextExpenseId`
  - Has:
    - `toJsonObject()`
    - `fromJsonObject(...)`
  - Seeds default categories on first run.

- **`main/masroofy/model/Expense.java`**
  - One transaction:
    - `id`, `amount`, `categoryId`, `timestampMillis`, `note`

- **`main/masroofy/model/Category.java`**
  - One category:
    - `id`, `name`

- **`main/masroofy/model/UserSettings.java`**
  - Settings data (used by privacy lock):
    - `privacyLockEnabled`
    - `pinHashHex` (simple hash)
    - `failedAttempts`
    - `lockoutUntilMillis`

- **`main/masroofy/model/Cycle.java`**
  - Cycle info (team may extend with start/end dates):
    - minimal fields needed for calculations

### Core (business logic)

- **`main/masroofy/core/ExpenseManager.java`**
  - Add/edit/delete expenses, validations, filters, history ordering.
  - Must call `JsonStore.saveState(state)` after any mutation.

- **`main/masroofy/core/ReportManager.java`**
  - Read-only calculations for dashboard:
    - category totals + percentages (US #4)

- **`main/masroofy/core/AuthManager.java`**
  - Privacy lock (US #12):
    - enable/disable PIN
    - verify PIN
    - lockout after 3 failed attempts
  - Must call `JsonStore.saveState(state)` after any settings change.

- **`main/masroofy/core/CycleManager.java`**
  - Cycle initialization, reset, daily limit, rollover, threshold warnings.

### UI (console)

UI should:
- read input
- call core methods
- print output

UI should NOT:
- calculate business rules
- modify JSON directly

Main UI files:
- `MenuUI` (routes to other screens)
- `SetupUI` (cycle setup)
- `DashboardUI` (prints dashboard + insights)
- `HistoryUI` (lists expenses + edit/delete/filter)
- `SettingsUI` (privacy lock + reset cycle)

## Orchestration pattern (how calls flow)

For any story, the flow is:

1. UI reads input
2. UI calls core method
3. Core validates + updates `AppState`
4. Core saves JSON immediately
5. UI prints result

## User stories (US #1–#12) — what to implement, where, with examples

Below: each user story has:
- **UI screen**
- **Core method(s)**
- **State fields touched**
- **Example flow**

### US #1 — Set Initial Budget Cycle

- **UI**: `SetupUI`
- **Core**: `CycleManager.initializeCycle(...)`
- **State**:
  - `state.activeCycle = new Cycle(...)`
  - maybe set cycle dates/amount
- **Save**: yes (new cycle)
- **Example**:
  - User enters allowance + start date + end date
  - `CycleManager` validates values
  - sets `activeCycle`
  - `JsonStore.saveState(state)`

### US #2 — Rapid Expense Logging

- **UI**: a quick-add option in `HistoryUI` or a separate small screen
- **Core**: `ExpenseManager.logExpense(amount, categoryId, note, timestamp)`
- **State**:
  - add to `state.expenses`
  - increment `state.nextExpenseId`
- **Save**: yes (new expense)
- **Example**:
  - User enters amount=50, category=Food, note="lunch"
  - create `Expense(id, 1, 50, now, "lunch")`
  - add to list
  - save JSON

### US #3 — Dynamic Daily Limit View

- **UI**: `DashboardUI`
- **Core**: `CycleManager.calculateSafeDailyLimit(state)`
- **State**: usually read-only (may store lastCalculatedDate if team chooses)
- **Save**: only if you store derived values/flags
- **Example**:
  - dashboard prints:
    - remaining balance
    - remaining days
    - safe daily limit

### US #4 — Visual Spending Insights

- **UI**: `DashboardUI`
- **Core**: `ReportManager.getSpendingInsights(state)`
- **State**: read-only (uses `state.expenses` + `state.categories`)
- **Save**: no
- **Example output**:
  - Food: 200.00 (40.0%) ||||||||------------
  - Transport: 100.00 (20.0%) ||||----------------

### US #5 — Daily Rollover Management

- **UI trigger**: on app start (in `MenuUI`) or when opening `DashboardUI`
- **Core**: `CycleManager.handleRolloverIfNeeded(state, today)`
- **State**:
  - store a `lastCalculatedDate` somewhere (in `AppState` or `Cycle`)
- **Save**: yes if lastCalculatedDate changes
- **Example**:
  - if today > lastCalculatedDate:
    - recompute daily limit
    - save updated date

### US #6 — Budget Threshold Notification (80%)

- **UI**: print warning on Dashboard or right after adding/editing/deleting an expense
- **Core**: `CycleManager.checkThreshold(state)` (or helper)
- **State**:
  - store flag "80% alert shown" in state (AppState or Cycle)
- **Save**: yes when you set the flag
- **Example**:
  - when spent/allowance >= 0.80 and flag is false:
    - print warning
    - set flag true
    - save

### US #7 — Transaction History Review

- **UI**: `HistoryUI`
- **Core**: `ExpenseManager.getHistory(state)` (sorted newest-first)
- **State**: read-only
- **Save**: no
- **Example output**:
  - `#12 | 50.00 | Food | 2026-05-05 17:30 | lunch`

### US #8 — Edit or Delete Transaction

- **UI**: `HistoryUI`
- **Core**:
  - `ExpenseManager.editExpense(state, id, newAmount, newCategoryId, newNote)`
  - `ExpenseManager.deleteExpense(state, id)`
- **State**:
  - update/remove items from `state.expenses`
- **Save**: yes (after edit/delete)
- **Example**:
  - Edit #12 amount from 50 → 60:
    - validate amount > 0
    - validate category exists
    - update expense fields
    - save JSON
  - Delete #12:
    - confirm
    - remove from list
    - save JSON

### US #9 — Filter Transaction History

- **UI**: `HistoryUI` filter option
- **Core**: `ExpenseManager.filterHistory(state, categoryId?, from?, to?)`
- **State**: read-only
- **Save**: no
- **Example**:
  - filter category Food only
  - or filter between two dates (inclusive)

### US #10 — Offline Local Data Persistence

- **UI**: startup load in `MenuUI`, and saving after any action
- **Core**: none specifically; this is a rule across all managers
- **State**: the whole `AppState`
- **Save**: always after change
- **Example**:
  - add expense → save → restart app → expense still listed

### US #11 — Cycle Reset and Data Clearance

- **UI**: `SettingsUI`
- **Core**: `CycleManager.resetCycle(state)`
- **State**:
  - clear `activeCycle`
  - clear `expenses`
  - reset per-cycle flags (like 80% alert shown)
- **Save**: yes
- **Example**:
  - Settings → Reset cycle → confirm → state cleared → save JSON

### US #12 — Local Privacy Lock

- **UI**:
  - `SettingsUI` (enable/disable)
  - `MenuUI` (startup PIN gate)
- **Core**:
  - `AuthManager.enablePin(state, pin, confirm)`
  - `AuthManager.disablePin(state)`
  - `AuthManager.verifyPin(state, pin)`
- **State**:
  - `state.settings.privacyLockEnabled`
  - `state.settings.pinHashHex`
  - `state.settings.failedAttempts`
  - `state.settings.lockoutUntilMillis`
- **Save**: yes (after enable/disable and after each failed attempt)
- **Example**:
  - On startup, if lock enabled:
    - ask PIN
    - if wrong 3 times → set lockoutUntil=now+30s → save

