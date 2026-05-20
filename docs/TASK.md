# TASK.md — CS251 Assignment 2 Execution Plan (Masroofy, Java Console)

This file is the **team’s detailed implementation task plan** derived from:

- Assignment requirements: `docs/CS251_Assignment_Details.md` (Task 3–5)
- Requirements baseline (what to implement): `docs/Masroofy_SRS.md` (US #1–#12)
- Design baseline (how to implement): `docs/Masroofy_SDS_Full.md` and `diagrams/DIAGRAMS.md`

**Technology decision:** Java **Console** application (OOP), with **persistence** (DB or files) and **Javadoc** documentation generation.

---

## 0) Non‑negotiable requirements checklist (from assignment)

- **Implement all required user stories**:
  - Official minimum is first 7, but our team scope is **ALL 12 user stories** from `docs/Masroofy_SRS.md`.
- **Persistence**:
  - Must save data (DB or files) and **reload last saved data on startup**.
- **Traceability to sequence diagrams**:
  - Every method call/message shown in the sequence diagrams must exist in code.
  - The **Class–Sequence Usage Table** must be usable by the TA to locate these interactions in code.
- **GitHub usage**:
  - Private repo, multiple meaningful commits from every member.
  - Prepare a PDF with snapshots of contribution graphs + commit history for every member.
- **Documentation**:
  - **Javadoc for every class + every public method** with meaningful descriptions.
  - Generate docs output and include it in submission.
- **Clean code**:
  - Follow Java coding style and keep the design modular and readable.
- **Presentation**:
  - 10 minutes, 10–15 slides; include PDF/PPT in submission.

---

## Fast-track plan (deadline in 2 days)

This is the **minimum plan** that still satisfies the assignment (12 US + persistence + traceability + Javadoc).

- **Day 1 (ship core app)**:
  - Implement **SQLite persistence** (`AppState` + `DatabaseHelper`) + startup load/save.
  - Implement flows for **US #1, #2, #7, #8, #9, #11, #12** (these are the “must-demo” operations).
  - Ensure dashboard always shows safe daily limit (covers **US #3**) and category breakdown (covers **US #4**).
  - Implement rollover check on app start/refresh (covers **US #5**) and threshold warnings (covers **US #6**).

- **Day 2 (requirements packaging)**:
  - Add/adjust **sequence diagrams for missing US** and update **Class–Sequence Usage Table** (or align code naming to existing diagrams).
  - Add Javadoc across the project and generate `generated-docs/`.
  - Prepare submission artifacts (Readme.txt, slides, GitHub screenshots PDF).

**Rule for the next 48 hours:** no optional features, no refactors unless they unblock delivery.

---

## 1) Important note: diagram coverage gap (must be fixed before coding “US‑complete”)

Current design artifacts (`docs/Masroofy_SDS_Full.md`, `diagrams/DIAGRAMS.md`) contain **sequence diagrams for only 6 flows** (US #1, #2, #4-ish rollover, threshold alert, history filter, privacy PIN).

But the SRS contains **12 user stories** (US #1–#12).

To truly satisfy “**conform to the diagrams for all 12 user stories**”, we must do one of the following:

- **Option A (recommended): Extend the SDS** by adding missing sequence diagrams + updating the Class–Sequence Usage Table for:
  - **US #3, #5, #6, #7, #8, #9, #10, #12**
  - (Some overlap exists with current diagrams, but we must still cover every user story explicitly.)
- **Option B:** If the TA accepts that some user stories are implemented using **existing diagrams only**, then we must explicitly map each user story to the existing diagram(s).  
  - Risk: The assignment text strongly suggests **1 sequence diagram per required story**.

This `TASK.md` assumes **Option A** (extend diagrams so implementation for all 12 is fully traceable).

---

## 2) Proposed Java Console architecture (maps to MVC in SDS)

Even though SDS mentions “UI screens”, in a console app we implement the same separation:

- **View (Console UI)**: prints menus, reads user input, displays results/errors.
- **Controller / Application services**: orchestrates use-cases, validates input, calls domain/services.
- **Model / Domain**: entities + business rules (cycle, expense, category, settings).
- **Persistence**: repository/DAO layer + database/file adapter.

### 2.1 Persistence decision (FINAL): SQLite (100%)

All application data is stored in a local **SQLite** database (`data/masroofy.db`), aligned with the SRS/SDS design.

**Why SQLite:**

- Matches the documented `DatabaseHelper` architecture in the SDS.
- Reliable transactional persistence with a single database file.
- No custom JSON parsing or ad-hoc file formats.

**Implementation rule:** after any state-changing operation, call `DatabaseHelper.saveState(state)` immediately so the app always restarts correctly.

**Dependency:** `lib/sqlite-jdbc.jar` (org.xerial sqlite-jdbc) on the compile/run classpath.

---

## 3) Proposed project structure (Java Console, simplified folders)

Keep packages aligned with responsibilities so the TA can find diagram interactions quickly.

**Folder convention:** keep the Java package root at `main/masroofy/` and use a simple structure: `ui/` (console I/O), `core/` (use-cases), `model/` (POJOs), `storage/` (SQLite).

```
Masroofy/
├─ README.md
├─ TASK.md
├─ WORK_ALLOCATION.md
├─ docs/                         # given docs (SRS/SDS/assignment)
├─ diagrams/                     # given diagrams + exports
├─ data/                         # runtime SQLite database (created on first run)
├─ lib/                          # sqlite-jdbc.jar
├─ main/
│  └─ masroofy/
│     ├─ Main.java
│     ├─ ui/
│     │  ├─ DashboardUI.java
│     │  ├─ HistoryUI.java
│     │  ├─ MenuUI.java
│     │  ├─ SettingsUI.java
│     │  └─ SetupUI.java
│     ├─ core/
│     │  ├─ AuthManager.java
│     │  ├─ CycleManager.java
│     │  ├─ ExpenseManager.java
│     │  └─ ReportManager.java
│     ├─ model/
│     │  ├─ AppState.java
│     │  ├─ Category.java
│     │  ├─ Cycle.java
│     │  ├─ Expense.java
│     │  └─ UserSettings.java
│     └─ storage/
│        ├─ DatabaseHelper.java
│        └─ Paths.java
└─ generated-docs/               # output of Javadoc (HTML) to include in submission zip
```

**Notes**

- This is intentionally “simple and student-friendly”, not production-grade.
- For traceability, put the methods referenced by sequence diagrams inside `core/*Manager` and reference those in the Class–Sequence table (or update diagrams to match these names).

---

## 4) Data model & DB schema tasks (minimum set)

### 4.1 Entities (domain model)

- **Cycle**
  - `id`, `totalAllowance`, `startDate`, `endDate`
  - Derived/calculated: `remainingDays`, `remainingBalance`, `safeDailyLimit`
- **Expense**
  - `id`, `cycleId`, `categoryId`, `amount`, `timestamp`, optional `note`
- **Category**
  - `id`, `name`, optional `iconId` (console: store name only; iconId may exist only for traceability)
- **UserSettings**
  - `privacyLockEnabled`, `pinHash`, optional `failedAttempts`, `lockoutUntil`

### 4.2 SQLite schema (FINAL)

Database file: `data/masroofy.db` (created on first run).

Tables (managed by `DatabaseHelper`):

- `app_meta` — key/value store (`next_expense_id`)
- `cycles` — active budget cycle and alert flags
- `categories` — category id + name
- `expenses` — all transactions
- `user_settings` — privacy lock, PIN hash, lockout, ntfy suffix

### 4.3 Persistence behavior requirements

- **Startup**: `DatabaseHelper.loadState()`; if empty, treat as first run (go to setup flow).
- **After any change** (add/edit/delete expense, reset cycle, enable lock): `DatabaseHelper.saveState(state)` immediately.
- **Data integrity**: full-state save inside a single SQLite transaction.

---

## 5) Core “engine” rules (business logic tasks)

### 5.1 Safe Daily Limit definition (from SRS)

- Initial: `totalAllowance / numberOfDaysInCycle`
- Dynamic:
  - Recalculate whenever:
    - an expense is logged/edited/deleted
    - day rollover is detected (new day since last open)
  - General formula: `remainingBalance / remainingDays`

### 5.2 Rollover behavior (US #5)

Because this is a console app (no background scheduler), implement rollover as:

- On app launch OR on dashboard refresh:
  - If `today > lastCalculatedDate`, apply rollover logic and recompute.

### 5.3 Threshold alerts (US #6)

Console version:

- Print warning line when crossing thresholds, and record “alert shown” to avoid spamming each refresh.
- Implement at least:
  - 80% threshold warning
  - optional “budget exhausted” warning (SRS exceptional scenario)

---

## 6) User stories (US #1–#12) → detailed implementation tasks

For each user story:

- Implement the flow exactly as described in SRS
- Ensure there is a matching sequence diagram (or update diagrams)
- Ensure method names in code can be traced from the Class–Sequence Usage Table

### US #1 — Set Initial Budget Cycle

- **Console UI**
  - Ask for total allowance (EGP) and start/end date
  - Validate: allowance > 0; endDate > startDate
- **Controller/Service**
  - `CycleController.initializeCycle(amount, startDate, endDate)`
  - Create `Cycle`
  - Persist via repository (`insertCycle`)
  - Compute initial daily limit and show dashboard
- **Persistence**
  - Mark created cycle as active
- **Sequence diagram requirement**
  - Already exists; ensure method names align (or update diagram)

### US #2 — Rapid Expense Logging

- **Console UI**
  - Quick add: amount + category (choose from list) + optional note
  - Validate: numeric amount, > 0
- **Controller/Service**
  - `ExpenseController.logExpense(amount, categoryId, timestamp)`
  - Persist expense
  - Recompute remaining balance + safe daily limit
  - Trigger threshold check
- **Sequence diagram requirement**
  - Exists (but uses `ExpenseManager/Transaction` naming in current diagrams; reconcile)

### US #3 — Dynamic Daily Limit View

- **Console UI**
  - Dashboard always prints:
    - remaining balance
    - remaining days
    - safe daily limit
    - (optional) final-day badge/message
- **Controller/Service**
  - Compute on demand from persisted state
  - Ensure it updates after any expense operation and after rollover detection
- **Sequence diagram requirement**
  - Missing today → add a sequence diagram specifically for “open dashboard → fetch + compute limit”

### US #4 — Visual Spending Insights

Console-friendly equivalent (since no UI charts):

- Show category spending breakdown as:
  - Percentages + totals per category
  - Text/ASCII bar chart is acceptable
- **Controller/Service**
  - Aggregate totals per category for current cycle
- **Sequence diagram requirement**
  - Current SDS has pie chart generation concept in class diagram; add/confirm a sequence for “dashboard → generate spending insights”

### US #5 — Daily Rollover Management

- Implement “rollover detection”:
  - store last-run date (in DB settings or cycle record)
  - if day advanced, recompute limit based on remaining balance/days
- Handle overspending case (exceptional scenario): limit decreases
- **Sequence diagram requirement**
  - Current “Automatic Midnight Rollover” exists; adapt to console “on app open” trigger and reconcile names

### US #6 — Budget Threshold Notification

- Track total spent / initial budget ratio
- When expense pushes ratio >= 0.80:
  - show warning
  - store “80% alerted for this cycle” to avoid repeating
- Exceptional: if jump crosses 100%, show “Budget Exhausted”
- **Sequence diagram requirement**
  - Current threshold diagram exists but is generic; update to include storage of “alert already shown” flag

### US #7 — Transaction History Review

- **Console UI**
  - View history list (newest first): amount, category, timestamp, note
  - Handle empty history case
- **Controller/Service**
  - `ExpenseController.getHistory(cycleId)`
  - repository query by cycle
- **Sequence diagram requirement**
  - Missing today → add a sequence diagram for “open history → query DB → display list”

### US #8 — Edit or Delete Transaction

- **Console UI**
  - Select transaction by ID/index
  - Edit amount/category/note OR delete
  - Confirm before delete
- **Controller/Service**
  - `ExpenseController.editExpense(expenseId, newAmount, newCategoryId, newTimestamp?)`
  - `ExpenseController.deleteExpense(expenseId)`
  - After either: recompute safe daily limit + threshold logic
- **Sequence diagram requirement**
  - Missing today → add “edit expense” and “delete expense” diagrams (can be combined with `alt` blocks)

### US #9 — Filter Transaction History

- **Console UI**
  - Filter by category and/or date range
  - Show “no results” message when empty
- **Controller/Service**
  - `ExpenseController.filterHistory(cycleId, categoryId?, fromDate?, toDate?)`
  - Repository filters in-memory (read from SQLite-loaded state) using Java predicates and date comparisons
- **Sequence diagram requirement**
  - Current “View History & Filters” diagram exists; ensure it supports date range too

### US #10 — Offline Local Data Persistence

In console terms, “offline” means:

- No network dependencies at all
- Works fully using local SQLite/file storage
- Works fully using local SQLite database storage
- Startup restores state correctly

Tasks:

- Remove/avoid any external services
- Add a “data integrity” test scenario: restart app and ensure records remain
- **Sequence diagram requirement**
  - Missing today → add a diagram for “app launch → load persisted state → show dashboard/setup”

### US #11 — Cycle Reset and Data Clearance

- **Console UI**
  - Settings menu → Reset cycle
  - Confirm prompt; cancel leaves everything intact
- **Controller/Service**
  - `CycleController.resetCycle()`
  - Delete cycle + expenses (and possibly reset alert flags)
  - Navigate to setup flow again
- **Sequence diagram requirement**
  - Not explicitly present; current SDS uses “Set/Verify Privacy PIN” tagged as US #11, but SRS says US #11 is reset cycle.
  - **Action required**: fix numbering mismatch in diagrams/SDS or implement the diagrams as-is and clearly document mapping.

### US #12 — Local Privacy Lock

- **Console UI**
  - Enable/disable privacy lock in settings
  - Set PIN and confirm PIN
  - On app startup, if lock enabled → prompt for PIN before showing dashboard/history/settings
  - Exceptional: 3 failed attempts → lockout 30 seconds
- **Controller/Service**
  - `AuthService.setupPIN(pin)`
  - `AuthService.verifyPIN(pin)`
  - Store hashed PIN + lock flag
- **Sequence diagram requirement**
  - Current diagram exists for PIN flow but labels it as US #11; reconcile with SRS US #12.

---

## 7) Traceability deliverable (TA navigation)

We must produce a **Class–Sequence Usage Table** that is “TA-friendly”. Tasks:

- For every sequence diagram:
  - list all participating classes
  - list all method calls (exact method names)
  - add “Where in code?” references (package + class names)
- Standardize naming across:
  - SRS user story IDs
  - SDS sequence diagram labels
  - Code class/method names

**Definition of done (traceability):**

- A TA can pick any arrow in a sequence diagram and find the matching method in code quickly.

---

## 8) Javadoc tasks (required)

### 8.1 What must be documented

- Every class: purpose/responsibility, invariants (if any)
- Every public method:
  - what it does (non-obvious intent)
  - parameters and return meaning
  - thrown exceptions (if used)
  - edge cases (validation rules) when important

### 8.2 Generation deliverable

- Generate HTML docs into `generated-docs/`
- Ensure it is included in the final submission zip

---

## 9) GitHub & contribution evidence tasks

- Branch strategy:
  - `main` protected (optional)
  - feature branches per user story or per module
- Commit discipline:
  - frequent, meaningful commits (not “wip”, not giant single commit)
  - each member commits code + docs + tests
- Evidence PDF tasks:
  - capture contribution graph per member
  - capture commit list showing member activity

---

## 10) Submission packaging tasks (zip content)

Prepare one zip containing:

- Opportunities report (Task 1)
- Final SDS PDF
- Source code folder + `Readme.txt` explaining:
  - how to run the program (console)
  - which DB/file is used
  - where generated docs are
- Generated Javadoc folder (`generated-docs/`)
- Presentation slides PDF/PPT
- GitHub screenshots PDF

---

## 11) Bonus recommendations backlog (pick items, then negotiate with TA)

Bonus work **must be explicitly out of scope** of the SDS diagrams (allowed to go off-script).  
Choose bonuses that are impressive but still realistic for a console app.

### High-impact bonuses (recommended)

- **Recurring expenses**:
  - model “recurrence” (daily/weekly/monthly) and auto-generate entries at app start
- **Category budgets**:
  - per-category limit, alert when category exceeds threshold
- **Export/import**:
  - export expenses to CSV/JSON
  - backup/restore DB file
- **Advanced analytics**:
  - top categories, average daily spend, trend compared to previous week
  - “remaining days if current spending continues” projection
- **Multi-cycle history**:
  - keep old cycles instead of deleting on reset; allow browsing past cycles

### Security/privacy bonuses

- **Stronger PIN security**:
  - use salted hash
  - configurable lockout window
- **Encrypt local database/file** (advanced):
  - if using SQLite, explore SQLCipher-like approach (may be heavy); or encrypt exported backups

### UX bonuses (console)

- **Command palette style**:
  - short commands: `add`, `history`, `filter`, `reset`, `lock on/off`
- **Colored output**:
  - highlight warnings (80%, exhausted), final-day warning, overspending indicator

### Engineering-quality bonuses

- **Full CI** (GitHub Actions):
  - run tests + checkstyle + build Javadoc on each PR
- **Static analysis**:
  - Checkstyle + SpotBugs (or minimal Checkstyle only)
- **Better layering**:
  - interfaces for repositories (mockable for tests)

### “Bonus ideas to avoid” (high risk / low reward)

- Heavy GUI (Swing/JavaFX) if you already committed to console
- Cloud sync / online features (breaks “offline” simplicity and adds risk)
- Overcomplicated encryption without time to verify correctness

---

## 12) Work breakdown (who does what) — suggested split for 4 members

Split by modules so each member has meaningful commits.

- **Member A (Cycle)**: US #1, #3, #5, cycle calculations + rollover + cycle repository
- **Member B (Expense)**: US #2, #7, #8, `ExpenseController/Service`, CRUD + history
- **Member C (Filtering + Analytics)**: US #4, #9, spending insights + filters + dashboard aggregation
- **Member D (Security + Settings)**: US #11, #12, reset flow + PIN setup/verify + lockout + settings repo

Everyone contributes:

- unit tests
- documentation/Javadoc
- README/Readme.txt
- diagram updates (missing sequence diagrams) + traceability table

---

## 13) “Definition of Done” (final readiness checklist)

- All **US #1–#12** implemented and demoable end-to-end in console.
- Data persists and reloads correctly.
- Sequence diagrams cover all required user stories (or mapping is explicitly justified and accepted).
- Class–Sequence table points to real classes/methods in code.
- Javadoc generated and included.
- GitHub shows meaningful contributions from each member.
- Submission zip contains everything required.

