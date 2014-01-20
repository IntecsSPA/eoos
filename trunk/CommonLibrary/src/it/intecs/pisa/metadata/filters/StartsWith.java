/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.metadata.filters;

import it.intecs.pisa.metadata.filesystem.AbstractFilesystem;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class StartsWith implements IFileFilter
    {
        protected String text;
        
        public StartsWith(String name)
        {
            text=name;
        }

        @Override
        public boolean isFiltered(AbstractFilesystem file) {
            return file.getName().startsWith(text);
        }
    }
