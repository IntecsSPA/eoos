/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
 */
package it.intecs.pisa.metadata.converter;

import it.intecs.pisa.log.Log;
import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.metadata.filesystem.FileFilesystem;
import it.intecs.pisa.openCatalogue.solr.ingester.BaseIngester;
import it.intecs.pisa.openCatalogue.solr.ingester.IngesterFactory;
import java.io.*;

public class Converter {
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
        AbstractFilesystem workspace = null;
        AbstractFilesystem repository = null;
        String url = null;
        String inputType = "";
        File schRoot = null;
        File schFile = null;
        String schematron = "";

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-source=")) {
                toBeHarvested = new FileFilesystem(args[i].substring(8));
            } 
            else if (args[i].startsWith("-workspace=")) {
                workspace = new FileFilesystem(args[i].substring(11));
            }
            else if (args[i].startsWith("-schemaFile=")) {
                schFile = new File(args[i].substring(12));
            } 
            else if (args[i].startsWith("-schemaRoot=")) {
                schRoot = new File(args[i].substring(12));
            } 
            else if (args[i].startsWith("-solr=")) {
                url = args[i].substring(6);
            } 
            else if (args[i].startsWith("-repo=")) {
                repository = new FileFilesystem(args[i].substring(6));
            } 
            else if (args[i].startsWith("-format=")) {
                inputType = args[i].substring(8);
            } 
            else if (args[i].startsWith("-schematron=")) {
                schematron = args[i].substring(12);
            }

        }
        
        IngesterFactory.init(workspace);
        
        BaseIngester harv = IngesterFactory.fromInputType(inputType);
        harv.setConfiguration(workspace);
        harv.setSolrURL(url);
        harv.setMetedateRepository(repository);
        
        Log.info("Processing data...");
        harv.ingestDataFromDir(toBeHarvested);
        Log.info("Done.");
    }

    

    
}
