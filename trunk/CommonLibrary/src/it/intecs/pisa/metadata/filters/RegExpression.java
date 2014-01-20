/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.metadata.filters;

import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;

/**
 *
 * @author massi
 */
public class RegExpression implements IFileFilter
    {
       protected String regEx;
        
       public RegExpression(String expr)
       {
           regEx=expr;
       }

        @Override
        public boolean isFiltered(AbstractFilesystem file) {
            boolean result= file.getName().matches(regEx);
            //Log.debug(this.getClass()+" "+regEx+" "+result);
            return result;
        }
    }
