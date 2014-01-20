/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.intecs.pisa.util.schemas;

import it.intecs.pisa.log.Log;
import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.util.DOMUtil;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author massi
 */
public class SchemasUtil {

    public static String SCHEMA_LANGUAGE =
            "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
            XML_SCHEMA =
            "http://www.w3.org/2001/XMLSchema",
            SCHEMA_SOURCE =
            "http://java.sun.com/xml/jaxp/properties/schemaSource";
    
    protected static final HashMap<File, DocumentBuilder> parser = new HashMap<File, DocumentBuilder>();
    protected static final HashMap<File, SAXParser> saxparser = new HashMap<File, SAXParser>();

    public static synchronized boolean validate(Document doc, File schemaFile) {
        DocumentBuilder validatingParser = null;
        try {
            if (parser.containsKey(schemaFile)) {
                validatingParser = parser.get(schemaFile);
            } else {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(true);
                factory.setAttribute(SCHEMA_LANGUAGE, XML_SCHEMA);
                factory.setAttribute(SCHEMA_SOURCE, schemaFile);

                validatingParser = factory.newDocumentBuilder();
                parser.put(schemaFile, validatingParser);
            }
            validatingParser.parse(DOMUtil.getDocumentAsInputStream(doc));

            return true;
        } catch (Exception e) {
            Log.info("File is not valid. Details: " + e.getMessage());
            return false;
        }
    }
    
    public static synchronized boolean validate(InputStream in, File schemaFile) {
        DocumentBuilder validatingParser = null;
        try {
            if (parser.containsKey(schemaFile)) {
                validatingParser = parser.get(schemaFile);
            } else {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(true);
                factory.setAttribute(SCHEMA_LANGUAGE, XML_SCHEMA);
                factory.setAttribute(SCHEMA_SOURCE, schemaFile);

                validatingParser = factory.newDocumentBuilder();
                
                parser.put(schemaFile, validatingParser);
            }
            validatingParser.parse(new InputSource(in));

            return true;
        } catch (Exception e) {
            Log.info("File is not valid. Details: " + e.getMessage());
            return false;
        }
    }
    
    public static synchronized boolean SAXvalidate(InputStream in, File schemaFile,File schemaFolderRoot,SchemaCache cache) {
        SAXParser validatingParser = null;
        try {
            if (saxparser.containsKey(schemaFile)) {
                validatingParser = saxparser.get(schemaFile);
            } else {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(true);
                factory.setFeature("http://xml.org/sax/features/use-entity-resolver2", true);
                validatingParser = factory.newSAXParser();
                validatingParser.setProperty(SCHEMA_LANGUAGE, XML_SCHEMA);
                validatingParser.setProperty(SCHEMA_SOURCE, schemaFile);
                
                saxparser.put(schemaFile, validatingParser);
            }
            
            if(cache==null)
                cache=new SchemaCache(schemaFolderRoot);
            
            SchemaDefaultHandler handler = new SchemaDefaultHandler(cache);
            validatingParser.parse(in, handler,schemaFolderRoot.getAbsolutePath());

            return handler.isValid;
        } catch (Exception e) {
            Log.info("File is not valid. Details: " + e.getMessage());
            return false;
        }
    }

    public static boolean SAXvalidate(InputStream in, AbstractFilesystem schemaFile, AbstractFilesystem schemaRoot, SchemaCache schemaCache) {
        SAXParser validatingParser = null;
        
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            factory.setFeature("http://xml.org/sax/features/use-entity-resolver2", true);
            validatingParser = factory.newSAXParser();
            validatingParser.setProperty(SCHEMA_LANGUAGE, XML_SCHEMA);
            validatingParser.setProperty(SCHEMA_SOURCE, schemaFile);
            
            if(schemaCache==null)
                schemaCache=new SchemaCache(schemaRoot);
            
            SchemaDefaultHandler handler = new SchemaDefaultHandler(schemaCache);
            validatingParser.parse(in, handler,schemaRoot.getAbsolutePath());

            return handler.isValid;
        }
        catch(Exception e)
        {
            return false;
        }
    }
}
