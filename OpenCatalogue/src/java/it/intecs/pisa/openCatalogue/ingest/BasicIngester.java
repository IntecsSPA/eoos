/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.intecs.pisa.openCatalogue.ingest;

import it.intecs.pisa.gis.util.CoordinatesUtil;
import it.intecs.pisa.openCatalogue.filesystem.AbstractFilesystem;
import it.intecs.pisa.openCatalogue.filesystem.FileFilesystem;
import static it.intecs.pisa.openCatalogue.ingest.NewIngester.VELOCITY_DATE;
import static it.intecs.pisa.openCatalogue.ingest.NewIngester.VELOCITY_MATH;
import it.intecs.pisa.openCatalogue.solr.solrHandler;
import java.io.IOException;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.saxon.s9api.SaxonApiException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.MathTool;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 * This is a basic ingester class that provides common functionalities to other ingester.
 * This abstract class has to be inherited and methods implemented 
 * 
 * @author Massimiliano Fanciulli
 * 
 */
public class BasicIngester {
    protected AbstractFilesystem metadataRepository;
    protected solrHandler solr;
    protected VelocityEngine ve;
        
    public BasicIngester(AbstractFilesystem mRepo, AbstractFilesystem configDirectory, String solrURL) throws SAXException, IOException, SaxonApiException, Exception {
        metadataRepository = mRepo;
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, configDirectory.getAbsolutePath());
        solr = new solrHandler(solrURL);
    }
    
    public void ingestData(HttpServletRequest request, HttpServletResponse response) throws Exception {
    
        
    }
    
    protected void uploadMetadataToSolr(org.jdom2.Document metadata, String key) throws IOException, SaxonApiException, Exception {
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_DATE, new DateTool());
        context.put(VELOCITY_MATH, new MathTool());
        context.put("coordinates", new CoordinatesUtil());
        context.put("KEY", key);
        context.put("metadataDocument", metadata);
        StringWriter swOut = new StringWriter();
        ve.getTemplate("generateSolrAddRequest.vm").merge(context, swOut);
        //TO DO UNCOMMENT WHEN DONE
        solr.postDocument(swOut.toString());
        // only for debug .... TODO - remove it
        saveSolrfile(swOut.toString(), key);
        swOut.close();
    }
    
    protected void saveSolrfile(String swOut, String key) throws IOException {
        FileFilesystem fs = new FileFilesystem(metadataRepository.getAbsolutePath() + "/" + key + ".slr");
        fs.getOutputStream().write(swOut.getBytes());
    }
    
     protected void storeMetadata(org.jdom2.Document metadata, String key) throws IOException {
        FileFilesystem fs = new FileFilesystem(metadataRepository.getAbsolutePath() + "/" + key + ".xml");
        XMLOutputter outputter = new XMLOutputter();
        String metadataString = outputter.outputString(metadata);
        byte[] b = metadataString.getBytes();
        fs.getOutputStream().write(b);
    }
}
