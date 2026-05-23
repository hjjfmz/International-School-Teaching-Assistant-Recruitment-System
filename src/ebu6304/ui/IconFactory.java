package ebu6304.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 * Generates simple vector icons via Java2D so the UI never depends on
 * system fonts or Unicode/emoji glyph availability.
 */
public final class IconFactory {

    private IconFactory() {}

    /* ── public entry points ───────────────────────────────── */

    public static ImageIcon bell(int size, Color fg)     { return build(size, fg, IconFactory::drawBell); }
    public static ImageIcon gear(int size, Color fg)     { return build(size, fg, IconFactory::drawGear); }
    public static ImageIcon home(int size, Color fg)     { return build(size, fg, IconFactory::drawHome); }
    public static ImageIcon user(int size, Color fg)     { return build(size, fg, IconFactory::drawUser); }
    public static ImageIcon document(int size, Color fg) { return build(size, fg, IconFactory::drawDocument); }
    public static ImageIcon hammer(int size, Color fg)   { return build(size, fg, IconFactory::drawHammer); }
    public static ImageIcon envelope(int size, Color fg) { return build(size, fg, IconFactory::drawEnvelope); }
    public static ImageIcon check(int size, Color fg)    { return build(size, fg, IconFactory::drawCheck); }
    public static ImageIcon download(int size, Color fg) { return build(size, fg, IconFactory::drawDownload); }
    public static ImageIcon menu(int size, Color fg)     { return build(size, fg, IconFactory::drawMenu); }
    public static ImageIcon shield(int size, Color fg)   { return build(size, fg, IconFactory::drawShield); }
    public static ImageIcon bullet(int size, Color fg)   { return build(size, fg, IconFactory::drawBullet); }
    public static ImageIcon spark(int size, Color fg)    { return build(size, fg, IconFactory::drawSpark); }

    /* ── scaffolding ───────────────────────────────────────── */

    @FunctionalInterface
    private interface Painter { void paint(Graphics2D g, int s); }

    private static ImageIcon build(int size, Color fg, Painter p) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            g.setColor(fg);
            g.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            p.paint(g, size);
        } finally {
            g.dispose();
        }
        return new ImageIcon(img);
    }

    /* ── individual icon painters ──────────────────────────── */

    private static void drawBell(Graphics2D g, int s) {
        float m = s * 0.15f;
        float w = s - 2 * m;
        float cx = s / 2f;
        // bell body
        GeneralPath p = new GeneralPath();
        p.moveTo(m, s * 0.65f);
        p.quadTo(m, s * 0.30f, cx, s * 0.15f);
        p.quadTo(s - m, s * 0.30f, s - m, s * 0.65f);
        p.lineTo(s - m + 1, s * 0.72f);
        p.lineTo(m - 1, s * 0.72f);
        p.closePath();
        g.fill(p);
        // clapper
        g.fillOval((int)(cx - s * 0.07f), (int)(s * 0.74f), (int)(s * 0.14f), (int)(s * 0.14f));
        // top nub
        g.fillOval((int)(cx - s * 0.05f), (int)(s * 0.08f), (int)(s * 0.10f), (int)(s * 0.10f));
    }

    private static void drawGear(Graphics2D g, int s) {
        float cx = s / 2f, cy = s / 2f;
        float outer = s * 0.42f, inner = s * 0.30f;
        int teeth = 8;
        GeneralPath p = new GeneralPath();
        for (int i = 0; i < teeth; i++) {
            double a1 = Math.PI * 2 * i / teeth - Math.PI / teeth * 0.4;
            double a2 = Math.PI * 2 * i / teeth + Math.PI / teeth * 0.4;
            double a3 = Math.PI * 2 * (i + 0.5) / teeth - Math.PI / teeth * 0.4;
            double a4 = Math.PI * 2 * (i + 0.5) / teeth + Math.PI / teeth * 0.4;
            if (i == 0) p.moveTo(cx + outer * Math.cos(a1), cy + outer * Math.sin(a1));
            else        p.lineTo(cx + outer * Math.cos(a1), cy + outer * Math.sin(a1));
            p.lineTo(cx + outer * Math.cos(a2), cy + outer * Math.sin(a2));
            p.lineTo(cx + inner * Math.cos(a3), cy + inner * Math.sin(a3));
            p.lineTo(cx + inner * Math.cos(a4), cy + inner * Math.sin(a4));
        }
        p.closePath();
        g.fill(p);
        // center hole
        Color bg = new Color(0, 0, 0, 0);
        g.setComposite(java.awt.AlphaComposite.Clear);
        float hole = s * 0.16f;
        g.fillOval((int)(cx - hole), (int)(cy - hole), (int)(hole * 2), (int)(hole * 2));
        g.setComposite(java.awt.AlphaComposite.SrcOver);
    }

    private static void drawHome(Graphics2D g, int s) {
        float m = s * 0.12f;
        float cx = s / 2f;
        // roof
        GeneralPath roof = new GeneralPath();
        roof.moveTo(cx, m);
        roof.lineTo(s - m, s * 0.48f);
        roof.lineTo(m, s * 0.48f);
        roof.closePath();
        g.fill(roof);
        // body
        float bx = s * 0.22f, by = s * 0.46f;
        float bw = s * 0.56f, bh = s * 0.42f;
        g.fillRect((int) bx, (int) by, (int) bw, (int) bh);
        // door cutout
        g.setComposite(java.awt.AlphaComposite.Clear);
        float dx = cx - s * 0.08f, dy = s * 0.58f;
        g.fillRect((int) dx, (int) dy, (int)(s * 0.16f), (int)(s * 0.30f));
        g.setComposite(java.awt.AlphaComposite.SrcOver);
    }

    private static void drawUser(Graphics2D g, int s) {
        float cx = s / 2f;
        // head
        float hr = s * 0.18f;
        g.fillOval((int)(cx - hr), (int)(s * 0.12f), (int)(hr * 2), (int)(hr * 2));
        // body
        GeneralPath body = new GeneralPath();
        body.moveTo(s * 0.15f, s * 0.88f);
        body.quadTo(s * 0.15f, s * 0.52f, cx, s * 0.50f);
        body.quadTo(s * 0.85f, s * 0.52f, s * 0.85f, s * 0.88f);
        body.closePath();
        g.fill(body);
    }

    private static void drawDocument(Graphics2D g, int s) {
        float m = s * 0.18f;
        float w = s - 2 * m;
        float h = s - 2 * m + s * 0.06f;
        float fold = s * 0.18f;
        // page with folded corner
        GeneralPath p = new GeneralPath();
        p.moveTo(m, m);
        p.lineTo(m + w - fold, m);
        p.lineTo(m + w, m + fold);
        p.lineTo(m + w, m + h);
        p.lineTo(m, m + h);
        p.closePath();
        g.fill(p);
        // fold triangle
        g.setComposite(java.awt.AlphaComposite.Clear);
        GeneralPath tri = new GeneralPath();
        tri.moveTo(m + w - fold, m);
        tri.lineTo(m + w, m + fold);
        tri.lineTo(m + w - fold, m + fold);
        tri.closePath();
        g.fill(tri);
        g.setComposite(java.awt.AlphaComposite.SrcOver);
        // fold outline
        Color c = g.getColor();
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 120));
        g.draw(tri);
        g.setColor(c);
        // text lines
        g.setComposite(java.awt.AlphaComposite.Clear);
        float lx = m + s * 0.08f, ly = m + s * 0.22f;
        float lw = w * 0.55f, lh = s * 0.04f;
        for (int i = 0; i < 3; i++) {
            g.fillRect((int) lx, (int)(ly + i * s * 0.12f), (int) lw, (int) lh);
        }
        g.setComposite(java.awt.AlphaComposite.SrcOver);
    }

    private static void drawHammer(Graphics2D g, int s) {
        // briefcase icon (for "job")
        float m = s * 0.12f;
        float bx = m, by = s * 0.32f;
        float bw = s - 2 * m, bh = s * 0.50f;
        g.fillRoundRect((int) bx, (int) by, (int) bw, (int) bh, (int)(s * 0.12f), (int)(s * 0.12f));
        // handle
        g.setComposite(java.awt.AlphaComposite.Clear);
        float hx = s * 0.32f, hy = s * 0.14f;
        float hw = s * 0.36f, hh = s * 0.24f;
        g.fillRoundRect((int) hx, (int) hy, (int) hw, (int) hh, (int)(s * 0.10f), (int)(s * 0.10f));
        g.setComposite(java.awt.AlphaComposite.SrcOver);
        Color c = g.getColor();
        g.setStroke(new BasicStroke(Math.max(1.5f, s / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawRoundRect((int) hx, (int) hy, (int) hw, (int) hh, (int)(s * 0.10f), (int)(s * 0.10f));
        // horizontal stripe
        g.setComposite(java.awt.AlphaComposite.Clear);
        g.fillRect((int) bx, (int)(by + bh * 0.35f), (int) bw, (int)(s * 0.05f));
        g.setComposite(java.awt.AlphaComposite.SrcOver);
    }

    private static void drawEnvelope(Graphics2D g, int s) {
        float m = s * 0.12f;
        float w = s - 2 * m;
        float h = s * 0.58f;
        float y0 = s * 0.22f;
        g.fillRect((int) m, (int) y0, (int) w, (int) h);
        // V flap (cut out lighter)
        g.setComposite(java.awt.AlphaComposite.Clear);
        GeneralPath v = new GeneralPath();
        v.moveTo(m, y0);
        v.lineTo(s / 2f, y0 + h * 0.55f);
        v.lineTo(s - m, y0);
        v.closePath();
        g.fill(v);
        g.setComposite(java.awt.AlphaComposite.SrcOver);
        // redraw the V as outline
        Color c = g.getColor();
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 140));
        g.draw(v);
        g.setColor(c);
    }

    private static void drawCheck(Graphics2D g, int s) {
        g.setStroke(new BasicStroke(Math.max(2f, s / 7f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath p = new GeneralPath();
        p.moveTo(s * 0.18f, s * 0.52f);
        p.lineTo(s * 0.40f, s * 0.74f);
        p.lineTo(s * 0.82f, s * 0.26f);
        g.draw(p);
    }

    private static void drawDownload(Graphics2D g, int s) {
        float cx = s / 2f;
        g.setStroke(new BasicStroke(Math.max(1.5f, s / 8f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // arrow shaft
        g.drawLine((int) cx, (int)(s * 0.14f), (int) cx, (int)(s * 0.62f));
        // arrow head
        GeneralPath p = new GeneralPath();
        p.moveTo(s * 0.28f, s * 0.50f);
        p.lineTo(cx, s * 0.70f);
        p.lineTo(s * 0.72f, s * 0.50f);
        g.draw(p);
        // tray
        g.drawLine((int)(s * 0.18f), (int)(s * 0.84f), (int)(s * 0.82f), (int)(s * 0.84f));
    }

    private static void drawMenu(Graphics2D g, int s) {
        g.setStroke(new BasicStroke(Math.max(1.5f, s / 8f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float m = s * 0.20f;
        float r = s - m;
        g.drawLine((int) m, (int)(s * 0.28f), (int) r, (int)(s * 0.28f));
        g.drawLine((int) m, (int)(s * 0.50f), (int) r, (int)(s * 0.50f));
        g.drawLine((int) m, (int)(s * 0.72f), (int) r, (int)(s * 0.72f));
    }

    private static void drawShield(Graphics2D g, int s) {
        float cx = s / 2f;
        GeneralPath p = new GeneralPath();
        p.moveTo(cx, s * 0.10f);
        p.lineTo(s * 0.85f, s * 0.24f);
        p.quadTo(s * 0.82f, s * 0.60f, cx, s * 0.90f);
        p.quadTo(s * 0.18f, s * 0.60f, s * 0.15f, s * 0.24f);
        p.closePath();
        g.fill(p);
    }

    private static void drawBullet(Graphics2D g, int s) {
        float r = s * 0.20f;
        float cx = s / 2f;
        g.fillOval((int)(cx - r), (int)(cx - r), (int)(r * 2), (int)(r * 2));
    }

    private static void drawSpark(Graphics2D g, int s) {
        float cx = s / 2f;
        float cy = s / 2f;
        float outer = s * 0.42f;
        float inner = s * 0.14f;
        int points = 4;
        GeneralPath p = new GeneralPath();
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI / points * i - Math.PI / 2;
            float r = (i % 2 == 0) ? outer : inner;
            float x = cx + (float)(r * Math.cos(angle));
            float y = cy + (float)(r * Math.sin(angle));
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.closePath();
        g.fill(p);
    }
}
