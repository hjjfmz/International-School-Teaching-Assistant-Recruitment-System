package ebu6304.ai.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ebu6304.storage.MiniJson;

public final class AiJsonSupport {
    private AiJsonSupport() {}

    @SuppressWarnings("unchecked")
    public static String extractAssistantContent(String responseJson) throws IOException {
        try {
            Object parsed = MiniJson.parse(responseJson);
            if (!(parsed instanceof Map)) return responseJson;
            Map<String, Object> root = (Map<String, Object>) parsed;
            Object choicesObj = root.get("choices");
            if (!(choicesObj instanceof List)) return responseJson;
            List<Object> choices = (List<Object>) choicesObj;
            if (choices.isEmpty()) return "";
            Object first = choices.get(0);
            if (!(first instanceof Map)) return responseJson;
            Map<String, Object> choice = (Map<String, Object>) first;
            Object msgObj = choice.get("message");
            if (!(msgObj instanceof Map)) return responseJson;
            Map<String, Object> msg = (Map<String, Object>) msgObj;
            Object content = msg.get("content");
            return content == null ? "" : String.valueOf(content);
        } catch (RuntimeException ex) {
            throw new IOException("Failed to parse AI response: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String content) throws IOException {
        try {
            Object parsed = MiniJson.parse(extractJsonObject(content));
            if (!(parsed instanceof Map)) throw new IOException("AI response is not a JSON object");
            return (Map<String, Object>) parsed;
        } catch (RuntimeException ex) {
            throw new IOException("Failed to parse AI JSON: " + ex.getMessage() + ", content=" + content);
        }
    }

    public static String extractJsonObject(String content) {
        String text = content == null ? "" : content.trim();
        if (text.startsWith("```")) {
            int firstLine = text.indexOf('\n');
            if (firstLine >= 0) text = text.substring(firstLine + 1).trim();
            if (text.endsWith("```")) text = text.substring(0, text.length() - 3).trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return text;
    }

    @SuppressWarnings("unchecked")
    public static String extractStreamDelta(String eventJson) throws IOException {
        try {
            Object parsed = MiniJson.parse(eventJson);
            if (!(parsed instanceof Map)) return "";
            Map<String, Object> root = (Map<String, Object>) parsed;
            Object choicesObj = root.get("choices");
            if (!(choicesObj instanceof List)) return "";
            List<Object> choices = (List<Object>) choicesObj;
            if (choices.isEmpty()) return "";
            Object first = choices.get(0);
            if (!(first instanceof Map)) return "";
            Map<String, Object> choice = (Map<String, Object>) first;
            Object deltaObj = choice.get("delta");
            if (!(deltaObj instanceof Map)) return "";
            Map<String, Object> delta = (Map<String, Object>) deltaObj;
            Object content = delta.get("content");
            return content == null ? "" : String.valueOf(content);
        } catch (RuntimeException ex) {
            throw new IOException("Failed to parse AI stream chunk: " + ex.getMessage());
        }
    }

    public static String readString(Map<String, Object> root, String key) {
        if (root == null || key == null) return "";
        Object value = root.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }

    public static int readInt(Map<String, Object> root, String key) {
        if (root == null || key == null) return 0;
        Object value = root.get(key);
        if (value instanceof Number) return ((Number) value).intValue();
        if (value == null) return 0;
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> readStringList(Map<String, Object> root, String key) {
        List<String> out = new ArrayList<String>();
        if (root == null || key == null) return out;
        Object value = root.get(key);
        if (value instanceof List) {
            for (Object item : (List<Object>) value) {
                String text = item == null ? "" : String.valueOf(item).trim();
                if (!text.isEmpty()) out.add(text);
            }
            return out;
        }
        String single = value == null ? "" : String.valueOf(value).trim();
        if (!single.isEmpty()) out.add(single);
        return out;
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> readObjectList(Map<String, Object> root, String key) {
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        if (root == null || key == null) return out;
        Object value = root.get(key);
        if (!(value instanceof List)) return out;
        for (Object item : (List<Object>) value) {
            if (item instanceof Map) {
                out.add(new LinkedHashMap<String, Object>((Map<String, Object>) item));
            }
        }
        return out;
    }
}
