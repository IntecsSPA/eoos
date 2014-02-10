/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.intecs.pisa.openCatalogue.solr.ingester;

import com.google.gson.JsonObject;
import it.intecs.pisa.log.Log;
import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.util.json.JsonUtil;
import java.util.HashMap;

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
            instance.setIdentifierXPath(info.get("idXPath").getAsString());
            
            return instance; 
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static void init(AbstractFilesystem ingestersRoot) throws Exception {
        if(initPerformed==false)
        {
            for(AbstractFilesystem folder:ingestersRoot.list())
            {
                if(folder.isFile()==false)
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
            if(folder.isFile()==false)
            {
                initSecondLevel(folder,firstLevel);
            }
        }
    }

    private static void initSecondLevel(AbstractFilesystem folder, String firstLevel) throws Exception {
        String format=firstLevel+"/"+folder.getName();
        
        JsonObject obj = JsonUtil.getInputAsJson(folder.get("plugin.json").getInputStream());
        pluginInfo.put(format, obj);
        
        Class clazz=Class.forName(obj.get("className").getAsString());
        matches.put(format, clazz);
        
        
        BaseIngester ingester=(BaseIngester) clazz.newInstance();
        ingester.install(folder);
        Log.debug("Ingester for format "+format+" has been initialized.");
    }
}
