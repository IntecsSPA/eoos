/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.intecs.pisa.util.schematron;

import it.intecs.pisa.log.Log;
import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;
import it.intecs.pisa.metadata.filesystem.FileFilesystem;
import it.intecs.pisa.util.xml.XMLUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import org.probatron.Session;
import org.probatron.ValidationReport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author massi
 */
public class Schematron {

    //private String schematronURI;
    private Session schematronSession;

    public Schematron(String schURI) {
        FileFilesystem schFile = new FileFilesystem(schURI);
        schematronSession = new Session();
        schematronSession.setSchemaDoc(schFile.getUri());
    }

    public boolean Validate(Document toValidate, AbstractFilesystem repository, String metadataID) throws Exception {
        ValidationReport vr;
        boolean returnValue = true;                  
        File candidateFile;
        candidateFile = File.createTempFile("to_schematron_validate"+metadataID, ".xml");
        XMLUtils.saveToDisk(toValidate, candidateFile);
        vr = schematronSession.doValidation(candidateFile.getAbsolutePath());
        candidateFile.delete();

        Document result = XMLUtils.inputStreamToDocument(new ByteArrayInputStream(vr.reportAsBytes()));

        Element root = result.getDocumentElement();
        NodeList failed = root.getElementsByTagNameNS("http://purl.oclc.org/dsdl/svrl", "failed-assert");

        if (failed != null || failed.getLength() != 0) {
            AbstractFilesystem vaultFile = repository.get(metadataID + "_schematron_validation_error.xml");
            XMLUtils.saveToDisk(result, vaultFile.getOutputStream(), true);
            Log.error("Input file failed schematron validation. Schematron output has been dumped in " + vaultFile.getAbsolutePath());
            returnValue = false;
        }
        
        return returnValue;
    }

    
    
    public boolean Validate(AbstractFilesystem toValidate) throws Exception {
        ValidationReport vr;
        boolean returnValue = true;                  
        vr = schematronSession.doValidation(toValidate.getAbsolutePath());
        Document result = XMLUtils.inputStreamToDocument(new ByteArrayInputStream(vr.reportAsBytes()));

        Element root = result.getDocumentElement();
        NodeList failed = root.getElementsByTagNameNS("http://purl.oclc.org/dsdl/svrl", "failed-assert");

        if (failed != null || failed.getLength() != 0) {
            String fileName = toValidate.getName();
            AbstractFilesystem vaultFile = toValidate.getParent().get(fileName.substring(0, fileName.lastIndexOf(".")) + "_schematron_validation_error.xml");
            XMLUtils.saveToDisk(result, vaultFile.getOutputStream(), true);
            Log.error("Input file failed schematron validation. Schematron output has been dumped in " + vaultFile.getAbsolutePath());
            returnValue = false;
        }
        
        return returnValue;
    }

}
