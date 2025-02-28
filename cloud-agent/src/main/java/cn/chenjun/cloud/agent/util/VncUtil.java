package cn.chenjun.cloud.agent.util;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.StringReader;

/**
 * @author chenjun
 */
public final class VncUtil {

    private VncUtil() {

    }


    public static int getVnc(String xml) throws DocumentException, SAXException {
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            String path = "/domain/devices/graphics";
            Element node = (Element) doc.selectSingleNode(path);
            return Integer.parseInt(node.attribute("port").getValue());
        }
    }
}
