/*  This file is part of the source of
 * 
 *  Probatron4J - a Schematron validator for Java(tm)
 * 
 *  Copyright (C) 2009 Griffin Brown Digitial Publishing Ltd
 *   
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.probatron;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.transform.TransformerFactory;

import org.apache.log4j.Logger;

public class Utils
{
    public final static String SCHEMATRON_NAME = "http://purl.oclc.org/dsdl/schematron";
    public static String SVRL_NAME = "http://purl.oclc.org/dsdl/svrl";
    private static final String PROPERTY_TRAX_IMPLEMENTATION = "javax.xml.transform.TransformerFactory";
    private static final String SAXON_TRAX_CLASS = "net.sf.saxon.TransformerFactoryImpl";
    private final static int READ_BUFFER_SIZE = 32768;
    final public static int CLOSE_NONE = 0x0000;
    final public static int CLOSE_IN = 0x0001;
    final public static int CLOSE_OUT = 0x0010;
    

    static Logger logger = Logger.getLogger( Utils.class );


    /**
     * Returns a new {@link TransformerFactory}&nbsp;(TrAX implementation). 
     *      
     * @return a TransformerFactory implementation
     */
    public static TransformerFactory getTransformerFactory()
    {
        System.setProperty( PROPERTY_TRAX_IMPLEMENTATION, SAXON_TRAX_CLASS );
        return TransformerFactory.newInstance();
    }


    public static byte[] derefUrl( URL url )
    {
        byte[] ba = null;

        InputStream is = null;
        try
        {
            // get what's at the url location and put it into a byte array
            URLConnection conn = url.openConnection();
            conn.connect();
            is = conn.getInputStream();
            ba = Utils.getBytesToEndOfStream( is, true ); // does close
        }
        catch( IOException e )
        {
            logger.warn( e.getMessage() );
            return null;
        }

        return ba;
    }


    /**
     * Reads all of an InputStream content into a byte array, and closes that
     * InputStream.
     * 
     * @param in
     *            the InputStream to be read
     * @return byte[] its content
     */
    public static byte[] getBytesToEndOfStream( InputStream in, boolean closeSteam )
            throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        transferBytesToEndOfStream( in, byteStream, ( closeSteam ? ( CLOSE_IN | CLOSE_OUT )
                : CLOSE_OUT ) );
        byte[] ba = byteStream.toByteArray();
        return ba;
    }


    /**
     * Reads all of an InputStream content into an OutputStream, via a buffer.
     * 
     * @param in
     *            the InputStream to be read
     * @return int number of bytes read
     */
    public static long transferBytesToEndOfStream( InputStream in, OutputStream out,
            int closeFlags ) throws IOException
    {
        if( in == null || out == null )
        {
            logger.fatal( "transferBytesToEndOfStream() called with a null parameter" );
            throw new IllegalArgumentException();
        }

        byte[] buf = new byte[ READ_BUFFER_SIZE + 1 ];

        long written = 0;
        int count;
        while( ( count = in.read( buf ) ) != - 1 )
        {
            out.write( buf, 0, count );
            written += count;
        }
        if( ( closeFlags & CLOSE_IN ) != 0 )
        {
            streamClose( in );
        }
        if( ( closeFlags & CLOSE_OUT ) != 0 )
        {
            streamClose( out );
        }

        buf = null;

        return written;
    }


    public static void streamClose( InputStream is )
    {
        try
        {
            if( is != null )
                is.close();
        }
        catch( Exception e )
        {
            logger.warn( e.getMessage() );
        }
    }


    public static void streamClose( OutputStream os )
    {
        try
        {
            if( os != null )
                os.close();
        }
        catch( Exception e )
        {
            logger.warn( e.getMessage() );
        }
    }


    public static void streamToFile( InputStream is, String fn, boolean closeStream )
            throws IOException
    {
        File f = new File( fn );
        f.createNewFile();

        try
        {
            FileOutputStream fos = new FileOutputStream( f );
            int flags = CLOSE_OUT;
            if( closeStream )
            {
                flags |= CLOSE_IN;
            }
            Utils.transferBytesToEndOfStream( is, fos, flags );

        }
        catch( FileNotFoundException e )
        {
            throw new RuntimeException( "File not found when writing: ", e );
        }

    }


    /**
     * Writes the bytes in <tt>ba</tt> to the file named <tt>fn</tt>,
     * creating it if necessary.
     * 
     * @param ba
     *            the byte array to be written
     * @param fn
     *            the filename of the file to be written to
     * @throws IOException
     */
    public static void writeBytesToFile( byte[] ba, String fn ) throws IOException
    {
        File f = new File( fn );
        f.createNewFile();

        FileOutputStream fos = null;
        ByteArrayInputStream bis = null;

        try
        {
            fos = new FileOutputStream( f );
            bis = new ByteArrayInputStream( ba );
            transferBytesToEndOfStream( bis, fos, CLOSE_IN | CLOSE_OUT );

        }
        catch( FileNotFoundException e )
        {
            throw new RuntimeException( "File not found when writing: ", e );
            // should never happen
        }

    }


    static String trimAttributePart( String xpath )
    {
        String ret = null;

        int n = xpath.indexOf( "@" );
        if( n != - 1 )
        {
            ret = xpath.substring( 0, n );
            logger.trace( "Trimmed xpath to: " + ret );
        }
        else
        {
            ret = xpath;
        }

        return ret;
    }

}
