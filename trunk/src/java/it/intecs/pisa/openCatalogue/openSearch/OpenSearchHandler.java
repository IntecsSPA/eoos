/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
 */
package it.intecs.pisa.openCatalogue.openSearch;

import it.intecs.pisa.gis.util.CoordinatesUtil;
import it.intecs.pisa.log.Log;
import it.intecs.pisa.openCatalogue.catalogue.ServletVars;
import it.intecs.pisa.openCatalogue.filesystem.AbstractFilesystem;
import it.intecs.pisa.openCatalogue.prefs.Prefs;
import it.intecs.pisa.openCatalogue.saxon.SaxonDocument;
import it.intecs.pisa.openCatalogue.saxon.SaxonURIResolver;
import it.intecs.pisa.openCatalogue.saxon.SaxonXSLT;
import it.intecs.pisa.openCatalogue.saxon.SaxonXSLTParameter;
import it.intecs.pisa.openCatalogue.solr.solrHandler;
import it.intecs.pisa.util.DOMUtil;
import it.intecs.pisa.util.IOUtil;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.StringReader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactoryConfigurationException;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.DateTool;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
//import org.jdom.input.SAXBuilder;

/**
 *
 * @author simone
 */
public class OpenSearchHandler {

    VelocityEngine ve;
    HashMap metadatas;
    AbstractFilesystem repository;
    solrHandler solr;
    private final static String ATOM_TEMPLATE = "atomResponse.vm";
    private final static String CZML_TEMPLATE = "czmlResponse.vm";
    private final static String PRODUCT_TEMPLATE = "productResponse.vm";
    private final static String JSON_TEMPLATE = "jsonResponse.vm";
    private static final String VELOCITY_DATE = "date";
    private static final String VELOCITY_XPATH = "xpath";
    private static final String VELOCITY_PRODUCT_URL = "productUrl";
    private static final String VELOCITY_BROWSE_URL = "browseUrl";
    private static final String VELOCITY_METADATA_LIST = "metadataList";
    private static final String OPEN_SEARCH_NUMBER_OF_RESULTS = "OPEN_SEARCH_NUMBER_OF_RESULTS";
    private static final String XPATH_NUMBER_OF_RESULTS = "count(//doc)";
    private static final String XPATH_NUM_FOUNDS = "//result/@numFound";    
    private static final String XPATH_IDENTIFIER = "//doc[$$]/str[@name='id']";
    private static final String XPATH_POLYGON = "//doc[$$]/str[@name='posListOrig']";
    private static final String XPATH_POS_LIST = "//doc[$$]/str[@name='posList']";
    private static final String XPATH_METADATA = "//doc[$$]/str[@name='metadataOrig']";
    private static final String XPATH_COUNT_DOC = "count(//doc)";
    private static final String OPEN_SEARCH_START_INDEX = "OPEN_SEARCH_START_INDEX";
    private static final String OPEN_SEARCH_ITEMS_PER_PAGE = "OPEN_SEARCH_ITEMS_PER_PAGE";
    private static final String OPEN_SEARCH_REQUEST = "OPEN_SEARCH_REQUEST";
    private static final String BASE_URL = "BASE_URL";
    private static final String IDENTIFIER = "identifier";
    private static final String POLYGON = "polygon";
    private static final String SHORT_NAME = "polygon";
    private static final String GEORSS = "georss";
    private static final String METADATA_DOCUMENT = "metadataDocument";

    public OpenSearchHandler(AbstractFilesystem configDirectory, AbstractFilesystem repo, String solrEndPoint) {


        this.ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, configDirectory.getAbsolutePath());
        ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");

        this.repository = repo;
        this.metadatas = new HashMap();
        solr = new solrHandler(solrEndPoint);
    }

    public void getDescription() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void processAtomRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        Log.info("New ATOM request received");
        SaxonDocument solrResponse = sendRequestToSolr(request);
        sendBackAtomResponse(solrResponse, request, response);
    }

    public void processCZMLRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        Log.info("New CZML request received");
        SaxonDocument solrResponse = sendRequestToSolr(request);
        sendBackCZMLResponse(solrResponse, request, response);
    }

    public void processJsonRequest(HttpServletRequest request, HttpServletResponse response) throws SaxonApiException, IOException, Exception {
        Log.info("New JSON request received");
        SaxonDocument solrResponse = sendRequestToSolr(request);
        sendBackJSONResponse(solrResponse, request, response);
    }

    public void processProductRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        Log.info("New O&M request received");
        String id = (String) request.getParameter("id");
        if (id == null || id == "") {
            throw new Exception("Missing id parameter");
        }
        SaxonDocument solrResponse = sendRequestToSolr(request);
        sendBackProductResponse(solrResponse, request, response);
    }

    public void processWktRequest(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public Document handleDescription(String requestURL) throws URISyntaxException, IOException, SaxonApiException, SAXException, Exception {
        Document description = IOUtil.getDocumentFromDirectory(ServletVars.appFolder + "/WEB-INF/openSearch/description.xml");
        DOMUtil domUtil = new DOMUtil();
        SaxonXSLT saxonUtil;
        PipedInputStream pipeInput;
        SaxonURIResolver uriResolver;
        ArrayList<SaxonXSLTParameter> parameters = new ArrayList();
        parameters.add(new SaxonXSLTParameter("url", requestURL));
        SAXSource docSource = new SAXSource(new InputSource(DOMUtil.getDocumentAsInputStream(description)));
        String xsltRef = ServletVars.appFolder + "/WEB-INF/openSearch/description.xslt";
        SAXSource xsltDoc = new SAXSource(new InputSource(xsltRef));
        String xsltPath = xsltRef.substring(0, xsltRef.lastIndexOf('/'));
        uriResolver = new SaxonURIResolver(new File(xsltPath));
        saxonUtil = new SaxonXSLT(uriResolver);
        pipeInput = saxonUtil.saxonXSLPipeTransform(docSource, xsltDoc, parameters, "xml");
        return domUtil.inputStreamToDocument(pipeInput);
    }

    private SaxonDocument sendRequestToSolr(HttpServletRequest request) throws SaxonApiException, IOException, Exception {
        // this is a simutation for the moment

        return solr.exchange(request);
    }

    private void sendBackAtomResponse(SaxonDocument solrResponse, HttpServletRequest request, HttpServletResponse response) throws IOException, XPathFactoryConfigurationException, XPathException, XPathExpressionException, SAXException, JDOMException {
        ArrayList metadataList = prepareDataForVelocity(solrResponse);
        VelocityContext context = new VelocityContext();
        String productUrl = Prefs.getProductURLBase();
        if (null != productUrl && !productUrl.equals("")) {
            context.put(VELOCITY_PRODUCT_URL, productUrl);
        }
        String browseUrl = Prefs.getBrowseURLBase();
        if (null != productUrl && !productUrl.equals("")) {
            context.put(VELOCITY_BROWSE_URL, browseUrl);
        }
        context.put(VELOCITY_DATE, new DateTool());
        context.put(VELOCITY_METADATA_LIST, metadataList);
        context.put(OPEN_SEARCH_NUMBER_OF_RESULTS, (String) solrResponse.evaluatePath(XPATH_NUM_FOUNDS, XPathConstants.STRING));
        String requestURL = request.getRequestURL().toString();
        context.put(BASE_URL, requestURL.subSequence(0, requestURL.indexOf("atom")));
        // we have to extract this from the request ...check also if solr returns this info in the response. 
        // If not we have to handle this in the prepareDataForVelocity
        String startIndex = request.getParameter("startIndex");
        String startPage = request.getParameter("startPage");
        
        if (startIndex != null || !startIndex.equals("")){
            context.put(OPEN_SEARCH_START_INDEX, request.getParameter("startIndex"));        
        } else {
            int itemsPerPage = Integer.parseInt(request.getParameter("count"));
            int pageNumber = Integer.parseInt(request.getParameter("startPage"));
            int startAt = itemsPerPage * pageNumber;
            context.put(OPEN_SEARCH_START_INDEX, startAt);                
        }            
        
        context.put(OPEN_SEARCH_ITEMS_PER_PAGE, request.getParameter("count"));
        context.put(OPEN_SEARCH_REQUEST, request.getRequestURL());

        response.setContentType("application/xml");
        Writer swOut = response.getWriter();
        ve.getTemplate(ATOM_TEMPLATE).merge(context, swOut);
        swOut.close();
    }

    private void sendBackCZMLResponse(SaxonDocument solrResponse, HttpServletRequest request, HttpServletResponse response) throws IOException, XPathFactoryConfigurationException, XPathException, XPathExpressionException, SAXException, JDOMException {
        ArrayList metadataList = prepareDataForVelocity(solrResponse);
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_DATE, new DateTool());
        context.put("coordinates", new CoordinatesUtil());
        context.put(VELOCITY_METADATA_LIST, metadataList);

        String productUrl = Prefs.getProductURLBase();
        if (null != productUrl && !productUrl.equals("")) {
            context.put(VELOCITY_PRODUCT_URL, productUrl);
        }
        String browseUrl = Prefs.getBrowseURLBase();
        if (null != productUrl && !productUrl.equals("")) {
            context.put(VELOCITY_BROWSE_URL, browseUrl);
        }

        response.setContentType("application/json");
        Writer swOut = response.getWriter();
        ve.getTemplate(CZML_TEMPLATE).merge(context, swOut);
        swOut.close();
    }

    private void sendBackJSONResponse(SaxonDocument solrResponse, HttpServletRequest request, HttpServletResponse response) throws XPathFactoryConfigurationException, XPathException, XPathExpressionException, IOException, SAXException, JDOMException {
        ArrayList metadataList = prepareDataForVelocity(solrResponse);
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_DATE, new DateTool());

        String productUrl = Prefs.getProductURLBase();
        if (null != productUrl && !productUrl.equals("")) {
            context.put(VELOCITY_PRODUCT_URL, productUrl);
        }
        String browseUrl = Prefs.getBrowseURLBase();
        if (null != productUrl && !productUrl.equals("")) {
            context.put(VELOCITY_BROWSE_URL, browseUrl);
        }

        context.put("coordinates", new CoordinatesUtil());
        context.put(VELOCITY_METADATA_LIST, metadataList);
        response.setContentType("application/json");
        Writer swOut = response.getWriter();
        ve.getTemplate(JSON_TEMPLATE).merge(context, swOut);
        swOut.close();
    }

    private void sendBackResponse(String ID, HttpServletRequest request, HttpServletResponse response) throws IOException, XPathFactoryConfigurationException, XPathException, XPathExpressionException, SAXException, JDOMException {
        ArrayList metadataList = prepareDataForVelocity(ID);
        VelocityContext context = new VelocityContext();
        String productUrl = Prefs.getProductURLBase();
        if (null != productUrl && !productUrl.equals("")) {
            context.put(VELOCITY_PRODUCT_URL, productUrl);
        }
        String browseUrl = Prefs.getBrowseURLBase();
        if (null != productUrl && !productUrl.equals("")) {
            context.put(VELOCITY_BROWSE_URL, browseUrl);
        }
        context.put(VELOCITY_DATE, new DateTool());
        context.put(VELOCITY_METADATA_LIST, metadataList);
        context.put(OPEN_SEARCH_NUMBER_OF_RESULTS, 1);
        String requestURL = request.getRequestURL().toString();
        context.put(BASE_URL, requestURL);
        // we have to extract this from the request ...check also if solr returns this info in the response. 
        // If not we have to handle this in the prepareDataForVelocity
        context.put(OPEN_SEARCH_START_INDEX, 1);
        context.put(OPEN_SEARCH_ITEMS_PER_PAGE, 10);
        context.put(OPEN_SEARCH_REQUEST, request.getRequestURL());

        response.setContentType("application/xml");
        Writer swOut = response.getWriter();
        ve.getTemplate(ATOM_TEMPLATE).merge(context, swOut);
        swOut.close();
    }

    private void sendBackProductResponse(SaxonDocument solrResponse, HttpServletRequest request, HttpServletResponse response) throws IOException, XPathFactoryConfigurationException, XPathException, XPathExpressionException, SAXException, JDOMException {
        ArrayList metadataList = prepareDataForVelocity(solrResponse);
        VelocityContext context = new VelocityContext();
        String productUrl = Prefs.getProductURLBase();
        if (null != productUrl && !productUrl.equals("")) {
            context.put(VELOCITY_PRODUCT_URL, productUrl);
        }
        String browseUrl = Prefs.getBrowseURLBase();
        if (null != productUrl && !productUrl.equals("")) {
            context.put(VELOCITY_BROWSE_URL, browseUrl);
        }    
        context.put(VELOCITY_METADATA_LIST, metadataList);
        response.setContentType("application/xml");
        Writer swOut = response.getWriter();
        ve.getTemplate(PRODUCT_TEMPLATE).merge(context, swOut);
        swOut.close();
    }
    
    
    private ArrayList prepareDataForVelocity(SaxonDocument solrResponse) throws XPathFactoryConfigurationException, XPathException, XPathExpressionException, IOException, SAXException, JDOMException {

        ArrayList metadataList = new ArrayList();
        SAXBuilder builder;
        org.jdom2.Document root = null;

        //Loop on the solrResponse and load the metadata in the repository 
        int results = Integer.parseInt((String) solrResponse.evaluatePath(XPATH_COUNT_DOC, XPathConstants.STRING));
        String id = "";
        String original_polygon = "";
        String original_metadata = "";
        String cdata_field = "";
        for (int i = 1; i <= results; i++) {
            Map metadata = new HashMap();
            id = (String) solrResponse.evaluatePath(XPATH_IDENTIFIER.replace("$$", String.valueOf(i)), XPathConstants.STRING);
            original_polygon = (String) solrResponse.evaluatePath(XPATH_POLYGON.replace("$$", String.valueOf(i)), XPathConstants.STRING);
            builder = new SAXBuilder();
            cdata_field = (String) solrResponse.evaluatePath(XPATH_METADATA.replace("$$", String.valueOf(i)), XPathConstants.STRING);
            //root = builder.build(cdata_field.substring(cdata_field.indexOf("<![CDATA["), cdata_field.indexOf("]]>"))));        
            //root = builder.build(new StringReader(cdata_field));
            //metadata.put(METADATA_DOCUMENT, root);
            metadata.put(IDENTIFIER, id);
            metadata.put(POLYGON, original_polygon);
            // load the metadata and add it to the array
            metadataList.add(metadata);
        }
        return metadataList;
    }

    private ArrayList prepareDataForVelocity(String id) throws XPathFactoryConfigurationException, XPathException, XPathExpressionException, IOException, SAXException, JDOMException {
        ArrayList metadataList = new ArrayList();
        SAXBuilder builder;
        org.jdom2.Document root = null;

        Map metadata = new HashMap();
        builder = new SAXBuilder();
        root = builder.build(this.repository.getAbsolutePath() + "/" + id + ".xml");

        metadata.put(METADATA_DOCUMENT, root);
        metadata.put(IDENTIFIER, id);
        // load the metadata and add it to the array
        metadataList.add(metadata);
        return metadataList;
    }
}
