/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import org.apache.xerces.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

public class DOMUtil {

    private static final String VALIDATE = "http://xml.org/sax/features/validation";
    private static final String SCHEMA_LOCATION = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";
    private static final String SCHEMA_LOCATION_NS = "http://apache.org/xml/properties/schema/external-schemaLocation";
    private static final String TRUE = "true";
    private static final String ONE = "1";

    public static LinkedList getChildren(Element element) {
        LinkedList result = new LinkedList();
        NodeList children = element.getChildNodes();
        Node child;
        for (int index = 0; index < children.getLength(); index++) {
            child = children.item(index);
            if ((child instanceof Element)) {
                result.add(child);
            }
        }
        return result;
    }

    public static Element getFirstChild(Element element) {
        NodeList children = element.getChildNodes();
        Node child;
        for (int index = 0; index < children.getLength(); index++) {
            child = children.item(index);
            if ((child instanceof Element)) {
                return (Element) child;
            }
        }
        return null;
    }
    
     public static Element getLastChild(Element element) {
        NodeList children = element.getChildNodes();
        Node child;
        Element last=null;
        for (int index = 0; index < children.getLength(); index++) {
            child = children.item(index);
            if ((child instanceof Element)) {
                return last=(Element) child;
            }
        }
        return last;
    }

    public static Element getNextElement(Element element) {
        Node next;

        try {
            next = element.getNextSibling();

            while (next != null && (next instanceof Element) == false) {
                next = next.getNextSibling();
            }
        } catch (Exception e) {
            return null;
        }
        return (Element) next;
    }

    public static Element getChildByTagName(Element element, String tag) {
        NodeList children = element.getChildNodes();
        Node child;
        for (int index = 0; index < children.getLength(); index++) {
            child = children.item(index);
            if ((child instanceof Element) && ((Element) child).getTagName().equals(tag)) {
                return (Element) child;
            }
        }
        return null;
    }

    public static LinkedList getChildrenByTagName(Element element, String tag) {
        LinkedList result = new LinkedList();
        NodeList children = element.getChildNodes();
        Node child;
        for (int index = 0; index < children.getLength(); index++) {
            child = children.item(index);
            if ((child instanceof Element) && ((Element) child).getTagName().equals(tag)) {
                result.add(child);
            }
        }
        return result;
    }

    public static Element getChildByLocalName(Element element, String tag) {
        NodeList children = element.getChildNodes();
        Node child;
        for (int index = 0; index < children.getLength(); index++) {
            child = children.item(index);
            if ((child instanceof Element) && ((Element) child).getLocalName().equals(tag)) {
                return (Element) child;
            }
        }
        return null;
    }

    public static LinkedList getChildrenByLocalName(Element element, String tag) {
        LinkedList result = new LinkedList();
        NodeList children = element.getChildNodes();
        Node child;
        for (int index = 0; index < children.getLength(); index++) {
            child = children.item(index);
            if ((child instanceof Element) && ((Element) child).getLocalName().equals(tag)) {
                result.add(child);
            }
        }
        return result;
    }
    private static final ErrorHandler THROWER_ERROR_HANDLER = new ErrorHandler() {

        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }
    };

    public static ErrorHandler getThrowerErrorHandler() {
        return THROWER_ERROR_HANDLER;
    }

    public static boolean hasElement(Element element, String tag) {
        return element.getElementsByTagName(tag).getLength() > 0;
    }

    public static boolean hasElementNS(Element element, String tag, String ns) {
        return element.getElementsByTagNameNS(ns, tag).getLength() > 0;
    }

    public static String getStringFromElement(Element element) {
        return element.hasChildNodes() ? element.getFirstChild().getNodeValue()
                : "";
    }

    public static Element setTextToElement(Document document, Element element,
            String text) {
        element.appendChild(document.createTextNode(text));
        return element;
    }

    public static boolean hasChildren(Element element) {
        return !getChildren(element).isEmpty();
    }

    public static Element createLeafElement(Document document, String tagName,
            String text) {
        return DOMUtil.setTextToElement(document, document.createElement(tagName), text);
    }

    public static Element createLeafElementNS(Document document, String nsURI,
            String qualifiedName, String text) {
        return DOMUtil.setTextToElement(document, document.createElementNS(
                nsURI, qualifiedName), text);
    }

    public static int getIntFromElement(Element element) {
        return Integer.parseInt(getStringFromElement(element));
    }

    public static boolean getBool(String b) {
        return b.equals(TRUE) || b.equals(ONE);
    }

    public static boolean getBoolFromElement(Element element) {
        return getBool(getStringFromElement(element));
    }

    public static String getStringByTagName(Element element, String tag) {
        return getStringFromElement(getByTagAndIndex(element, tag, 0));
    }

    public static int getIntByTagName(Element element, String tag) {
        return Integer.parseInt(getStringByTagName(element, tag));
    }

    public static Element getByTagAndIndex(Element element, String tag,
            int index) {
        return (Element) element.getElementsByTagName(tag).item(index);
    }

    public static DocumentBuilder getValidatingParser() {
        final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
        final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
        final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(true);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE,
                W3C_XML_SCHEMA);
        //documentBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemaURL);
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(DOMUtil.getThrowerErrorHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return documentBuilder;
    }


    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder documentBuilder;
    private DOMParser parser = new DOMParser();

    public DOMUtil() {
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        parser.setErrorHandler(THROWER_ERROR_HANDLER);
    }

    public DOMUtil(boolean nsAwareness) {
        this();
        setNamespaceAware(nsAwareness);
    }

    public Document newDocument() {
        return documentBuilder.newDocument();
    }

    public static Document newDocument(boolean nsAwareness) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            return documentBuilder.newDocument();
        } catch (Exception e) {
            return null;
        }
    }

    public void setNamespaceAware(boolean awareness) {
        documentBuilderFactory.setNamespaceAware(awareness);
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void setValidating(String schemaLocation) {
        try {
            if (schemaLocation == null) {
                parser.setFeature(VALIDATE, false);
            } else {
                parser.setFeature(VALIDATE, true);
                parser.setProperty(SCHEMA_LOCATION, schemaLocation);
            }
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public void setValidatingNS(String schemaLocation) {
        try {
            if (schemaLocation == null) {
                parser.setFeature(VALIDATE, false);
            } else {
                parser.setFeature(VALIDATE, true);
                parser.setProperty(SCHEMA_LOCATION_NS, schemaLocation);
            }
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public Document fileToDocument(String fileName) throws IOException,
            SAXException {
        // return documentBuilder.parse(fileName);
        synchronized (parser) {
            parser.parse(new InputSource(new FileInputStream(fileName)));
            return parser.getDocument();
        }
    }

    public Document fileToDocument(File file) throws IOException, SAXException {
        synchronized (parser) {
            parser.parse(new InputSource(new FileInputStream(file)));
            return parser.getDocument();
        }
    }

    public Document inputStreamToDocument(InputStream xml) throws IOException,
            SAXException {
        synchronized (parser) {
            parser.parse(new InputSource(xml));
            return parser.getDocument();
        }
    }

    public Document stringToDocument(String xml) throws IOException,
            SAXException {
        // return documentBuilder.parse(new InputSource(new StringReader(xml)));
        synchronized (parser) {
            parser.parse(new InputSource(new StringReader(xml)));
            return parser.getDocument();
        }
    }

    public Document readerToDocument(Reader xml) throws IOException,
            SAXException {
        // return documentBuilder.parse(new InputSource(new StringReader(xml)));
        synchronized (parser) {
            parser.parse(new InputSource(xml));
            return parser.getDocument();
        }
    }

      public static DocumentBuilder getValidatingParser(String schemaURL) {
        final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
        final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
        final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(true);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE,
                W3C_XML_SCHEMA);
        documentBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemaURL);
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(DOMUtil.getThrowerErrorHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return documentBuilder;
    }

    public static DocumentBuilder getValidatingParser(File schema) {
        final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
        final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
        final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(true);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setAttribute(JAXP_SCHEMA_LANGUAGE,
                W3C_XML_SCHEMA);
        documentBuilderFactory.setAttribute(JAXP_SCHEMA_SOURCE, schema);
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(DOMUtil.getThrowerErrorHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return documentBuilder;
    }

    public static void dumpXML(Document document, File xml) throws Exception {
        OutputStream out = new FileOutputStream(xml);
        dumpXML(document, out, false);
        out.close();
    }

    public static void dumpXML(Document document, File xml, boolean indent)
            throws Exception {
        OutputStream out = new FileOutputStream(xml);
        dumpXML(document, out, indent);
        out.close();
    }

    public static void dumpXML(Document document, OutputStream out,
            boolean indent) throws Exception {
        if (indent) {
            DOMUtil.indent(document);
        }
        new XMLSerializer2(out).serialize(document);
    }

    public static Document loadXML(File xml, File schema) throws Exception {
        return getValidatingParser(schema).parse(xml);
    }

    public Document stringToValidatedDocument(File schema, String xml)
            throws Exception {
        /*
         * Document document = stringToDocument(xml); Transformer transformer =
         * TransformerFactory.newInstance().newTransformer();
         * ByteArrayOutputStream out = new ByteArrayOutputStream(); StreamResult
         * res = new StreamResult(out); transformer.transform(new
         * DOMSource(document), res); ByteArrayInputStream in = new
         * ByteArrayInputStream(out.toByteArray()); return
         * getValidatingParser(schema).parse(in);
         */
        return validateDocument(schema, stringToDocument(xml));
    }

    public Document validateDocument(File schema, Document xml)
            throws Exception {
        /*
         * Transformer transformer =
         * TransformerFactory.newInstance().newTransformer();
         * ByteArrayOutputStream out = new ByteArrayOutputStream(); StreamResult
         * res = new StreamResult(out); transformer.transform(new
         * DOMSource(xml), res); ByteArrayInputStream in = new
         * ByteArrayInputStream(out.toByteArray()); return
         * getValidatingParser(schema).parse(in);
         */
        return getValidatingParser(schema).parse(getDocumentAsInputStream(xml));
    }

    public Document validateDocument(File schema, InputStream xml)
            throws Exception {
        /*
         * Transformer transformer =
         * TransformerFactory.newInstance().newTransformer();
         * ByteArrayOutputStream out = new ByteArrayOutputStream(); StreamResult
         * res = new StreamResult(out); transformer.transform(new
         * DOMSource(xml), res); ByteArrayInputStream in = new
         * ByteArrayInputStream(out.toByteArray()); return
         * getValidatingParser(schema).parse(in);
         */
        return getValidatingParser(schema).parse(xml);
    }

    public static InputStream getElementAsInputStream(Element xml)
            throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult res = new StreamResult(out);
        transformer.transform(new DOMSource(xml), res);
        return new ByteArrayInputStream(out.toByteArray());
    }

    public static InputStream getDocumentAsInputStream(Document xml)
            throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult res = new StreamResult(out);
        transformer.transform(new DOMSource(xml), res);
        return new ByteArrayInputStream(out.toByteArray());
    }

    public static String getDocumentAsString(Document xml)
            throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult res = new StreamResult(out);
        transformer.transform(new DOMSource(xml), res);
        return new String(out.toByteArray());
    }

    public static InputStream getNodeAsInputStream(Node node) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult res = new StreamResult(out);
        transformer.transform(new DOMSource(node), res);
        return new ByteArrayInputStream(out.toByteArray());
    }

    public InputStream getDocumentAsInputStream(File schema, Document xml)
            throws Exception {
        return getDocumentAsInputStream(validateDocument(schema, xml));
    }

    public static void fileIndent(String filePath) {
        Document doc = null;
        DOMUtil util = null;
        try {
            util = new DOMUtil();

            doc = util.fileToDocument(filePath);
            DOMUtil.indent(doc);
            DOMUtil.dumpXML(doc, new File(filePath));
        } catch (Exception e) {
        }
    }

    public static void indent(Document document) {
        indent(document.getDocumentElement(), "\n");
    }

    public static void indent(Document document, String indentStr) {
        indent(document.getDocumentElement(), indentStr);
    }

    private static void indent(Element element, String indent) {
        if (getChildren(element).isEmpty()) {
            return;
        }
        NodeList children = element.getChildNodes();
        String deeperIndent = indent + '\t';
        Node node;
        Document document = element.getOwnerDocument();
        for (int index = 0; index < children.getLength(); index++) {
            node = children.item(index);
            if (node.getNodeType() == Node.TEXT_NODE) {
                element.removeChild(node);
                index--;
                continue;
            }
            element.insertBefore(document.createTextNode(deeperIndent), node);
            index++;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                indent((Element) node, deeperIndent);
            }
        }
        element.appendChild(document.createTextNode(indent));
    }

    public static void copyAttributeNodes(Element source, Element target)
            throws Exception {
        NamedNodeMap sourceAttributes = source.getAttributes();
        for (int i = 0; i <= sourceAttributes.getLength() - 1; i++) {
            target.setAttributeNode((Attr) sourceAttributes.item(i).cloneNode(
                    false));
        }
    }

    public static void moveAttributeNodes(Element source, Element target)
            throws Exception {
        NamedNodeMap sourceAttributes = source.getAttributes();
        int numOfAttributes = sourceAttributes.getLength();
        Attr attribute;
        for (int i = numOfAttributes - 1; i >= 0; i--) {
            target.setAttributeNode((Attr) (attribute = (Attr) sourceAttributes.item(i)).cloneNode(false));
            source.removeAttributeNode(attribute);
        }
    }

    public static String getXPath(Element element, Document document) {
        Node currNode = element;
        String xPath = "";
        Node parentNode;
        String currNodeName;
        do {
            currNodeName = currNode.getNodeName();
            parentNode = currNode.getParentNode();
            NodeList children = parentNode.getChildNodes();
            int nodePosition = 0;
            Node tempNode;
            for (int i = 0; i < children.getLength(); i++) {
                if (!((tempNode = children.item(i)) instanceof Element)) {
                    continue;
                }
                if (tempNode.getNodeName().equals(currNodeName)) {
                    nodePosition++;
                }
                if (children.item(i) == currNode) {
                    break;
                }
            }
            xPath = "/" + currNodeName + (nodePosition > 1 ? "[" + String.valueOf(nodePosition) + "]" : "") + xPath;
            currNode = parentNode;
        } while (parentNode != document);
        return xPath;
    }

    public static String getXPathNS(Element element, Document document) {
        Node currNode = element;
        String xPath = "";
        Node parentNode;
        String currNodeName;
        String currNameSpace;
        do {
            currNodeName = currNode.getLocalName();
            currNameSpace = currNode.getNamespaceURI();
            parentNode = currNode.getParentNode();
            NodeList children = parentNode.getChildNodes();
            int nodePosition = 0;
            Node tempNode;
            for (int i = 0; i < children.getLength(); i++) {
                if (!((tempNode = children.item(i)) instanceof Element)) {
                    continue;
                }
                if (tempNode.getLocalName().equals(currNodeName) && tempNode.getNamespaceURI().equals(currNameSpace)) {
                    nodePosition++;
                }
                if (children.item(i) == currNode) {
                    break;
                }
            }
            xPath = "/" + currNameSpace + ":" + currNodeName + (nodePosition > 1 ? "[" + String.valueOf(nodePosition) + "]" : "") + xPath;
            currNode = parentNode;
        } while (parentNode != document);
        return xPath;
    }

    /**
     * @param currentNode
     */
    public static String getFirstChildXPath(Element currentNode,
            Document document) {
        Element child;

        child = DOMUtil.getFirstChild(currentNode);
        return DOMUtil.getXPath(child, document);
    }

    public static Element getElementByXPath(String xpath, Document document) {
        StringTokenizer tokenizer;
        int tokenCount = 0, cardinality = 0, index;
        String tagname;
        Element rootElement;
        Element currentNode = null;
        LinkedList childrenList;

        try {
            tokenizer = new StringTokenizer(xpath, "/");
            tokenCount = tokenizer.countTokens();

            if (tokenCount >= 1) {
                tagname = tokenizer.nextToken();
                rootElement = document.getDocumentElement();

                if (tagname.equals(rootElement.getNodeName()) == false) {
                    return null;
                }
                currentNode = rootElement;
                for (int i = 1; i < tokenCount; i++) {
                    // scan all tokens
                    tagname = tokenizer.nextToken();

                    // checking cardinality
                    if ((index = tagname.indexOf('[')) != -1) {
                        int closedSquareIndex = tagname.indexOf(']');
                        String cardString = tagname.substring(index + 1,
                                closedSquareIndex);

                        cardinality = Integer.parseInt(cardString);

                        // removeing cardinality from tagname
                        tagname = tagname.substring(0, index);

                    } else {
                        cardinality = 1;
                    }
                    childrenList = DOMUtil.getChildrenByTagName(currentNode,
                            tagname);

                    currentNode = (Element) childrenList.get(cardinality - 1);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return currentNode;
    }

    public static boolean isLeaf(Element element) {
        return getChildren(element).size() == 0;
    }

    public static void main(String[] args) throws Exception {
        DOMUtil domUtil;
        Document doc;

        domUtil = new DOMUtil();

        DOMUtil.indent(doc = domUtil.fileToDocument(args[0]));
        DOMUtil.dumpXML(doc, new File(args[1]));
    }

    public Document getCollapsedDocument(Document oldDoc, Document newDoc) {
        if (newDoc.getDocumentElement() != null) {
            newDoc.removeChild(newDoc.getDocumentElement());
        }

        collapse(oldDoc.getDocumentElement(), newDoc, newDoc);
        return newDoc;
    }

    public static void collapse(Element oldNode, Node newNodeParent,
            Document newDoc) {
        Element newNode = (Element) newDoc.importNode(oldNode, false);
        newNodeParent.appendChild(newNode);
        LinkedList oldNodeChildren = getChildren(oldNode);
        Element currOldChild;
        Element currNewChild;
        for (int i = 0; i < oldNodeChildren.size(); i++) {
            if (isLeaf(currOldChild = (Element) oldNodeChildren.get(i))) {
                currNewChild = (Element) newDoc.importNode(currOldChild, true);
                newNode.appendChild(currNewChild);
            } else {
                collapse(currOldChild, newNode, newDoc);
            }
        }
    }

    public static String getTextFromNode(org.w3c.dom.Element parent) {
        NodeList children;
        Node node;

        children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            node = children.item(i);

            if (node.getNodeType() == Node.TEXT_NODE) {
                return node.getNodeValue();
            }
        }

        return null;

    }

    public static void copyNamespaces(Element from, Element to) {
        NamedNodeMap map = null;
        int count = 0;
        Node node = null;
        String name = null;
        String value = null;

        map = from.getAttributes();
        count = map.getLength();

        // cycling over each attribute
        for (int i = 0; i < count; i++) {
            node = map.item(i);
            if (node instanceof Attr) {
                name = node.getNodeName();
                if (name.startsWith("xmlns:")) {
                    value = node.getNodeValue();

                    to.setAttribute(name, value);
                }
            }
        }

    }

    public static void removeSchemaLocation(Element el) {
        NamedNodeMap children;
        Node node;
        Attr attribute;
        String xsi = null;

        children = el.getAttributes();

        for (int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if (node instanceof Attr) {
                attribute = (Attr) node;
                if (attribute.getValue().equals("http://www.w3.org/2001/XMLSchema-instance")) {
                    xsi = attribute.getName();

                   if (xsi.startsWith("xmlns:")) {
                        xsi = xsi.substring(6);
                    }
                }

            }
        }

        if (xsi != null) {
            for (int i = 0; i < children.getLength(); i++) {
                node = children.item(i);
                if (node instanceof Attr) {
                    attribute = (Attr) node;
                    if (attribute.getName().equals(xsi+":schemaLocation")) {
                        attribute.setValue("");
                    }

                }
            }
        }
    }

    public static Document getDocumentFromOutputStream(PipedOutputStream pOutStream)
    {
        try {
            Document doc = null;
            PipedInputStream pInStream;
            DOMUtil util;

            util=new DOMUtil();

            pInStream = new PipedInputStream(pOutStream);

            doc=util.inputStreamToDocument(pInStream);

            return doc;
        } catch (Exception ex) {
            Logger.getLogger(DOMUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static Document getCopyOfDocument(Document doc) throws Exception
    {
        Document newDoc;
        InputStream str;
        DOMUtil util;

        if(doc==null)
            return null;
        
        util=new DOMUtil();
        str=DOMUtil.getDocumentAsInputStream(doc);
        return util.inputStreamToDocument(str);
    }

    public static Document getElementAsNewDocument(Element el) throws Exception
    {
        DOMParser parser=new DOMParser();
        parser.parse(new InputSource(getElementAsInputStream(el)));
        return parser.getDocument();
    }
}

