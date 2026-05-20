package masroofy.model;

/** Budget category (e.g., Food, Transport). */
public class Category {
  private int id;
  private String name;

  /** Creates an empty category (used when loading from the database). */
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
}
