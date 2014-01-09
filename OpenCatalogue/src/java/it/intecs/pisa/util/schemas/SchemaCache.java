/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.intecs.pisa.util.schemas;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;

/**
 *
 * @author massi
 */
public class SchemaCache {
    protected static final HashMap<String,SchemaCache> caches=new HashMap<String,SchemaCache>();
    protected static final HashMap<String,ByteBuffer> cache=new HashMap<String,ByteBuffer>();
    protected File folder=null;
    
    public static SchemaCache getCache(String name)
    {
        return caches.get(name);
    }
    
    public static void createCache(String name,File schemaFolder)
    {
        caches.put(name, new SchemaCache(schemaFolder));
    }
    
    public SchemaCache(File schemaFolder)
    {
       folder=schemaFolder;
    }
    
    protected ByteBuffer createEntry(String key) throws Exception {
        //Log.log("CACHE: Creating item "+key);
        File schemaFile = new File(folder, key);
        
        byte[] byteBuffer=IOUtils.toByteArray(new FileInputStream(schemaFile));
        ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);
        
        cache.put(key, buffer);
        return buffer;
    }

    public InputSource get(String systemId)  {
       // Log.log("CACHE: Getting item "+systemId);
        
        try
        {
            ByteBuffer buff;
            buff = cache.get(systemId);
            if(buff==null)
            {
                buff=createEntry(systemId);
            }

            return new InputSource(new ByteArrayInputStream(buff.array()));
        }
        catch(Exception e)
        {
            return null;
        }
    }
}
