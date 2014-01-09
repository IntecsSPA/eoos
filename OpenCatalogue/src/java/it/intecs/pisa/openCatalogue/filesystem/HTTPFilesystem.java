/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.filesystem;

import it.intecs.pisa.openCatalogue.filters.IFileFilter;
import it.intecs.pisa.util.IOUtil;
import it.intecs.pisa.util.http.HttpUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import org.apache.commons.net.ftp.FTPFile;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class HTTPFilesystem implements AbstractFilesystem {

    protected String currentUri;
    protected String host, username, password, path;
    protected int port;
    protected boolean isFile;
    
    public HTTPFilesystem(String uri) throws MalformedURLException, IOException, Exception {
        currentUri = uri;
        host = ConnectionUriParser.getHost(uri);
        username = ConnectionUriParser.getUsername(uri);
        password = ConnectionUriParser.getPassword(uri);
        path = ConnectionUriParser.getPath(uri);

        int parsedPort = ConnectionUriParser.getPort(uri);
        port = parsedPort == -1 ? 80 : parsedPort;
        
        doHead();
    }

    @Override
    public AbstractFilesystem[] list() {
        return list(false, null);
    }

    @Override
    public boolean delete() {
        try {
            return rmdir();
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public boolean rename(String name) {
        return false;
    }

    @Override
    public InputStream getInputStream() {
        try {
            URL url=new URL(currentUri);
            return url.openStream();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public AbstractFilesystem get(String name) {
        try {
            String newUri=currentUri.endsWith("/")?currentUri:currentUri+"/";
            newUri+=name.startsWith("/")?name.substring(1):name;
            return new HTTPFilesystem(newUri);
        } catch (Exception e) {
            return null;
        }
    }
   
    @Override
    public boolean rmdir() {
        return false;
    }

    @Override
    public boolean mkdirs() {
        return false;
    }

    @Override
    public String getName() {
        try {
            StringTokenizer tokenizer=new StringTokenizer(path,"/");
            String name=null;
            
            while(tokenizer.hasMoreTokens()) {
                name=tokenizer.nextToken();
            }
            return name;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean copyTo(AbstractFilesystem destination) {
        return false;
    }

    @Override
    public boolean moveTo(AbstractFilesystem destination) {
        return false;
    }

    @Override
    public String getAbsolutePath() {
        return path;
    }

    @Override
    public boolean isFile() {
        return isFile;
    }

    @Override
    public String getUri() {
        return currentUri;
    }

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractFilesystem[] list(boolean deep, IFileFilter filter) {
        try {
            if(isFile==false)
            {
                ArrayList<AbstractFilesystem> listing = new ArrayList<AbstractFilesystem>();

                InputStream dataStream=HttpUtils.get(new URL(currentUri), null, username, password);
                String webPage=IOUtil.inputToString(dataStream);

                return parseWebPage(webPage,filter, deep);
            }
            else return new HTTPFilesystem[0];
        } catch (Exception e) {
            return new HTTPFilesystem[0];
        } finally {
        }
    }
    
     
    @Override
    public AbstractFilesystem getParent() {
        String parentUri;
        try {
           String cleanedUri=currentUri.endsWith("/")?currentUri.substring(0,currentUri.length()-1):currentUri;
           parentUri=cleanedUri.substring(0,cleanedUri.lastIndexOf("/"));
           
           if(parentUri.equals(""))
               parentUri="/";
        } catch (Exception e) {
            parentUri="/";
        }
        
        return ConnectionUriParser.getFS(parentUri);
    }

    @Override
    public void close() {
       
    }

    private void doHead() throws MalformedURLException, IOException, Exception {
        URL url=new URL(currentUri);        
        HttpURLConnection con = (HttpURLConnection) url.openConnection();        
        con.setRequestMethod("HEAD");
        String type=con.getContentType();
        
        if(con.getResponseCode()!=200)
            throw new Exception("Not found");
        
        if(type==null || type.startsWith("text/html")==false)
            isFile=true;
        else isFile=false;
    }

    private AbstractFilesystem[] parseWebPage(String webPage, IFileFilter filter, boolean deep) {
       StringTokenizer tokenizer=new StringTokenizer(webPage,"<");
       ArrayList<AbstractFilesystem> listing=new ArrayList<AbstractFilesystem>();
       
       while(tokenizer.hasMoreTokens())
       {
           try
           {
                String token=tokenizer.nextToken();
                String tag=token.substring(0,token.indexOf(">"));
                if(tag.startsWith("a") && tag.contains("href"))
                {
                    int index=tag.indexOf("href");
                    int ampStart=tag.indexOf("\"",index);
                    int ampEnd=tag.indexOf("\"",ampStart+1);
                    String hrefContent=tag.substring(ampStart+1,ampEnd);
                    if(hrefContent!=null && hrefContent.equals("")==false && hrefContent.equals("/")==false && hrefContent.startsWith("?")==false)
                    {
                        System.out.println(hrefContent);
                        HTTPFilesystem fs=(HTTPFilesystem) get(hrefContent);

                        if(fs!=null && (filter==null|| filter.isFiltered(fs) == false))
                        {
                            listing.add(fs);

                            if (deep && fs.isFile()==false) {
                                listing.addAll(Arrays.asList(fs.list(deep, filter)));
                            }
                        }
                        }
                }
                    
           }
           catch(Exception e)
           {
               
           }
       }
       
       return listing.toArray(new AbstractFilesystem[0]);
    }
}
