/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class DateUtil {
    public static String getDateAsString(String format,Date date)
    {
        SimpleDateFormat formatter;

        formatter=new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String getCurrentDateAsString(String format)
    {
        return getDateAsString(format,new Date());
    }

    public static String getCurrentDateAsUniqueId()
    {
        return getCurrentDateAsString("yyyyMMddHHmmssSSS");
    }

    public static Date getFutureDate(long interval_from_now)
    {
        Date now;
        long future;

        now=new Date();
        future=now.getTime()+interval_from_now;

        return new Date(future);
    }
}
