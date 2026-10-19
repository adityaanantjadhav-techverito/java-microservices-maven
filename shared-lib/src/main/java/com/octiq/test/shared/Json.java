package com.octiq.test.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Json {
    private Json() { }

    public static String array(List<String> items) {
        return items.stream()
                .map(s -> "\"" + s.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    public static List<String> parseArray(String body) {
        String s = body == null ? "" : body.trim();
        if (s.startsWith("[")) {
            return parseStrings(s);
        }
        List<String> items = new ArrayList<>();
        String item = parseItem(s);
        if (item != null) {
            items.add(item);
        }
        return items;
    }

    public static String parseItem(String body) {
        if (body == null) {
            return null;
        }
        String s = body.trim();
        if (s.isEmpty()) {
            return null;
        }
        if (s.startsWith("\"")) {
            List<String> values = parseStrings(s);
            return values.isEmpty() ? null : values.get(0);
        }
        if (s.startsWith("{")) {
            int key = s.indexOf("\"id\"");
            if (key >= 0) {
                int colon = s.indexOf(':', key + 4);
                if (colon >= 0) {
                    List<String> values = parseStrings(s.substring(colon + 1));
                    if (!values.isEmpty()) {
                        return values.get(0);
                    }
                }
            }
            List<String> values = parseStrings(s);
            return values.isEmpty() ? null : values.get(0);
        }
        return s;
    }

    private static List<String> parseStrings(String s) {
        List<String> values = new ArrayList<>();
        int i = 0;
        while (i < s.length()) {
            if (s.charAt(i) != '"') {
                i++;
                continue;
            }
            StringBuilder sb = new StringBuilder();
            i++;
            while (i < s.length() && s.charAt(i) != '"') {
                char c = s.charAt(i);
                if (c == '\\' && i + 1 < s.length()) {
                    i++;
                    char escaped = s.charAt(i);
                    if (escaped == 'n') {
                        sb.append('\n');
                    } else if (escaped == 't') {
                        sb.append('\t');
                    } else if (escaped == 'r') {
                        sb.append('\r');
                    } else {
                        sb.append(escaped);
                    }
                } else {
                    sb.append(c);
                }
                i++;
            }
            values.add(sb.toString());
            i++;
        }
        return values;
    }
}
