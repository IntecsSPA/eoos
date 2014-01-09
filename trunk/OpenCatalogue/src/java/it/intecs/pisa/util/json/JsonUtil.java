/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.intecs.pisa.util.IOUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class JsonUtil {

    public static JsonObject getStringAsJSON(String str) {
        JsonParser parser;
        JsonObject obj;

        parser = new JsonParser();

        JsonElement parsedEl = parser.parse(str);
        obj = (JsonObject) parsedEl.getAsJsonObject();

        return obj;
    }

    public static JsonObject getInputAsJson(InputStream in) {
        JsonObject obj;

        try {
            String jsonAsString = IOUtil.inputToString(in);
            if (jsonAsString == null || jsonAsString.equals("")) {
                return new JsonObject();
            }

            obj = getStringAsJSON(jsonAsString);
        } catch (Throwable t) {
            obj = null;
        }
        return obj;
    }

    public static String getJsonAsString(JsonObject outputJson) {
        Gson gson = new Gson();
        return gson.toJson(outputJson);
    }

    public static InputStream getJsonAsStream(JsonObject obj) {
        return new ByteArrayInputStream(getJsonAsString(obj).getBytes());
    }

    public static void writeJsonToStream(JsonObject outputJson, OutputStream outstream) throws IOException {
        String jsonStr;

        jsonStr=JsonUtil.getJsonAsString(outputJson);
        outstream.write(jsonStr.getBytes());
    }
}
