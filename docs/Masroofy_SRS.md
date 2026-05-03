# Software Requirements Specifications (Version 1.0)
**Project: Masroofy**
**Course:** CS251 - Intro. to Software Engineering
**University:** Cairo University, Faculty of Computers and Artificial Intelligence
**Date:** Feb. 2026

## Team Members
| Name | Email | ID/Section |
| :--- | :--- | :--- |
| Yamen ali Fathi | 20****64@stud.fci-cu.edu.eg | 20240664/S9 |
| Ziad alaa Mokhtar | 20****16@stud.fci-cu.edu.eg | 20240216/S3 |
| Omar mohamed Abdelkader | 20****85@stud.fci-cu.edu.eg | 20240385/S6 |
| Mohamed bashier Mostafa | 20****84@stud.fci-cu.edu.eg | 20240484/S6 |

---

## Contents
1. Document Purpose and Audience
2. Introduction
   - Software Purpose
   - Software Scope
3. Definitions, acronyms, and abbreviations
4. Requirements
   - Functional Requirements
   - Non Functional Requirements
5. System Models
   - Use Case Model
6. Enriched User Stories
7. System Navigation Map
8. Tools
9. Ownership Report

---

## Document Purpose and Audience
**Purpose:** This document specifies the software requirements for our project to ensure all stakeholders and developers understand the system's functionality and constraints.
**Audience:** The primary audience includes the Course Instructor (Dr. M. El-Ramly), the TA, and the development team.

## Introduction
### Software Purpose
The purpose of this software is to provide an easy-use, offline, manual micro-budgeting mobile application designed specifically for Egyptian students and young adults. It helps replace traditional tracking methods like physical notebooks or basic note app.

### Software Scope
The scope of this project is an offline Android-based mobile application. Features included:
* **Cycle Management Module:** A setup system where users input their cash allowance and define the timeframe.
* **Expense Tracking System:** Manual entry interface allowing users to rapidly log daily transactions and assign them to specific lifestyle categories.
* **Financial Dashboard:** A visual interface providing real-time summaries of remaining funds, the current daily limit, and visual charts (e.g., pie charts) breaking down spending by category.
* **Alerting System:** A local push notification system that warns the user when 80% of their total cycle allowance has been consumed.

## Definitions, acronyms, and abbreviations

| Term / Acronym | Definition |
| :--- | :--- |
| **Micro-Budgeting** | The practice of managing small, frequent cash flows over a short, predefined time cycle (e.g., a week or a month), tailored to daily living expenses. |
| **Allowance** | The total starting amount of money available for the user to spend during the current active cycle. |
| **Safe Daily Limit** | A dynamically recalculated value indicating exactly how much cash the user can spend today without running out of money before the cycle ends. |
| **EGP** | Egyptian Pound; the standard local currency used for all calculations within the application. |
| **UI/UX** | User Interface / User Experience; refers to the visual design, layout, and overall ease-of-use of the application. |
| **API** | Application Programming Interface; a mechanism that allows two software components to communicate. (Note: This software intentionally avoids banking APIs to maintain an offline state). |
| **SQLite** | A lightweight, local database engine used to securely store the user's financial transactions directly on their mobile device without needing internet access. |
| **MVC** | Model-View-Controller; the underlying architectural software pattern used to structure the application's code. |

## Requirements

### Functional Requirements

| ID | Requirement Name | Requirement Description |
| :--- | :--- | :--- |
| FR-1 | Allowance Initialization | The system shall allow the user to initialize an allowance cycle by inputting a total cash amount and selecting a specific start and end date. |
| FR-2 | Limit Calculation | The system shall automatically calculate the initial "Safe Daily Limit" by dividing the total allowance by the total number of days in the defined cycle. |
| FR-3 | Expense Logging | The system shall provide a logging interface where users can record an expense by entering an amount and selecting a category (e.g., Food, Transport). |
| FR-4 | Dynamic Rollover | The system shall dynamically recalculate the daily limit for remaining days whenever an expense is logged or when unspent funds from the previous day are rolled over. |
| FR-5 | Spending Dashboard | The system shall generate a visual dashboard featuring a pie chart that displays the percentage of total spending allocated to each category. |
| FR-6 | Usage Alerts | The system shall monitor the total budget and trigger a local push notification alert once the user has consumed 80% of their total allowance. |
| FR-7 | Local Persistence | The system shall ensure all transaction data is immediately persisted to a local SQLite database after every user entry. |

### Non Functional Requirements

| Category | Details |
| :--- | :--- |
| **Performance** | - The expense logging process must be completed in 3 taps or fewer.<br>- Dashboard updates and calculations must occur in less than 1 second. |
| **Availability** | The application must be 100% functional offline, requiring no internet connection for core features or data entry. |
| **Security/Privacy** | All financial data must be stored exclusively on the user's local device; the system shall not transmit data to any external server or API. |
| **Reliability** | The system shall maintain data integrity in the SQLite database during unexpected application terminations or device restarts. |
| **Usability** | The interface shall be designed for one-handed use, specifically targeting rapid entry for students on the move. |

## System Models

### Use Case Model
*[Placeholder: Use Case Diagram Image]*

**Actors:**
* Student

**Use Cases:**
* Personal Budgeting Software
  * Authentication (Log-In, Sign-Up)
    * Verify PIN `<include>`
    * Set Local PIN `<include>`
  * Manage Cycle
    * Edit Active Cycle `<extend>`
    * Generate Threshold Alert `<extend>`
  * Log Expense
    * Recalculate Limits `<include>`
  * View Dashboard
    * Filter by Category `<extend>`
  * View Transaction History
    * Filter Transactions `<extend>`
    * Filter by Date `<extend>`
  * Time Scheduler

## Enriched User Stories

### User Story #1: Set Initial Budget Cycle
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #1 |
| **User Story Name** | Set Initial Budget Cycle |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to input my total cash allowance and the duration of my spending cycle So the system can calculate my starting daily limit and track my progress. |
| **Pre condition** | The application is installed, and the user has no active budget cycle running. |
| **Post condition** | A new budget cycle is created in the SQLite database, and the "Safe Daily Limit" is calculated and displayed. |
| **Acceptance Criteria** | Given I am on the setup screen for the first time, When I enter a valid numeric amount (ex: 3000 EGP), select a start date (ex: today), and select an end date (ex: 30 days from now), Then the system saves this configuration and navigates me to the dashboard. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| User opens the app for the first time. | System detects no active cycle and displays "Initial Setup" screen. |
| User enters total Budget amount (ex: 1000). | |
| User selects start and end dates for the cycle. | System validates date range. |
| Click Start Cycle. | System calculates daily limit and saves data to SQLite. System displays dashboard. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| User enters negative number or zero in allowance field. | |
| Click Start Cycle. | Input is invalid. System rejects entry and displays error message: "Allowance must be a positive number." |

*[Placeholder: Screen Design Image for US #1]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Total Allowance | Decimal / Float | Must be > 0; cannot be empty. |
| Start Date | Date Picker | Defaults to current device date. |
| End Date | Date Picker | Must be strictly greater than Start Date. |

---

### User Story #2: Rapid Expense Logging
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #2 |
| **User Story Name** | Rapid Expense Logging |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to record an expense in under three taps So I can track my spending instantly without manual friction. |
| **Pre condition** | An active budget cycle exists in the system. |
| **Post condition** | The transaction is saved to the SQLite database; the "Safe Daily Limit" and dashboard charts are updated. |
| **Acceptance Criteria** | Given I am on the Quick-Entry screen, When I fill in the "Amount" field and tap a "Category" icon (ex: Food), Then the system saves the record and redirects me to the dashboard. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| User enters the expense amount (ex: 50). | |
| User taps the "Transport" category icon. | |
| Click Save. | System validates numeric data. System writes the transaction to local SQLite database. System recalculates daily limit and updates charts. System returns to Dashboard with "Saved" confirmation. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| User enters non-numeric text in amount field. | |
| Click Save. | Data type is invalid. System rejects entry and displays error message: "Please enter a valid number." |

*[Placeholder: Screen Design Image for US #2]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Expense Amount | Decimal / Float | Must be numeric; maximum 10 digits. |
| Category Icon | Selection (ID) | User must select exactly one from the grid. |
| Timestamp | DateTime | Auto-generated by system at moment of saving. |

---

### User Story #3: Dynamic Daily Limit View
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #3 |
| **User Story Name** | Dynamic Daily Limit View |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to see my updated "Safe Daily Limit" on the home screen, So I can immediately know how much I am allowed to spend today without running out of money before the cycle ends. |
| **Pre condition** | An active budget cycle has been initialized. |
| **Post condition** | The current "Safe Daily Limit" is displayed prominently on the dashboard. |
| **Acceptance Criteria** | Given I have logged expenses or passed a day without spending, When I open the app's dashboard, Then the system displays a recalculated daily limit based on the remaining balance and remaining days. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User opens the application. | 2- System retrieves the remaining balance and remaining days from SQLite.<br>3- System calculates the new limit: Remaining Balance / Remaining Days.<br>4- System displays the "Safe Daily Limit" in a large, central font on the dashboard. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User opens the app on the final day of the cycle. | 2- System calculates the limit for the last 24 hours.<br>3- System displays a "Final Day" warning badge alongside the limit. |

*[Placeholder: Screen Design Image for US #3]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Safe Daily Limit | Decimal / Float | Calculated: Remaining Budget ÷ Remaining Days. |
| Category Totals | Decimal / Float | Aggregated sum of transactions per category. |
| Pie Chart Slice | Graphic Percentage | Represents (Category Total / Total Spent) x 100. |

---

### User Story #4: Visual Spending Insights
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #4 |
| **User Story Name** | Visual Spending Insights |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to view a visual breakdown of my expenses by category So I can identify where I am spending the most money and adjust my habits accordingly. |
| **Pre condition** | The user has logged at least one expense in the current budget cycle. |
| **Post condition** | A pie chart is rendered on the dashboard reflecting current spending percentages. |
| **Acceptance Criteria** | Given I am on the Dashboard screen, When the system calculates transaction totals for each category, Then a pie chart is displayed with segments representing each category's share of total spending. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User navigates to the Dashboard. | 2- System queries SQLite for all transactions in the current cycle.<br>3- System aggregates totals per category (e.g., Food, Transport).<br>4- System generates and displays a pie chart where each slice represents a category's percentage of total expenditure. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User navigates to the Dashboard with zero expenses logged. | 2- System identifies that the transaction table is empty for the current cycle.<br>3- System displays a placeholder message: "No data available. Log an expense to see your insights." |

*[Placeholder: Screen Design Image for US #4]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Safe Daily Limit | Decimal / Float | Calculated: Remaining Budget ÷ Remaining Days. |
| Category Totals | Decimal / Float | Aggregated sum of transactions per category. |
| Pie Chart Slice | Graphic Percentage | Represents (Category Total / Total Spent) x 100. |

---

### User Story #5: Daily Rollover Management
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #5 |
| **User Story Name** | Daily Rollover Management |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to have my unspent daily limit automatically added to the remainder of my cycle, So that my daily limit for the following days increases as a reward for saving. |
| **Pre condition** | A new day has started, and the user spent less than their allocated "Safe Daily Limit" on the previous day. |
| **Post condition** | The "Safe Daily Limit" for all remaining days in the cycle is recalculated and increased in the SQLite database. |
| **Acceptance Criteria** | Given my daily limit was 50 EGP and I spent only 30 EGP, When the system clock reaches 00:00 (midnight), Then the remaining 20 EGP is redistributed across the remaining days of the cycle, increasing the displayed daily limit. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User opens the app on a new day. | 2- System checks the timestamp of the last login and the current balance.<br>3- System identifies unspent funds from the previous day.<br>4- System divides the Total Remaining Balance by the Number of Remaining Days (including today).<br>5- System updates the dashboard with the newly increased "Safe Daily Limit." |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User opens the app after overspending the previous day's limit. | 2- System identifies a "negative rollover" (deficit).<br>3- System recalculates the limit, which results in a lower daily amount for the remaining days.<br>4- System displays the updated limit with a subtle color change (e.g., orange) to indicate a tighter budget. |

*[Placeholder: Screen Design Image for US #5]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Safe Daily Limit | Decimal / Float | Calculated: Remaining Budget ÷ Remaining Days. |
| Category Totals | Decimal / Float | Aggregated sum of transactions per category. |
| Pie Chart Slice | Graphic Percentage | Represents (Category Total / Total Spent) x 100. |

---

### User Story #6: Budget Threshold Notification
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #6 |
| **User Story Name** | Budget Threshold Notification |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to receive a local push notification when I have spent 80% of my total budget, So I can make a plan to prevent running out of money before the end of the week or month. |
| **Pre condition** | The user has an active allowance cycle and has logged multiple expenses. |
| **Post condition** | A system notification is triggered and displayed on the user's device. |
| **Acceptance Criteria** | Given my initial allowance was 1000 EGP, When I log a transaction that brings my total spending to 800 EGP (80%) or more, Then the system triggers a local notification saying: "Warning: You have used 80% of your allowance." |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User enters an expense amount that crosses the 80% threshold of the total cycle budget. | 2- System saves the transaction to the SQLite database.<br>3- System calculates the new total expenditure percentage.<br>4- System identifies that the total is >=80% of the initial budget.<br>5- System triggers a local push notification as an early warning. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User logs an expense that immediately jumps from 70% to 100% of the budget. | 2- System detects that both the 80% and 100% thresholds were crossed.<br>3- System prioritizes the most critical alert.<br>4- System sends a "Budget Exhausted" notification instead of the 80% warning. |

*[Placeholder: Screen Design Image for US #6]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Filter Selection | String/Dropdown | Matches existing categories in SQLite database. |
| PIN Field | Integer / 4 digits | Must match the stored hashed PIN in local storage. |
| Reset Confirmation| Boolean / Binary | Requires explicit user "Confirm" before deletion. |

---

### User Story #7: Transaction History Review
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #7 |
| **User Story Name** | Transaction History Review |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to view a chronological list of all my recorded expenses, So I can track exactly when and where my money was spent during the current cycle. |
| **Pre condition** | The user has an active budget cycle and has logged at least one transaction. |
| **Post condition** | A list of transactions is displayed on the screen, sorted by date and time. |
| **Acceptance Criteria** | Given I have logged several expenses (e.g., Food and Transport), When I navigate to the "History" tab or screen, Then the system displays a list showing each expense amount, category, and timestamp. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User taps the "History" icon on the bottom navigation bar. | 2- System queries the local SQLite database for all transactions linked to the current cycle ID.<br>3- System sorts the transactions in descending order (newest first).<br>4- System renders a list where each row shows the category icon, date, and amount spent. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User navigates to the History screen with no transactions logged yet. | 2- System identifies that the SQLite transaction table is empty for the current cycle.<br>3- System displays a "No Transactions Found" placeholder message.<br>4- System provides a shortcut button to "Log your first expense." |

*[Placeholder: Screen Design Image for US #7]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Filter Selection | String/Dropdown | Matches existing categories in SQLite database. |

---

### User Story #8: Edit or Delete Transaction
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #8 |
| **User Story Name** | Edit or Delete Transaction |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to modify or remove a previously recorded expense, So I can correct input mistakes and keep my daily limit calculations accurate. |
| **Pre condition** | The user has at least one recorded expense in the current cycle. |
| **Post condition** | The transaction is updated or removed from the SQLite database, and the "Safe Daily Limit" is automatically recalculated. |
| **Acceptance Criteria** | Given I am on the Transaction History screen, When I select an expense to "Edit" (change amount/category) or "Delete", Then the system updates the database and immediately reflects the new limit on the dashboard. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User opens the History screen and taps the "Edit" icon next to a 50 EGP entry. | 2- System displays the entry details in an editable form. |
| 3- User changes the amount to 30 EGP and clicks Save. | 4- System validates the new numeric data.<br>5- System updates the record in the local SQLite database.<br>6- System triggers the calculation engine to update the "Safe Daily Limit."<br>7- System returns to History with a "Transaction Updated" confirmation. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User selects "Delete" on a transaction. | 2- System prompts: "Are you sure you want to delete this? This will update your daily limit." |
| 3- User confirms deletion. | 4- System removes the record from SQLite.<br>5- System recalculates the increased daily limit for the remaining days. |

*[Placeholder: Screen Design Image for US #8]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Filter Selection | String/Dropdown | Matches existing categories in SQLite database. |
| PIN Field | Integer / 4 digits | Must match the stored hashed PIN in local storage. |
| Reset Confirmation| Boolean / Binary | Requires explicit user "Confirm" before deletion. |

---

### User Story #9: Filter Transaction History
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #9 |
| **User Story Name** | Filter Transaction History |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to filter my transactions by specific categories or date ranges, So I can analyze my spending for specific items like "Food" or review what I spent during a particular weekend. |
| **Pre condition** | The user is on the Transaction History screen with multiple logged expenses. |
| **Post condition** | The displayed list is filtered to show only transactions matching the selected criteria. |
| **Acceptance Criteria** | Given I am on the History screen, When I select "Transport" from the category filter, Then the system hides all other expenses and shows only Transport-related logs. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User taps the "Filter" icon on the History screen. | 2- System displays a dropdown with available categories and a date-range picker. |
| 3- User selects "Food" and sets the date to "Yesterday." | 4- System queries the SQLite database using WHERE clauses for category and date.<br>5- System updates the UI to display only the matching transactions. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User applies a filter for a category with no logged transactions. | 2- System performs the search and finds zero results.<br>3- System displays a message: "No transactions found for the selected filter." |

*[Placeholder: Screen Design Image for US #9]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Filter Selection | String/Dropdown | Matches existing categories in SQLite database. |
| PIN Field | Integer / 4 digits | Must match the stored hashed PIN in local storage. |

---

### User Story #10: Offline Local Data Persistence
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #10 |
| **User Story Name** | Offline Local Data Persistence |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to use all application features while my device is offline, So I can manage my cash allowance anywhere regardless of internet connectivity or cellular signal. |
| **Pre condition** | The device has no active internet or network connection. |
| **Post condition** | All inputs are saved to the local SQLite database and are available upon the next application launch. |
| **Acceptance Criteria** | Given my phone is in airplane mode, When I log an expense or view my dashboard, Then the system performs all calculations and saves the data locally without any "no connection" errors. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User opens the app while in an area with zero internet coverage. | 2- System launches immediately without attempting an external login or API sync. |
| 3- User logs an expense of 40 EGP. | 4- System writes the entry directly to the local SQLite file.<br>5- System updates the "Safe Daily Limit" using local logic.<br>6- System displays a "Saved Locally" confirmation. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User restarts the device while the app is running in an offline state. | 2- System triggers the SQLite journaling mechanism to prevent data corruption. |
| 3- User reopens the app after the restart. | 4- System restores the state from the last committed transaction in SQLite.<br>5- System confirms that no data was lost during the power cycle. |

*[Placeholder: Screen Design Image for US #10]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Filter Selection | String/Dropdown | Matches existing categories in SQLite database. |
| PIN Field | Integer / 4 digits | Must match the stored hashed PIN in local storage. |
| Reset Confirmation| Boolean / Binary | Requires explicit user "Confirm" before deletion. |

---

### User Story #11: Cycle Reset and Data Clearance
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #11 |
| **User Story Name** | Cycle Reset and Data Clearance |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to end my current budget cycle early and clear all associated transaction data, So I can start a fresh cycle immediately if my financial circumstances change or a new allowance arrives early. |
| **Pre condition** | The user has an active budget cycle and is in the "Settings" or "History" menu. |
| **Post condition** | The active cycle and all related transactions are deleted from the SQLite database, and the user is returned to the "Initial Setup" screen. |
| **Acceptance Criteria** | Given I am on the Settings screen, When I select "Reset Current Cycle" and confirm the deletion, Then the system wipes the local budget data and prompts me to enter a new allowance. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User navigates to Settings and taps "Reset Current Cycle." | 2- System displays a confirmation dialog: "This will permanently delete all logs for this cycle. Continue?" |
| 3- User taps "Yes, Reset." | 4- System executes a DELETE command on the cycle and transaction tables in SQLite.<br>5- System clears the "Safe Daily Limit" from the local cache.<br>6- System redirects the user to the "Allowance Initialization" screen. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User taps "Reset" by accident. | 2- System displays the confirmation dialog. |
| 3- User taps "Cancel" or anywhere outside the dialog. | 4- System closes the dialog and makes no changes to the database.<br>5- System returns the user to the previous screen with data intact. |

*[Placeholder: Screen Design Image for US #11]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Filter Selection | String / Dropdown | Matches existing categories in SQLite database. |
| PIN Field | Integer / 4 digits | Must match the stored hashed PIN in local storage. |
| Reset Confirmation| Boolean / Binary | Requires explicit user "Confirm" before deletion. |

---

### User Story #12: Local Privacy Lock
| Field | Description |
| :--- | :--- |
| **User Story ID** | US #12 |
| **User Story Name** | Local Privacy Lock |
| **Actors** | College Student |
| **Description** | As a college student, I like to be able to enable a local PIN or biometric lock, So I can ensure my personal financial records and allowance data remain private from others who might access my phone. |
| **Pre condition** | The user has initialized a budget cycle and is in the "Settings" menu. |
| **Post condition** | A local authentication flag is set in the SQLite database, requiring verification upon next app launch. |
| **Acceptance Criteria** | Given I have enabled the "Privacy Lock" in settings, When I close and then reopen the application, Then the system prompts for my device PIN or biometric (fingerprint/face) before displaying the dashboard. |

**Scenarios**
*Normal Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User navigates to Settings and toggles "Enable Privacy Lock." | 2- System requests the user to define a 4-digit PIN or confirm biometric authentication. |
| 3- User enters PIN and confirms. | 4- System saves the encrypted hash of the PIN to the local SQLite database. |
| 5- User closes the app and reopens it. | 6- System detects the active lock and displays the authentication screen. |
| 7- User provides valid authentication. | 8- System grants access and displays the dashboard. |

*Exceptional Scenario*
| Actor Action | System Response |
| :--- | :--- |
| 1- User provides an incorrect PIN three times. | 2- System identifies the failed authentication attempts.<br>3- System enforces a 30-second lockout period to prevent brute-force entry.<br>4- System displays a "Try again later" message. |

*[Placeholder: Screen Design Image for US #12]*

**Data Dictionary**
| Element Label | Type/Length | Data Validation / Business Rule |
| :--- | :--- | :--- |
| Filter Selection | String/Dropdown | Matches existing categories in SQLite database. |
| PIN Field | Integer / 4 digits | Must match the stored hashed PIN in local storage. |
| Reset Confirmation| Boolean / Binary | Requires explicit user "Confirm" before deletion. |

## System Navigation Map
*[Placeholder: System Navigation Map Image]*
**Reference Link:** [Excalidraw Map](https://excalidraw.com/#json=8lUn8Czbxi-MvtnLdoPgS,R8uJC5NtdbJGTQrreAYYDw)

## Tools
* PlantUML
* Excalidraw
* Figma

## Ownership Report
| Student | Items he created |
| :--- | :--- |
| Yamen Ali Fathi | Use case model / mock up system navigation / design |
| Ziad Alaa Mokhtar | Introduction / user stories |
| Omar Mohamed Abdelkader | System Navigation Map / user stories |
| Mohamed Basheir Mostafa | Requirements / user stories |
