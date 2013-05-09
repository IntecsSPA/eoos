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
public class JsonErrorObject {
    public static JsonObject get(String errorMsg)
    {
        JsonObject outputJson = new JsonObject();
        outputJson.addProperty("success", false);
        outputJson.addProperty("reason", errorMsg);

        return outputJson;
    }
}
