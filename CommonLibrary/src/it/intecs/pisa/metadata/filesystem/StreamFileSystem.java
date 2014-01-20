/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.metadata.filesystem;

import it.intecs.pisa.metadata.filters.IFileFilter;
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
public class StreamFileSystem implements AbstractFilesystem {

    protected InputStream myStream;
    
    public StreamFileSystem(InputStream stream) {
        myStream = stream;
    }

    @Override
    public AbstractFilesystem[] list() {
        return new AbstractFilesystem[0];
    }

    @Override
    public boolean delete() {
            return false;
    }

    @Override
    public boolean rename(String name) {
        return false;
    }

    @Override
    public InputStream getInputStream() {
            return myStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public AbstractFilesystem get(String name) {
            return null;
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
            return "";
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
        return "";
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public String getUri() {
        return "";
    }

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractFilesystem[] list(boolean deep, IFileFilter filter) {
return new AbstractFilesystem[0];
    }
    
     
    @Override
    public AbstractFilesystem getParent() {
        return null;
    }

    @Override
    public void close() {
       
    }

}
