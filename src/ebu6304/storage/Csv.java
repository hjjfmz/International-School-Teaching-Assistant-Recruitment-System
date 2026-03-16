package ebu6304.storage;

import java.util.ArrayList;
import java.util.List;

public final class Csv {
    private Csv() {}

    public static String[] splitLine(String line, int expectedFields) {
        List<String> out = new ArrayList<String>();
        if (line == null) line = "";

        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else if (c == '"') {
                    inQuotes = true;
                } else {
                    cur.append(c);
                }
            }
        }
        out.add(cur.toString());

        while (out.size() < expectedFields) out.add("");

        if (out.size() > expectedFields) {
            List<String> trimmed = out.subList(0, expectedFields - 1);
            StringBuilder last = new StringBuilder(out.get(expectedFields - 1));
            for (int i = expectedFields; i < out.size(); i++) {
                last.append(",").append(out.get(i));
            }
            List<String> merged = new ArrayList<String>(trimmed);
            merged.add(last.toString());
            return merged.toArray(new String[0]);
        }

        return out.toArray(new String[0]);
    }

    public static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) s = "";
        boolean mustQuote = s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
        if (!mustQuote) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
