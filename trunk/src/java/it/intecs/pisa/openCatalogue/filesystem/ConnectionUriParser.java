/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.filesystem;

import java.io.File;
import java.net.URI;
import java.util.StringTokenizer;

/**
 *
 * @author massi
 */
public class ConnectionUriParser {
    
    public static String getScheme(String uri) {
        try
        {
            URI u=new URI(uri);
            if(u.isAbsolute()) {
                return u.getScheme();
            }
            else {
                return "file";
            }
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public static String getUsername(String uri)
    {
        try
        {
            URI u=new URI(uri);
            String userInfo=u.getUserInfo();
            if(userInfo.indexOf(":")>-1)
            {
                return userInfo.substring(0,userInfo.indexOf(":"));
            }
            else {
                return userInfo;
            }
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public static String getPassword(String uri)
    {
        try
        {
            URI u=new URI(uri);
            String userInfo=u.getUserInfo();
            if(userInfo.indexOf(":")>-1)
            {
                return userInfo.substring(userInfo.indexOf(":")+1);
            }
            else {
                return null;
            }
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public static String getHost(String uri)
    {
        try
        {
            URI u=new URI(uri);
            return u.getHost();
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public static int getPort(String uri)
    {
        try
        {
            URI u=new URI(uri);
            return u.getPort();
        }
        catch(Exception e)
        {
            return -1;
        }
    }
    
    public static String getPath(String uri)
    {
        try
        {
            URI u=new URI(uri);
            return u.getPath();
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public static AbstractFilesystem getFS(String uri)
    {
        String scheme=getScheme(uri);
        if(AbstractFilesystem.SCHEME_FILE.equals(scheme))
        {
            return new FileFilesystem(uri);
        }
        else if(AbstractFilesystem.SCHEME_FTP.equals(scheme))
        {
            return new FTPFilesystem(uri);
        }
        else if(AbstractFilesystem.SCHEME_FTPS.equals(scheme))
        {
            return new FTPSFilesystem(uri);
        }

        else {
            return null;
        }
    }
    
    
}
