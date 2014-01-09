/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.intecs.pisa.util.schemas;

import it.intecs.pisa.log.ErrorCodes;
import it.intecs.pisa.log.Log;
import java.io.IOException;
import java.util.ArrayList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

/**
 *
 * @author massi
 */
public class SchemaDefaultHandler extends DefaultHandler2 {
    protected ArrayList<String> schema=new ArrayList<String>();
    protected SchemaCache cache;
    public boolean isValid=true;
            
    public SchemaDefaultHandler(SchemaCache schemaCache)
    {
        cache=schemaCache;
    }
    
    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
        try {
            if(schema.contains(systemId)==false)
            {
                //Log.debug("Loading schema baseURI=" + baseURI + " systemId=" + systemId);

                schema.add(systemId);
        
                InputSource schema=cache.get(systemId);
                return schema;
            }
            else return null;
            
           // return schema.get(systemId);
        } catch (Exception e) {
            throw new SAXException("Schema file " + systemId + " not found");
        }
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        Log.error(ErrorCodes.ERROR_CODE_17+" Details:"+e.getMessage());
        isValid=false;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        Log.error(ErrorCodes.ERROR_CODE_17+" Details:"+e.getMessage());
        isValid=false;
    }
}
