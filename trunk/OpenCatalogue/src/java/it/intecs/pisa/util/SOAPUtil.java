/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author massi
 */
public class SOAPUtil {
    public static final String FAULT_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String FAULT_ROOT_NODE = "Fault";
    public static final String ENVELOPE_ROOT_NODE = "Envelope";
    public static final String BODY_NODE = "Body";
    
    public static boolean isSOAPFault(Document doc)
    {
        Element rootEl;
        String namespace;
        String name;

        try
        {
            rootEl=doc.getDocumentElement();
            namespace=rootEl.getNamespaceURI();
            name=rootEl.getNodeName();

            if(namespace.equals(FAULT_NAMESPACE) && name.endsWith(FAULT_ROOT_NODE))
                return isBosySOAPFault(rootEl);
            else if(namespace.equals(FAULT_NAMESPACE) && name.endsWith(ENVELOPE_ROOT_NODE))
            {
            	Element bodyEl;
            	Element faultEl;
            	
            	bodyEl=DOMUtil.getChildByLocalName(rootEl,BODY_NODE);
            	faultEl=DOMUtil.getFirstChild(bodyEl);
            	return isBosySOAPFault(faultEl);
            }
            else return false;
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
    protected static boolean isBosySOAPFault(Element el)
    {
    	String namespace;
        String name;
        
    	namespace=el.getNamespaceURI();
        name=el.getNodeName();

        if(namespace.equals(FAULT_NAMESPACE) && name.endsWith(FAULT_ROOT_NODE))
            return true;
        else return false;
    }
}
