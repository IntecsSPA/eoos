/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.filters;

import it.intecs.pisa.openCatalogue.filesystem.AbstractFilesystem;

/**
 *
 * @author massi
 */
public class EndsWith implements IFileFilter
    {
        protected String text;
        
        public EndsWith(String name)
        {
            text=name;
        }

        @Override
        public boolean isFiltered(AbstractFilesystem file) {
            String fileName=file.getName();
            if(fileName!=null) {
                return fileName.endsWith(text);
            }
            else {
                return false;
            }
        }
    }
