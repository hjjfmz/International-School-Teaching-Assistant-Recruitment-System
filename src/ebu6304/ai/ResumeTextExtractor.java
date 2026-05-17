package ebu6304.ai;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ResumeTextExtractor {
    private ResumeTextExtractor() {}

    public static String extract(Path file) throws IOException {
        if (file == null) return "";
        Path candidate = resolveReadableResumeFile(file);
        if (candidate == null) return "";

        String name = candidate.getFileName() == null ? "" : candidate.getFileName().toString();
        String ext = extLower(name);
        if ("docx".equals(ext)) return extractDocx(candidate);
        if ("pdf".equals(ext)) return extractPdfBestEffort(candidate);
        if ("doc".equals(ext)) return extractDocBestEffort(candidate);

        byte[] b = Files.readAllBytes(candidate);
        return sanitizeText(new String(b, StandardCharsets.UTF_8));
    }

    private static Path resolveReadableResumeFile(Path file) throws IOException {
        if (Files.exists(file) && !Files.isDirectory(file) && Files.size(file) > 0) {
            return file;
        }

        String name = file.getFileName() == null ? "" : file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String stem = dot < 0 ? name : name.substring(0, dot);
        Path dir = file.getParent();
        if (dir == null || stem.isEmpty() || !Files.isDirectory(dir)) return null;

        String[] exts = new String[] { "pdf", "docx", "doc" };
        for (String ext : exts) {
            Path alt = dir.resolve(stem + "." + ext);
            if (Files.exists(alt) && !Files.isDirectory(alt) && Files.size(alt) > 0) {
                return alt;
            }
        }
        return null;
    }

    private static String extractDocx(Path file) throws IOException {
        StringBuilder sb = new StringBuilder();
        ZipInputStream zis = new ZipInputStream(Files.newInputStream(file));
        try {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                String n = e.getName();
                if (n == null) continue;
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
        if (s.trim().length() < 6) return;
        out.append(s).append("\n");
    }

    private static String extractPdfBestEffort(Path file) throws IOException {
        String text = null;
        try {
            text = extractPdfWithPdfBox(file);
        } catch (Throwable e) {
            System.err.println("PDFBox execution failed (Check if JAR is in Build Path): " + e.getMessage());
        }

        if (text == null || text.trim().length() < 50) {
            byte[] b = Files.readAllBytes(file);
            String fallback = extractPrintableFallback(b);
            if (fallback.length() > (text == null ? 0 : text.length())) {
                text = fallback;
            }
        }

        return text != null ? sanitizeText(text) : "";
    }

    private static String extractPdfWithPdfBox(Path file) throws Exception {
        Class<?> pdDocClass = Class.forName("org.apache.pdfbox.pdmodel.PDDocument");
        Class<?> stripperClass = Class.forName("org.apache.pdfbox.text.PDFTextStripper");
        Object pdDoc = null;
        try {
            pdDoc = loadPdfDocument(file, pdDocClass);
            Object stripper = stripperClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method getTextMethod = stripperClass.getMethod("getText", pdDocClass);
            return (String) getTextMethod.invoke(stripper, pdDoc);
        } finally {
            if (pdDoc != null) {
                try {
                    java.lang.reflect.Method closeMethod = pdDocClass.getMethod("close");
                    closeMethod.invoke(pdDoc);
                } catch (ReflectiveOperationException ignore) {}
            }
        }
    }

    private static Object loadPdfDocument(Path file, Class<?> pdDocClass) throws Exception {
        try {
            Class<?> loaderClass = Class.forName("org.apache.pdfbox.Loader");
            java.lang.reflect.Method loadMethod = loaderClass.getMethod("loadPDF", java.io.File.class);
            return loadMethod.invoke(null, file.toFile());
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            java.lang.reflect.Method legacyLoad = pdDocClass.getMethod("load", java.io.File.class);
            return legacyLoad.invoke(null, file.toFile());
        }
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
        if (cur.length() > 10) {
            sb.append(cur).append("\n");
        }
        return sanitizeText(sb.toString());
    }

    private static String xmlToText(String xml) {
        if (xml == null) return "";
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
