package masroofy.ui;
import masroofy.model.AppState;
import masroofy.core.CycleManager;
public class DashboardUI {

    /**
     * Displays the updated Safe Daily Limit (US #3). Jana
     *
     * <p>Uses CycleManager to:
     * - apply daily rollover if needed
     * - calculate remaining balance and days
     * - compute safe daily limit
     * @param state application state
     */
    public void showSafeDailyLimit(AppState state) {
        if (state == null) throw new IllegalArgumentException("state cannot be null");

        CycleManager cm = new CycleManager();
        CycleManager.RolloverResult result =
                cm.handleRolloverIfNeeded(state, java.time.LocalDate.now());

        System.out.println("----- Dashboard -----");
        System.out.println("Remaining Balance: " + result.getRemainingBalance());
        System.out.println("Remaining Days: " + result.getRemainingDays());
        System.out.println("Safe Daily Limit: " + result.getSafeDailyLimit());
    }
}

