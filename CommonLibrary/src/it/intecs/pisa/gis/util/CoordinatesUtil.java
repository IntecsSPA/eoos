/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.gis.util;

/**
 *
 * @author simone
 */
public class CoordinatesUtil {
    
    

    public String revertCoordinates(final String coordinates) {
        String[] CA = stringtoArray(coordinates, " ");
     return CA[6] + " " + CA[7] + " " + 
            CA[4] + " " + CA[5] + " " + 
            CA[2] + " " + CA[3] + " " + 
            CA[0] + " " + CA[1] + " " + 
            CA[6] + " " + CA[7];
    }

    public String adjustCoordinatesBottomRight(final String coordinates) {
        String[] CA = stringtoArray(coordinates, " ");
     return CA[4] + " " + CA[5] + " " + 
            CA[6] + " " + CA[7] + " " + 
            CA[0] + " " + CA[1] + " " + 
            CA[2] + " " + CA[3] + " " + 
            CA[4] + " " + CA[5];
    }

/*    
    public String adjustCoordinatesForSolr(final String coordinates) {
        String[] CA = stringtoArray(coordinates, " ");
        String solr="";
        for (int i = 0; i< CA.length/2; i++){
            solr += CA[2*i] + " " + CA[2*i+1] + ",";
        }

        if(!CA[0].equals(CA[CA.length-2])|| !CA[1].equals(CA[CA.length-1])){
            solr += CA[0] + " " + CA[1];        
        }else{
            // remove the ","
            solr = solr.substring(0, solr.length()-1);
        }
        
        return solr;
    }
  */  
    
// swap lat/lon
    public String adjustCoordinatesForSolr(final String coordinates) {
        String[] CA = stringtoArray(coordinates, " ");
        String solr="";
        for (int i = 0; i< CA.length/2; i++){
            solr += CA[2*i+1] + " " + CA[2*i] + ",";
        }

        if(!CA[0].equals(CA[CA.length-2])|| !CA[1].equals(CA[CA.length-1])){
            solr += CA[1] + " " + CA[0];        
        }else{
            // remove the ","
            solr = solr.substring(0, solr.length()-1);
        }
        
        return solr;
    }    
    
    
    public String wkt2oem(final String coordinates) {
        //MULTIPOLYGON(((25.1663 53.0465,20.7661 53.5979,19.9131 50.6252,24.0371 50.0855,25.1663 53.0465)))
        String cleanCoordinates = coordinates.substring(coordinates.indexOf("("));
        cleanCoordinates = cleanCoordinates.replaceAll("\\(", "");
        cleanCoordinates = cleanCoordinates.replaceAll("\\)", "");
        cleanCoordinates = cleanCoordinates.replaceAll(",", " ");
        
        String[] CA = stringtoArray(cleanCoordinates, " ");
        String solr="";
        for (int i = 0; i< CA.length/2; i++){
            solr += CA[2*i+1] + " " + CA[2*i] + " ";
        }

        solr = solr.substring(0, solr.length()-1);
        
        return solr;
    }

    public String adjustCoordinatesFromKML(final String coordinates) {
        String[] CA = stringtoArray(coordinates, " ");

        String out="";

        String[] point;
        for (int i = 0; i< CA.length; i++){
            point = stringtoArray(CA[i], ",");
            out += point[0] + " " + point[1] + " ";
        }
        return out;
    }        

    
    public String adjustCoordinatesForCzml(final String coordinates) {
        String[] CA = stringtoArray(coordinates, " ");

        String out="";

        for (int i = 0; i< CA.length/2; i++){
            out += ","+CA[2*i] + "," + CA[2*i+1] + ",0.0";
        }
        return out.substring(1);
    }            
    

    public String adjustCoordinatesForGeoJson(final String coordinates) {
        String[] CA = stringtoArray(coordinates, " ");

        String out="";

        for (int i = 0; i< CA.length/2; i++){
            out += ",["+CA[2*i+1] + "," + CA[2*i] + "]";
        }
        return out.substring(1);
    }        

    
    public String getContainingBBOXFromPolygon(final String coordinates) {
        String[] CA = stringtoArray(coordinates, " ");
        float minLat=180;
        float minLon=180;
        float maxLat=-180;
        float maxLon=-180;
        float lon;
        float lat;
        
        for (int i = 0; i< CA.length/2; i++){
            lon = Float.parseFloat(CA[2*i]);
            if ( lon < minLon )
                minLon = lon; 
            if ( lon > maxLon )
                maxLon = lon; 
            lat = Float.parseFloat(CA[2*i+1]);
            if ( lat < minLat )
                minLat = lat; 
            if ( lat > maxLat )
                maxLat = lat;         
        }

        
        return ""+minLon+","+minLat+","+maxLon+","+maxLat;
    }    
    
    
    /**
     * This function convert a string s into an array of strings using sep as
     * separator
     *
     * @param s string to be split
     * @param sep separator
     * @return
     */
    public String[] stringtoArray(String s, String sep) {
        // convert a String s to an Array, the elements
        // are delimited by sep
        StringBuffer buf = new StringBuffer(s);
        int arraysize = 1;
        for (int i = 0; i < buf.length(); i++) {
            if (sep.indexOf(buf.charAt(i)) != -1) {
                arraysize++;
            }
        }
        String[] elements = new String[arraysize];
        int y, z = 0;
        if (buf.toString().indexOf(sep) != -1) {
            while (buf.length() > 0) {
                if (buf.toString().indexOf(sep) != -1) {
                    y = buf.toString().indexOf(sep);
                    if (y != buf.toString().lastIndexOf(sep)) {
                        elements[z] = buf.toString().substring(0, y);
                        z++;
                        buf.delete(0, y + 1);
                    } else if (buf.toString().lastIndexOf(sep) == y) {
                        elements[z] = buf.toString().substring(0, buf.toString().indexOf(sep));
                        z++;
                        buf.delete(0, buf.toString().indexOf(sep) + 1);
                        elements[z] = buf.toString();
                        z++;
                        buf.delete(0, buf.length());
                    }
                }
            }
        } else {
            elements[0] = buf.toString();
        }
        buf = null;
        return elements;
    }
}
