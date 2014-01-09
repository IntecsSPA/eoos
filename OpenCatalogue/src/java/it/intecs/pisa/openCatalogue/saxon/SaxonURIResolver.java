/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.saxon;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;

/**
 *
 * @author Andrea Marongiu
 */
public class SaxonURIResolver implements URIResolver {
   
   private String contextURL=null;
   private String contextPath=null;


  public SaxonURIResolver(String contextURL) throws URISyntaxException{
     this.contextURL = contextURL.toString();
  }

  public SaxonURIResolver(File contextPath) throws URISyntaxException, IOException{
     this.contextPath = contextPath.getCanonicalPath();
  }

     public Source resolve(String href, String base) throws TransformerException {
       if(this.contextURL!=null)
           return resolveURL(href, base);
       else
         if(this.contextPath!=null)
           return resolvePATH(href, base);
         else
           return null;

    }

     private Source resolveURL(String href, String base) throws TransformerException{
     Source sourceResult=resolveAbsolute(href,base);
     if(sourceResult != null)
         return sourceResult;

        URL sourceURL = null;
        try {
            sourceURL = new URL(this.contextURL + "/" + href);
        } catch (MalformedURLException ex) {
            throw new TransformerException("Malformed Internal Resource URL: " + this.contextURL + "/" + href);
        }
        try {
            sourceResult=new SAXSource(new InputSource(sourceURL.openStream()));
        } catch (IOException ex) {
            throw new TransformerException("Could not load internal Resource " + sourceURL.toString());
        }
         return sourceResult;
     }

     private Source resolvePATH(String href, String base) throws TransformerException{
         Source sourceResult=resolveAbsolute(href,base);
             if(sourceResult != null)
                 return sourceResult;

         String sourcePath = this.contextPath + "/" + href;
            try {
                sourceResult=new SAXSource(new InputSource(new FileInputStream(sourcePath)));
            } catch (IOException ex) {
                throw new TransformerException("Could not load internal Resource " + sourcePath);
            }
             return sourceResult;


     }


     private Source resolveAbsolute(String href, String base) throws TransformerException{
         if(href.startsWith("http://"))
            try {
                return new SAXSource(new InputSource((new URL(href)).openStream()));
           } catch (IOException ex) {
                throw new TransformerException("Resource URL Error: " +  href);
            }

         if(href.startsWith("file://"))
            try {
                String path=href.substring(7, href.length());
                return new SAXSource(new InputSource(new FileInputStream(path)));
           } catch (IOException ex) {
                throw new TransformerException("Resource URL Error: " +  href);
            }
         else
           return null;

     }

}
/*String base_path;
>   String nav_style;
>   public MyResolver(ServletContext context, String
> style) {
>     this.base_path =
> context.getRealPath("/WEB-INF/styling/");
>     this.nav_style = style;
>   }
*/