/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrea Marongiu
 */
public class LoggerSetup {

    private static Logger log;

    public static void setup() {

        if (log == null) {
            System.out.println("Setting up logger");
            try {                
                Handler consHandler = new ConsoleHandler();
                consHandler.setFormatter(new LogFormatter());
                consHandler.setLevel(Level.FINEST);
                
                log = Logger.getLogger("be.kzen.ergorr");
                log.addHandler(consHandler);
               // log.addHandler(fileHandler);
                log.setLevel(Level.FINEST);
            } catch (Exception ex) {
                System.out.println("Error setting up Logger");
                ex.printStackTrace();
            }
        }
    }
    
    public static void setLevel(Level newLevel) {

           System.out.println("Setting new level logger");
            try {
                Handler consHandler = new ConsoleHandler();
                consHandler.setFormatter(new LogFormatter());
                consHandler.setLevel(newLevel);
                
                log = Logger.getLogger("be.kzen.ergorr");
                log.addHandler(consHandler);
                //log.addHandler(fileHandler);
                log.setLevel(newLevel);
            } catch (Exception ex) {
                System.out.println("Error setting new Level Logger");
                ex.printStackTrace();
            }

        
    }
    
    public static String getLevel() {
          if(log == null)
              setup();
          return log.getLevel().toString();

    }
    
    
   
    
}
