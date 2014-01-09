/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.log;

import it.intecs.pisa.openCatalogue.catalogue.ServletVars;
import it.intecs.pisa.openCatalogue.prefs.Prefs;
import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class Log {
    
    private static Logger log;
    
    public static void setup() {

        if (log == null) {
            try { 
                if(ServletVars.appFolder!=null)
                {
                    log = Logger.getLogger("it.intecs.pisa.openCatalogue");
                    log.setLevel(Prefs.getLogLevel());
                    
                    String folderStr=Prefs.getLoggingFolder();
                    File folder=new File(folderStr);
                    folder.mkdirs();
                    
                    folderStr=folderStr.endsWith(File.pathSeparator)?folderStr:folderStr+File.separator;
                    Handler handler = new FileHandler(folderStr+"OpenCatalogue.log");
                    handler.setFormatter(new LogFormatter());
                    handler.setLevel(Prefs.getLogLevel());
                    log.addHandler(handler);
                    
/*                    handler = new LogHandler(folderStr+"ToControllerServer.log");
                    handler.setFormatter(new LogFormatter());
                    handler.setLevel(Prefs.getLogLevel());
                    log.addHandler(handler);*/
                }
            } catch (Exception ex) {
                
            }
        }
    }
    
    public static void info(String text){
        setup();
        
        if(log!=null)
            log.log(Level.INFO,text);
        else System.out.println(text);
    }
    
    
    public static void debug(String text){
        if(log == null)
              setup();
        if(log!=null)
            log.log(Level.FINE,text);
        else System.out.println("DEBUG: "+text);
    }

    public static void logException(Exception e)
    {
        if(log == null)
              setup();
        log.log(Level.SEVERE, "An exception has been thrown. Details: ", e);
        log.log(Level.SEVERE, e.toString());
       
    }

    public static void logHTTPStatus(String url, int code)
    {
        if(log == null)
              setup();
        log.log(Level.FINEST, "The HTTP exchange with URL {0} return code {1}", new Object[]{url, code});
    }
    
    public static Level getLogLevel(){
        if(log == null)
              setup();
        return log.getLevel();
    }
    
    public static void setLevel(Level level) {
        Log.debug("Setting new log level: " + level.toString());
        try {
            log.setLevel(level);

            for (Handler handler : log.getHandlers()) {
                handler.setLevel(level);
            }
        } catch (Exception ex) {
            Log.error(ErrorCodes.ERROR_CODE_22);
        }
    }

    public static void error(String text) {
        if(log == null) {
            setup();
        }
        if(log!=null) {
            log.log(Level.SEVERE,text);
        }
        else {
            System.out.println(text);
        }
    }
}
