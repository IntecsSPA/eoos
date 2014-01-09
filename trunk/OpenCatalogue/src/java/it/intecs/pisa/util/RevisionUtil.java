/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util;

import java.util.StringTokenizer;

/**
 *
 * @author massi
 */
public class RevisionUtil {
    public static boolean isEarlier(String version, String reference)
    {
        StringTokenizer tokenizer,reftokenizer;
        String value,refValue;
        boolean result=false;
        int v,rv;

        tokenizer=new StringTokenizer(version,"./");
        reftokenizer=new StringTokenizer(reference,"./");

        if(tokenizer.hasMoreTokens() && reftokenizer.hasMoreTokens())
        {
            value=tokenizer.nextToken();
            refValue=reftokenizer.nextToken();

            v=Integer.parseInt(value);
            rv=Integer.parseInt(refValue);
            if(v<rv)
                return true;
            else if(v>rv)
                return false;
        }

         if(tokenizer.hasMoreTokens() && reftokenizer.hasMoreTokens())
         {
            value=tokenizer.nextToken();
            refValue=reftokenizer.nextToken();

            v=Integer.parseInt(value);
            rv=Integer.parseInt(refValue);

            if(v<rv)
                return true;
            else if(v>rv)
                return false;
         }
         else return false;

        if(tokenizer.hasMoreTokens() && reftokenizer.hasMoreTokens())
         {
            value=tokenizer.nextToken();
            refValue=reftokenizer.nextToken();

            v=Integer.parseInt(value);
            rv=Integer.parseInt(refValue);
            if(v<rv)
                return true;
            else if(v>rv)
                return false;
         }
         else return false;

        return result;
    }
}
