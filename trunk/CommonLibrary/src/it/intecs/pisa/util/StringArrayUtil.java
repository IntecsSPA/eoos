/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util;

import java.util.StringTokenizer;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class StringArrayUtil {
    /**
     * This method adds an empty item in the string array.
     * The content of the input array is assigned to the output array, not cloned.
     * @param array
     * @return
     */
    public static String[] addEmptyItemOnTop(String[] array)
    {
        String[] newArray;
        int i=1;

        newArray=new String[array.length+1];
        newArray[0]="";
        for(String item:array)
        {
            newArray[i]=item;
            i++;
        }
        
        return newArray;
    }
    
    public static String[] getFromCommaSeparated(String str)
    {
        StringTokenizer tokenizer=new StringTokenizer(str,",");
        int count=tokenizer.countTokens();
        
        String[] array=new String[count];
        for(int i=0;i<count;i++)
        {
            array[i]=tokenizer.nextToken();
        }
        
        return array;
    }

    public static String toCommaSeparated(String[] str)
    {
        String result="";
        
        if(str.length>0)
        {
            result=str[0];
            for(int i=1;i<str.length;i++)
            {
                result+=","+str[i];
            }
        }
        
        return result;
    }
}
