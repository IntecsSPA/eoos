/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Andrea Marongiu
 */
public class LogFormatter extends Formatter {

    private static final String lineSep = System.getProperty("line.separator");

    @Override
    public String format(LogRecord record) {
        
        StringBuilder out = new StringBuilder(1000);
        out.append(record.getLevel()).append("[");
        
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(record.getMillis());
        out.append(c.get(Calendar.YEAR)).append("-");
        out.append(c.get(Calendar.MONTH)).append("-");
        out.append(c.get(Calendar.DAY_OF_MONTH)).append(" ");
        out.append(c.get(Calendar.HOUR)).append(":");
        out.append(c.get(Calendar.MINUTE)).append(":");
        out.append(c.get(Calendar.SECOND)).append(".");
        out.append(c.get(Calendar.MILLISECOND)).append("]");
        
        
        out.append(record.getMessage());
        out.append(lineSep);
        
        if (record.getThrown() != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter w = new PrintWriter(baos);
            record.getThrown().printStackTrace(w);
            out.append(new String(baos.toByteArray()));
            out.append(lineSep);
        }

        return out.toString();
    }
}
