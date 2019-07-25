package org.ing.xpath;

import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ing on 2019-07-21.
 */
public class XPathMain {

    public static void main(String[] args) {
        XPath xPath=  XPathFactory.newInstance().newXPath();
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            InputStream input=XPathMain.class.getResource("/config.xml").openStream();
            Document doc= builder.parse(input);

            Element root= doc.getDocumentElement();
            NodeList nodeList=root.getElementsByTagName("configuration");
            for (int i=0;i<nodeList.getLength();i++){
                Element config = (Element) nodeList.item(i);
                System.out.println(config);
            }



            DeferredElementImpl config= (DeferredElementImpl) evalNode(xPath,doc,"/configuration",XPathConstants.NODE);
            NodeList childs=config.getChildNodes();
            for (int i=0;i<childs.getLength();i++){
                Node element =  nodeList.item(i);
                System.out.println(element.getNodeName());
            }


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    public static Object evalNode(XPath xpath, Object root, String expression, QName returnType) throws XPathExpressionException {
        return xpath.evaluate(expression, root, returnType);
    }
}
