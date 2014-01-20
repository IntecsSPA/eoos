/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author massi
 */
public class LogHandler extends FileHandler{

    public LogHandler() throws IOException
    {
        super();
    }
    
    LogHandler(String feedlog) throws IOException {
        super(feedlog);
    }

    @Override
    public synchronized void publish(LogRecord record) {
        if(Level.SEVERE.intValue()<=record.getLevel().intValue())
        {
            super.publish(record);
        }
    }
}
