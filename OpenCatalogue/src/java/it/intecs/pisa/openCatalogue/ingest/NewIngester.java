/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.ingest;

import it.intecs.pisa.gis.util.CoordinatesUtil;
import it.intecs.pisa.openCatalogue.filesystem.AbstractFilesystem;
import it.intecs.pisa.openCatalogue.filesystem.FileFilesystem;
import it.intecs.pisa.openCatalogue.saxon.SaxonDocument;
import it.intecs.pisa.openCatalogue.solr.solrHandler;
import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.MathTool;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 * @author simone
 */
public class NewIngester {

    public static final String DOLLAR = "$";
    public static final String SLASH = "/";
    public static final String STRING_PERIOD = "##PERIOD##";
    public static final String TAG_DEFAULT_VALUE = "defaultValue";
    public static final String TAG_INDEX_FIELD_NAME = "indexFieldName";
    public static final String VELOCITY_DATE = "date";
    public static final String VELOCITY_MATH = "math";
    public static final String VELOCITY_RESULT_LIST = "resultsList";
    public static final String VELOCITY_INGESTION_SUMMARY = "ingestionSummary";
    public static final String VELOCITY_PERIOD_START = "PERIOD_START";
    public static final String VELOCITY_PLATFORM_SHORT_NAME = "PLATFORM_SHORT_NAME";
    public static final String VELOCITY_INSTRUMENT_SHORT_NAME = "INSTRUMENT_SHORT_NAME";
    public static final String VELOCITY_OPERATIONAL_MODE = "OPERATIONAL_MODE";
    public static final String VELOCITY_PRODUCT_TYPE = "PRODUCT_TYPE";
    public static final String BROWSE_FROM_HARVEST = "browse_from_harvest";
    private static final String INGEST_RESPONSE = "ingestResponse.vm";
    private VelocityEngine ve;
    private AbstractFilesystem metadataRepository;
    private solrHandler solr;

    /**
     *
     * @param resourceToScan End point to be harvested
     * @param lastHarvestDateTime it should specify the date when the last
     * processed element was added or updated (e.g. LAST_UPDATE) in a specific
     * the index file. Only the metadata of records which have a LAST_DATE value
     * greater than this date (i.e more recent) will be processed.
     * @param configDirectory It specify the folder that holds the configuration
     * file
     * @throws SAXException
     * @throws IOException
     * @throws SaxonApiException
     * @throws Exception
     */
    public NewIngester(AbstractFilesystem metadataRepository, AbstractFilesystem configDirectory, String solrURL) throws SAXException, IOException, SaxonApiException, Exception {
        this.metadataRepository = metadataRepository;
        this.ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, configDirectory.getAbsolutePath());
        solr = new solrHandler(solrURL);
    }

    public void ingestData(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int toBeInserted = 0;
        int actuallyInserted = 0;
        Document metadata;
        InputStream stream = request.getInputStream();
        // set the start and stop period        
        String key = "";
        org.jdom2.Document doc = useSAXParser(stream);

        //Get list of Employee element
        Element rootElement = doc.getRootElement();
        List<Element> listEmpElement = rootElement.getChildren();
        //loop through to edit every Employee element
        ArrayList responseElements = new ArrayList();
        Map result;
        for (Element empElement : listEmpElement) {
            try {
                if (empElement.getName().equals("EarthObservation")) {
                    toBeInserted++;
                    result = new HashMap();
                    empElement.detach();
                    metadata = new Document(empElement);
                    key = UUID.randomUUID().toString();
                    storeMetadata(metadata, key);
                    uploadMetadataToSolr(metadata, key);
                    result.put("status", "success");
                    responseElements.add(result);
                    actuallyInserted++;
                }
            } catch (Exception ex) {
                result = new HashMap();
                result.put("status", "failure");
                result.put("message", ex.getMessage());
                responseElements.add(result);
            }        
        }
        Map ingestionSummary = new HashMap();
        String status = (toBeInserted==actuallyInserted)?"success":"partial";
        ingestionSummary.put("status", status);
        ingestionSummary.put("toBeInserted", toBeInserted);
        ingestionSummary.put("actuallyInserted",actuallyInserted);
        ingestionSummary.put("toBeDeleted", 0);
        ingestionSummary.put("actuallyDeleted", 0);
        ingestionSummary.put("toBeUpdated", 0);
        ingestionSummary.put("actuallyUpdated", 0);
        sendBackResponse(responseElements,ingestionSummary, request, response);
    }

    //Get JDOM document from SAX Parser
    private static org.jdom2.Document useSAXParser(InputStream stream) throws JDOMException,
            IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(stream);
    }

    private void storeMetadata(org.jdom2.Document metadata, String key) throws IOException {
        FileFilesystem fs = new FileFilesystem(metadataRepository.getAbsolutePath() + "/" + key + ".xml");
        XMLOutputter outputter = new XMLOutputter();
        String metadataString = outputter.outputString(metadata);
        byte[] b = metadataString.getBytes();
        fs.getOutputStream().write(b);
    }

    private void uploadMetadataToSolr(org.jdom2.Document metadata, String key) throws IOException, SaxonApiException, Exception {
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_DATE, new DateTool());
        context.put(VELOCITY_MATH, new MathTool());
        context.put("coordinates", new CoordinatesUtil());
        context.put("KEY", key);
        context.put("metadataDocument", metadata);
        StringWriter swOut = new StringWriter();
        ve.getTemplate("generateSolrAddRequest.vm").merge(context, swOut);
        //TO DO UNCOMMENT WHEN DONE
        //solr.postDocument(swOut.toString());
        // only for debug .... TODO - remove it
        saveSolrfile(swOut.toString(), key);
        swOut.close();
    }

    private void saveSolrfile(String swOut, String key) throws IOException {
        FileFilesystem fs = new FileFilesystem(metadataRepository.getAbsolutePath() + "/" + key + ".slr");
        fs.getOutputStream().write(swOut.getBytes());
    }

    private void sendBackResponse(ArrayList responseElements,Map is,HttpServletRequest request, HttpServletResponse response) throws IOException, XPathFactoryConfigurationException, XPathException, XPathExpressionException, SAXException, JDOMException {
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_DATE, new DateTool());
        context.put(VELOCITY_RESULT_LIST, responseElements);
        context.put(VELOCITY_INGESTION_SUMMARY, is);
        response.setContentType("application/xml");
        Writer swOut = response.getWriter();
        ve.getTemplate(INGEST_RESPONSE).merge(context, swOut);
        swOut.close();
    }
}
