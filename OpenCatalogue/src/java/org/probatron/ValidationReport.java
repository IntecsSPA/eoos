/*
 * This file is part of the source of
 * 
 * Probatron4J - a Schematron validator for Java(tm)
 * 
 * Copyright (C) 2009 Griffin Brown Digitial Publishing Ltd
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.probatron;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.apache.log4j.Logger;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.megginson.sax.XMLWriter;

public class ValidationReport
{
    static Logger logger = Logger.getLogger( ValidationReport.class );

    private byte[] reportBytes;
    LogicalPhysicalMap locMap = new LogicalPhysicalMap();
    private SvrlHarvestHandler harvestHandler = new SvrlHarvestHandler( this );
    private LocationMapHandler locationMapHandler = new LocationMapHandler( this );
    NamespacePrefixMappings nsMap = new NamespacePrefixMappings();
    
    final public static int REPORT_SVRL_COMPACT = 0;
    final public static int REPORT_SVRL_FULL = 1;
    final public static int REPORT_SVRL_MERGED = 2;


    public ValidationReport( byte[] ba )
    {
        this.reportBytes = ba;
       
    }


    public void streamOut( OutputStream os )
    {
        try
        {
            Utils.transferBytesToEndOfStream( new ByteArrayInputStream( this.reportBytes ), os,
                    Utils.CLOSE_NONE );
        }
        catch( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public byte[] reportAsBytes()
    {
        return this.reportBytes;
    }
    
    public void mergeSvrlIntoCandidate(Session session, URL candidateUrl )
    {
        
    }


    public void annotateWithLocators( Session session, URL candidateUrl )
    {
        try
        {
            // seed a dictionary with the xpaths
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler( this.harvestHandler );
            parser.parse( new InputSource( new ByteArrayInputStream( reportBytes ) ) );

            // enrich the dictionary with the physical locations
            parser = XMLReaderFactory.createXMLReader(); // reset
            parser.setContentHandler( ( ContentHandler )this.locationMapHandler );
            parser.parse( candidateUrl.toString() );
            logger.debug( "Mapped " + locMap.size() + " XPaths to physical locations" );

            // final prep of SVRL report
            XMLReader reader = XMLReaderFactory.createXMLReader();
            EnrichmentFilter filter = new EnrichmentFilter( session, this );
            filter.setParent( reader );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            filter.setContentHandler( new XMLWriter( new OutputStreamWriter( baos ) ) );
            filter.parse( new InputSource( new ByteArrayInputStream( reportBytes ) ) );
            this.reportBytes = baos.toByteArray();

        }
        catch( Exception e )
        {
            logger.fatal( e );
            e.printStackTrace();
            throw new RuntimeException( e.getMessage() );
        }

    }
}
