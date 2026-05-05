# Yehia Hassan — Implementation Guide (US #4, #8, #12)

This guide explains **how to implement Yehia’s allocated user stories** in this codebase, keeping everything **simple, console-based, and JSON-persisted**.

## Scope (your responsibility)

- **US #4 — Visual Spending Insights (console version)**
  - Category totals + percentages + simple ASCII bars on the dashboard (keyboard characters only, like `|||||-----`).
- **US #8 — Edit or Delete Transaction**
  - From History, select an expense by ID, then edit or delete it (with validation + confirmation).
  - Save immediately after changes.
- **US #12 — Local Privacy Lock**
  - Enable/disable PIN lock from Settings.
  - On startup: if enabled, require PIN before allowing access.
  - 3 failed attempts → 30 seconds lockout (persist attempts + lockout time).

## Storage (go over the JSON store)

### Where state is stored

- File path: `data/app-state.json`
- Loader/writer: `masroofy.storage.JsonStore`
- Root object: `masroofy.model.AppState`

### What is stored (minimum)

`AppState` (serialized to a JSON object) should contain:

- **`activeCycle`**: cycle metadata (minimal is enough for these US).
- **`expenses`**: list of expenses (each with `id`, `amount`, `categoryId`, `timestampMillis`, `note`).
- **`categories`**: list of categories (id + name). Seed defaults on first run.
- **`settings`**: privacy lock fields:
  - `privacyLockEnabled` (boolean)
  - `pinSaltBase64` (string)
  - `pinHashHex` (string)
  - `failedAttempts` (int)
  - `lockoutUntilMillis` (long)
- **Important rule**: after any edit/delete, after lock enable/disable, and after failed-attempt changes → **save immediately**.

### Simple persistence rule

The project’s rule is:

- **Load once on startup**
- Operate on the in-memory `AppState`
- **Save after every state mutation**

This ensures “offline persistence” expectations and makes debugging easy for the TA.

## US #12 — Local Privacy Lock (Auth + lockout)

### Data + security approach

Keep it simple but not plain-text:

- Generate a random salt (16 bytes).
- Store:
  - `pinSaltBase64`
  - `pinHashHex = SHA-256(saltBase64 + ":" + pin)`

This is simple, consistent, and avoids external dependencies.

### Required behaviors

- **Enable flow** (Settings):
  - Ask for PIN + confirm PIN
  - If mismatch → error, do not enable
  - If ok → store salt + hash; set `privacyLockEnabled=true`
  - Reset `failedAttempts=0`, `lockoutUntilMillis=0`
  - Save to JSON immediately

- **Disable flow** (Settings):
  - Set `privacyLockEnabled=false`
  - Optionally keep the salt+hash stored (simpler; enable again can either reuse or reset)
  - Save immediately

- **Startup gate**:
  - If `privacyLockEnabled=false` → continue to menu
  - Else:
    - If now < `lockoutUntilMillis` → show lockout remaining and exit or wait
    - Ask PIN; verify
    - On success:
      - `failedAttempts=0`, `lockoutUntilMillis=0`
      - Save (so state is consistent after restart)
      - Continue to main menu
    - On failure:
      - `failedAttempts++`
      - If `failedAttempts >= 3`:
        - set `lockoutUntilMillis = now + 30_000`
      - Save after every failed attempt

### Suggested method surface (simple)

Put this in `masroofy.core.AuthManager`:

- `boolean isLockEnabled(AppState state)`
- `void enablePin(AppState state, String pin, String confirmPin)`
- `void disablePin(AppState state)`
- `boolean verifyPin(AppState state, String pin)`
- `boolean isLockedOut(AppState state, long nowMillis)`
- `long lockoutRemainingMillis(AppState state, long nowMillis)`

The UI should call these, and `JsonStore` should be invoked to save after any mutation.

## US #8 — Edit or Delete Transaction (History-driven CRUD)

### Console flow requirements

From History screen:

- List expenses (ID, amount, category name, timestamp, note)
- Prompt: choose an expense **by ID**
- Offer:
  - Edit:
    - change amount (must be numeric and > 0)
    - change category (choose from list)
    - change note (optional)
  - Delete:
    - ask confirmation (“Are you sure? y/n”)
- Handle invalid ID:
  - print error and return to History

### Core logic expectations

Put these in `masroofy.core.ExpenseManager`:

- `Expense findExpenseById(AppState state, long expenseId)`
- `void editExpense(AppState state, long expenseId, double newAmount, int newCategoryId, String newNote)`
  - Validate: `newAmount > 0`
  - Validate: category exists
  - Mutate the expense object
  - Save immediately
  - Trigger any recalculation hooks your team uses (remaining balance / safe daily limit / threshold flags)
- `void deleteExpense(AppState state, long expenseId)`
  - Remove from list if found
  - Save immediately
  - Trigger recalculation hooks

### Recalculation side-effects

Your work allocation explicitly says after edit/delete you should trigger recomputation of:

- remaining balance
- safe daily limit
- thresholds (80% warning flag)

How to do it simply without “over-architecting”:

- If there is a dedicated calculator/service (team may put it in `CycleManager`), call it.
- If not, do a lightweight recompute in the place your app already computes dashboard values (often `ReportManager` / `DashboardUI`).

The key is: **do not forget to save** after state changes.

## US #4 — Visual Spending Insights (console aggregation)

### What to output (dashboard)

On the dashboard:

- If no expenses: print “No expenses yet.”
- Else print per-category:
  - category name
  - total spent in that category
  - percent of total spending
  - simple ASCII bar, e.g. `||||||-----` proportional to percent

### Core logic (aggregation)

Put this in `masroofy.core.ReportManager`:

- `List<SpendingInsight> getSpendingInsights(AppState state)`

Where `SpendingInsight` is a simple DTO (could be nested static class) with:

- `categoryId`
- `categoryName`
- `total`
- `percent` (0..100)

**Formula:**

- `totalSpent = sum(expense.amount)`
- For each category:
  - `catTotal = sum(expense.amount where expense.categoryId == category.id)`
  - `percent = (catTotal / totalSpent) * 100`

**Rounding rule (simple and consistent):**

- Print totals with 2 decimals.
- Print percent with 0–1 decimals (your choice, but be consistent).

## Orchestration (who calls what)

At runtime, the typical orchestration is:

1. **Startup**
   - Load `AppState` from `JsonStore`
   - If privacy lock enabled → `AuthManager` gate
2. **Menu**
   - Navigate to Dashboard / History / Settings
3. **Dashboard**
   - Calls `ReportManager.getSpendingInsights(state)` for US #4
4. **History**
   - Lists expenses
   - For edit/delete (US #8), calls `ExpenseManager.editExpense(...)` or `ExpenseManager.deleteExpense(...)`
5. **Settings**
   - Enable/disable lock (US #12), calls `AuthManager.enablePin/disablePin`

Every step that changes state should call `JsonStore.saveObject(state.toJsonObject())`.

## Project-wide file map reference

For a general (non-member-specific) guide that covers **all packages/files** and **US #1–#12 orchestration**, see:

- `docs/File_Map_and_Orchestration.md`

