package ebu6304.storage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MiniJson {
    private MiniJson() {}

    public static Object parse(String json) {
        if (json == null) json = "";
        Parser p = new Parser(json);
        Object v = p.readValue();
        p.skipWs();
        return v;
    }

    public static String stringify(Object v) {
        StringBuilder sb = new StringBuilder();
        writeValue(sb, v);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(StringBuilder sb, Object v) {
        if (v == null) {
            sb.append("null");
            return;
        }
        if (v instanceof String) {
            sb.append('"').append(escape((String) v)).append('"');
            return;
        }
        if (v instanceof Number || v instanceof Boolean) {
            sb.append(String.valueOf(v));
            return;
        }
        if (v instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : ((Map<String, Object>) v).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey())).append('"').append(':');
                writeValue(sb, e.getValue());
            }
            sb.append('}');
            return;
        }
        if (v instanceof List) {
            sb.append('[');
            boolean first = true;
            for (Object it : (List<Object>) v) {
                if (!first) sb.append(',');
                first = false;
                writeValue(sb, it);
            }
            sb.append(']');
            return;
        }

        sb.append('"').append(escape(String.valueOf(v))).append('"');
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int k = hex.length(); k < 4; k++) sb.append('0');
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private static final class Parser {
        private final String s;
        private int i;

        private Parser(String s) {
            this.s = s;
            this.i = 0;
        }

        private void skipWs() {
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    i++;
                } else {
                    break;
                }
            }
        }

        private Object readValue() {
            skipWs();
            if (i >= s.length()) return null;
            char c = s.charAt(i);
            if (c == '"') return readString();
            if (c == '{') return readObject();
            if (c == '[') return readArray();
            if (c == 't' || c == 'f') return readBoolean();
            if (c == 'n') return readNull();
            return readNumber();
        }

        private String readString() {
            if (s.charAt(i) != '"') throw new IllegalArgumentException("Invalid JSON string");
            i++;
            StringBuilder out = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c == '\\') {
                    if (i >= s.length()) break;
                    char esc = s.charAt(i++);
                    switch (esc) {
                        case '"': out.append('"'); break;
                        case '\\': out.append('\\'); break;
                        case '/': out.append('/'); break;
                        case 'b': out.append('\b'); break;
                        case 'f': out.append('\f'); break;
                        case 'n': out.append('\n'); break;
                        case 'r': out.append('\r'); break;
                        case 't': out.append('\t'); break;
                        case 'u':
                            if (i + 4 <= s.length()) {
                                String hex = s.substring(i, i + 4);
                                i += 4;
                                out.append((char) Integer.parseInt(hex, 16));
                            }
                            break;
                        default: out.append(esc); break;
                    }
                } else {
                    out.append(c);
                }
            }
            return out.toString();
        }

        private Map<String, Object> readObject() {
            if (s.charAt(i) != '{') throw new IllegalArgumentException("Invalid JSON object");
            i++;
            skipWs();
            Map<String, Object> obj = new LinkedHashMap<String, Object>();
            if (i < s.length() && s.charAt(i) == '}') {
                i++;
                return obj;
            }
            while (i < s.length()) {
                skipWs();
                String key = readString();
                skipWs();
                if (i >= s.length() || s.charAt(i) != ':') throw new IllegalArgumentException("Expected ':'");
                i++;
                Object val = readValue();
                obj.put(key, val);
                skipWs();
                if (i < s.length() && s.charAt(i) == ',') {
                    i++;
                    continue;
                }
                if (i < s.length() && s.charAt(i) == '}') {
                    i++;
                    break;
                }
            }
            return obj;
        }

        private List<Object> readArray() {
            if (s.charAt(i) != '[') throw new IllegalArgumentException("Invalid JSON array");
            i++;
            skipWs();
            List<Object> arr = new ArrayList<Object>();
            if (i < s.length() && s.charAt(i) == ']') {
                i++;
                return arr;
            }
            while (i < s.length()) {
                Object v = readValue();
                arr.add(v);
                skipWs();
                if (i < s.length() && s.charAt(i) == ',') {
                    i++;
                    continue;
                }
                if (i < s.length() && s.charAt(i) == ']') {
                    i++;
                    break;
                }
            }
            return arr;
        }

        private Boolean readBoolean() {
            if (s.startsWith("true", i)) {
                i += 4;
                return Boolean.TRUE;
            }
            if (s.startsWith("false", i)) {
                i += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Invalid JSON boolean");
        }

        private Object readNull() {
            if (s.startsWith("null", i)) {
                i += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid JSON null");
        }

        private Number readNumber() {
            int start = i;
            if (i < s.length() && s.charAt(i) == '-') i++;
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    i++;
                } else {
                    break;
                }
            }
            boolean isDouble = false;
            if (i < s.length() && s.charAt(i) == '.') {
                isDouble = true;
                i++;
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if (c >= '0' && c <= '9') i++; else break;
                }
            }
            if (i < s.length() && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
                isDouble = true;
                i++;
                if (i < s.length() && (s.charAt(i) == '+' || s.charAt(i) == '-')) i++;
                while (i < s.length()) {
                    char c = s.charAt(i);
                    if (c >= '0' && c <= '9') i++; else break;
                }
            }
            String num = s.substring(start, i);
            try {
                if (isDouble) return Double.valueOf(num);
                long l = Long.parseLong(num);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return Integer.valueOf((int) l);
                return Long.valueOf(l);
            } catch (NumberFormatException nfe) {
                return Integer.valueOf(0);
            }
        }
    }
}
