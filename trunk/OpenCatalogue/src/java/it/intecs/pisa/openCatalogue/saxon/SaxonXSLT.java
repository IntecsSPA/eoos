/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.saxon;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.functions.JavaExtensionLibrary;
import net.sf.saxon.s9api.*;


/**
 *
 * @author Andrea Marongiu
 */


public class SaxonXSLT {
     Processor processor = null;
     XsltCompiler compiler = null;
     Configuration configuration=null;

    public SaxonXSLT(){
        this.processor=new Processor(false);
        this.compiler=this.processor.newXsltCompiler();
    }

    public SaxonXSLT(SaxonURIResolver uriResolver){
        this.processor=new Processor(false);
        this.configuration=this.processor.getUnderlyingConfiguration();
        this.configuration.setURIResolver(uriResolver);
        this.configuration.setAllowExternalFunctions(true);
       /* this.configuration.setConfigurationProperty(FeatureKeys.TRACE_EXTERNAL_FUNCTIONS,
                                Boolean.TRUE);*/
        this.configuration.setExtensionBinder("java", new JavaExtensionLibrary(this.configuration));
        this.compiler=this.processor.newXsltCompiler();
        this.compiler.setCompileWithTracing(true);
    }


    public XdmNode getXdmNode(SAXSource xmlSAXSource) throws IOException, SaxonApiException{
       XdmNode source = this.processor.newDocumentBuilder().build(xmlSAXSource);
       return(source);
    }

    public  XsltExecutable getXsltExecutable(SAXSource xsltSAXSource) throws SaxonApiException{
       XsltExecutable exp=this.compiler.compile(xsltSAXSource);
       return exp;
    }
    /**
     * 	Perform an XSLT Saxon transformation and returns the
     * <code>PipedInputStream</code> of result.
     *
     * @param xmlURL XML Document Url.
     * @param xsltPath XSLT Url.
     * @throws javax.xml.xpath.IOException
     * @return Result as PipedInputStream.
     */
   /* public static PipedInputStream saxonXSLPipeTransform(URL xmlURL, URL xsltPath) throws IOException{
        PipedInputStream pipeIn = new PipedInputStream();
        final PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
         PrintStream ps = new PrintStream(pipeOut);
        new XSLTSaxonThread(xmlURL,xsltPath,ps);
        return pipeIn;
    }*/

   /**
     * 	Perform an XSLT Saxon transformation and returns the
     * <code>PipedInputStream</code> of result.
     *
     * @param xmlURL XML Document Url.
     * @param xsltPath XSLT Url.
     * @param xsltParameters XSLT Parameters Array.
     * @throws javax.xml.xpath.IOException
     * @return Result as PipedInputStream.
     */
    public PipedInputStream saxonXSLPipeTransform(SAXSource xml, SAXSource xslt,
           ArrayList<SaxonXSLTParameter> xsltParameters, String output) throws IOException, SaxonApiException{
      PipedInputStream pipeIn = new PipedInputStream();
      PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);
      XdmNode xmlSource=this.getXdmNode(xml);
      XsltExecutable xsltSource=this.getXsltExecutable(xslt);
      SaxonXSLTThread saxonThread=new SaxonXSLTThread(xmlSource, xsltSource,xsltParameters,output,pipeOut);
      saxonThread.start();
      return pipeIn;
    }


    






}
