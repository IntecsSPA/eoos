/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
 */
package it.intecs.pisa.openCatalogue.openSearch;

import it.intecs.pisa.gis.util.CoordinatesUtil;
import it.intecs.pisa.log.Log;
import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.openCatalogue.catalogue.ServletVars;
import it.intecs.pisa.openCatalogue.prefs.Prefs;
import it.intecs.pisa.openCatalogue.saxon.SaxonDocument;
import it.intecs.pisa.openCatalogue.saxon.SaxonURIResolver;
import it.intecs.pisa.openCatalogue.saxon.SaxonXSLT;
import it.intecs.pisa.openCatalogue.saxon.SaxonXSLTParameter;
import it.intecs.pisa.openCatalogue.solr.SolrHandler;
import it.intecs.pisa.util.DOMUtil;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
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
import org.apache.velocity.tools.generic.XmlTool;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
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

    private final static String ATOM_TEMPLATE = "atomResponse.vm";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private final static String CZML_TEMPLATE = "czmlResponse.vm";
    private final static String JSON_TEMPLATE = "jsonResponse.vm";
    private final static String KML_TEMPLATE = "kmlResponse.vm";
    private static final String VELOCITY_TOOL_CUSTOM_COORDINATES = "coordinates";
    private static final String VELOCITY_TOOL_DATE = "date";
    private static final String VELOCITY_TOOL_COORDINATES = "coordinates";
    private static final String VELOCITY_PRODUCT_URL_WCS = "productUrlWcs";
    private static final String VELOCITY_PRODUCT_URL_FTP = "productUrlFtp";
    private static final String VELOCITY_PRODUCT_URL_HTTP = "productUrlHttp";
    private static final String VELOCITY_PRODUCT_URL = "productUrl";
    private static final String VELOCITY_BROWSE_URL = "browseUrl";
    private static final String VELOCITY_METADATA_LIST = "metadataList";
    private static final String XPATH_NUM_FOUNDS = "//result/@numFound";
    private static final String XPATH_IDENTIFIER = "//doc[$$]/str[@name='id']";
    private static final String XPATH_POLYGON = "//doc[$$]/str[@name='posListOrig']";
    private static final String XPATH_METADATA = "//doc[$$]/str[@name='metadataOrig']";
    private static final String XPATH_COUNT_DOC = "count(//doc)";
    private static final String OPEN_SEARCH_START_INDEX = "OPEN_SEARCH_START_INDEX";
    private static final String OPEN_SEARCH_ITEMS_PER_PAGE = "OPEN_SEARCH_ITEMS_PER_PAGE";
    private static final String OPEN_SEARCH_NUMBER_OF_RESULTS = "OPEN_SEARCH_NUMBER_OF_RESULTS";
    private static final String OPEN_SEARCH_NEXT_RESULTS = "OPEN_SEARCH_NEXT_RESULTS";
    private static final String OPEN_SEARCH_LAST_RESULTS = "OPEN_SEARCH_LAST_RESULTS";
    private static final String OPEN_SEARCH_QUERY = "OPEN_SEARCH_QUERY";
    private static final String OPEN_SEARCH_REQUEST = "OPEN_SEARCH_REQUEST";
    private static final String OPEN_SEARCH_RECORD_SCHEMA = "OPEN_SEARCH_RECORD_SCHEMA";
    private static final String BASE_URL = "BASE_URL";
    private static final String IDENTIFIER = "identifier";
    private static final String POLYGON = "polygon";
    private static final String METADATA_DOCUMENT = "metadataDocument";
    private static final String METADATA_STRING = "metadataString";
    private static final String BROWSE_URL_KEY = "$browseUrlBasePath";
    private static final String PRODUCT_URL_WCS_KEY = "$productUrlWcsBasePath";
    private static final String PRODUCT_URL_HTTP_KEY = "$productUrlHttpBasePath";
    private static final String PRODUCT_URL_FTP_KEY = "$productUrlFtpBasePath";
    private static final String PRODUCT_URL_KEY = "$productUrlBasePath";
    private static final String XML_TOOL = "xmlTOOL";
    VelocityEngine ve;
    HashMap metadatas;
    AbstractFilesystem repository;
    SolrHandler solr;

    public OpenSearchHandler(AbstractFilesystem configDirectory, AbstractFilesystem repo, String solrEndPoint) {
        this.ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, configDirectory.getAbsolutePath());
        ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");

        this.repository = repo;
        this.metadatas = new HashMap();
        solr = new SolrHandler(solrEndPoint);
    }

    public void processAtomRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        Log.info("New ATOM request received");
        SaxonDocument solrResponse = sendRequestToSolr(request);
        sendBackAtomResponse(solrResponse, request, response);
    }

    public void processKmlRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, Exception {
        Log.info("New KML request received");
        SaxonDocument solrResponse = sendRequestToSolr(request);
        sendBackKmlResponse(solrResponse, request, response);
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
        //Select the OSDD for a specific collection. The collection name ie extracted from the URL
        //pattern http://localhost/opencat/service/opensearch/COLLECTION_ID/description.xml
        // or http://localhost/opencat/service/opensearch/COLLECTION_ID
        // if the collection is not provided it returns the template for the whole database
        String collection = requestURL.substring(requestURL.indexOf("opensearch")+11).replace("description.xml", "").replace("/", "");
        SaxonDocument descriptionSource = this.solr.getStats(collection);        
        //Document descriptionSource = IOUtil.getDocumentFromDirectory(ServletVars.appFolder + "/WEB-INF/openSearch/description.xml");
        DOMUtil domUtil = new DOMUtil();
        SaxonXSLT saxonUtil;
        PipedInputStream pipeInput;
        SaxonURIResolver uriResolver;
        ArrayList<SaxonXSLTParameter> parameters = new ArrayList();
        parameters.add(new SaxonXSLTParameter("url", requestURL.substring(0,requestURL.indexOf("service"))));
        SAXSource docSource = new SAXSource(new InputSource(new ByteArrayInputStream(descriptionSource.getXMLDocumentString().getBytes())));
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
        HashMap<String, String> params = getParametersHashMap(request);
        return solr.search(params);
    }

    private void sendBackAtomResponse(SaxonDocument solrResponse, HttpServletRequest request, HttpServletResponse response) throws IOException, XPathFactoryConfigurationException, XPathException, XPathExpressionException, SAXException, JDOMException, DocumentException {
        ArrayList metadataList = prepareDataForVelocity(solrResponse);
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_TOOL_DATE, new DateTool());
        context.put(VELOCITY_METADATA_LIST, metadataList);
        String numFound = (String) solrResponse.evaluatePath(XPATH_NUM_FOUNDS, XPathConstants.STRING);
        context.put(OPEN_SEARCH_NUMBER_OF_RESULTS, numFound);
        String requestURL = request.getRequestURL().toString();
        context.put(BASE_URL, requestURL.subSequence(0, requestURL.indexOf("/service")));
        String startIndex = request.getParameter("startIndex");
        String count = request.getParameter("count");

        int next = 1;
        if (count == null || count.equals("")) {
            count = "10";
        }

        int last = Integer.parseInt(numFound) / Integer.parseInt(count) * Integer.parseInt(count) + 1;

        if (startIndex != null && !startIndex.equals("")) {
            context.put(OPEN_SEARCH_START_INDEX, startIndex);
            next = Integer.parseInt(startIndex) + Integer.parseInt(count);
        } else if (request.getParameter("count") != null && request.getParameter("startPage") != null) {
            int itemsPerPage = Integer.parseInt(request.getParameter("count"));
            int pageNumber = Integer.parseInt(request.getParameter("startPage"));
            int startAt = itemsPerPage * pageNumber;
            next = startAt + Integer.parseInt(count);
            context.put(OPEN_SEARCH_START_INDEX, startAt);
        } else {
            context.put(OPEN_SEARCH_START_INDEX, "1");
        }
        String query = getQuery(request);
        String rs = request.getParameter("recordSchema");
        context.put(OPEN_SEARCH_RECORD_SCHEMA, rs);
        context.put(OPEN_SEARCH_QUERY, query);
        context.put(OPEN_SEARCH_LAST_RESULTS, last);
        context.put(OPEN_SEARCH_NEXT_RESULTS, next);
        context.put(OPEN_SEARCH_ITEMS_PER_PAGE, count);
        context.put(OPEN_SEARCH_REQUEST, request.getRequestURL());

        response.setContentType("application/atom+xml");
        Writer swOut = response.getWriter();
        ve.getTemplate(ATOM_TEMPLATE).merge(context, swOut);


        swOut.close();
    }

    private void sendBackKmlResponse(SaxonDocument solrResponse, HttpServletRequest request, HttpServletResponse response) throws IOException, XPathFactoryConfigurationException, XPathException, XPathExpressionException, SAXException, JDOMException, DocumentException {
        ArrayList metadataList = prepareDataForVelocity(solrResponse);
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_TOOL_DATE, new DateTool());
        context.put(VELOCITY_METADATA_LIST, metadataList);
        context.put("coordinates", new CoordinatesUtil());
        

//        response.setContentType("application/kml");
        response.setContentType("application/vnd.google-earth.kml+xml");
        Writer swOut = response.getWriter();
        ve.getTemplate(KML_TEMPLATE).merge(context, swOut);
        swOut.close();
    }
    
    
    private void sendBackCZMLResponse(SaxonDocument solrResponse, HttpServletRequest request, HttpServletResponse response) throws IOException, XPathFactoryConfigurationException, XPathException, XPathExpressionException, SAXException, JDOMException, DocumentException {
        ArrayList metadataList = prepareDataForVelocity(solrResponse);
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_TOOL_DATE, new DateTool());
        context.put(VELOCITY_TOOL_CUSTOM_COORDINATES, new CoordinatesUtil());
        context.put(VELOCITY_METADATA_LIST, metadataList);

        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        Writer swOut = response.getWriter();
        ve.getTemplate(CZML_TEMPLATE).merge(context, swOut);
        swOut.close();
    }

    private void sendBackJSONResponse(SaxonDocument solrResponse, HttpServletRequest request, HttpServletResponse response) throws XPathFactoryConfigurationException, XPathException, XPathExpressionException, IOException, SAXException, JDOMException, DocumentException {
        ArrayList metadataList = prepareDataForVelocity(solrResponse);
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_TOOL_DATE, new DateTool());

        HashMap<String, String> urls = Prefs.getURLBase();
        if (null != urls && !urls.isEmpty()) {
            for (String stringKey : urls.keySet()) {
                context.put(stringKey, urls.get(stringKey));
            }
        }

        context.put(VELOCITY_TOOL_COORDINATES, new CoordinatesUtil());
        context.put(VELOCITY_METADATA_LIST, metadataList);
        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        Writer swOut = response.getWriter();
        ve.getTemplate(JSON_TEMPLATE).merge(context, swOut);
        swOut.close();
    }

    private void sendBackResponse(String ID, HttpServletRequest request, HttpServletResponse response) throws IOException, XPathFactoryConfigurationException, XPathException, XPathExpressionException, SAXException, JDOMException {
        ArrayList metadataList = prepareDataForVelocity(ID);
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_TOOL_DATE, new DateTool());
        context.put(VELOCITY_METADATA_LIST, metadataList);
        context.put(OPEN_SEARCH_NUMBER_OF_RESULTS, 1);
        String requestURL = request.getRequestURL().toString();
        context.put(BASE_URL, requestURL);
        // we have to extract this from the request ...check also if solr returns this info in the response. 
        // If not we have to handle this in the prepareDataForVelocity
        context.put(OPEN_SEARCH_START_INDEX, 1);
        context.put(OPEN_SEARCH_ITEMS_PER_PAGE, 10);
        context.put(OPEN_SEARCH_REQUEST, request.getRequestURL());

        response.setContentType(CONTENT_TYPE_APPLICATION_JSON);
        Writer swOut = response.getWriter();
        ve.getTemplate(ATOM_TEMPLATE).merge(context, swOut);
        swOut.close();
    }

    private void sendBackProductResponse(SaxonDocument solrResponse, HttpServletRequest request, HttpServletResponse response) throws IOException, XPathFactoryConfigurationException, XPathException, XPathExpressionException, SAXException, JDOMException {
        String originalMetadata = this.getOriginalMetadata(solrResponse);
        

            HashMap<String, String> urls = Prefs.getURLBase();
            if (null != urls && !urls.isEmpty()) {
                for (String stringKey : urls.keySet()) {
                originalMetadata = originalMetadata.replace(stringKey, (String) urls.get(stringKey));
                }
            }

        /*
         * String urls = Prefs.getBrowseURLBase(); if (null != urls &&
         * !urls.equals("")) { // replace the string in the metadata
         * originalMetadata = originalMetadata.replace(BROWSE_URL_KEY,
         * urls); }
         *
         */
        response.setContentType("application/xml");
        Writer swOut = response.getWriter();
        if (!originalMetadata.contains("<?xml")) {
            swOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + originalMetadata);
        } else {
            swOut.write(originalMetadata);
        }
        swOut.close();
    }

    private ArrayList prepareDataForVelocity(SaxonDocument solrResponse) throws XPathFactoryConfigurationException, XPathException, XPathExpressionException, IOException, SAXException, JDOMException, DocumentException {
        ArrayList metadataList = new ArrayList();
        SAXBuilder builder;
        //Loop on the solrResponse and load the metadata in the repository 
        int results = Integer.parseInt((String) solrResponse.evaluatePath(XPATH_COUNT_DOC, XPathConstants.STRING));
        String id = "";
        String original_polygon = "";
        String cdata_field = "";
        for (int i = 1; i <= results; i++) {
            Map metadata = new HashMap();
            id = (String) solrResponse.evaluatePath(XPATH_IDENTIFIER.replace("$$", String.valueOf(i)), XPathConstants.STRING);
            original_polygon = (String) solrResponse.evaluatePath(XPATH_POLYGON.replace("$$", String.valueOf(i)), XPathConstants.STRING);
            builder = new SAXBuilder();
//            builder.setJDOMFactory( (JDOMFactory)new AnakiaJDOMFactory()); 
            cdata_field = (String) solrResponse.evaluatePath(XPATH_METADATA.replace("$$", String.valueOf(i)), XPathConstants.STRING);

            HashMap<String, String> urls = Prefs.getURLBase();
            if (null != urls && !urls.isEmpty()) {
                for (String stringKey : urls.keySet()) {
                    cdata_field = cdata_field.replace(stringKey, (String) urls.get(stringKey));
                    //System.out.println("stringKey=" +stringKey +" --> value="+(String) urls.get(stringKey));
                }
            }
            /*
             * String urls = Prefs.getBrowseURLBase(); if (null !=
             * urls && !urls.equals("")) { // replace the string in
             * the metadata cdata_field = cdata_field.replace(BROWSE_URL_KEY,
             * urls); }
             */
            SAXReader reader = new SAXReader();
            org.dom4j.Document root = reader.read(new StringReader(cdata_field));

//            XmlTool x = new XmlTool(root);
//            x.find("//*[local-name() = 'ProductInformation']//*[local-name() = 'ServiceReference']/@*[local-name() = 'href']");
//            x.find("//*[local-name() = 'ProductInformation']/*[local-name() = 'fileName']/*[local-name() = 'ServiceReference']/@*[local-name() = 'href']");

            /*
             * some example on how to use the xpath XmlTool x = new
             * XmlTool(root); x.find("//*[local-name() =
             * 'ServiceReference']/@*[local-name() = 'href']");
             * x.find("//*[local-name() = 'orbitType']/text()");
             * x.find("//*[local-name() = 'sensorType']/text()");
             * x.find("//*[local-name() = 'orbitType']/text()").isEmpty();
             * x.find("//*[local-name() = 'sensorType']/text()").isEmpty();
             */
            metadata.put(XML_TOOL, new XmlTool(root));
            metadata.put(METADATA_STRING, cdata_field.trim());
            metadata.put(METADATA_DOCUMENT, root);
            metadata.put(IDENTIFIER, id);
            metadata.put(POLYGON, original_polygon);
            // load the metadata and add it to the array
            metadataList.add(metadata);
        }
        return metadataList;
    }

    private String getOriginalMetadata(SaxonDocument solrResponse) throws XPathFactoryConfigurationException, XPathException, XPathExpressionException, IOException, SAXException, JDOMException {
        String cdata_field = (String) solrResponse.evaluatePath(XPATH_METADATA.replace("$$", "1"), XPathConstants.STRING);
        String toReturn = cdata_field;
        if (cdata_field.startsWith("<![CDATA[")) {
            toReturn = cdata_field.substring(cdata_field.indexOf("<![CDATA["), cdata_field.indexOf("]]>"));
        };
        return toReturn;
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

    private String getQuery(HttpServletRequest request) throws UnsupportedEncodingException {
        Enumeration params = request.getParameterNames();
        String q = "";
        String name;
        String value;

        while (params.hasMoreElements()) {
            name = (String) params.nextElement();
            value = request.getParameter(name);

            if (!name.equals("startIndex") && !name.equals("startPage") && !name.equals("count")) {
                q += name + "=" + value + "&amp;";
            }
        }

        return q;
    }

    private HashMap<String, String> getParametersHashMap(HttpServletRequest request) {
        HashMap<String, String> hash = new HashMap<String, String>();

        Enumeration<String> names = request.getParameterNames();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = request.getParameter(name);

            hash.put(name, value);
        }

        return hash;
    }
}
