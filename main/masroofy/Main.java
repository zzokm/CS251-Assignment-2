package masroofy;

import masroofy.model.AppState;
import masroofy.storage.JsonStore;
import masroofy.ui.MenuUI;
import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException {
    AppState state = JsonStore.loadState();
    JsonStore.saveState(state);
    MenuUI menu = new MenuUI();
    menu.start(state);
  }
}