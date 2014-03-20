/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.intecs.pisa.openCatalogue.solr.ingester;

import it.intecs.pisa.gis.util.CoordinatesUtil;
import it.intecs.pisa.log.Log;
import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.openCatalogue.saxon.SaxonDocument;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactoryConfigurationException;
import net.sf.saxon.s9api.SaxonApiException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.DateTool;
import org.dom4j.io.SAXReader;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.SAXException;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class Ingester extends BaseIngester {

    protected final String XPATH_SEPARATOR = "//separator";
    protected final String XPATH_DATE_TIME_FORMAT = "//dateTimeFormat";
    protected final String XPATH_SENSOR_TYPE = "//attribute[@id='sensorType']";
    protected static final String DOLLAR = "$";
    protected final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    protected Map defaultMap = null;
    private String period_start;
    private String period_end;

    public String configuration = "configuration.xml";
    public String oemVelocityTemplate="generateVelocityTemple_oem.xsl";
    
    @Override
    public void setConfiguration(AbstractFilesystem configDirectory) throws SAXException, IOException, SaxonApiException, Exception {
        super.setConfiguration(configDirectory);

        configDocument = new SaxonDocument(configDirectory.get(configuration).getInputStream());

        sensorType = (!"".equals((String) configDocument.evaluatePath(XPATH_SENSOR_TYPE + SLASH + TAG_INDEX_FIELD_NAME, XPathConstants.STRING))
                ? (DOLLAR + (String) configDocument.evaluatePath(XPATH_SENSOR_TYPE + SLASH + TAG_INDEX_FIELD_NAME, XPathConstants.STRING))
                : (String) configDocument.evaluatePath(XPATH_SENSOR_TYPE + SLASH + TAG_DEFAULT_VALUE, XPathConstants.STRING));

        dateTimeFormat = !"".equals((String) configDocument.evaluatePath(XPATH_DATE_TIME_FORMAT, XPathConstants.STRING))
                ? ((String) configDocument.evaluatePath(XPATH_DATE_TIME_FORMAT, XPathConstants.STRING))
                : DEFAULT_DATE_TIME_FORMAT;

        elements_separator = (String) configDocument.evaluatePath(XPATH_SEPARATOR, XPathConstants.STRING);

        SAXBuilder saxBuilder = new SAXBuilder();
        org.jdom2.Document doc = saxBuilder.build(configDirectory.get(configuration).getInputStream());

        Element rootElement = doc.getRootElement();
        defaultMap = new HashMap();
        generateDefaultMap(rootElement, defaultMap);
    }

    @Override
    protected Document[] parse(AbstractFilesystem indexFile,HashMap<String,String> queryHeaders) {
        try {
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
            metadata = generateMetadata(map, key,queryHeaders);

            return new Document[]{metadata};
        } catch (Exception e) {
            e.printStackTrace();
            return new Document[0];
        }
    }

    protected org.jdom2.Document generateMetadata(Map metadataMap, String key,HashMap<String,String> queryHeaders) throws IOException, SaxonApiException, XPathFactoryConfigurationException, JDOMException {
        VelocityContext context = new VelocityContext();
        context.put(VELOCITY_DATE, new DateTool());
        context.put(VELOCITY_METADATA_LIST, metadataMap);
        context.put("coordinates", new CoordinatesUtil());
        context.put(VELOCITY_PERIOD_START, this.period_start);
        context.put(VELOCITY_PERIOD_END, this.period_end);
        context.put("KEY", key);
        
        if(queryHeaders!=null)
        {
            context.put("queryValues",queryHeaders);
        }
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
        return ve.getTemplate("ingester/"+format+"/"+METADATA_REPORT_TEMPLATE.replace("sType", sType));
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

    @Override
    public void install(AbstractFilesystem folder) {
       try {
            createTemplate("RADAR", folder);
            createTemplate("LIMB", folder);
            createTemplate("ATMOSPHERIC", folder);
            createTemplate("ALTIMETRIC", folder);
            createTemplate("OPTICAL", folder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void createTemplate(String type, AbstractFilesystem workspace) throws Exception {
        AbstractFilesystem pluginFolder = workspace.get("ingester").get(format);
        Log.debug("Creating template "+type+" in plugin folder "+pluginFolder.getAbsolutePath());
        AbstractFilesystem stylesheet=workspace.get(oemVelocityTemplate);
        
        TransformerFactory tFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = tFactory.newTransformer(new StreamSource(new File(stylesheet.getAbsolutePath())));
            AbstractFilesystem outFile = pluginFolder.get("metadataReport" + type + ".vm");
            outFile.delete();
            transformer.setParameter("sType", type);
            transformer.transform(new StreamSource(new File(workspace.get(configuration).getAbsolutePath())), new StreamResult(new File(outFile.getAbsolutePath())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
