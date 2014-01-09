/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util.json;

import com.google.gson.*;
import it.intecs.pisa.util.DOMUtil;
import java.lang.reflect.Type;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class DomSerializer implements JsonSerializer<org.w3c.dom.Document>{

    public JsonElement serialize(Document t, Type type, JsonSerializationContext jsc) {
        JsonObject rootJsonObj=null;

        rootJsonObj=new JsonObject();
        addItems(rootJsonObj,t.getDocumentElement());

        return rootJsonObj;
    }

    protected void addItems(JsonObject jsonObj, Element el) {
        List<Element> children=DOMUtil.getChildren(el);

        for(Element ithel: children)
        {
            addElement(ithel,jsonObj);
        }
    }

    private void addElement(Element el, JsonObject jsonObj) {
        String localName;
        JsonArray array;

        localName=el.getLocalName();
        if(jsonObj.has(localName))
        {
            JsonElement existingObj = jsonObj.get(localName);
            if(existingObj instanceof JsonObject)
            {
                jsonObj.remove(localName);
                array=new JsonArray();
                jsonObj.add(localName, array);
                array.add(existingObj);
            }
            else array=(JsonArray) existingObj;

        }
    }



}
