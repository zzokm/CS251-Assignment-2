package masroofy.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Budget category (e.g., Food, Transport). */
public class Category {
  private int id;
  private String name;

  /** Creates an empty category (used by JSON loading). */
  public Category() {}

  /**
   * Creates a category with id and name.
   *
   * @param id category id
   * @param name display name
   */
  public Category(int id, String name) {
    this.id = id;
    this.name = name;
  }

  /** @return category id */
  public int getId() {
    return id;
  }

  /** @param id category id */
  public void setId(int id) {
    this.id = id;
  }

  /** @return category name */
  public String getName() {
    return name;
  }

  /** @param name category name */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Converts this category to a JSON-friendly object.
   *
   * @return map containing id and name
   */
  public Map<String, Object> toJsonObject() {
    Map<String, Object> o = new LinkedHashMap<>();
    o.put("id", id);
    o.put("name", name);
    return o;
  }

  /**
   * Builds a category from a JSON-friendly object.
   *
   * @param o object map
   * @return parsed category
   */
  public static Category fromJsonObject(Map<String, Object> o) {
    Category c = new Category();
    if (o == null) return c;
    Object id = o.get("id");
    if (id instanceof Number) c.setId(((Number) id).intValue());
    Object name = o.get("name");
    if (name instanceof String) c.setName((String) name);
    return c;
  }
}

