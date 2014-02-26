/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.intecs.pisa.openCatalogue.solr.ingester;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.intecs.pisa.gis.util.CoordinatesUtil;
import it.intecs.pisa.log.Log;
import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.metadata.filesystem.FileFilesystem;
import it.intecs.pisa.openCatalogue.saxon.SaxonDocument;
import it.intecs.pisa.openCatalogue.solr.solrHandler;
import it.intecs.pisa.util.schemas.SchemaCache;
import it.intecs.pisa.util.schemas.SchemasUtil;
import it.intecs.pisa.util.schematron.Schematron;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import net.sf.saxon.s9api.SaxonApiException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.MathTool;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author massi
 */
public abstract class BaseIngester {

    public static final String FILENAME = "filename";
    public static final String HARVEST_FILE_NAME = "*.index";
    public static final String METADATA_REPORT_TEMPLATE = "metadataReportsType.vm";
    public static final String SLASH = "/";
    public static final String STRING_PERIOD = "##PERIOD##";
    public static final String TAG_DEFAULT_VALUE = "defaultValue";
    public static final String TAG_INDEX_FIELD_NAME = "indexFieldName";
    public static final String VELOCITY_DATE = "date";
    public static final String VELOCITY_MATH = "math";
    public static final String VELOCITY_METADATA_LIST = "metadata";
    public static final String VELOCITY_PERIOD_END = "PERIOD_END";
    public static final String VELOCITY_PERIOD_START = "PERIOD_START";
    public static final String VELOCITY_PLATFORM_SHORT_NAME = "PLATFORM_SHORT_NAME";
    public static final String VELOCITY_INSTRUMENT_SHORT_NAME = "INSTRUMENT_SHORT_NAME";
    public static final String VELOCITY_OPERATIONAL_MODE = "OPERATIONAL_MODE";
    public static final String VELOCITY_PRODUCT_TYPE = "PRODUCT_TYPE";
    public static final String BROWSE_FROM_HARVEST = "browse_from_harvest";

    protected static final String INGESTION_ITEM_STATUS_SUCCESS = "success";
    protected static final String INGESTION_ITEM_STATUS_FAILURE = "failure";
    protected static final String INGESTION_ITEM_STATUS_FAILURE_ON_VALLIDATION = "Item is not schema or schematron valid.";

    protected AbstractFilesystem metadataRepository;
    protected VelocityEngine ve;
    protected SaxonDocument configDocument;
    protected String sensorType;
    protected String dateTimeFormat;
    protected String elements_separator;
    protected solrHandler solr = null;
    private Schematron schematron = null;
    private AbstractFilesystem schemaRoot = null;
    private AbstractFilesystem schemaFile = null;
    private static SchemaCache schemaCache = null;
    

    /* These field are being set during initialization through reflection */
    public String idXPath ;
    public String solrRequestTemplate;
    public String format;
    

    public void setConfiguration(AbstractFilesystem configDirectory) throws SAXException, IOException, SaxonApiException, Exception {
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, configDirectory.getAbsolutePath());
    }

    public JsonObject ingestData(AbstractFilesystem file) throws Exception {
        if (file.isFile()) {
            Log.debug("Trying to ingest file " + file.getName());
            String fileName = file.getName();

            Document[] metadata = parse(file);
            HashMap<String, String> ingestionStatuses = new HashMap<String, String>();

            for (Document doc : metadata) {
                boolean isValid = true;
                String status = INGESTION_ITEM_STATUS_FAILURE_ON_VALLIDATION;

                isValid = validateMetadata(doc);

                if (isValid) {
                    isValid = validateThroughSchematron();

                    if (isValid) {
                        status = uploadMetadataToSolr(doc);
                    } else {
                        Log.error("Metadata is not schematron valid.");
                    }

                } else {
                    Log.error("Metadata file " + fileName + " (or part of it) is not valid. Ingestion is skipped.");
                }

                String itemId = getItemId(doc);
                ingestionStatuses.put(itemId, status);

                storeMetadata(doc, isValid);
            }

            return createIngestionResponse(ingestionStatuses);
        }
        return null;
    }

    public void ingestDataFromDir(AbstractFilesystem dir) throws Exception {
        AbstractFilesystem[] files = dir.list(false, null);
        Log.debug("Going to ingest " + files.length + " metadata");
        int fileNo = files.length;
        for (AbstractFilesystem file : files) {
            try {
                fileNo--;
                ingestData(file);
                Log.debug("[" + fileNo + "] files remaining");
            } catch (Exception e) {
                Log.error("Failed to ingest " + file.getName());
                Log.error(e.getMessage());
            }

        }
    }

    protected String uploadMetadataToSolr(org.jdom2.Document metadata) throws IOException, SaxonApiException, Exception {
        int retValue = 500;

        if (solr != null) {
            VelocityContext context = new VelocityContext();
            context.put(VELOCITY_DATE, new DateTool());
            context.put(VELOCITY_MATH, new MathTool());
            context.put("coordinates", new CoordinatesUtil());
            context.put("metadataDocument", metadata);
            StringWriter swOut = new StringWriter();

            ve.getTemplate(solrRequestTemplate).merge(context, swOut);
            retValue = solr.postDocument(swOut.toString());
            // only for debug .... TODO - remove it
            String key = getItemId(metadata);
            saveSolrfile(swOut.toString(), key);
            // only for debug .... TODO - remove it
            swOut.close();
        }

        return retValue == 200 ? INGESTION_ITEM_STATUS_SUCCESS : INGESTION_ITEM_STATUS_FAILURE;
    }

    private boolean validateThroughSchematron() {
        //temporarily disabled
        /*
         * if (metadataRepository != null) { AbstractFilesystem metadataFile =
         * storeMetadata(metadata, isValid); if (schematron != null) {
         * schematron.Validate(metadataFile); }
         }
         */

        return true;

    }

    protected JsonObject createIngestionResponse(HashMap<String, String> ingestionStatuses) {
        JsonObject response = new JsonObject();
        JsonArray array = new JsonArray();
        

        int success=0;
        int failure=0;
        int total=ingestionStatuses.size();
        
        for (String key : ingestionStatuses.keySet()) {
            String itemStatus=ingestionStatuses.get(key);
            
            if(itemStatus.equals("success")==false)
            {
                JsonObject item = new JsonObject();
                item.addProperty("id", key);
                item.addProperty("status", ingestionStatuses.get(key));

                array.add(item);
                
                failure++;
            }
            else success++;
        }
        
        if(array.size()>0)
            response.add("report", array);
        
        response.addProperty("total", total);
        response.addProperty("success", success);
        response.addProperty("failure",failure);
        return response;
    }

    private void saveSolrfile(String swOut, String key) throws IOException {
        Log.debug(swOut);
        if (metadataRepository != null) {
            FileFilesystem fs = new FileFilesystem(metadataRepository.getAbsolutePath() + "/" + key + ".slr");
            fs.getOutputStream().write(swOut.getBytes());
        }
    }

    private FileFilesystem storeMetadata(org.jdom2.Document metadata, boolean isValid) throws IOException {
        if (metadataRepository != null) {
            XPathExpression<Element> xpath = XPathFactory.instance().compile(idXPath, Filters.element());

            String key = xpath.evaluateFirst(metadata.getRootElement()).getTextTrim();
            FileFilesystem fs = null;
            if (isValid) {
                fs = new FileFilesystem(metadataRepository.getAbsolutePath() + "/" + key + ".xml");
            } else {
                fs = new FileFilesystem(metadataRepository.getAbsolutePath() + "/" + key + ".notValid.xml");
            }
            XMLOutputter outputter = new XMLOutputter();
            String metadataString = outputter.outputString(metadata);
            byte[] b = metadataString.getBytes();
            fs.getOutputStream().write(b);
            return fs;
        } else {
            return null;
        }
    }

    private boolean validateMetadata(Document metadata) {
        if (schemaRoot != null && schemaFile != null) {
            try {
                XMLOutputter outputter = new XMLOutputter();
                String metadataString = outputter.outputString(metadata);
                byte[] b = metadataString.getBytes();
                return SchemasUtil.SAXvalidate(new ByteArrayInputStream(b), schemaFile, schemaRoot, schemaCache);
            } catch (Exception e) {
                return false;
            }
        } else {
            return true;
        }
    }

    public void setSchema(AbstractFilesystem schemaFolder, AbstractFilesystem schema) {
        schemaRoot = schemaFolder;
        schemaFile = schema;

        if (schemaRoot != null && schemaFile != null) {
            schemaCache = new SchemaCache(schemaRoot);
        }
    }

    public void setSolrURL(String solrURL) {
        if (solrURL != null && !solrURL.equals("")) {
            solr = new solrHandler(solrURL);
        }
    }

    public void setSchematronURI(String schematronURI) {
        if (schematronURI != null && !schematronURI.equals("")) {
            schematron = new Schematron(schematronURI);
        }
    }

    public void setRunsOnTomcat() {
        if (ve != null) {
            ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
        }
    }

    public void setMetedateRepository(AbstractFilesystem repo) {
        if (repo != null) {
            this.metadataRepository = repo;
            this.metadataRepository.mkdirs();
        }
    }

    protected String getItemId(Document doc) {
        XPathExpression<Element> xpath = XPathFactory.instance().compile(idXPath, Filters.element());
        String key = xpath.evaluateFirst(doc.getRootElement()).getTextTrim();
        return key;
    }

    /**
     * This method parses the input file and provides an OEM version of data
     *
     * @param file
     * @return
     */
    protected abstract Document[] parse(AbstractFilesystem file);

    public void install(AbstractFilesystem folder) {}

}
