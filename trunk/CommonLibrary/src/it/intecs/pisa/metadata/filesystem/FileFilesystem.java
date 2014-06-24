/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.metadata.filesystem;

import it.intecs.pisa.metadata.filters.IFileFilter;
import it.intecs.pisa.util.IOUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Massimiliano Fanciulli
 */
public  class FileFilesystem implements AbstractFilesystem{
    protected File file;
    protected String currentUri;
    
    public FileFilesystem(File f)
    {
        file=f;
        currentUri=f.toURI().toString();
    }
    
    public FileFilesystem(String uri)
    {
        try
        {
            currentUri=uri;
            if(uri.startsWith("file:")) {
                file=new File(new URI(uri));
            }
            else {
                file=new File(uri);
            }
        }
        catch(Exception e)
        {
            
        }
        
    }
    
    @Override
    public String getUri() {
        return currentUri;
    }
    
    public File getFile()
    {
        return file;
    }
    
    @Override
    public AbstractFilesystem[] list() {
        String[] listing=file.list();
        int listingCount=listing!=null?listing.length:0;
        AbstractFilesystem[] abstractListing=new AbstractFilesystem[listingCount];
        
        for(int i=0;i<listingCount;i++)
        {
            abstractListing[i]=get(listing[i]);
        }
        return abstractListing;
    }

    @Override
    public boolean delete() {
        return file.delete();
    }

    @Override
    public boolean rename(String name) {
        
        if(file.isFile())
        {
            File folder=file.getParentFile();
            File newFile=new File(folder,name);
            file.renameTo(newFile);
            file=newFile;
            currentUri=file.toURI().toString();
            return true;
        }
        else {
            return false;
        }
        
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
           return null;
        }
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
           return null;
        }
    }

    @Override
    public AbstractFilesystem get(String folder) {
        String uri=currentUri.endsWith("/")?currentUri:currentUri+"/";
        uri+=folder;        
        return new FileFilesystem(uri);
    }

    @Override
    public boolean rmdir() {
        try {
            IOUtil.rmdir(file);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    @Override
    public boolean mkdirs() {
        file.mkdirs();
        return true;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public boolean copyTo(AbstractFilesystem destination) {
        try {
            IOUtil.copy(getInputStream(),destination.getOutputStream());
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public boolean moveTo(AbstractFilesystem destination) {
       try
       {
            if(destination instanceof FileFilesystem)
            {
                FileFilesystem dest=(FileFilesystem)destination;
                file.renameTo(dest.getFile());
            }   
            else
            {
             copyTo(destination);
             file.delete();
            }
            return true;
       }
       catch(Exception e)
       {
           return false;
       }
    }

    @Override
    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public AbstractFilesystem[] list(boolean deep, IFileFilter filter) {
        if(deep==false) {
            return list();
        }
        else
        {
            return getFolderList(deep,filter);
        }
    }
    
    protected AbstractFilesystem[] getFolderList(boolean deep, IFileFilter filter)
    {
        AbstractFilesystem[] listingFile= list();
        ArrayList<AbstractFilesystem> listing=new ArrayList<AbstractFilesystem>();
        
        for(AbstractFilesystem file:listingFile)
        {
            if(filter==null || filter.isFiltered(file)==false)
            {
                listing.add(file);
                if(file.isFile()==false && deep) {
                    listing.addAll(Arrays.asList(((FileFilesystem)file).getFolderList(deep,filter)));
                }
            }
            if(file.isFile()==false && deep) {
                    listing.addAll(Arrays.asList(((FileFilesystem)file).getFolderList(deep,filter)));
                }
        }
        
        return listing.toArray(new AbstractFilesystem[0]);
    }

    @Override
    public AbstractFilesystem getParent() {
       return new FileFilesystem(file.getParentFile());
    }

    @Override
    public void close() {
    }
    
}
