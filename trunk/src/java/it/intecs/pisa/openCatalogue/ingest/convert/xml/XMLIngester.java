/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
 */
package it.intecs.pisa.openCatalogue.ingest.convert.xml;

import it.intecs.pisa.gis.util.CoordinatesUtil;
import it.intecs.pisa.openCatalogue.filesystem.AbstractFilesystem;
import it.intecs.pisa.openCatalogue.filesystem.FileFilesystem;
import it.intecs.pisa.openCatalogue.saxon.SaxonDocument;
import it.intecs.pisa.openCatalogue.solr.solrHandler;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactoryConfigurationException;
import net.sf.saxon.s9api.SaxonApiException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.MathTool;
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
 * This class is in charge of performing the harvesting operations. The
 * harvesting process is triggered automatically on a specific basket according
 * to its configuration. The process can also be triggered manually by the ngEO
 * administrator (e.g. for data synchronisation). Once launched, the harvesting
 * process performs the following actions: <ul>Connection to the basket to be
 * harvested. <li>If the basket contains an index.txt file, it is processed</li>
 * <li>The process, then, scans recursively all sub-baskets looking for
 * additional index.txt files to process. Sub-baskets whose name starts with "."
 * or is listed in the excluded directories list in configuration are not
 * processed.</li> </ul> For every index.txt file found, the following process
 * is performed: <ul> <li>Index file is retrieved and the harvester start
 * scanning each line. A specific a attribute in the index file (to be
 * configured) gives, for every record/product, the date when it was added or
 * updated (e.g. LAST_UPDATE). After an harvesting session, the FEED remembers
 * the most recent of these dates (among all records that satisfy the filter
 * configured for the harvesting service). During the next harvesting session,
 * only the metadata of records which have a LAST_DATE value greater than the
 * saved date (i.e more recent) are processed.</li> <li>Header record is
 * analysed. Its information is compared with configuration data in order to
 * determine the completeness of the information set (i.e. if valid product
 * metadata can be generated using values from the index files and
 * default/computed values according to configuration. In case of an error
 * raised during header record analysis, the processing is cancelled for this
 * specific index.txt file and an error is stored for notification.</li> <li>
 * Every subsequent record is then processed:</li> <ul> <li>In the case of a
 * "period record": Period is retrieved and stored for the corresponding
 * collection of product.</li> <li>In the case of a "metadata record": Metadata
 * values are retrieved and mapped according to the header declarations.</li>
 * </ul> <li> The corresponding EO Product metadata following [ngEO-MDGICD]
 * format is generated and stored and grouped by native dataset (i.e. if an
 * index file contains metadata records corresponding to several collection of
 * products, they are stored separately). </li> <li>If metadata record contains
 * browse references (i.e. the location of browse image and metadata
 * files):</li> <ul> <li>The two browse files are retrieved by the harvesting
 * process and the two files are shipped in a single zip file according to
 * [ngEOBRGICD]. The zip file is then copied in the browse ingestion basket
 * declared in configuration.</li> <li>Once all records have been processed, a
 * bulk update report complying with the IF-ngEOMetadataReport format of
 * [ngEO-MDGICD] is generated for every collection of product: </ul> </ul>
 *
 * @author simone
 */
public class XMLIngester {

    public static final String HARVEST_FILE_NAME = "*.index";
    public static final String DOLLAR = "$";
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
    private final String KEY_SEPARATOR = ":";
    private final String MAP_KEY_SEPARATOR = "=";
    private final String DEFAULT_CONFIGURATION_FILE_NAME = "harvestConfiguration.xml";
    private String ELEMENTS_SEPARATOR;
    private final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private String PERIOD_START;
    private String PERIOD_END;
    private final String XPATH_SEPARATOR = "//separator";
    private final String XPATH_DATE_TIME_FORMAT = "//dateTimeFormat";
    private final String XPATH_PLATFORM_SHORT_NAME = "//attribute[@id='platformShortName']";
    private final String XPATH_INSTRUMENT_SHORT_NAME = "//attribute[@id='instrumentShortName']";
    private final String XPATH_OPERATIONAL_MODE = "//attribute[@id='operationalMode']";
    private final String XPATH_PRODUCT_TYPE = "//attribute[@id='productType']";
    private final String XPATH_SENSOR_TYPE = "//attribute[@id='sensorType']";
    private final String XPATH_BROWSE_IMAGE_LOCATION = "//attribute[@id='browseImageLocation']";
    private final String XPATH_BROWSE_METADATA_LOCATION = "//attribute[@id='browseMetadataLocation']";
    private final String XPATH_TIME_FILTER = "//timeFilter";
    private SaxonDocument configuration;
    private VelocityEngine ve;
    private String sensorType;
    private AbstractFilesystem metadataRepository = null;
    private String dateTimeFormat;
    private solrHandler solr = null;

    /**
     * Displays startup command syntax.
     *
     * @param cmd Name of the startup command (i.e. test.bat or test.sh)
     */
    static void syntax() {
        System.out.println();
        System.out.println("Metadata Converter:");
        System.out.println("  converter -source=dir -conf=configDirectory -confFile=configurationFileName -tmplGen=templateGenerator [-solr=solrUrl] [-repo=metadata_repository]  ");
        System.out.println("where:");
        System.out.println("  -source=dir directory containing the metadata to be harvested");
        System.out.println("  -conf=configDirectory directory which should contain the configuration file (i.e. confFile). The directory is also used to store the metadata template generated.");
        System.out.println("  -confFile=configurationFileName name of the file containing the mapping between the original metadata and the metadata to be generated");
        System.out.println("  -tmplGen=templateGenerator xslt file used to generate the metadata template starting from the mapping file");
        System.out.println("  -solr=solrUrl optional parameter. URL of a solar instance containing the ogc schema");
        System.out.println("  -repo=metadata_repository optional parameter. Path to the folder where the result metadata have to be stored.  ");
    }

    /**
     * @param args the command line arguments
     * D:\Projects\SVN_checkout\eoos\trunk\web\WEB-INF\openSearch
     */
    public static void main(String[] args) throws Exception {


        if (args.length < 4) {
            syntax();
            return;
        }
        AbstractFilesystem toBeHarvested = null;
        AbstractFilesystem transformer = null;
        AbstractFilesystem model = null;
        AbstractFilesystem configuration = null;
        AbstractFilesystem repository = null;
        createVelocityTemplates(transformer, configuration, model);
        String url = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-source=")) {
                toBeHarvested = new FileFilesystem(args[i].substring(8));
            }else if (args[i].startsWith("-tmplGen=")) {
                transformer = new FileFilesystem(args[i].substring(9));
            }else if (args[i].startsWith("-conf=")) {
                configuration = new FileFilesystem(args[i].substring(6));
            }else if (args[i].startsWith("-confFile=")) {
                model = new FileFilesystem(args[i].substring(10));
            }else if (args[i].startsWith("-solr=")) {
                url = args[i].substring(6);
            }else if (args[i].startsWith("-repo=")) {
                repository = new FileFilesystem(args[i].substring(6));
            }
        }


        //RdfIngester harv = new XMLIngester(repository, configuration, url, isTomcatCall);
        XMLIngester harv = new XMLIngester(repository, configuration, "ingestConfiguration_rdf.xml", url, false);
        harv.ingestDataFromDir(toBeHarvested);
    }

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
    public XMLIngester(AbstractFilesystem metadataRepository, AbstractFilesystem configDirectory, String configFileName, String solrURL, boolean runsOnTomcat) throws SAXException, IOException, SaxonApiException, Exception {
        if (metadataRepository != null) {
            this.metadataRepository = metadataRepository;
            this.metadataRepository.mkdirs();
        }

        this.ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
        ve.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, configDirectory.getAbsolutePath());

        if (runsOnTomcat) {
            ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
        }

        this.configuration = new SaxonDocument(configDirectory.get(configFileName).getInputStream());

        this.sensorType = (!"".equals((String) configuration.evaluatePath(XPATH_SENSOR_TYPE + SLASH + TAG_INDEX_FIELD_NAME, XPathConstants.STRING))
                ? (DOLLAR + (String) configuration.evaluatePath(XPATH_SENSOR_TYPE + SLASH + TAG_INDEX_FIELD_NAME, XPathConstants.STRING))
                : (String) configuration.evaluatePath(XPATH_SENSOR_TYPE + SLASH + TAG_DEFAULT_VALUE, XPathConstants.STRING));

        this.dateTimeFormat = !"".equals((String) configuration.evaluatePath(XPATH_DATE_TIME_FORMAT, XPathConstants.STRING))
                ? ((String) configuration.evaluatePath(XPATH_DATE_TIME_FORMAT, XPathConstants.STRING))
                : DEFAULT_DATE_TIME_FORMAT;

        this.ELEMENTS_SEPARATOR = (String) configuration.evaluatePath(XPATH_SEPARATOR, XPathConstants.STRING);
        if (solrURL != null && !solrURL.equals("")) {
            solr = new solrHandler(solrURL);
        }
    }

    /**
     * This functions perform the real harvesting. It loads from the
     * configuration file the list of directories to be excluded from the scan
     * process and invoke the FileSystemScanner.scan to collect the list of file
     * to be processed Then invoke the parse() function to generate a set of
     * HashMaps (one for each dataset) that will be used in the
     * generateOutputFiles() function to generate the metadata report (via a
     * velocity template).
     *
     * @return the data of the last (represented as long) harvested metadata
     * @throws Exception
     */
    public void ingestDataFromDir(AbstractFilesystem dir) throws Exception {
        AbstractFilesystem[] files = dir.list(false, null);
        for (AbstractFilesystem file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                String uri = file.getUri();
                //setStartStopTime(fileName);
                parse(file);
            }
        }
    }

    public void ingestData(AbstractFilesystem file) throws Exception {
        if (file.isFile()) {
            String fileName = file.getName();
            String uri = file.getUri();
            //setStartStopTime(fileName);
            parse(file);
        }
    }

    public void ingestDataFromStream(AbstractFilesystem streamFile) throws Exception {
        parse(streamFile);
    }

    /**
     * This class generates the ouput files (one for each dataset identified in
     * the index file) via a velocity template (a different template for each
     * sensorType is available) generated at configuration time.
     *
     * @param outputDirectory path to the directory where the ouput metadata
     * report will be stored (i.e this should be an input directory for the
     * polling mechanism)
     * @throws IOException
     */
    public org.jdom2.Document generateMetadata(Map metadataMap, String key) throws IOException, SaxonApiException, XPathFactoryConfigurationException, JDOMException {
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_DATE, new DateTool());
        context.put(VELOCITY_METADATA_LIST, metadataMap);
        context.put(VELOCITY_PERIOD_START, this.PERIOD_START);
        context.put(VELOCITY_PERIOD_END, this.PERIOD_END);
        context.put("KEY", key);

        StringWriter swOut = new StringWriter();
        String sType = sensorType.startsWith(DOLLAR) ? ((String) metadataMap.get(sensorType.substring(1))) : sensorType;
        this.getTemplate(sType).merge(context, swOut);

        SAXBuilder builder;
        builder = new SAXBuilder();
        org.jdom2.Document metadata = builder.build(new ByteArrayInputStream(swOut.toString().getBytes()));

        //SaxonDocument metadata = new SaxonDocument(swOut.toString());
        swOut.close();
        return metadata;
    }

    //Get JDOM document from SAX Parser
    private static org.jdom2.Document useSAXParser(InputStream stream) throws JDOMException,
            IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        return saxBuilder.build(stream);
    }

    /**
     * This function parses the indexFile passed as input and for each line:
     * <ul> <li>In the case of a "period record": Period is retrieved and stored
     * for the corresponding collection of product.</li> <li>In the case of a
     * "metadata record": Metadata values are retrieved and mapped according to
     * the header declarations and stored in a Map corresponding to the
     * dataset.</li> </ul> If metadata record contains browse references (i.e.
     * the location of browse image and metadata files) the two browse files are
     * retrieved by the harvesting process and the two files are shipped in a
     * single zip file according to [ngEOBRGICD]. The zip file is then copied in
     * the browse ingestion basket declared in configuration.</li>
     *
     * @param indexFile
     * @throws FileNotFoundException
     * @throws IOException
     * @throws Exception
     */
    public void parse(AbstractFilesystem indexFile) throws FileNotFoundException, IOException, Exception {
        // open XML
        // navigate XML and store the value of the leaf in an HASH MAP using as key the PATH created using the local names separated by an _


        Document metadata;
        InputStream stream = indexFile.getInputStream();
        // set the start and stop period        
        String key = "";
        org.jdom2.Document doc = useSAXParser(stream);

        //Get list of Employee element
        Element rootElement = doc.getRootElement();
        Map map = new HashMap();
        String path = "";
        generateMap(rootElement, map, path);

        key = UUID.randomUUID().toString();
        metadata = generateMetadata(map, key);
        if (metadataRepository != null) {
            storeMetadata(metadata);
        }
        if (solr != null) {
            uploadMetadataToSolr(metadata);
        }
    }

    private static void generateMap(Element el, Map map, String path) {
        String value = el.getTextTrim();
        List<Element> listEmpElement = el.getChildren();
        if (listEmpElement.isEmpty() && value != null) {
//                System.out.println("PATH:"+path.substring(1));
            map.put(path.substring(1), value);
        } else {
            for (Element empElement : listEmpElement) {
                generateMap(empElement, map, path + "_" + empElement.getName());
            }
        }
    }

    ;
    
    
    /**
     * This function convert a string s into an array of strings using sep as
     * separator
     *
     * @param s string to be split
     * @param sep separator
     * @return
     */
    public static String[] stringtoArray(String s, String sep) {
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

    /**
     * This function load the correct template in the VelocityEngine. The
     * template is loaded according to the sensor type (sType) passed as input
     *
     * @param sType sensor type to be used to load the correct template. In te
     * current implementation this can be RADAR, LIMB, OPTICAL, ATMOSPHERIC,
     * ALTIMETRIC.
     * @return the template
     */
    public Template getTemplate(String sType) {
        return ve.getTemplate(METADATA_REPORT_TEMPLATE.replace("sType", sType));
    }

    /**
     * @param metadataBasket the metadataBasket to set
     */
    private void setStartStopTime(String relativePath) throws ParseException {
        String filename = relativePath.substring((relativePath.replace("\\", "/")).lastIndexOf("/") + 1, relativePath.lastIndexOf(".index"));
        String[] values = stringtoArray(filename, "_");
        DateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        DateFormat ddf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = sdf.parse(values[0]);
        this.PERIOD_START = ddf.format(date);
        date = sdf.parse(values[1]);
        this.PERIOD_END = ddf.format(date);
    }

    private void storeMetadata(org.jdom2.Document metadata) throws IOException {
        XPathExpression<Element> xpath = XPathFactory.instance().compile("//*[local-name() = 'identifier']", Filters.element());
        String key = xpath.evaluateFirst(metadata.getRootElement()).getTextTrim();

        FileFilesystem fs = new FileFilesystem(metadataRepository.getAbsolutePath() + "/" + key + ".xml");
        XMLOutputter outputter = new XMLOutputter();
        String metadataString = outputter.outputString(metadata);
        byte[] b = metadataString.getBytes();
        fs.getOutputStream().write(b);
    }

    private void uploadMetadataToSolr(org.jdom2.Document metadata) throws IOException, SaxonApiException, Exception {
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

    private void saveSolrfile(String swOut, String key) throws IOException {
        FileFilesystem fs = new FileFilesystem(metadataRepository.getAbsolutePath() + "/" + key + ".slr");
        fs.getOutputStream().write(swOut.getBytes());
    }

    public static void createVelocityTemplates(AbstractFilesystem transformer, AbstractFilesystem outputFolder, AbstractFilesystem model) {
        try {
            createTemplate("RADAR", transformer, outputFolder, model);
            createTemplate("LIMB", transformer, outputFolder, model);
            createTemplate("ATMOSPHERIC", transformer, outputFolder, model);
            createTemplate("ALTIMETRIC", transformer, outputFolder, model);
            createTemplate("OPTICAL", transformer, outputFolder, model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void createTemplate(String type, AbstractFilesystem stylesheet, AbstractFilesystem processing, AbstractFilesystem modelFile) throws Exception {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = tFactory.newTransformer(new StreamSource(new File(stylesheet.getAbsolutePath())));
            AbstractFilesystem outFile = processing.get("metadataReport" + type + ".vm");
            outFile.delete();
            transformer.setParameter("sType", type);
            transformer.transform(new StreamSource(new File(modelFile.getAbsolutePath())), new StreamResult(new File(outFile.getAbsolutePath())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
