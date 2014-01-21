/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.intecs.pisa.openCatalogue.prefs;

import it.intecs.pisa.log.Log;
import it.intecs.pisa.log.ErrorCodes;
import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.metadata.filesystem.FileFilesystem;
import it.intecs.pisa.openCatalogue.catalogue.ServletVars;
import it.intecs.pisa.util.IOUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author Massimiliano Fanciulli, Andrea Marongiu
 */
public class Prefs {
    public static final String WORKSPACE_DIR="workspace.dir";
    public static final String TRANSFORM_INPUT_DIR="workspace.dir.transform.input";
    public static final String LOAD_INPUT_DIR="workspace.dir.input";
    public static final String LOAD_OUTPUT_DIR="workspace.dir.output";
    public static final String LOAD_PROCESSING_DIR="workspace.dir.processing";
    public static final String LOAD_RETRY_DIR="workspace.dir.retry";
    public static final String LOAD_ERROR_DIR="workspace.dir.error";
     
    public static final String BROWSE_MAX_ITEM_PER_TRANSACTION="browse.ingestion.maxpertransaction";
    
    
    public static final String PUBLISH_LOCAL_FTP_PORT="publish.local.ftp.port";
    public static final String PUBLISH_LOCAL_FTP_IP="publish.local.ftp.ip";
    public static final String DOWNLOAD_DIR="download";
    public static final String STORE_DIR="storeData";
    public static final String NOT_PROCESSED_DIR="notProcessedData";
    public static final String PUBLISH_LOCAL_HTTP_URL="http.public.url";
    public static final String DELETE_AFTER="delete.after";
    public static final String PREFS_PURGE_AFTER="backup.purging.after";
    public static final String PUBLISH_LOCAL_HTTP_ENABLE="publish.local.http.enable";
    public static final String PUBLISH_LOCAL_FTP_ENABLE="publish.local.ftp.enable";
    public static final String PUBLISH_LOCAL_FTP_ROOT_DIR="publish.local.ftp.rootdir";
    public static final String PUBLISH_LOCAL_FOLDER_DIR="publish.local.folder.dirs";
    public static final String PUBLISH_LOCAL_FOLDER_TYPES="publish.local.folder.types";
    public static final String PUBLISH_LOCAL_FOLDER_INTERVAL="publish.local.folder.interval";
    public static final String AUTH_PROTOCOL_CLASS_NAME="auth.protocol.className";
    
    private static final String CONFIG_PROPERTIES_FILE_PATH="misc/config.properties";
    public static final String PREFS_LOGLEVEL = "log.level";
    public static final String PREFS_SOLR_URL = "solr.url";
    public static final String PREFS_PRODUCT_URL_WCS_BASE = "product.base.url.wcs";
    public static final String PREFS_PRODUCT_URL_HTTP_BASE = "product.base.url.http";
    public static final String PREFS_PRODUCT_URL_FTP_BASE = "product.base.url.ftp";
    public static final String PREFS_BROWSE_URL_BASE = "browse.base.url";
    private static final String PREFS_PRODUCT_URL_BASE="product.base.url";
   
 
    public static void install()
    {
        File propFile=new File(ServletVars.workspace,"misc/config.properties");
        File webinfDir = new File(ServletVars.appFolder, "WEB-INF");
        if(propFile.exists()==false)
        {
            try {
                IOUtil.copyFile(new File(webinfDir,"classes/config.properties"), propFile);
            } catch (Exception ex) {
                Log.error("Could not move the properties file to the workspace folder");
            }
        }
    }
    
    
   public static Properties load() throws FileNotFoundException, IOException
    {
        Properties prop;
        File propFile;

        propFile=new File(ServletVars.workspace,CONFIG_PROPERTIES_FILE_PATH);
        
        prop=new Properties();
        FileInputStream propIs=new FileInputStream(propFile);
        prop.load(propIs);

        propIs.close();
        return prop;
    }
    
    public static void save(Properties prop) throws FileNotFoundException, IOException
    {
        File propFile;
        propFile=new File(ServletVars.workspace,CONFIG_PROPERTIES_FILE_PATH);
        
        FileOutputStream propIs=new FileOutputStream(propFile);
        prop.store(propIs, null);
    }
        
    public static File getWorkspaceFolder()
    {
        return ServletVars.workspace;
    }
    
    public static AbstractFilesystem getWorkspaceFolderAsAbstractFS() throws FileNotFoundException, IOException
    {
        return new FileFilesystem(ServletVars.workspace);
    }
    
    public static String getLoggingFolder() throws Exception {
        Properties props;
        props = Prefs.load();
        
        String folder=props.getProperty("workspace.dir");
        if(folder.endsWith("/")==false)
            folder+="/";
        return folder+props.getProperty("workspace.dir.logs");
    }

    public static String getMetadataFolder() throws Exception {
        Properties props;
        props = Prefs.load();
        
        String folder=props.getProperty("workspace.dir");
        if(folder.endsWith("/")==false)
            folder+="/";
        return folder+props.getProperty("workspace.dir.metadata");
    }    
    
    public static Level getLogLevel() {
        try
        {
            Properties props;
            props = Prefs.load();
            return Level.parse(props.getProperty(PREFS_LOGLEVEL));
        }
        catch(Exception e)
        {
            return Level.INFO;
        }
    }

    public static String getSolrUrl() throws FileNotFoundException, IOException {
            Properties props;
            props = Prefs.load();
            return props.getProperty(PREFS_SOLR_URL);
    }

    public static String getProductURLBaseWcs() throws FileNotFoundException, IOException {
            Properties props;
            props = Prefs.load();
            return props.getProperty(PREFS_PRODUCT_URL_WCS_BASE);
    }

    public static String getProductURLBaseHttp() throws FileNotFoundException, IOException {
            Properties props;
            props = Prefs.load();
            return props.getProperty(PREFS_PRODUCT_URL_HTTP_BASE);
    }

    public static String getProductURLBaseFtp() throws FileNotFoundException, IOException {
            Properties props;
            props = Prefs.load();
            return props.getProperty(PREFS_PRODUCT_URL_FTP_BASE);
    }
    
    public static String getProductURLBase() throws IOException
    {
        Properties props;
            props = Prefs.load();
            return props.getProperty(PREFS_PRODUCT_URL_BASE);
    }
    
    
    public static String getBrowseURLBase() throws FileNotFoundException, IOException {
            Properties props;
            props = Prefs.load();
            return props.getProperty(PREFS_BROWSE_URL_BASE);
    }
    
    
    public static void setLogLevel(Level level) {
        try
        {
            Properties props;
            props = Prefs.load();
            props.setProperty(PREFS_LOGLEVEL, level.toString());
            Prefs.save(props);
        }
        catch(Exception e)
        {
            Log.error(ErrorCodes.ERROR_CODE_21);
        }
    } 
}
