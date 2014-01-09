/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util;

import java.util.HashMap;

/**
 *
 * @author Andrea Marongiu
 */
public class TemplateStringUtil {
    
    
    public static HashMap<String,String> getMetadataString (String originalString, String template){
        HashMap<String,String> fileNameMetadata=new HashMap<String,String>();
        
        String [] metadataInfo= getFirstMetadataInfo(template);
        String remTemplate=null;
        String metadataValue;
        String currentString=originalString;
        while(metadataInfo != null){
            if(!"".equals(metadataInfo[1]))
                metadataValue=currentString.substring(0, 
                                        currentString.indexOf(metadataInfo[1]));
            else
                metadataValue=currentString;
            
            fileNameMetadata.put(metadataInfo[0], metadataValue);
            
            if(!"".equals(metadataInfo[1])){
                currentString=currentString.substring(currentString.indexOf(metadataInfo[1])+1);
                remTemplate=template.substring(template.indexOf(metadataInfo[1])+1);
                metadataInfo= getFirstMetadataInfo(remTemplate);
            }else
                break;
        }
        return fileNameMetadata;
    }
    
    
    private static String [] getFirstMetadataInfo(String template){
        String []metadataInfo=null;
        int metStart=template.indexOf("<");
        int metEnd=template.indexOf(">");
        
        if(metStart > -1 && metEnd > -1){
           String metadataName=template.substring(metStart+1, metEnd);
           metadataInfo= new String[2]; 
           char separator = 0;
           try{ 
             separator=template.charAt(metEnd+1);
           }catch(Exception ex){
             metadataInfo[0]=metadataName;  
             metadataInfo[1]="";  
             return metadataInfo;
           } 
           
           metadataInfo[0]=metadataName;
           metadataInfo[1]=""+separator;
        }
        return metadataInfo;
    }
    
}
