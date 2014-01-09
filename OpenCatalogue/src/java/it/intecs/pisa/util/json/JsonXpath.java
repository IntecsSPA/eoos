/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.StringTokenizer;

/**
 *
 * @author massi
 */
public class JsonXpath {
    public static String getString(JsonObject obj,String xpath)
    {
        StringTokenizer tokenizer;

        tokenizer=new StringTokenizer(xpath,"/");

        Object selectedObject;
        selectedObject=get(obj,tokenizer);

        JsonPrimitive primitive;
        primitive=(JsonPrimitive) selectedObject;

        return (String) primitive.getAsString();
    }

    private static Object get(JsonObject obj, StringTokenizer tokenizer) {
        String tok;

        tok=tokenizer.nextToken();
        JsonArray array;
        JsonObject focusedObj;

        String token;
        if(tok.endsWith("]"))
        {
            String index;
            index=tok.substring(tok.indexOf("[")+1,tok.indexOf("]"));

            String tokenName;
            tokenName=tok.substring(0,tok.indexOf("["));

            array=obj.getAsJsonArray(tokenName);
            JsonElement a = array.get(Integer.valueOf(index) - 1);
            if(a instanceof JsonPrimitive)
                return a;
            else focusedObj=a.getAsJsonObject();
        }
        else focusedObj=obj;

        return focusedObj.get(tok);
  
    }

    public static boolean exists(JsonObject obj,String xpath)
    {
        try
        {
            StringTokenizer tokenizer;

            tokenizer=new StringTokenizer(xpath,"/");

            Object selectedObject;
            selectedObject=get(obj,tokenizer);
            if(selectedObject==null)
                return false;
            else return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }
}
