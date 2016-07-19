/**
 * @author Jackie
 */

package com.jing0.MassMailing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;

public class CsvParser {

    private String fileContent;
    private String separator;
    private HashMap<String, String> map = new HashMap<String, String>();

    public CsvParser(String fileContent) {
        this.setSeparator(",( )*");
        this.fileContent = fileContent;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    private void parse() {
        String[] dataRecords = fileContent.split("\n");
        for (String record : dataRecords) {
            String[] recordFields = record.split(separator);
            map.put(recordFields[0], recordFields[1]);
        }
    }

    public Document parseToXML() {
        this.parse();
        Document doc;
        Element root;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            root = doc.createElement("contact");
            doc.appendChild(root);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Element element;
        for (String name : map.keySet()) {
            element = doc.createElement("contact-item");
            element.setAttribute("name", name);
            element.setAttribute("email", map.get(name));
            root.appendChild(element);
        }

        return doc;
    }
}
