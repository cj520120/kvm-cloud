package cn.chenjun.cloud.agent.util;

import cn.chenjun.cloud.common.bean.Graphics;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.StringReader;

/**
 * @author chenjun
 */
public final class GraphicsUtil {

    private GraphicsUtil() {

    }


    public static Graphics getGraphics(String xml) throws DocumentException, SAXException {
        try (StringReader sr = new StringReader(xml)) {
            SAXReader reader = new SAXReader();
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = reader.read(sr);
            String path = "/domain/devices/graphics";
            Element node = (Element) doc.selectSingleNode(path);
            String type = node.attribute("type").getValue();
            int port = Integer.parseInt(node.attribute("port").getValue());
            return Graphics.builder().protocol(type).port(port).build();
        }
    }
}
