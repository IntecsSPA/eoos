/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util;

import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SOAPMessageBuilder {
	  private static final String SOAP_SERVICE_NAME = "ServiceName";
	private static final String SOAP_PORT_TYPE = "PortType";
	private static final String SOAP_ADDRESS = "Address";
	private static final String SOAP_REPLY_TO = "ReplyTo";
	private static final String SOAP_MESSAGE_ID = "MessageID";
	public static final String SOAP_ENVELOPE = "Envelope";
	  public static final String SOAP_HEADER = "Header";
	  public static final String SOAP_BODY = "Body";
	  public static final String SOAP_NS_URI = "http://schemas.xmlsoap.org/soap/envelope/";
	  public static final String SOAP_ADDRESSING_URI="http://schemas.xmlsoap.org/ws/2003/03/addressing";
	  private static final String XMLNS = "xmlns";
	  private static final String SOAP_ENV = "soap-env";
	  public static final String WSA_URI = "http://schemas.xmlsoap.org/ws/2003/03/addressing";
	  public static final String SCHEMA_INSTANCE_URI = "www.w3.org/2001/XMLSchema-instance";
	  public static final String SCHEMA_URI = "www.w3.org/2001/XMLSchema";
	  public static final String XSD = "xsd";
	  public static final String XSI = "xsi";
	  public static final String RELATES_TO = "RelatesTo";
	  public static final String WSA = "wsa";
	  public static final String MUST_UNDERSTAND = "mustUnderstand";
	  public static final String ZERO = "0";
	  public static final String TYPE = "type";
	  public static final String STRING = "string";
	  
	 public static Document buildSOAPMessage(Document document,String messageId,String replyToStr) {
		 Document soapDocument;
		 DOMUtil util;
		 Element envelope;
	     Element header;
	     Element relatesToElement;
	     Element payload;
	     Element messageID;
	     Element replyTo;
	     Element address;
	     Element portType;
	     Element serviceName;
	     Element body;
	     Node importedNode;
	     
	     //checking if provided message is already a SOAP message
	     if(isSOAPMessage(document))
	    	 {
	    	 	//setting messageID;
	    	 	setMessageId(document,messageId);
	    	 	return document;
	    	 }
	     
	     util=new DOMUtil();
	     soapDocument=util.newDocument();
	     
	     //creating envelope node
	     envelope = soapDocument.createElementNS(SOAP_NS_URI, SOAP_ENV + ":" + SOAP_ENVELOPE);
	     envelope.setAttribute(XMLNS + ":" + SOAP_ENV, SOAP_NS_URI);
             envelope.setAttribute(XMLNS+":"+WSA,WSA_URI);
	     soapDocument.appendChild(envelope);
	     
	     //creating header
	     header=soapDocument.createElementNS(SOAP_NS_URI, SOAP_ENV + ":" + SOAP_HEADER);
	     envelope.appendChild(header);
	     
	     //creating messageId
	     messageID=soapDocument.createElementNS(WSA_URI, WSA + ":" + SOAP_MESSAGE_ID);
	     header.appendChild(messageID);
	     DOMUtil.setTextToElement(soapDocument,messageID, messageId);
	     
	     replyTo=soapDocument.createElementNS(WSA_URI, WSA + ":" + SOAP_REPLY_TO);
	     header.appendChild(replyTo);
	     
	     address=soapDocument.createElementNS(WSA_URI, WSA + ":" + SOAP_ADDRESS);
	     replyTo.appendChild(address);
	     DOMUtil.setTextToElement(soapDocument,address, replyToStr);
	     
	     portType=soapDocument.createElementNS(WSA_URI, WSA + ":" + SOAP_PORT_TYPE);
	     replyTo.appendChild(portType);
	     DOMUtil.setTextToElement(soapDocument,portType, "ServicePortType");
	     
	     serviceName=soapDocument.createElementNS(WSA_URI, WSA + ":" + SOAP_SERVICE_NAME);
	     replyTo.appendChild(serviceName);
	     DOMUtil.setTextToElement(soapDocument,serviceName, SOAP_SERVICE_NAME);
	     
	     body=soapDocument.createElementNS(SOAP_NS_URI, SOAP_ENV + ":" + SOAP_BODY);
	     envelope.appendChild(body);
	     
	     importedNode=soapDocument.importNode(document.getDocumentElement(), true);
	     body.appendChild(importedNode);
	     
	     return soapDocument;
	    }
		  
     private static void setMessageId(Document document, String messageId) {
		Element headerEl;
		Element messageIdEl;
		Element root;
		Node textNode;
		NodeList children;
						
		root=document.getDocumentElement();
		headerEl=(Element) root.getElementsByTagNameNS(SOAP_NS_URI,SOAP_HEADER).item(0);
		messageIdEl=(Element) headerEl.getElementsByTagNameNS(SOAP_ADDRESSING_URI,SOAP_MESSAGE_ID).item(0);
		
		children=messageIdEl.getChildNodes();
		for(int i=0;i<children.getLength();i++)
		{
			textNode=children.item(i);
			messageIdEl.removeChild(textNode);
		}
		DOMUtil.setTextToElement(document, messageIdEl, messageId);
	}

	public static boolean isSOAPMessage(Document document)
     {
    	Element root;
    	String messageLocalName;
    	String messageNameSpace;
    	try
    	{
    		root=document.getDocumentElement();
    		
    		messageLocalName=root.getLocalName();
    		messageNameSpace=root.getNamespaceURI();
    	
    		if(messageLocalName.equals(SOAP_ENVELOPE) &&
    		   messageNameSpace.equals(SOAP_NS_URI))
    			return true;
    		else return false;
    	}
    	catch(Exception e)
    	{
    	 return false;
    	}
    	 
     }
}
