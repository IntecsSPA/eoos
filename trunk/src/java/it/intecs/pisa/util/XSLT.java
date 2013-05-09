/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util;

import java.io.File;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

public class XSLT {

  public static void addParameter(Document xsl, Hashtable parameters) {
    Enumeration keys = parameters.keys();
    String key;
    while (keys.hasMoreElements()) {
      key = (String) keys.nextElement();
      addParameter(xsl, key, (String) parameters.get(key));
    }
  }

  public static void addParameter(Document xsl, String parameterName, String parameterValue) {
    Element paramElement = xsl.createElementNS("http://www.w3.org/1999/XSL/Transform", "xsl:param");
    paramElement.setAttribute("name", parameterName);
    paramElement.appendChild(xsl.createTextNode(parameterValue));
    Element rootElement = xsl.getDocumentElement();
    
    LinkedList children=DOMUtil.getChildrenByTagName(rootElement,"xsl:import");
    
    if(children.size()==0)
        rootElement.insertBefore(paramElement, rootElement.getFirstChild());
    else
        rootElement.insertBefore(paramElement,((Element)children.getLast()).getNextSibling());
  }

  public static void transform(Source xsl, Source input, Result output) throws Exception {
    TransformerFactory.newInstance().newTransformer(xsl).transform(input, output);
  }
  
  public static void transform(Source xsl, Source input, Result output,HashMap<String,String> vars) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);
        for(String key:vars.keySet())
        {
            transformer.setParameter(key, vars.get(key));
        }
        transformer.transform(input, output);
  }
  
  public static void transform(Source xsl, Source input, Result output,String outputFolder) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer(xsl);
        transformer.setParameter("output_folder", outputFolder);
        transformer.transform(input, output);
  }

  public static void serialize(Document input, StreamResult output) throws Exception {
    TransformerFactory.newInstance().newTransformer().transform(new DOMSource(input), output);
  }
  
  public static void serialize(Document input, StreamResult output, boolean omitXMLDecl, boolean indent) throws Exception {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXMLDecl ? "yes" : "no");
    transformer.transform(new DOMSource(input), output);
  }
  public static void serialize(Document input, StreamResult output, boolean omitXMLDecl) throws Exception {
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXMLDecl ? "yes" : "no");
    transformer.transform(new DOMSource(input), output);
  }
  

}
