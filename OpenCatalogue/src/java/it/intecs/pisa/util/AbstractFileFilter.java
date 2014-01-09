/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.util;

/**
 * @author Massimiliano
 *
 */
public interface AbstractFileFilter 
{
	/**
	 * This method is used by callee to decide if the file shall be accepted or not.
	 * The file under investigation is described by the path argument.
	 * If the file is accepted and shall be processed by the callee, this method shall
	 * return a true value. Otherwise a false value is returned.
	 * 
	 * @author Massimiliano
	 * @param path
	 * @return
	 */
	
	public abstract boolean acceptFile(String path);
}
