/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.saxon;
import it.intecs.pisa.util.DOMUtil;
import it.intecs.pisa.util.IOUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.*;
import net.sf.saxon.om.NamespaceConstant;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.xpath.XPathEvaluator;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrea Marongiu
 */
public class SaxonDocument {

    private String stringDoc=null;
    private XPathFactoryImpl xpf=null;
 
    
    private SaxonNamespaceResolver namespaceResolver=null;
  

    public SaxonDocument(URL xmlURL) throws IOException, SaxonApiException, Exception{
        InputSource is = new InputSource(xmlURL.openStream());
        this.stringDoc=IOUtil.inputToString(is.getByteStream());
        this.saxonDocumentInit();
    }

    public SaxonDocument(String xml) throws SaxonApiException, XPathFactoryConfigurationException{
        this.stringDoc=xml;
        this.saxonDocumentInit();
    }

    public SaxonDocument(Document domDoc) throws SaxonApiException, Exception{
        this.stringDoc=DOMUtil.getDocumentAsString(domDoc);
        this.saxonDocumentInit();
    }

    public SaxonDocument(InputStream xmlInputStream) throws SaxonApiException, IOException, Exception{
        InputSource is = new InputSource(xmlInputStream);
        this.stringDoc=IOUtil.inputToString(is.getByteStream());
        this.saxonDocumentInit();
    }

   private void saxonDocumentInit() throws SaxonApiException, XPathFactoryConfigurationException{
        this.namespaceResolver=new SaxonNamespaceResolver();
        System.setProperty("javax.xml.xpath.XPathFactory:"+
                     NamespaceConstant.OBJECT_MODEL_SAXON,
                    "net.sf.saxon.xpath.XPathFactoryImpl");  
        this.xpf=(XPathFactoryImpl) XPathFactoryImpl.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
   }

    /**
     * 
     */
    public String getRootNameSpace() throws XPathFactoryConfigurationException, XPathException, XPathExpressionException {
        XPath xpe = xpf.newXPath();
        NodeInfo doc = ((XPathEvaluator) xpe).setSource(this.getSAXSource());
        javax.xml.xpath.XPathExpression rootXpath = xpe.compile("*");
        NodeInfo root=(NodeInfo)rootXpath.evaluate(doc, XPathConstants.NODE);
        return root.getURI();
    }


   public Object evaluatePath(String xpath, QName nameType) throws XPathFactoryConfigurationException, XPathException, XPathExpressionException, IOException, SAXException {
        XPath xpe = xpf.newXPath();
        xpe.setNamespaceContext(this.namespaceResolver);
        //NodeInfo doc = ((XPathEvaluator)xpe).setSource(this.getSAXSource());
        DOMUtil du= new DOMUtil();
        Document doc=du.stringToDocument(this.stringDoc);
        XPathExpression xPathExp =xpe.compile(xpath);
        return xPathExp.evaluate(doc, nameType);
    }


    public void declareXPathNamespace(String prefix, String uri){
        this.namespaceResolver.addNamespace(prefix, uri);
    }

    /**
     * @return the saxDoc
     */
    private SAXSource getSAXSource() {
       // System.out.println("this.stringDoc: " + this.stringDoc);
        return(new SAXSource(new InputSource(new StringReader(this.stringDoc))));
    }

    private DOMSource getDOMSource() throws IOException, SAXException {
       // System.out.println("this.stringDoc: " + this.stringDoc);
        DOMUtil du= new DOMUtil();
        Node n=du.stringToDocument(this.stringDoc);
        return(new DOMSource(n));
    }

    /**
     * @return the stringDoc
     */
    public String getXMLDocumentString() {
        return stringDoc;
    }

}
