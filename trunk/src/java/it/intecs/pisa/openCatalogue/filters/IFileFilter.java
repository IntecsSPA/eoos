/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.filters;

import it.intecs.pisa.openCatalogue.filesystem.AbstractFilesystem;

/**
 *  
 * @author Massimiliano Fanciulli
 */
public interface IFileFilter {
    public boolean isFiltered(AbstractFilesystem file);
}
