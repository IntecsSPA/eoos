/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.intecs.pisa.openCatalogue.solr.ingester;

import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.util.StringUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jdom2.Document;

/**
 *
 * @author massi
 */
public class CSVIngester extends Ingester{
  
    
    @Override
    protected Document[] parse(AbstractFilesystem indexFile) {
        try
        {
        
        BufferedReader br = new BufferedReader(new InputStreamReader(indexFile.getInputStream()));
        String strLine;
        
        String keysLine = br.readLine();
        String[] keys = StringUtil.stringtoArray(keysLine, elements_separator);

        //Now we have to check if we have all the info to generate the metadata
        int lineCounter = 1;

        String key = "";
        
        ArrayList<Document> docarray=new ArrayList<Document>();

        while ((strLine = br.readLine()) != null) {
            String[] values = StringUtil.stringtoArray(strLine, elements_separator);
            if (values.length == keys.length) {
                Map map = new HashMap();
                map.put(FILENAME,indexFile.getName().replaceAll(".xml",""));
                for (int i = 0; i < keys.length; i++) {
                    map.put(keys[i], values[i]);
                }
                key = UUID.randomUUID().toString();
                docarray.add(generateMetadata(map, key));
            } else {
                System.out.println("Line " + lineCounter + " is corrupted we skip it : (expected=" + keys.length + " read=" + values.length + ") TEXT = " + strLine);
            }
            lineCounter++;
        }
        
        return docarray.toArray(new Document[0]);
        }
        catch(Exception e)
        {
            return new Document[0];
        }
    }
    
}
