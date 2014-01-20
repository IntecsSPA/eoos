/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.intecs.pisa.util.schemas;

import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.metadata.filesystem.FileFilesystem;
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
    protected static final HashMap<String,SchemaCache> caches=new HashMap<>();
    protected static final HashMap<String,ByteBuffer> cache=new HashMap<>();
    protected AbstractFilesystem folder=null;
    
    public static SchemaCache getCache(String name)
    {
        return caches.get(name);
    }
    
    public static void createCache(String name,AbstractFilesystem schemaFolder)
    {
        caches.put(name, new SchemaCache(schemaFolder));
    }
    
    public SchemaCache(AbstractFilesystem schemaFolder)
    {
       folder=schemaFolder;
    }
    
    public SchemaCache(File schemaFolder)
    {
       folder=new FileFilesystem(schemaFolder);
    }
    
    protected ByteBuffer createEntry(String key) throws Exception {
        //Log.log("CACHE: Creating item "+key);
        AbstractFilesystem schemaFile = folder.get(key);
        
        byte[] byteBuffer=IOUtils.toByteArray(schemaFile.getInputStream());
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
