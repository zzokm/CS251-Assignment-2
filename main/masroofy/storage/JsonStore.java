package masroofy.storage;

import masroofy.model.AppState;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads/saves application JSON to {@link Paths#APP_STATE_JSON}.
 *
 * <h3>Design goals</h3>
 * <ul>
 *   <li>Zero external dependencies (no Gson/Jackson).</li>
 *   <li>Human-readable JSON for easy TA review/debugging.</li>
 *   <li>Atomic save: write to temp file then rename.</li>
 * </ul>
 *
 * <p>Supported JSON types: object, array, string, number, boolean, null.</p>
 */
public final class JsonStore {
  private JsonStore() {}

  /**
   * Loads application state from disk.
   *
   * <p>If the state file is missing or invalid, returns a new state with seeded categories.
   */
  public static AppState loadState() {
    return AppState.fromJsonObject(loadObject());
  }

  /**
   * Saves application state to disk (atomic replace).
   *
   * @param state state to persist
   * @throws IOException if saving fails
   */
  public static void saveState(AppState state) throws IOException {
    if (state == null) throw new IllegalArgumentException("state cannot be null");
    saveObject(state.toJsonObject());
  }

  /**
   * Loads a JSON value from disk.
   *
   * @return parsed JSON root value (typically a {@code Map<String,Object>}); if missing/invalid, returns
   *     an empty object map.
   */
  public static Map<String, Object> loadObject() {
    Path file = Paths.APP_STATE_JSON;
    if (!Files.exists(file)) {
      return new LinkedHashMap<>();
    }

    try {
      String json = Files.readString(file, StandardCharsets.UTF_8);
      Object root = Json.parse(json);
      if (!(root instanceof Map)) return new LinkedHashMap<>();
      @SuppressWarnings("unchecked")
      Map<String, Object> obj = (Map<String, Object>) root;
      return obj;
    } catch (Exception e) {
      return new LinkedHashMap<>();
    }
  }

  /**
   * Saves a JSON root value to disk using an atomic replace strategy.
   *
   * @param root JSON root value (typically a {@code Map<String,Object>})
   * @throws IOException if the save fails
   */
  public static void saveObject(Object root) throws IOException {
    if (root == null) throw new IllegalArgumentException("root cannot be null");

    Files.createDirectories(Paths.DATA_DIR);
    Path target = Paths.APP_STATE_JSON;
    Path tmp = target.resolveSibling(target.getFileName() + ".tmp");

    String json = Json.stringify(root);
    Files.writeString(tmp, json + "\n", StandardCharsets.UTF_8);
    Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
  }

  // ----------------------------
  // Minimal JSON implementation
  // ----------------------------

  /**
   * Minimal JSON utilities (parser + writer), implemented here to avoid external dependencies.
   */
  static final class Json {
    private Json() {}

    static Object parse(String json) {
      if (json == null) throw new IllegalArgumentException("json cannot be null");
      Parser p = new Parser(json);
      Object v = p.parseValue();
      p.skipWs();
      return v;
    }

    static String stringify(Object value) {
      StringBuilder sb = new StringBuilder();
      writeValue(sb, value);
      return sb.toString();
    }

    private static void writeValue(StringBuilder sb, Object v) {
      if (v == null) {
        sb.append("null");
      } else if (v instanceof String) {
        sb.append('"').append(escape((String) v)).append('"');
      } else if (v instanceof Boolean) {
        sb.append(((Boolean) v) ? "true" : "false");
      } else if (v instanceof Number) {
        sb.append(v.toString());
      } else if (v instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> o = (Map<String, Object>) v;
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : o.entrySet()) {
          if (!first) sb.append(',');
          first = false;
          sb.append('"').append(escape(e.getKey())).append('"').append(':');
          writeValue(sb, e.getValue());
        }
        sb.append('}');
      } else if (v instanceof List) {
        @SuppressWarnings("unchecked")
        List<Object> a = (List<Object>) v;
        sb.append('[');
        for (int i = 0; i < a.size(); i++) {
          if (i > 0) sb.append(',');
          writeValue(sb, a.get(i));
        }
        sb.append(']');
      } else {
        sb.append('"').append(escape(v.toString())).append('"');
      }
    }

    private static String escape(String s) {
      StringBuilder out = new StringBuilder();
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        switch (c) {
          case '\\': out.append("\\\\"); break;
          case '"': out.append("\\\""); break;
          case '\n': out.append("\\n"); break;
          case '\r': out.append("\\r"); break;
          case '\t': out.append("\\t"); break;
          default:
            if (c < 0x20) {
              out.append(String.format("\\u%04x", (int) c));
            } else {
              out.append(c);
            }
        }
      }
      return out.toString();
    }

    private static final class Parser {
      private final String s;
      private int i = 0;

      Parser(String s) {
        this.s = s;
      }

      void skipWs() {
        while (i < s.length()) {
          char c = s.charAt(i);
          if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
          else break;
        }
      }

      Object parseValue() {
        skipWs();
        if (i >= s.length()) throw new IllegalArgumentException("Unexpected end of JSON");
        char c = s.charAt(i);
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        if (c == '-' || (c >= '0' && c <= '9')) return parseNumber();
        throw new IllegalArgumentException("Unexpected character at " + i + ": " + c);
      }

      Map<String, Object> parseObject() {
        expect('{');
        skipWs();
        Map<String, Object> o = new LinkedHashMap<>();
        if (peek('}')) {
          i++;
          return o;
        }
        while (true) {
          skipWs();
          String key = parseString();
          skipWs();
          expect(':');
          Object value = parseValue();
          o.put(key, value);
          skipWs();
          if (peek(',')) {
            i++;
            continue;
          }
          expect('}');
          return o;
        }
      }

      List<Object> parseArray() {
        expect('[');
        skipWs();
        List<Object> a = new ArrayList<>();
        if (peek(']')) {
          i++;
          return a;
        }
        while (true) {
          Object v = parseValue();
          a.add(v);
          skipWs();
          if (peek(',')) {
            i++;
            continue;
          }
          expect(']');
          return a;
        }
      }

      String parseString() {
        expect('"');
        StringBuilder out = new StringBuilder();
        while (i < s.length()) {
          char c = s.charAt(i++);
          if (c == '"') return out.toString();
          if (c == '\\') {
            if (i >= s.length()) throw new IllegalArgumentException("Invalid escape at end");
            char e = s.charAt(i++);
            switch (e) {
              case '"': out.append('"'); break;
              case '\\': out.append('\\'); break;
              case '/': out.append('/'); break;
              case 'b': out.append('\b'); break;
              case 'f': out.append('\f'); break;
              case 'n': out.append('\n'); break;
              case 'r': out.append('\r'); break;
              case 't': out.append('\t'); break;
              case 'u':
                if (i + 4 > s.length()) throw new IllegalArgumentException("Invalid unicode escape");
                String hex = s.substring(i, i + 4);
                i += 4;
                out.append((char) Integer.parseInt(hex, 16));
                break;
              default:
                throw new IllegalArgumentException("Invalid escape: \\" + e);
            }
          } else {
            out.append(c);
          }
        }
        throw new IllegalArgumentException("Unterminated string");
      }

      Boolean parseBoolean() {
        if (s.startsWith("true", i)) {
          i += 4;
          return Boolean.TRUE;
        }
        if (s.startsWith("false", i)) {
          i += 5;
          return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Invalid boolean at " + i);
      }

      Object parseNull() {
        if (s.startsWith("null", i)) {
          i += 4;
          return null;
        }
        throw new IllegalArgumentException("Invalid null at " + i);
      }

      Number parseNumber() {
        int start = i;
        if (peek('-')) i++;
        while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
        boolean isFloat = false;
        if (peek('.')) {
          isFloat = true;
          i++;
          while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
        }
        if (peek('e') || peek('E')) {
          isFloat = true;
          i++;
          if (peek('+') || peek('-')) i++;
          while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
        }
        String num = s.substring(start, i);
        try {
          return isFloat ? Double.parseDouble(num) : Long.parseLong(num);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid number: " + num);
        }
      }

      boolean peek(char c) {
        return i < s.length() && s.charAt(i) == c;
      }

      void expect(char c) {
        if (i >= s.length() || s.charAt(i) != c) {
          throw new IllegalArgumentException("Expected '" + c + "' at " + i);
        }
        i++;
      }
    }
  }
}

