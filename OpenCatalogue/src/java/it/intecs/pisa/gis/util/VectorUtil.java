/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/package it.intecs.pisa.gis.util;

import it.intecs.pisa.util.DOMUtil;
import it.intecs.pisa.util.IOUtil;
import it.intecs.pisa.util.xml.XMLUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 * @author Andrea Marongiu
 */
public class VectorUtil {

  protected static final String SLD_NAMESPACE="http://www.opengis.net/sld";
  protected static final String SLD_ELEMENT="StyledLayerDescriptor";
  protected static final String SLD_NAMED_LAYER_ELEMENT="NamedLayer";
  protected static final String SLD_USER_STYLE_ELEMENT="UserStyle";
  protected static final String SLD_USER_STYLE_NAME_ELEMENT="Name";

  public static String createSHPZipDeployName (String shpZipFile, String deployName) throws Exception{
      ZipFile zipFile=null;
      zipFile = new ZipFile(shpZipFile);
      Enumeration entries;
      String fullpath;
      File temp=null, current=null;
      List <File> fileList=new ArrayList <File> ();
      int i=0;
      String entryFileSuffix,entryFileName;

      entries = zipFile.entries();
      temp=IOUtil.getTemporaryDirectory();
      try
      {
      while(entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry)entries.nextElement();
        entryFileName=entry.getName();
        entryFileSuffix=entryFileName.substring(entryFileName.indexOf("."));
        if(entryFileSuffix.equalsIgnoreCase(".shx") ||
           entryFileSuffix.equalsIgnoreCase(".shp") ||
           entryFileSuffix.equalsIgnoreCase(".prj") ||
           entryFileSuffix.equalsIgnoreCase(".dbf") ){
           current=new File(temp,deployName+entryFileSuffix);
           IOUtil.copy(zipFile.getInputStream(entry), new FileOutputStream(current));
           fileList.add(current);
        }
      }
      zipFile.close();
      }
      catch(Throwable t)
      {
          t.printStackTrace();
      }
      i=0;
      int len;
      byte[] buf = new byte[1024];
      //File newShpZip=new File(td.getLogDir()+"/temp/"+deployName+".zip");

      File newShpZip=new File(temp, deployName+".zip");
      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(newShpZip));
      while(i<fileList.size()){
          fullpath = fileList.get(i).getAbsolutePath();
          FileInputStream in = new FileInputStream(fullpath);
          out.putNextEntry(new ZipEntry(fileList.get(i).getName()));
          while ((len = in.read(buf)) > 0) {
              out.write(buf, 0, len);
          }
          out.closeEntry();
          in.close();
          fileList.get(i).delete();
          i++;
      }
      out.close();
   return newShpZip.getAbsolutePath();
  }


  public static String createSLDDeployName (String sldFilePath, String deployName) throws Exception{
    DOMUtil du= new DOMUtil();
    Document sldDocument=du.fileToDocument(new File(sldFilePath));
    File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
    Element userStyleNameElementNew= null;
    Element sldElement=(Element) sldDocument.getElementsByTagNameNS(SLD_NAMESPACE,SLD_ELEMENT).item(0);
    Element namedLayerElement=(Element) sldElement.getElementsByTagNameNS(SLD_NAMESPACE,SLD_NAMED_LAYER_ELEMENT).item(0);
    Element userStyleElement=(Element) namedLayerElement.getElementsByTagNameNS(SLD_NAMESPACE,SLD_USER_STYLE_ELEMENT).item(0);
    Element userStyleNameElement=(Element) userStyleElement.getElementsByTagNameNS(SLD_NAMESPACE,SLD_USER_STYLE_NAME_ELEMENT).item(0);

    if(userStyleNameElement != null){
        userStyleElement.removeChild(userStyleNameElement);

    }
    userStyleNameElementNew=sldDocument.createElementNS(SLD_NAMESPACE,SLD_USER_STYLE_NAME_ELEMENT);
    userStyleNameElementNew.setTextContent(deployName);
      userStyleElement.appendChild(userStyleNameElementNew);

    File newSLDDocument=new File(temp, deployName+".sld");
    XMLUtils.saveToDisk(sldDocument, newSLDDocument);
    return newSLDDocument.getAbsolutePath();
  }

   

}
