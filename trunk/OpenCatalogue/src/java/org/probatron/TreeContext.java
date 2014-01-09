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
import java.util.Stack;

@SuppressWarnings("serial")
public class TreeContext extends Stack<HashMap<String, Integer>>
{
    boolean justClosed;
    Stack<String> parents = new Stack<String>();


    public void onStartElement( String uri, String localName )
    {
        String pseudo = "{" + uri + "}" + localName;

        if( justClosed == false || size() == 0 )
        {
            // going deeper ...
            HashMap<String, Integer> newContext = new HashMap<String, Integer>();
            newContext.put( pseudo, new Integer( 1 ) );
            push( newContext );
        }
        else
        {
            // there are preceding sibling elements ...
            HashMap<String, Integer> currContext = peek();
            Integer preceding = currContext.get( pseudo );
            if( preceding != null )
            {
                int newCount = preceding.intValue() + 1;
                currContext.remove( pseudo );
                preceding = new Integer( newCount );
                currContext.put( pseudo, newCount );
            }
            else
            {
                currContext.put( pseudo, new Integer( 1 ) );
            }
        }

        parents.push( pseudo );
        this.justClosed = false;
    }


    public void onEndElement()
    {
        if( justClosed == true )
        {
            pop();
        }
        this.justClosed = true;
        parents.pop();

    }


    public String currentContext()
    {
        StringBuffer sb = new StringBuffer();
        for( int i = 0; i < size(); i++ )
        {
            if( i == 0 )
            {
                sb.append( "/" + parents.get( 0 ) + "[1]" );
            }
            else
            {
                String pseudo = parents.get( i );
                sb.append( pseudo );
                HashMap<String, Integer> context = get( i );
                Integer n = context.get( pseudo );
                sb.append( "[" + ( n == null ? "1" : n ) + "]" );
            }
            if( i < size() - 1 )
            {
                sb.append( "/" );
            }

        }
        return sb.toString();

    }

}
