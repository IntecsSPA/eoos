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

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class LocationMapHandler implements ContentHandler, LexicalHandler
{

    static Logger logger = Logger.getLogger( LocationMapHandler.class );
    ValidationReport rpt;
    String rollingPath;
    int depth;
    TreeContext tc = new TreeContext();
    Locator loc;


    public LocationMapHandler( ValidationReport rpt )
    {
        this.rpt = rpt;
    }


    public void characters( char[] arg0, int arg1, int arg2 ) throws SAXException
    {
    // do nothing
    }


    public void endDocument() throws SAXException
    {
        logger.debug( "Map enrichment done." );
    }


    public void endElement( String arg0, String arg1, String arg2 ) throws SAXException
    {
        tc.onEndElement();
    }


    public void endPrefixMapping( String arg0 ) throws SAXException
    {
    // do nothing
    }


    public void ignorableWhitespace( char[] arg0, int arg1, int arg2 ) throws SAXException
    {
    // do nothing
    }


    public void processingInstruction( String arg0, String arg1 ) throws SAXException
    {
    // do nothing
    }


    public void setDocumentLocator( Locator loc )
    {
        this.loc = loc;
    }


    public void skippedEntity( String arg0 ) throws SAXException
    {
    // do nothing
    }


    public void startDocument() throws SAXException
    {
        logger.debug( "Beginning map enrichment" );
    }


    public void startElement( String uri, String localName, String qName, Attributes atts )
            throws SAXException
    {
        tc.onStartElement( uri, localName );
        this.rpt.locMap.handleMapping( this.tc, this.loc );

        if( uri != "" )
        {
            String regPrefix = this.rpt.nsMap.prefixForNs( uri );
            if( regPrefix == null )
            {
                String prefix = null;
                if( qName.indexOf( ":" ) != - 1 )
                {
                    prefix = qName.substring( 0, qName.indexOf( ":" ) );
                }
                if( prefix != null )
                {
                    this.rpt.nsMap.registerMapping( prefix, uri );
                }
                else
                {
                    this.rpt.nsMap.registerUnprefixed( uri );
                }

            }
        }

    }


    public void startPrefixMapping( String prefix, String url ) throws SAXException
    {
    // do nothing
    }


    public void comment( char[] arg0, int arg1, int arg2 ) throws SAXException
    {
    // do nothing

    }


    public void endCDATA() throws SAXException
    {
    // do nothing

    }


    public void endDTD() throws SAXException
    {
    // do nothing

    }


    public void endEntity( String arg0 ) throws SAXException
    {
    // do nothing

    }


    public void startCDATA() throws SAXException
    {
    // do nothing

    }


    public void startDTD( String arg0, String arg1, String arg2 ) throws SAXException
    {
    // do nothing

    }


    public void startEntity( String name ) throws SAXException
    {
    // do nothing
    }

}
