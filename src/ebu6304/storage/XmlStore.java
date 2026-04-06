package ebu6304.storage;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class XmlStore {
    private XmlStore() {}

    public static void ensureAdminSystemXmlExists(Path xmlFile) throws IOException {
        if (Files.exists(xmlFile)) return;
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            DocumentBuilder b = f.newDocumentBuilder();
            Document doc = b.newDocument();

            Element root = doc.createElement("system");
            doc.appendChild(root);

            Element users = doc.createElement("users");
            root.appendChild(users);

            Element admin = doc.createElement("user");
            admin.setAttribute("role", "Admin");
            admin.setAttribute("account", "admin");
            admin.setAttribute("password", "admin");
            admin.setAttribute("name", "Administrator");
            users.appendChild(admin);

            Element config = doc.createElement("config");
            root.appendChild(config);

            Element workload = doc.createElement("workload");
            root.appendChild(workload);

            write(xmlFile, doc);
        } catch (Exception e) {
            Files.write(xmlFile, "<system></system>".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        }
    }

    public static void write(Path xmlFile, Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        try (OutputStream os = Files.newOutputStream(xmlFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            t.transform(new DOMSource(doc), new StreamResult(os));
        }
    }
}
