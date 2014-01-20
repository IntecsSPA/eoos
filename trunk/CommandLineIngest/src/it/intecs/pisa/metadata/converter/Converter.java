/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
 */
package it.intecs.pisa.metadata.converter;

import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.metadata.filesystem.FileFilesystem;
import it.intecs.pisa.openCatalogue.saxon.SaxonDocument;
import it.intecs.pisa.openCatalogue.solr.ingester.Ingester;
import it.intecs.pisa.openCatalogue.solr.ingester.IngesterFactory;
import it.intecs.pisa.openCatalogue.solr.solrHandler;
import it.intecs.pisa.util.schemas.SchemaCache;
import it.intecs.pisa.util.schematron.Schematron;
import java.io.*;
import java.util.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.velocity.app.VelocityEngine;

public class Converter {
    public static final String FILENAME = "filename";

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
   
    private VelocityEngine ve;
    private String sensorType;
    private AbstractFilesystem metadataRepository = null;
    private String dateTimeFormat;
    private solrHandler solr = null;
    private Map defaultMap = null;
    private File schemaRoot = null;
    private File schemaFile = null;
    private Schematron schematron = null;
   
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
        System.out.println("  -format=input file format. possible values are XML(default),CSV.  ");
        System.out.println("  -schematron=schematron_file optional parameter. Path to the schematron file to be used to validate the generated metadata");
        System.out.println("              NOTE to activate the schematron validation you have to provide a valid repo.");

    }

    /**
     * @param args the command line arguments
     * D:\Projects\SVN_checkout\eoos\trunk\web\WEB-INF\openSearch
     */
    public static void main(String[] args) throws Exception {
        /*
         * RUN CONFIGURATION -source=C:\catalogue_workspace\rdf\original
         * -conf=C:\catalogue_workspace\config\rdf
         * -confFile=ingestConfiguration_rdf.xml
         * -templGen=C:\Users\simone\Documents\NetBeansProjects\openCatalogue\web\WEB-INF\config\generateVelocityTemple_oem.xsl
         * -repo=C:\catalogue_workspace\rdf\repo
         * -schemaRoot="C:\Users\simone\Documents\NetBeansProjects\openCatalogue\web\WEB-INF\schemas\"
         * -schemaFile="C:\Users\simone\Documents\NetBeansProjects\openCatalogue\web\WEB-INF\schemas\ogc\all.xsd"
         * -solr="http://ergo.pisa.intecs.it/solr/ogc1"
         * -schematron="C:\catalogue_workspace\schematron\schematron_rules_for_eop.xml"
         */

        if (args.length < 4) {
            syntax();
            return;
        }
        AbstractFilesystem toBeHarvested = null;
        AbstractFilesystem transformer = null;
        String model = null;
        AbstractFilesystem configuration = null;
        AbstractFilesystem repository = null;
        String url = null;
        String inputType = "";
        File schRoot = null;
        File schFile = null;
        String schematron = "";

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-source=")) {
                toBeHarvested = new FileFilesystem(args[i].substring(8));
            } else if (args[i].startsWith("-templGen=")) {
                transformer = new FileFilesystem(args[i].substring(10));
            } else if (args[i].startsWith("-conf=")) {
                configuration = new FileFilesystem(args[i].substring(6));
            } else if (args[i].startsWith("-confFile=")) {
                model = args[i].substring(10);
            } else if (args[i].startsWith("-schemaFile=")) {
                schFile = new File(args[i].substring(12));
            } else if (args[i].startsWith("-schemaRoot=")) {
                schRoot = new File(args[i].substring(12));
            } else if (args[i].startsWith("-solr=")) {
                url = args[i].substring(6);
            } else if (args[i].startsWith("-repo=")) {
                repository = new FileFilesystem(args[i].substring(6));
            } else if (args[i].startsWith("-format=")) {
                inputType = args[i].substring(8);
            } else if (args[i].startsWith("-schematron=")) {
                schematron = args[i].substring(12);
            }

        }

        createVelocityTemplates(transformer, configuration, configuration.get(model));

        Ingester harv = IngesterFactory.fromInputType(inputType);
        harv.setConfiguration(configuration,model);
        harv.setSolrURL(url);
        harv.setMetedateRepository(repository);
        harv.ingestDataFromDir(toBeHarvested);
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
