# WORK_ALLOCATION.md — Team S26 (Masroofy, Java Console, JSON)

This file assigns **exact implementation responsibilities** per member based on the 12 SRS user stories in `docs/Masroofy_SRS.md`.

**Global rules (everyone must follow):**

- **Persistence**: JSON-only (`data/app-state.json`). Any change must be saved immediately.
- **Traceability**: each user story must map to sequence diagrams in `docs/Masroofy_SDS_Full.md` / `diagrams/DIAGRAMS.md` and to real code method names (Class–Sequence Usage Table).
- **Javadoc**: every class and every public method must have meaningful Javadoc.
- **Deadline focus**: implement the simplest console flows that satisfy acceptance criteria; avoid optional refactors.
- **No tests**: skip test writing (deadline). Do quick manual checks per flow instead.

---

## Shared baseline (do once, then everyone uses it)

These are “platform” tasks that unblock all user stories. Assign one person to implement, others review quickly.

- **JSON store + state model**
  - Create `AppState` containing: active cycle, expenses list, categories list, settings, and flags (e.g., “80% alert shown”).
  - Create `JsonStore` with:
    - `load()` (if missing → first run)
    - `save(AppState)` (write temp then atomic rename)
- **Category seed**
  - Ensure default categories exist on first run (Food, Transport, Bills, etc.).

---

## Yehia Hassan Abdelmoaty — US #4, #8, #12

### US #4 — Visual Spending Insights (console version)
- **Console output**
  - On dashboard, print category totals and percentages (text/ASCII bars).
  - Handle “no expenses yet” message.
- **Core logic**
  - Implement aggregation: sum expenses per category for current cycle.
  - Provide method(s) that return a stable structure (e.g., list of `{category, total, percent}`) for the UI to print.
- **Persistence touchpoints**
  - No schema changes required, but must read from `AppState.expenses` and `AppState.categories`.
- **Traceability deliverables**
  - Add/update a sequence diagram for “Dashboard → Generate spending insights”.
  - Update Class–Sequence table with exact method names and code locations.
- **Manual checks**
  - Aggregation correctness with multiple categories; zero-expense case.
- **Javadoc**
  - Document calculation formula and rounding rules.

### US #8 — Edit or Delete Transaction
- **Console flow**
  - From History: select expense by ID/index.
  - Edit: change amount/category/note; validate numeric amount > 0.
  - Delete: require confirmation prompt.
- **Core logic**
  - Implement:
    - `editExpense(expenseId, newAmount, newCategoryId, newNote, newTimestamp?)`
    - `deleteExpense(expenseId)`
  - After edit/delete, trigger recomputation of:
    - remaining balance
    - safe daily limit
    - thresholds (80% warning)
- **Persistence touchpoints**
  - Save JSON immediately after edit/delete.
- **Traceability deliverables**
  - Add sequence diagram(s) for Edit/Delete (can be one diagram with `alt`).
  - Update Class–Sequence table entries.
- **Manual checks**
  - Edit updates totals; delete removes record; invalid ID handling.
- **Javadoc**
  - Explain recalculation side-effects and validations.

### US #12 — Local Privacy Lock
- **Console flow**
  - Settings: enable/disable lock.
  - Enable: set PIN + confirm PIN.
  - Startup gate: if enabled, prompt for PIN before showing dashboard/history/settings.
  - Exceptional: 3 failed attempts → 30s lockout.
- **Core logic**
  - Implement:
    - `setupPIN(pin)` → store salted hash (or at least hash) + enable flag
    - `verifyPIN(pin)` → compare hash
    - lockout tracking (`failedAttempts`, `lockoutUntil`)
- **Persistence touchpoints**
  - Store in `AppState.settings`.
  - Save after enable/disable and after failed-attempt counter changes.
- **Traceability deliverables**
  - Existing sequence diagram exists (but may be mislabeled in SDS as US #11). Either:
    - fix the label in diagrams/SDS, or
    - add a US #12-specific diagram and map it in the Class–Sequence table.
- **Manual checks**
  - Hash verify success/fail; lockout after 3 failures; lockout expiry.
- **Javadoc**
  - Document lockout rule and security constraints.

---

## Noha Mohamed Ahmed — US #2, #6, #10

### US #2 — Rapid Expense Logging
- **Console flow**
  - Quick entry: amount + category selection (+ optional note).
  - Validate numeric amount > 0.
- **Core logic**
  - Implement:
    - `logExpense(amount, categoryId, timestamp, note)`
  - After add: recompute daily limit and update dashboard view model.
- **Persistence touchpoints**
  - Append to `AppState.expenses`, then save JSON immediately.
- **Traceability deliverables**
  - Ensure existing “Log Expense” sequence diagram matches final class/method names.
  - Update Class–Sequence table with exact names + code location.
- **Manual checks**
  - Add expense persists; invalid input rejected.
- **Javadoc**
  - Document validation and timestamp behavior.

### US #6 — Budget Threshold Notification (80%)
- **Console behavior**
  - When total spent crosses 80% of initial allowance, print warning once (no spam).
  - Exceptional: if total >= 100%, show “Budget Exhausted” message.
- **Core logic**
  - Implement threshold calculator:
    - totalSpent / initialAllowance
  - Track “alert already shown” in `AppState` (per active cycle).
- **Persistence touchpoints**
  - Save JSON when alert flags change.
- **Traceability deliverables**
  - Update existing threshold sequence diagram to include “already alerted” flag storage (or add a small extension).
  - Update Class–Sequence table.
- **Manual checks**
  - Crossing 80% triggers once; crossing 100% triggers exhausted message.
- **Javadoc**
  - Document thresholds and one-time alert rule.

### US #10 — Offline Local Data Persistence (console interpretation)
- **What “offline” means here**
  - No network dependencies.
  - All actions work using only JSON local files.
  - Restart restores state accurately.
- **Implementation tasks**
  - Ensure startup loads `AppState` and restores:
    - active cycle
    - expenses
    - settings (privacy lock)
    - category list
  - Add a “Saved locally” confirmation text after writes (optional but helpful).
- **Traceability deliverables**
  - Add sequence diagram for “App launch → Load state → route to Setup/Dashboard/Auth”.
  - Update Class–Sequence table.
- **Manual checks**
  - Add expense → restart app → expense still present.
- **Javadoc**
  - Document persistence guarantees and failure handling.

---

## Jana Ahmed Farahat Hassan — US #3, #7, #11

### US #3 — Dynamic Daily Limit View
- **Console output**
  - Dashboard must always print:
    - remaining balance
    - remaining days
    - safe daily limit
  - Final-day message on last day (simple text).
- **Core logic**
  - Implement deterministic calculation:
    - `safeDailyLimit = remainingBalance / remainingDays`
  - Define how to compute remainingDays from start/end dates (inclusive/exclusive) and be consistent.
- **Persistence touchpoints**
  - Read from `AppState.cycle` + expenses totals.
  - Store `lastCalculatedDate` if needed for rollover logic.
- **Traceability deliverables**
  - Add sequence diagram for “Open dashboard → compute safe daily limit”.
  - Update Class–Sequence table.
- **Manual checks**
  - Final day handling; remainingDays computation edge cases.
- **Javadoc**
  - Document date arithmetic assumptions.

### US #7 — Transaction History Review
- **Console flow**
  - Show chronological list (newest first) with: amount, category, timestamp, note.
  - Empty-state message if no transactions.
- **Core logic**
  - Implement `getHistory()` returning expenses sorted by timestamp desc.
- **Persistence touchpoints**
  - Read from `AppState.expenses`.
- **Traceability deliverables**
  - Add sequence diagram for “History → fetch list → display”.
  - Update Class–Sequence table.
- **Manual checks**
  - Sorting correctness; empty list.
- **Javadoc**
  - Document sort order and formatting.

### US #11 — Cycle Reset and Data Clearance
- **Console flow**
  - Settings → Reset cycle → confirmation prompt.
  - Cancel → no changes.
  - Confirm → clear cycle data and go to setup.
- **Core logic**
  - Implement `resetCycle()` to clear:
    - active cycle
    - expenses for that cycle
    - per-cycle flags (e.g., threshold alert shown)
  - Keep categories and privacy settings decision consistent:
    - Recommended: keep categories; keep privacy lock setting unless SRS implies otherwise.
- **Persistence touchpoints**
  - Save JSON immediately after reset.
- **Traceability deliverables**
  - Add a sequence diagram for “Reset cycle” (currently missing/mislabeled in SDS).
  - Update Class–Sequence table (and fix US numbering mismatch in SDS if needed).
- **Manual checks**
  - Reset actually clears; cancel does nothing.
- **Javadoc**
  - Document exactly what data is cleared vs retained.

---

## Hana Khaled Abdelhamed — US #1, #5, #9

### US #1 — Set Initial Budget Cycle
- **Console flow**
  - First run: prompt for allowance + start date + end date.
  - Validate: allowance > 0; endDate > startDate.
- **Core logic**
  - Implement `initializeCycle(amount, startDate, endDate)`:
    - create cycle
    - compute initial safe daily limit
    - route to dashboard
- **Persistence touchpoints**
  - Save cycle into `AppState.cycle` and write JSON.
- **Traceability deliverables**
  - Ensure existing setup sequence diagram matches final names or update it.
  - Update Class–Sequence table.
- **Manual checks**
  - Invalid allowance rejected; invalid date range rejected.
- **Javadoc**
  - Document validation rules and date semantics.

### US #5 — Daily Rollover Management
- **Console interpretation**
  - No background scheduler. Do rollover detection:
    - at app start
    - when dashboard is opened/refreshed
- **Core logic**
  - Track a `lastActiveDate` / `lastCalculatedDate`.
  - If day advanced:
    - recompute safe daily limit using remaining balance and remaining days.
  - Overspending case reduces limit (print a warning message).
- **Persistence touchpoints**
  - Store lastCalculatedDate in `AppState` and save JSON after update.
- **Traceability deliverables**
  - Update existing rollover diagram to match console trigger (on app open).
  - Update Class–Sequence table.
- **Manual checks**
  - Simulate date advancement; overspending scenario.
- **Javadoc**
  - Document rollover trigger points and assumptions.

### US #9 — Filter Transaction History
- **Console flow**
  - Filter by category and/or date range.
  - Display “no results” if empty.
- **Core logic**
  - Implement `filterHistory(categoryId?, fromDate?, toDate?)` over in-memory expenses.
  - Ensure date filtering is inclusive/exclusive and document it.
- **Persistence touchpoints**
  - Read-only (filters do not change state).
- **Traceability deliverables**
  - Ensure existing “View History & Filters” diagram covers date range; update if needed.
  - Update Class–Sequence table.
- **Manual checks**
  - Category-only filter; date-only filter; combined filter; empty results.
- **Javadoc**
  - Document filtering rules.

---

## Bonus / extra work ideas (do only after core is done)

- Recurring expenses (daily/weekly/monthly) with auto-generation at app start.
- Category budgets + per-category alerts.
- Export/import (CSV/JSON) + backup/restore `data/app-state.json`.
- Advanced analytics (avg daily spend, projections, trends, top categories).
- Multi-cycle history (keep past cycles instead of deleting; browse old cycles).
- Stronger PIN security (salted hash, configurable lockout).
- Encryption for backups (encrypt exported file).
- Command-style interface (`add`, `history`, `filter`, `reset`, `lock on/off`).
- Colored console output for warnings and final-day messages.
- GitHub Actions CI (build, tests, Javadoc generation).
- Checkstyle/SpotBugs (at least Checkstyle).
