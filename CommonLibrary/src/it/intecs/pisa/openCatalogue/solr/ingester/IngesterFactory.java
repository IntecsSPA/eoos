/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.intecs.pisa.openCatalogue.solr.ingester;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.intecs.pisa.log.Log;
import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.util.json.JsonUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class matches input format with ingesters
 * @author Massimilian Fanciulli
 */
public class IngesterFactory {
    protected static final HashMap<String,Class> matches=new HashMap<String,Class>();
    protected static final HashMap<String,JsonObject> pluginInfo=new HashMap<String,JsonObject>();
    
    protected static boolean initPerformed=false;
    
    
    public static BaseIngester fromInputType(String type)
    {
        try
        {
            Class clazz=matches.get(type);
            BaseIngester instance=(BaseIngester) clazz.newInstance();
            
            JsonObject info=pluginInfo.get(type);
            
            Iterator<Map.Entry<String, JsonElement>> entryset = info.entrySet().iterator();
            while(entryset.hasNext())
            {
                setField(instance,entryset.next());
            }
            
            return instance; 
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    private static void setField(BaseIngester instance, Map.Entry<String, JsonElement> entry) {
        String fieldName="Unknown";
        String fieldValue="";
        try
        {
            fieldName=entry.getKey();
            fieldValue=entry.getValue().getAsString();
            
            instance.getClass().getField(fieldName).set(instance, fieldValue);
        }
        catch(Exception e)
        {
            Log.debug("Error while setting field "+fieldName);
        }
    }
    
    public static void init(AbstractFilesystem workspaceRoot) throws Exception {
        if(initPerformed==false)
        {
            AbstractFilesystem ingester=workspaceRoot.get("ingester");
            for(AbstractFilesystem folder: ingester.list())
            {
                if(folder.isFile()==false && folder.getName().startsWith(".")==false)
                {
                   initFirstLevel(folder);
                }
            }
            initPerformed=true;
        }
        
    }
    
    protected static void initFirstLevel(AbstractFilesystem level) throws Exception
    {
        String firstLevel=level.getName();
        
        for(AbstractFilesystem folder:level.list())
        {
            if(folder.isFile()==false && folder.getName().startsWith(".")==false)
            {
                initSecondLevel(folder,firstLevel);
            }
        }
    }

    private static void initSecondLevel(AbstractFilesystem folder, String firstLevel) throws Exception {
        String format=firstLevel+"/"+folder.getName();
        
        Log.debug("Ingester for format "+format+" initialization.");
        JsonObject obj = JsonUtil.getInputAsJson(folder.get("plugin.json").getInputStream());
        pluginInfo.put(format, obj);
        
        Class clazz=Class.forName(obj.get("className").getAsString());
        matches.put(format, clazz);
        
        BaseIngester ingester=fromInputType(format);
        ingester.install(folder.getParent().getParent().getParent());
        Log.debug("Initialization done.");
    }

    
}
