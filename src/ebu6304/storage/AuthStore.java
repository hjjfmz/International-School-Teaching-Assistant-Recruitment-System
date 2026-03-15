package ebu6304.storage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class AuthStore {
    private AuthStore() {}

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
            out.add(new User(e.getAttribute("role"), e.getAttribute("account"), e.getAttribute("password"), e.getAttribute("name"), enabled));
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
        return p.equals(u.get().password());
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
        found.setAttribute("password", user.password());
        found.setAttribute("name", user.name());
        found.setAttribute("enabled", user.enabled() ? "true" : "false");

        try {
            XmlStore.write(adminSystemXml, doc);
        } catch (Exception e) {
        }
    }

    public static boolean setEnabled(Path adminSystemXml, String role, String account, boolean enabled) {
        Optional<User> u = findUser(adminSystemXml, role, account);
        if (!u.isPresent()) return false;
        upsertUser(adminSystemXml, new User(u.get().role(), u.get().account(), u.get().password(), u.get().name(), enabled));
        return true;
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
