package masroofy.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Root persisted state of the application.
 *
 * <p>This object is stored fully in {@code data/app-state.json}. Any state-changing operation should
 * save immediately after updating this object.
 */
public class AppState {
  private Cycle activeCycle;
  private List<Expense> expenses;
  private List<Category> categories;
  private UserSettings settings;
  private long nextExpenseId;

  public AppState() {
    this.expenses = new ArrayList<>();
    this.categories = new ArrayList<>();
    this.settings = new UserSettings();
    this.nextExpenseId = 1L;
  }

  public Cycle getActiveCycle() {
    return activeCycle;
  }

  public void setActiveCycle(Cycle activeCycle) {
    this.activeCycle = activeCycle;
  }

  public List<Expense> getExpenses() {
    return expenses;
  }

  public void setExpenses(List<Expense> expenses) {
    this.expenses = (expenses == null) ? new ArrayList<>() : expenses;
  }

  public List<Category> getCategories() {
    return categories;
  }

  public void setCategories(List<Category> categories) {
    this.categories = (categories == null) ? new ArrayList<>() : categories;
  }

  public UserSettings getSettings() {
    return settings;
  }

  public void setSettings(UserSettings settings) {
    this.settings = (settings == null) ? new UserSettings() : settings;
  }

  public long getNextExpenseId() {
    return nextExpenseId;
  }

  public void setNextExpenseId(long nextExpenseId) {
    this.nextExpenseId = Math.max(1L, nextExpenseId);
  }

  public long allocateExpenseId() {
    long id = nextExpenseId;
    nextExpenseId = id + 1;
    return id;
  }

  /** Ensures default categories exist (first run convenience). */
  public void ensureSeedCategories() {
    if (categories == null) categories = new ArrayList<>();
    if (!categories.isEmpty()) return;
    categories.add(new Category(1, "Food"));
    categories.add(new Category(2, "Transport"));
    categories.add(new Category(3, "Bills"));
    categories.add(new Category(4, "Shopping"));
    categories.add(new Category(5, "Entertainment"));
    categories.add(new Category(6, "Other"));
  }

  public Map<String, Object> toJsonObject() {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("nextExpenseId", nextExpenseId);
    root.put("activeCycle", (activeCycle == null) ? null : activeCycle.toJsonObject());

    List<Object> exp = new ArrayList<>();
    for (Expense e : expenses) exp.add(e.toJsonObject());
    root.put("expenses", exp);

    List<Object> cats = new ArrayList<>();
    for (Category c : categories) cats.add(c.toJsonObject());
    root.put("categories", cats);

    root.put("settings", (settings == null) ? null : settings.toJsonObject());
    return root;
  }

  public static AppState fromJsonObject(Map<String, Object> root) {
    AppState s = new AppState();
    if (root == null) return s;

    Object nId = root.get("nextExpenseId");
    if (nId instanceof Number) s.setNextExpenseId(((Number) nId).longValue());

    Object c = root.get("activeCycle");
    if (c instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> cm = (Map<String, Object>) c;
      s.setActiveCycle(Cycle.fromJsonObject(cm));
    }

    Object exps = root.get("expenses");
    if (exps instanceof List) {
      List<Expense> list = new ArrayList<>();
      for (Object o : (List<?>) exps) {
        if (o instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> em = (Map<String, Object>) o;
          list.add(Expense.fromJsonObject(em));
        }
      }
      s.setExpenses(list);
    }

    Object cats = root.get("categories");
    if (cats instanceof List) {
      List<Category> list = new ArrayList<>();
      for (Object o : (List<?>) cats) {
        if (o instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> cm = (Map<String, Object>) o;
          list.add(Category.fromJsonObject(cm));
        }
      }
      s.setCategories(list);
    }

    Object set = root.get("settings");
    if (set instanceof Map) {
      @SuppressWarnings("unchecked")
      Map<String, Object> sm = (Map<String, Object>) set;
      s.setSettings(UserSettings.fromJsonObject(sm));
    }

    s.ensureSeedCategories();
    return s;
  }
}

