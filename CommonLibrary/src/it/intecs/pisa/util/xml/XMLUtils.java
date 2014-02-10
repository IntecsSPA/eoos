/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util.xml;

import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class XMLUtils {
    
    public static void saveToDisk(Document doc,OutputStream out) throws TransformerConfigurationException, TransformerException
    {
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.transform(new DOMSource(doc), new StreamResult(out));
    }
    
    public static void saveToDisk(Document doc,OutputStream out,boolean indent) throws TransformerConfigurationException, TransformerException
    {
        saveToDisk(doc,out);
    }
    
    public static void saveToDisk(Document doc, File file) throws FileNotFoundException, TransformerConfigurationException, TransformerException {
        saveToDisk(doc,new FileOutputStream(file));
    }
    
    public static Document inputStreamToDocument(InputStream stream) throws SAXException, IOException
    {
         DOMParser parser = new DOMParser();
         parser.parse(new InputSource(stream));
         return parser.getDocument();
    }

    public static Document newDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        return documentBuilder.newDocument();
    }
    
    public static Document newDocumentFromElement(Element toBeImported) throws ParserConfigurationException
    {
        Document doc=newDocument();
        Element importedNode=(Element) doc.importNode(toBeImported, true);
        doc.appendChild(importedNode);
        return doc;
    }

    public static String getNodeTextByXPath(Document doc, String xpath) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpathEn = factory.newXPath();
            return xpathEn.evaluate(xpath, doc);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
    
    
    public static String getNodeTextByXPath(Element eoEl, String xpath) {
        try {
            Document doc=newDocument();
            Element importedEl=(Element) doc.importNode(eoEl.cloneNode(true), true);
            doc.appendChild(importedEl);
            
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpathEn = factory.newXPath();
            return xpathEn.evaluate(xpath, doc);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
    
    public static byte[] dumpToByteArray(Document document) throws TransformerConfigurationException, TransformerException
    {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        StreamResult result=new StreamResult(bos);
        transformer.transform(new DOMSource(document), result);
        byte []array=bos.toByteArray();
        
        return array;
    }
    
    
}
