package masroofy.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Budget category (e.g., Food, Transport). */
public class Category {
  private int id;
  private String name;

  public Category() {}

  public Category(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> toJsonObject() {
    Map<String, Object> o = new LinkedHashMap<>();
    o.put("id", id);
    o.put("name", name);
    return o;
  }

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

