/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.metadata.filesystem;

import it.intecs.pisa.metadata.filters.IFileFilter;
import it.intecs.pisa.util.IOUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class FTPFilesystem implements AbstractFilesystem {

    protected String currentUri;
    protected FTPClient c = null;
    protected String host, username, password, path;
    protected int port;
    protected FTPFile ftpFile;

    public FTPFilesystem(String uri) {
        currentUri = uri;
        host = ConnectionUriParser.getHost(uri);
        username = ConnectionUriParser.getUsername(uri);
        password = ConnectionUriParser.getPassword(uri);
        path = ConnectionUriParser.getPath(uri);

        int parsedPort = ConnectionUriParser.getPort(uri);
        port = parsedPort == -1 ? 21 : parsedPort;
    }

    @Override
    protected void finalize() throws Throwable {
        if (c != null && c.isConnected()) {
//            Log.info("DISCONNECTING FROM FTP SERVER");
            c.disconnect();
        }
    }

    protected void connect() {

        try {
            if (c == null) {
                c = new FTPClient();
            }

            if (c.isConnected() == false) {
                c.connect(host, port);
                c.login(username, password);
            }
        } catch (Exception e) {
//            Log.info("ERROR CONNECTING " + e.getMessage());
            c = null;
        }
    }

    public void setFTPFile(FTPFile file) {
        ftpFile = file;
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
        try {
            connect();
            String currentFolder;
            currentFolder = path.substring(0, path.lastIndexOf("/"));
            String newPath = currentFolder + "/" + name;

            c.rename(path, newPath);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            disconnect();
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            connect();
            return c.retrieveFileStream(path);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            connect();
            return c.storeFileStream(path);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public AbstractFilesystem get(String name) {
        try {
            String newUri=currentUri.endsWith("/")?currentUri:currentUri+"/";
            newUri+=name.startsWith("/")?name.substring(1):name;
            return ConnectionUriParser.getFS(newUri);
        } catch (Exception e) {
            return null;
        }
    }
   
    @Override
    public boolean rmdir() {
        try {
            connect();
            if (isFile()) {
                c.deleteFile(path);
            } else {
                c.removeDirectory(path);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean mkdirs() {
        try {
            connect();
            c.makeDirectory(path);
            return true;
        } catch (Exception e) {
            return false;
        }
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
        try {
            InputStream input = getInputStream();
            OutputStream output = destination.getOutputStream();
            IOUtil.copy(input, output);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean moveTo(AbstractFilesystem destination) {

        try {
            connect();
            copyTo(destination);
            delete();
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public String getAbsolutePath() {
        return path;
    }

    @Override
    public boolean isFile() {


        if (ftpFile != null) {
            return ftpFile.isFile();
        } else {
            try {
                connect();
                FTPFile[] listing = c.listFiles(path);
                if (listing.length == 1 && path.endsWith(listing[0].getName())) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return true;
            } finally {
                disconnect();
            }

        }


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
            connect();
//            Log.info("LISTING PATH " + path);

            AbstractFilesystem[] listing = getFolderList(deep, this, filter);

            return listing;
        } catch (Exception e) {
            return new FTPFilesystem[0];
        } finally {
            disconnect();
        }
    }

    protected AbstractFilesystem[] getFolderList(boolean deep, AbstractFilesystem path, IFileFilter filter) throws IOException {
        ArrayList<AbstractFilesystem> listing = new ArrayList<AbstractFilesystem>();

        FTPFile[] listingFile = c.listFiles(path.getAbsolutePath());
        for (FTPFile file : listingFile) {
            String fileName = file.getName();
            FTPFilesystem subFile = (FTPFilesystem) path.get(fileName);
            subFile.setFTPFile(file);

            if (filter.isFiltered(subFile) == false) {
                listing.add(subFile);
                if (deep && file.isDirectory()) {
                    AbstractFilesystem[] subFolderListing = getFolderList(deep, subFile, filter);
                    listing.addAll(Arrays.asList(subFolderListing));
                }
            }
        }
        return listing.toArray(new AbstractFilesystem[0]);
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

    private void disconnect() {
        try {
            if(c!=null && c.isConnected()) {
                c.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
       disconnect();
    }
}
