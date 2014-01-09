/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util.json;

import com.google.gson.JsonObject;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class JsonSuccessObject {
    public static JsonObject get()
    {
        JsonObject outputJson = new JsonObject();
        outputJson.addProperty("success", true);
        
        return outputJson;
    }
}
