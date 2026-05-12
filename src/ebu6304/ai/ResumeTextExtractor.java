package ebu6304.ai;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ResumeTextExtractor {
    private ResumeTextExtractor() {}

    public static String extract(Path file) throws IOException {
        if (file == null) return "";
        if (!Files.exists(file)) return "";
        if (Files.isDirectory(file)) return "";

        String name = file.getFileName() == null ? "" : file.getFileName().toString();
        String ext = extLower(name);
        if ("docx".equals(ext)) return extractDocx(file);
        if ("pdf".equals(ext)) return extractPdfBestEffort(file);
        if ("doc".equals(ext)) return extractDocBestEffort(file);

        // fallback: try plain text
        byte[] b = Files.readAllBytes(file);
        return sanitizeText(new String(b, StandardCharsets.UTF_8));
    }

    private static String extractDocx(Path file) throws IOException {
        StringBuilder sb = new StringBuilder();
        ZipInputStream zis = new ZipInputStream(Files.newInputStream(file));
        try {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                String n = e.getName();
                if (n == null) continue;
                // main body + headers/footers (best effort)
                if (!n.startsWith("word/")) continue;
                if (!("word/document.xml".equals(n) || n.startsWith("word/header") || n.startsWith("word/footer"))) continue;

                String xml = readAllToString(zis);
                String text = xmlToText(xml);
                if (!text.trim().isEmpty()) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(text.trim());
                }
            }
        } finally {
            try { zis.close(); } catch (IOException ignore) {}
        }
        return sanitizeText(sb.toString());
    }

    private static String extractDocBestEffort(Path file) throws IOException {
        // .doc is binary. We do a best-effort extraction by scanning for printable ASCII/Latin chunks.
        byte[] b = Files.readAllBytes(file);
        StringBuilder out = new StringBuilder();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xFF;
            char c = (char) v;
            boolean ok = (c >= 32 && c <= 126) || c == '\n' || c == '\r' || c == '\t';
            if (ok) {
                cur.append(c);
            } else {
                flushChunk(cur, out);
            }
        }
        flushChunk(cur, out);
        return sanitizeText(out.toString());
    }

    private static void flushChunk(StringBuilder cur, StringBuilder out) {
        if (cur.length() == 0) return;
        String s = cur.toString();
        cur.setLength(0);
        // ignore very short noise chunks
        if (s.trim().length() < 6) return;
        out.append(s).append("\n");
    }

    private static String extractPdfBestEffort(Path file) throws IOException {
        String text = null;
        try {
            // 尝试使用 PDFBox (方案 A)
            Class<?> pdDocClass = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
            Class<?> stripperClass = Class.forName("org.apache.pdfbox.text.PDFTextStripper");
            
            java.lang.reflect.Method loadMethod = pdDocClass.getMethod("load", java.io.File.class);
            Object pdDoc = loadMethod.invoke(null, file.toFile());
            
            Object stripper = stripperClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method getTextMethod = stripperClass.getMethod("getText", pdDocClass);
            text = (String) getTextMethod.invoke(stripper, pdDoc);
            
            java.lang.reflect.Method closeMethod = pdDocClass.getMethod("close");
            closeMethod.invoke(pdDoc);
            
        } catch (Throwable e) {
            // 运行时如果找不到类，会抛出 NoClassDefFoundError (属于 Throwable)
            System.err.println("PDFBox execution failed (Check if JAR is in Build Path): " + e.getMessage());
        }

        // 强力兜底逻辑
        if (text == null || text.trim().length() < 50) {
            // 如果 PDFBox 拿到的内容太少（可能是扫描件或库未加载），
            // 混合使用暴力字节扫描 + 特殊关键词提取
            byte[] b = Files.readAllBytes(file);
            String fallback = extractPrintableFallback(b);
            if (fallback.length() > (text == null ? 0 : text.length())) {
                text = fallback;
            }
        }

        return text != null ? sanitizeText(text) : "";
    }

    private static int findClosingParen(String s, int open) {
        int depth = 0;
        for (int i = open; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(' && (i == 0 || s.charAt(i-1) != '\\')) depth++;
            else if (c == ')' && (i == 0 || s.charAt(i-1) != '\\')) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static boolean isInsideTjArray(String raw, int pos) {
        // 查找该位置是否被 [] 包围且后面跟着 TJ
        int start = raw.lastIndexOf('[', pos);
        int end = raw.indexOf(']', pos);
        if (start >= 0 && end > pos) {
            String after = raw.substring(end + 1, Math.min(end + 10, raw.length()));
            return after.contains("TJ");
        }
        return false;
    }

    private static String extractPrintableFallback(byte[] b) {
        StringBuilder sb = new StringBuilder();
        StringBuilder cur = new StringBuilder();
        for (byte bb : b) {
            int v = bb & 0xFF;
            if ((v >= 32 && v <= 126) || v == 10 || v == 13 || (v >= 160 && v <= 255)) {
                cur.append((char) v);
            } else {
                if (cur.length() > 10) {
                    sb.append(cur).append("\n");
                }
                cur.setLength(0);
            }
        }
        return sanitizeText(sb.toString());
    }

    private static String unescapePdfString(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case '(' : sb.append('('); break;
                    case ')' : sb.append(')'); break;
                    case '\\': sb.append('\\'); break;
                    default:
                        // octal escapes: \ddd
                        if (n >= '0' && n <= '7') {
                            int j = i;
                            int val = n - '0';
                            int count = 1;
                            while (count < 3 && j + 1 < s.length()) {
                                char o = s.charAt(j + 1);
                                if (o < '0' || o > '7') break;
                                j++;
                                val = (val * 8) + (o - '0');
                                count++;
                            }
                            i = j;
                            sb.append((char) val);
                        } else {
                            sb.append(n);
                        }
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String xmlToText(String xml) {
        if (xml == null) return "";
        // Keep paragraph-ish separators
        String s = xml;
        s = s.replace("</w:p>", "\n");
        s = s.replace("</w:tr>", "\n");
        s = s.replaceAll("<[^>]+>", " ");
        s = unescapeXml(s);
        return sanitizeText(s);
    }

    private static String unescapeXml(String s) {
        if (s == null) return "";
        return s.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
    }

    private static String readAllToString(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) >= 0) {
            if (n == 0) continue;
            bos.write(buf, 0, n);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static String sanitizeText(String s) {
        if (s == null) return "";
        String t = s;
        t = t.replace('\u0000', ' ');
        t = t.replaceAll("[\\t\\r\\n]+", "\n");
        t = t.replaceAll("[ ]+", " ");
        t = t.replaceAll("\\n[ ]+", "\n");
        t = t.replaceAll("[ ]+\\n", "\n");
        return t.trim();
    }

    private static String extLower(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        if (dot < 0) return "";
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
