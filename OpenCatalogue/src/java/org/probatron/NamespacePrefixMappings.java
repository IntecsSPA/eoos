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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.xml.sax.helpers.AttributesImpl;

public class NamespacePrefixMappings
{
    static Logger logger = Logger.getLogger( NamespacePrefixMappings.class );

    private HashMap<String, String> nsMap = new HashMap<String, String>();
    private HashMap<String, String> prefixMap = new HashMap<String, String>();
    private int custom;


    public String prefixForNs( String ns )
    {
        return nsMap.get( ns );
    }


    public String nsForPrefix( String prefix )
    {
        return prefixMap.get( prefix );
    }


    public void registerMapping( String prefix, String ns )
    {
        if( nsMap.get( ns ) != null || prefixMap.get( prefix ) != null )
        {
            throw new IllegalArgumentException(
                    "Cannot register a prefix or Namespace name that has already been registered" );
        }
        logger.debug( "Registering Namespace/prefix binding:" + prefix + " " + ns );
        this.prefixMap.put( prefix, ns );
        this.nsMap.put( ns, prefix );
    }


    public String registerUnprefixed( String ns )
    {
        String customPrefix = "ns" + custom;
        custom++;
        registerMapping( customPrefix, ns );
        return customPrefix;
    }


    public String fixupXpath( String pseudo )
    {
        String ret = pseudo.replaceAll( "\\{\\}", "" );
        for( ;; )
        {
            int n1 = ret.indexOf( "{" );

            if( n1 == - 1 )
            {
                break;
            }

            int n2 = ret.indexOf( "}" );
            String uri = ret.substring( n1 +1, n2 );
            logger.trace( "Fixing up URI: <" + uri + ">" );
            String prefix = prefixForNs( uri );
            ret = ret.replaceAll( "\\{" + uri + "\\}", prefix + ":" );

        }

        return ret;

    }


    public ArrayList<AttributesImpl> asAttributes()
    {
        ArrayList<AttributesImpl> a = new ArrayList<AttributesImpl>();
        Iterator<String> iter = prefixMap.keySet().iterator();
        while( iter.hasNext() )
        {
            String prefix = iter.next();
            String uri = nsForPrefix( prefix );

            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute( "", "prefix", "prefix", "CDATA", prefix );
            atts.addAttribute( "", "uri", "uri", "CDATA", uri );

            a.add( atts );

        }

        return a;

    }
}
