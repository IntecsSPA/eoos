/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.intecs.pisa.openCatalogue.solr.ingester;

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
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactoryConfigurationException;
import net.sf.saxon.s9api.SaxonApiException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.MathTool;
import org.dom4j.io.SAXReader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class Ingester {

    private final String XPATH_SEPARATOR = "//separator";
    private final String XPATH_DATE_TIME_FORMAT = "//dateTimeFormat";
    private final String XPATH_SENSOR_TYPE = "//attribute[@id='sensorType']";
    private static final String DOLLAR = "$";
    public static final String FILENAME = "filename";
    private final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
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
    protected AbstractFilesystem metadataRepository;
    protected VelocityEngine ve;
    protected SaxonDocument configuration;
    protected String sensorType;
    protected String dateTimeFormat;
    protected String elements_separator;
    private solrHandler solr = null;
    private Map defaultMap = null;
    private Schematron schematron = null;
    private AbstractFilesystem schemaRoot = null;
    private AbstractFilesystem schemaFile = null;
    private static SchemaCache schemaCache = null;
    private String period_start;
    private String period_end;

    public void setConfiguration(AbstractFilesystem configDirectory, String configFileName) throws SAXException, IOException, SaxonApiException, Exception {
        ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, configDirectory.getAbsolutePath());

        configuration = new SaxonDocument(configDirectory.get(configFileName).getInputStream());

        sensorType = (!"".equals((String) configuration.evaluatePath(XPATH_SENSOR_TYPE + SLASH + TAG_INDEX_FIELD_NAME, XPathConstants.STRING))
                ? (DOLLAR + (String) configuration.evaluatePath(XPATH_SENSOR_TYPE + SLASH + TAG_INDEX_FIELD_NAME, XPathConstants.STRING))
                : (String) configuration.evaluatePath(XPATH_SENSOR_TYPE + SLASH + TAG_DEFAULT_VALUE, XPathConstants.STRING));

        dateTimeFormat = !"".equals((String) configuration.evaluatePath(XPATH_DATE_TIME_FORMAT, XPathConstants.STRING))
                ? ((String) configuration.evaluatePath(XPATH_DATE_TIME_FORMAT, XPathConstants.STRING))
                : DEFAULT_DATE_TIME_FORMAT;

        elements_separator = (String) configuration.evaluatePath(XPATH_SEPARATOR, XPathConstants.STRING);

        SAXBuilder saxBuilder = new SAXBuilder();
        org.jdom2.Document doc = saxBuilder.build(configDirectory.get(configFileName).getInputStream());

        Element rootElement = doc.getRootElement();
        defaultMap = new HashMap();
        generateDefaultMap(rootElement, defaultMap);
    }

    public void ingestDataFromDir(AbstractFilesystem dir) throws Exception {
        AbstractFilesystem[] files = dir.list(false, null);
        Log.info("Going to ingest " + files.length + " metadata");
        int fileNo= files.length;
        for (AbstractFilesystem file : files) {
            try {
                fileNo--;
                ingestData(file);
                Log.info("["+ fileNo +"] files remaining");
            } catch (Exception e) {
                Log.error("Failaed to ingest " + file.getName());
                Log.error(e.getMessage());
            }

        }
    }

    public void ingestData(AbstractFilesystem file) throws Exception {
        if (file.isFile()) {
            Log.info("Trying to ingest file " + file.getName());
            String fileName = file.getName();

            Document[] metadata = parse(file);

            for (Document doc : metadata) {
                boolean isValid = true;
                isValid = validateMetadata(doc);

                if (isValid) {
                    isValid = validateThroughSchematron();

                    if (isValid) {

                        uploadMetadataToSolr(doc);
                    } else {
                        Log.info("Metadata is not schematron valid.");
                    }
                } else {
                    Log.info("Metadata file " + fileName + " (or part of it) is not valid. Ingestion is skipped.");
                }

                storeMetadata(doc, isValid);
            }
        }
    }

    protected Document[] parse(AbstractFilesystem indexFile) throws Exception {
        Document metadata;
        InputStream stream = indexFile.getInputStream();

        SAXReader reader = new SAXReader();
        org.dom4j.Document doc = reader.read(stream);

        String key = "";
        org.dom4j.Element rootElement = doc.getRootElement();

        Map map = new HashMap(defaultMap);
        map.put(FILENAME, indexFile.getName().replaceAll(".xml", ""));

        generateMap2(rootElement, map);
        key = UUID.randomUUID().toString();
        metadata = generateMetadata(map, key);

        return new Document[]{metadata};
    }

    private void uploadMetadataToSolr(org.jdom2.Document metadata) throws IOException, SaxonApiException, Exception {
        if (solr != null) {
            VelocityContext context = new VelocityContext();
            context.put(VELOCITY_DATE, new DateTool());
            context.put(VELOCITY_MATH, new MathTool());
            context.put("coordinates", new CoordinatesUtil());
            context.put("metadataDocument", metadata);
            StringWriter swOut = new StringWriter();

            ve.getTemplate("generateSolrAddRequest.vm").merge(context, swOut);
            solr.postDocument(swOut.toString());
            // only for debug .... TODO - remove it
            XPathExpression<Element> xpath = XPathFactory.instance().compile("//*[local-name() = 'identifier']", Filters.element());
            String key = xpath.evaluateFirst(metadata.getRootElement()).getTextTrim();
            saveSolrfile(swOut.toString(), key);
            // only for debug .... TODO - remove it
            swOut.close();
        }
    }

    private void saveSolrfile(String swOut, String key) throws IOException {
        FileFilesystem fs = new FileFilesystem(metadataRepository.getAbsolutePath() + "/" + key + ".slr");
        fs.getOutputStream().write(swOut.getBytes());
    }

    private FileFilesystem storeMetadata(org.jdom2.Document metadata, boolean isValid) throws IOException {
        if (metadataRepository != null) {
            XPathExpression<Element> xpath = XPathFactory.instance().compile("//*[local-name() = 'identifier']", Filters.element());

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

    public org.jdom2.Document generateMetadata(Map metadataMap, String key) throws IOException, SaxonApiException, XPathFactoryConfigurationException, JDOMException {
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_DATE, new DateTool());
        context.put(VELOCITY_METADATA_LIST, metadataMap);
        context.put("coordinates", new CoordinatesUtil());
        context.put(VELOCITY_PERIOD_START, this.period_start);
        context.put(VELOCITY_PERIOD_END, this.period_end);
        context.put("KEY", key);

        StringWriter swOut = new StringWriter();
        String sType = sensorType.startsWith(DOLLAR) ? ((String) metadataMap.get(sensorType.substring(1))) : sensorType;
        getTemplate(sType).merge(context, swOut);

        SAXBuilder builder;
        builder = new SAXBuilder();
        org.jdom2.Document metadata = builder.build(new ByteArrayInputStream(swOut.toString().getBytes()));

        //SaxonDocument metadata = new SaxonDocument(swOut.toString());
        swOut.close();
        return metadata;
    }

    public Template getTemplate(String sType) {
        return ve.getTemplate(METADATA_REPORT_TEMPLATE.replace("sType", sType));
    }

    private static void generateMap2(org.dom4j.Element el, Map map) {
        String value = el.getTextTrim();

        Iterator i = el.elementIterator();
        if (!i.hasNext() && value != null) {
            //System.out.println("PATH:" + getXPath(el));
            map.put(getXPath(el), value);
        } else {
            while (i.hasNext()) {
                generateMap2((org.dom4j.Element) i.next(), map);
            }
        }
    }

    private static String getXPath(org.dom4j.Element el) {
        String path = el.getUniquePath();
        if (path.contains(":")) {
            String[] elements = path.split("/");
            path = "";
            for (int i = 0; i < elements.length; i++) {
                if (elements[i].contains(":")) {
                    path += "_" + elements[i].substring(elements[i].indexOf(":") + 1);
                } else if (!elements[i].equalsIgnoreCase("")) {
                    path += "_" + elements[i];
                }
            }
        } else {
            path = path.replace("/", "_").replace("[", "_").replace("]", "");
        }

        return path.substring(1);
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

    protected static void generateDefaultMap(Element el, Map map) {
        String key = "";
        List<Element> listEmpElement = el.getChildren();
        if (el.getName().equals("indexFieldName")) {
            key = el.getTextTrim();
//            System.out.println("key:" + key);
            map.put(key, "");
        } else {
            for (Element empElement : listEmpElement) {
                generateDefaultMap(empElement, map);
            }
        }
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
}
