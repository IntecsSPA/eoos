/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.intecs.pisa.openCatalogue.solr.ingester;

import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author massi
 */
public class OEMIngester extends BaseIngester{

    @Override
    protected Document[] parse(AbstractFilesystem indexFile) {
        try
        {
            SAXBuilder builder=new SAXBuilder();

            Document document = builder.build(indexFile.getInputStream());

            return new Document[]{document};
        }
        catch(Exception e)
        {
            return new Document[0];
        }
    }
    
}
