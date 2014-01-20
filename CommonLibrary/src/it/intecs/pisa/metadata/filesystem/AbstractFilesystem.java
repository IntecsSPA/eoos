/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.metadata.filesystem;

import it.intecs.pisa.metadata.filters.IFileFilter;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author massi
 */
public interface AbstractFilesystem {

    public static final String SCHEME_FILE = "file";
    public static final String SCHEME_SFTP = "sftp";
    public static final String SCHEME_WATCH = "watch";
    public static final String SCHEME_FTP = "ftp";
    public static final String SCHEME_FTPS = "ftps";
    public static final String SCHEME_WORKSPACE = "workspace";

    public AbstractFilesystem get(String folder);

    public AbstractFilesystem[] list();

    public AbstractFilesystem[] list(boolean deep, IFileFilter filter);

    public boolean delete();

    public boolean rename(String name);

    public boolean rmdir();

    public boolean mkdirs();

    public boolean isFile();

    public String getName();

    public String getUri();

    public AbstractFilesystem getParent();

    public boolean copyTo(AbstractFilesystem destination);

    public boolean moveTo(AbstractFilesystem destination);

    public InputStream getInputStream();

    public OutputStream getOutputStream();

    public String getAbsolutePath();

    public boolean exists();

    public void close();
}
