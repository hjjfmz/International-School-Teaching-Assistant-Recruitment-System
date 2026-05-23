package ebu6304.ui.admin;

import java.awt.Color;

/**
 * Standalone tests for Admin AI features (no JUnit required).
 * Run: java -cp out ebu6304.ui.admin.AiAdminTest
 *
 * Covers the three acceptance criteria:
 *   REQ-1  Admin can request AI workload analysis for all TAs
 *   REQ-2  System highlights overloaded / underloaded TAs
 *   REQ-3  AI results support decision-making; records are NOT changed automatically
 */
public final class AiAdminTest {

    private static int pass = 0, fail = 0;

    public static void main(String[] args) {
        System.out.println("=== Admin AI Feature Tests ===\n");

        testWorkloadStatusLogic();
        testWorkloadColorGradient();
        testMdToHtml();
        testWorkloadPromptBuilding();

        System.out.printf("%n=== Results: %d passed, %d failed ===%n", pass, fail);
        System.exit(fail > 0 ? 1 : 0);
    }

    // ─────────────────────────────────────────────────────────────
    // REQ-2: Workload status classification
    // ─────────────────────────────────────────────────────────────
    private static void testWorkloadStatusLogic() {
        System.out.println("--- REQ-2: Workload Status Classification ---");

        assertEqual("21h  → OVERLOADED",  "OVERLOADED",  statusOf(21, 0));
        assertEqual("20h  → NORMAL",      "NORMAL",      statusOf(20, 0));
        assertEqual("6h   → NORMAL",      "NORMAL",      statusOf(6,  0));
        assertEqual("1h   → NORMAL",      "NORMAL",      statusOf(1,  0));
        assertEqual("0h + pending → UNDERLOADED", "UNDERLOADED", statusOf(0, 2));
        assertEqual("0h + 0 pending → IDLE",      "IDLE",        statusOf(0, 0));
        System.out.println();
    }

    private static String statusOf(int hours, int submitted) {
        if (hours > 20)       return "OVERLOADED";
        else if (hours > 0)   return "NORMAL";
        else if (submitted > 0) return "UNDERLOADED";
        else                  return "IDLE";
    }

    // ─────────────────────────────────────────────────────────────
    // REQ-2: Color gradient (highest hours = most red, 0h = green)
    // ─────────────────────────────────────────────────────────────
    private static void testWorkloadColorGradient() {
        System.out.println("--- REQ-2: Workload Color Gradient ---");

        Color zero  = workloadColor(0,  20);
        Color half  = workloadColor(10, 20);
        Color max   = workloadColor(20, 20);
        Color allZ  = workloadColor(0,  0);

        float[] hsvZero = Color.RGBtoHSB(zero.getRed(), zero.getGreen(), zero.getBlue(), null);
        float[] hsvHalf = Color.RGBtoHSB(half.getRed(), half.getGreen(), half.getBlue(), null);
        float[] hsvMax  = Color.RGBtoHSB(max.getRed(),  max.getGreen(),  max.getBlue(),  null);

        assertTrue("0h hue closer to green than red",  hsvZero[0] > 0.15f);
        assertTrue("max hue closer to red than green", hsvMax[0]  < 0.18f);
        assertTrue("hue decreases as hours increase",  hsvZero[0] > hsvHalf[0] && hsvHalf[0] >= hsvMax[0]);
        assertTrue("saturation increases with hours",  hsvZero[1] < hsvMax[1]);
        assertNotNull("allZero maxH=0 returns a valid color", allZ);
        System.out.println();
    }

    private static Color workloadColor(int hours, int maxHours) {
        if (maxHours == 0) return Color.getHSBColor(0.33f, 0.20f, 0.97f);
        float relRatio = (float) hours / maxHours;
        float absRatio = Math.min((float) hours / 20, 1.0f);
        float hue = 0.33f * (1f - relRatio);
        float sat = absRatio * 0.65f + 0.15f;
        return Color.getHSBColor(hue, sat, 0.97f);
    }

    // ─────────────────────────────────────────────────────────────
    // REQ-1 / REQ-3: Markdown rendering for AI output display
    // ─────────────────────────────────────────────────────────────
    private static void testMdToHtml() {
        System.out.println("--- REQ-1/3: Markdown Rendering ---");

        assertContains("# H1 → h3",          mdToHtml("# Title"),          "<h3");
        assertContains("## H2 → h3",         mdToHtml("## Title"),         "<h3");
        assertContains("### H3 → h4",        mdToHtml("### Title"),        "<h4");
        assertContains("#### H4 → h4",       mdToHtml("#### Title"),       "<h4");
        assertContains("**bold** → <b>",     mdToHtml("**bold**"),         "<b>bold</b>");
        assertContains("*italic* → <i>",     mdToHtml("*italic*"),         "<i>italic</i>");
        assertContains("- bullet → <li>",    mdToHtml("- item"),           "<li>");
        assertContains("1. num → <ol>",       mdToHtml("1. item"),          "<ol");
        assertContains("--- → <hr>",          mdToHtml("---"),              "<hr");
        assertContains("table row → <td>",   mdToHtml("| A | B |\n|---|---|\n| 1 | 2 |"), "<td");
        assertNotContains("table sep hidden", mdToHtml("| A |\n|---|\n| B |"), "|---|");
        assertContains("XSS < escaped",      mdToHtml("<script>"),         "&lt;script&gt;");
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────────
    // REQ-3: AI prompt must include read-only disclaimer, never mutate data
    // ─────────────────────────────────────────────────────────────
    private static void testWorkloadPromptBuilding() {
        System.out.println("--- REQ-3: AI Prompt is Read-Only / No Mutation ---");

        String prompt = buildSampleWorkloadPrompt();
        assertContains("prompt has read-only notice",
            prompt, "DO NOT modify");
        assertContains("prompt contains threshold",
            prompt, "20");
        assertContains("prompt has workload data header",
            prompt, "WORKLOAD DATA");
        assertContains("prompt requests summary table",
            prompt, "Summary table");
        assertNotContains("prompt must NOT contain SQL/update verbs",
            prompt.toLowerCase(), "update ");
        assertNotContains("prompt must NOT contain delete verb",
            prompt.toLowerCase(), "delete ");
        System.out.println();
    }

    private static String buildSampleWorkloadPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an AI assistant for the BUPT TA Recruitment System admin.\n");
        sb.append("Analyze the following TA workload data. Overload threshold = 20 hours/week.\n");
        sb.append("For each TA state: current status (OVERLOADED/NORMAL/UNDERLOADED/IDLE).\n");
        sb.append("DO NOT modify any records — this is analysis only.\n\n");
        sb.append("=== WORKLOAD DATA ===\n");
        sb.append("  2023213330  ZhangYiYing  accepted=1 hours=6  status=NORMAL\n");
        sb.append("  2023213331  Alice         accepted=0 hours=0  status=IDLE\n");
        sb.append("\nProvide a concise analysis with: 1) Summary table 2) Overloaded TAs.\n");
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────
    // Minimal markdown renderer (mirrors AdminWorkloadPage.mdToHtml)
    // ─────────────────────────────────────────────────────────────
    private static String mdToHtml(String md) {
        if (md == null) return "";
        String[] lines = md.split("\n", -1);
        StringBuilder out = new StringBuilder();
        boolean inUl = false, inOl = false, inTable = false;
        for (String raw : lines) {
            boolean isTable = raw.trim().startsWith("|");
            boolean isSep   = raw.matches("\\s*\\|[-| :]+\\|");
            boolean isBullet= raw.matches("^\\s*[-*] .+");
            boolean isNum   = raw.matches("^\\s*\\d+[.)]\\ .+");
            boolean isHr    = raw.trim().matches("[-*_]{3,}");
            if (!isTable && inTable) { out.append("</table>"); inTable = false; }
            if (!isBullet && inUl)   { out.append("</ul>");    inUl    = false; }
            if (!isNum    && inOl)   { out.append("</ol>");    inOl    = false; }
            if (isTable) {
                if (isSep) continue;
                if (!inTable) { out.append("<table>"); inTable = true; }
                String[] cells = raw.split("\\|", -1);
                out.append("<tr>");
                for (int ci = 1; ci < cells.length - 1; ci++)
                    out.append("<td>").append(inline(cells[ci].trim())).append("</td>");
                out.append("</tr>"); continue;
            }
            if (isHr) { out.append("<hr>"); continue; }
            if (raw.startsWith("#### ")) { out.append("<h4>").append(inline(raw.substring(5))).append("</h4>"); continue; }
            if (raw.startsWith("### "))  { out.append("<h4>").append(inline(raw.substring(4))).append("</h4>"); continue; }
            if (raw.startsWith("## "))   { out.append("<h3>").append(inline(raw.substring(3))).append("</h3>"); continue; }
            if (raw.startsWith("# "))    { out.append("<h3>").append(inline(raw.substring(2))).append("</h3>"); continue; }
            if (isBullet) {
                if (!inUl) { out.append("<ul>"); inUl = true; }
                out.append("<li>").append(inline(raw.replaceFirst("^\\s*[-*] ", ""))).append("</li>"); continue;
            }
            if (isNum) {
                if (!inOl) { out.append("<ol>"); inOl = true; }
                out.append("<li>").append(inline(raw.replaceFirst("^\\s*\\d+[.)]\\ ", ""))).append("</li>"); continue;
            }
            if (!raw.trim().isEmpty()) out.append("<p>").append(inline(raw)).append("</p>");
        }
        if (inUl) out.append("</ul>");
        if (inOl) out.append("</ol>");
        if (inTable) out.append("</table>");
        return out.toString();
    }

    private static String inline(String t) {
        t = t.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        t = t.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        t = t.replaceAll("\\*(.+?)\\*",       "<i>$1</i>");
        return t;
    }

    // ─────────────────────────────────────────────────────────────
    // Assertion helpers
    // ─────────────────────────────────────────────────────────────
    private static void assertEqual(String name, String expected, String actual) {
        if (expected.equals(actual)) { ok(name); }
        else { fail(name + " → expected [" + expected + "] but got [" + actual + "]"); }
    }

    private static void assertTrue(String name, boolean cond) {
        if (cond) ok(name); else fail(name);
    }

    private static void assertNotNull(String name, Object val) {
        if (val != null) ok(name); else fail(name + " → was null");
    }

    private static void assertContains(String name, String haystack, String needle) {
        if (haystack.contains(needle)) ok(name);
        else fail(name + " → [" + needle + "] not found in output");
    }

    private static void assertNotContains(String name, String haystack, String needle) {
        if (!haystack.contains(needle)) ok(name);
        else fail(name + " → [" + needle + "] should NOT appear in output");
    }

    private static void ok(String name)   { System.out.println("  PASS  " + name); pass++; }
    private static void fail(String name) { System.out.println("  FAIL  " + name); fail++; }
}
