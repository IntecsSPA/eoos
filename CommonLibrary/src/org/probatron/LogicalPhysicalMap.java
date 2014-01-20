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

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.xml.sax.Locator;

@SuppressWarnings("serial")
public class LogicalPhysicalMap extends HashMap<String, PhysicalLocation>
{
    static Logger logger = Logger.getLogger( LogicalPhysicalMap.class );


    void handleMapping( TreeContext tc, Locator loc )
    {
        String pxpath =  tc.currentContext(); // guaranteed to have no attribute part

        PhysicalLocation pl = get( pxpath );
        if( pl != null )
        {
            remove( pxpath );
            logger.trace( "Associating physical location with pseudo-XPath: <" + pxpath + ">" );
            put( pxpath, new PhysicalLocation( loc.getLineNumber(), loc.getColumnNumber() ) );
        }
        // else do nothing

    }

}
