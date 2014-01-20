/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.intecs.pisa.openCatalogue.solr.ingester;

/**
 *
 * @author Massimilian Fanciulli
 */
public class IngesterFactory {
    public static Ingester fromInputType(String type)
    {
        if(type.equals("CSV"))
        {
            return new CSVIngester();
        }
        else return new Ingester();
    }
}
