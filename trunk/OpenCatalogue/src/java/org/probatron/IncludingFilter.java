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
import java.net.URL;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Processed Schematron's &lt;include> element. This filter processes an instance and performs
 * inclusion of documents referenced by Schamtron's &lt;include> element. *
 */
public class IncludingFilter extends XMLFilterImpl
{

    static Logger logger = Logger.getLogger( IncludingFilter.class );
    private boolean outermost;
    private URL base;


    public IncludingFilter( URL base, boolean outermost )
    {
        this.base = base;
        this.outermost = outermost;
    }


    @Override
    public void startDocument() throws SAXException
    {
        logger.debug( "Writing  schema; outermost=" + outermost );

        if( this.outermost )
        {
            super.startDocument(); // only emit XML declaration for outermost document
        }
    }


    @Override
    public void endElement( String uri, String localName, String name ) throws SAXException
    {
        boolean isIncludeElement = localName.equals( "include" )
                && uri.equals( Utils.SCHEMATRON_NAME );

        if( !isIncludeElement )
        {
            super.endElement( uri, localName, name );
        }
    }


    @Override
    public void startElement( String uri, String localName, String name, Attributes atts )
            throws SAXException
    {
        boolean isIncludeElement = localName.equals( "include" )
                && uri.equals( Utils.SCHEMATRON_NAME );

        if( isIncludeElement )
        {
            try
            {
                String href = atts.getValue( "href" );
                URL url = new URL( this.base, href );

                logger.debug( "Expanding inclusion at: " + url.toExternalForm() );

                // do the inclusion
                XMLReader reader = XMLReaderFactory.createXMLReader();
                IncludingFilter filter = new IncludingFilter( url, false );
                filter.setParent( reader );
                filter.setContentHandler( this.getContentHandler() );
                filter
                        .parse( new InputSource(
                                new ByteArrayInputStream( Utils.derefUrl( url ) ) ) );

            }
            catch( Exception e )
            {
                logger.error( e.getMessage() );
            }

        }
        else
        {
            super.startElement( uri, localName, name, atts );
        }

    }

}
