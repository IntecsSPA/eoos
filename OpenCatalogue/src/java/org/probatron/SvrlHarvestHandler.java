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

public class SvrlHarvestHandler implements ContentHandler
{

    static Logger logger = Logger.getLogger( SvrlHarvestHandler.class );
    ValidationReport rpt;


    public SvrlHarvestHandler( ValidationReport rpt )
    {
        this.rpt = rpt;
    }


    public void characters( char[] arg0, int arg1, int arg2 ) throws SAXException
    {
    // do nothing
    }


    public void endDocument() throws SAXException
    {
        logger.debug( "SVRL harvest done; xpath count:" + rpt.locMap.size() );
    }


    public void endElement( String arg0, String arg1, String arg2 ) throws SAXException
    {
    // do nothing
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


    public void setDocumentLocator( Locator arg0 )
    {
    // do nothing
    }


    public void skippedEntity( String arg0 ) throws SAXException
    {
    // do nothing
    }


    public void startDocument() throws SAXException
    {
        logger.debug( "Beginning SVRL harvest" );
    }


    public void startElement( String uri, String localName, String arqName, Attributes atts )
            throws SAXException
    {
        if( uri.equals( Utils.SVRL_NAME ) )
        {
            if( localName.equals( "successful-report" )
                    || localName.equals( "failed-assert" ) )
            {
                String xpath = atts.getValue( "location" );
                logger.trace( "Harvesting xpath:" + xpath );

                if( this.rpt.locMap.get( xpath ) == null )
                {
                    // TODO: handle paths with atts
                    xpath = Utils.trimAttributePart( xpath );
                    this.rpt.locMap.put( xpath, new PhysicalLocation() );
                }
            }
            else if( localName.equals( "ns-prefix-in-attribute-values" ) )
            {
                String prefix = atts.getValue( "prefix" );
                String ns = atts.getValue( "uri" );
                this.rpt.nsMap.registerMapping( prefix, ns );
            }
        }
    }


    public void startPrefixMapping( String prefix, String url ) throws SAXException
    {
    // do nothing
    }

}
