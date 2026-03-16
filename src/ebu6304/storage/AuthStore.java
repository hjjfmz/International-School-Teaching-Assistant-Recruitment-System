package ebu6304.storage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class AuthStore {
    private AuthStore() {}

    private static final String HASH_PREFIX = "pbkdf2$sha256$";
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    public static final class User {
        private final String role;
        private final String account;
        private final String password;
        private final String name;
        private final boolean enabled;

        public User(String role, String account, String password, String name) {
            this(role, account, password, name, true);
        }

        public User(String role, String account, String password, String name, boolean enabled) {
            this.role = role == null ? "" : role;
            this.account = account == null ? "" : account;
            this.password = password == null ? "" : password;
            this.name = name == null ? "" : name;
            this.enabled = enabled;
        }

        public String role() { return role; }
        public String account() { return account; }
        public String password() { return password; }
        public String name() { return name; }
        public boolean enabled() { return enabled; }
    }

    public static List<User> listUsers(Path adminSystemXml) {
        Document doc = read(adminSystemXml);
        List<User> out = new ArrayList<User>();
        Element root = doc.getDocumentElement();
        if (root == null) return out;

        Element usersEl = firstChildElement(root, "users");
        if (usersEl == null) return out;

        NodeList kids = usersEl.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (!(n instanceof Element)) continue;
            Element e = (Element) n;
            if (!"user".equals(e.getTagName())) continue;
            String en = e.getAttribute("enabled");
            boolean enabled = en == null || en.trim().isEmpty() || "true".equalsIgnoreCase(en);
            String cred = e.getAttribute("passwordHash");
            if (cred == null || cred.trim().isEmpty()) cred = e.getAttribute("password");
            out.add(new User(e.getAttribute("role"), e.getAttribute("account"), cred, e.getAttribute("name"), enabled));
        }
        return out;
    }

    public static Optional<User> findUser(Path adminSystemXml, String role, String account) {
        if (role == null) role = "";
        if (account == null) account = "";
        for (User u : listUsers(adminSystemXml)) {
            if (role.equalsIgnoreCase(u.role()) && account.equals(u.account())) return Optional.of(u);
        }
        return Optional.empty();
    }

    public static boolean authenticate(Path adminSystemXml, String role, String account, String password) {
        Optional<User> u = findUser(adminSystemXml, role, account);
        if (!u.isPresent()) return false;
        if (!u.get().enabled()) return false;
        String p = password == null ? "" : password;
        String stored = u.get().password();
        if (stored != null && stored.startsWith(HASH_PREFIX)) {
            return verifyPbkdf2(stored, p);
        }
        return p.equals(stored);
    }

    public static Optional<String> authenticateAndGetRole(Path adminSystemXml, String account, String password) {
        if (account == null) account = "";
        String p = password == null ? "" : password;
        for (User u : listUsers(adminSystemXml)) {
            if (!account.equals(u.account())) continue;
            if (!u.enabled()) continue;
            String stored = u.password();
            boolean ok;
            if (stored != null && stored.startsWith(HASH_PREFIX)) {
                ok = verifyPbkdf2(stored, p);
            } else {
                ok = p.equals(stored);
            }
            if (ok) return Optional.of(u.role());
        }
        return Optional.empty();
    }

    public static void upsertUser(Path adminSystemXml, User user) {
        if (user == null) return;
        Document doc = read(adminSystemXml);
        Element root = doc.getDocumentElement();
        if (root == null) return;
        Element usersEl = firstChildElement(root, "users");
        if (usersEl == null) {
            usersEl = doc.createElement("users");
            root.appendChild(usersEl);
        }

        Element found = null;
        NodeList kids = usersEl.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (!(n instanceof Element)) continue;
            Element e = (Element) n;
            if (!"user".equals(e.getTagName())) continue;
            if (user.role().equalsIgnoreCase(e.getAttribute("role")) && user.account().equals(e.getAttribute("account"))) {
                found = e;
                break;
            }
        }

        if (found == null) {
            found = doc.createElement("user");
            usersEl.appendChild(found);
        }
        found.setAttribute("role", user.role());
        found.setAttribute("account", user.account());
        String hash = hashPbkdf2(user.password());
        found.setAttribute("passwordHash", hash);
        if (found.hasAttribute("password")) found.removeAttribute("password");
        found.setAttribute("name", user.name());
        found.setAttribute("enabled", user.enabled() ? "true" : "false");

        try {
            XmlStore.write(adminSystemXml, doc);
        } catch (Exception e) {
        }
    }

    public static boolean setEnabled(Path adminSystemXml, String role, String account, boolean enabled) {
        if (role == null) role = "";
        if (account == null) account = "";
        Document doc = read(adminSystemXml);
        Element root = doc.getDocumentElement();
        if (root == null) return false;
        Element usersEl = firstChildElement(root, "users");
        if (usersEl == null) return false;

        NodeList kids = usersEl.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (!(n instanceof Element)) continue;
            Element e = (Element) n;
            if (!"user".equals(e.getTagName())) continue;
            if (role.equalsIgnoreCase(e.getAttribute("role")) && account.equals(e.getAttribute("account"))) {
                e.setAttribute("enabled", enabled ? "true" : "false");
                try {
                    XmlStore.write(adminSystemXml, doc);
                } catch (Exception ex) {
                }
                return true;
            }
        }
        return false;
    }

    public static int migratePlaintextPasswords(Path adminSystemXml) {
        Document doc = read(adminSystemXml);
        Element root = doc.getDocumentElement();
        if (root == null) return 0;
        Element usersEl = firstChildElement(root, "users");
        if (usersEl == null) return 0;

        int changed = 0;
        NodeList kids = usersEl.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (!(n instanceof Element)) continue;
            Element e = (Element) n;
            if (!"user".equals(e.getTagName())) continue;

            String hash = e.getAttribute("passwordHash");
            if (hash != null && !hash.trim().isEmpty()) continue;
            String plain = e.getAttribute("password");
            if (plain == null || plain.trim().isEmpty()) continue;

            e.setAttribute("passwordHash", hashPbkdf2(plain));
            e.removeAttribute("password");
            changed++;
        }

        if (changed > 0) {
            try {
                XmlStore.write(adminSystemXml, doc);
            } catch (Exception e) {
            }
        }
        return changed;
    }

    private static String hashPbkdf2(String password) {
        String p = password == null ? "" : password;
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] dk = pbkdf2(p, salt, PBKDF2_ITERATIONS, KEY_BITS);
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String dkB64 = Base64.getEncoder().encodeToString(dk);
        return HASH_PREFIX + PBKDF2_ITERATIONS + "$" + saltB64 + "$" + dkB64;
    }

    private static boolean verifyPbkdf2(String stored, String password) {
        if (stored == null) return false;
        if (!stored.startsWith(HASH_PREFIX)) return false;
        String rest = stored.substring(HASH_PREFIX.length());
        String[] parts = rest.split("\\$", -1);
        if (parts.length != 3) return false;
        int it;
        try {
            it = Integer.parseInt(parts[0]);
        } catch (NumberFormatException nfe) {
            return false;
        }
        byte[] salt;
        byte[] expected;
        try {
            salt = Base64.getDecoder().decode(parts[1].getBytes(StandardCharsets.UTF_8));
            expected = Base64.getDecoder().decode(parts[2].getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException iae) {
            return false;
        }
        byte[] actual = pbkdf2(password == null ? "" : password, salt, it, expected.length * 8);
        return constantTimeEquals(expected, actual);
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations, int keyBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        int r = 0;
        for (int i = 0; i < a.length; i++) {
            r |= (a[i] ^ b[i]);
        }
        return r == 0;
    }

    public static boolean deleteUser(Path adminSystemXml, String role, String account) {
        if (role == null) role = "";
        if (account == null) account = "";
        Document doc = read(adminSystemXml);
        Element root = doc.getDocumentElement();
        if (root == null) return false;
        Element usersEl = firstChildElement(root, "users");
        if (usersEl == null) return false;

        NodeList kids = usersEl.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (!(n instanceof Element)) continue;
            Element e = (Element) n;
            if (!"user".equals(e.getTagName())) continue;
            if (role.equalsIgnoreCase(e.getAttribute("role")) && account.equals(e.getAttribute("account"))) {
                usersEl.removeChild(e);
                try {
                    XmlStore.write(adminSystemXml, doc);
                } catch (Exception ex) {
                }
                return true;
            }
        }
        return false;
    }

    private static Document read(Path p) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            DocumentBuilder b = f.newDocumentBuilder();
            return b.parse(p.toFile());
        } catch (Exception e) {
            try {
                XmlStore.ensureAdminSystemXmlExists(p);
            } catch (Exception ignored) {
            }
            try {
                DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
                DocumentBuilder b = f.newDocumentBuilder();
                return b.parse(p.toFile());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static Element firstChildElement(Element parent, String tagName) {
        if (parent == null) return null;
        NodeList kids = parent.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (!(n instanceof Element)) continue;
            Element e = (Element) n;
            if (tagName.equals(e.getTagName())) return e;
        }
        return null;
    }
}
