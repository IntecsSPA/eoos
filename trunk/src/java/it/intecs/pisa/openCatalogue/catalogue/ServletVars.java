/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.catalogue;

import java.io.File;

/**
 *
 * @author massi
 */
public class ServletVars {
    public static final ServletVars instance=new ServletVars();
    public static File appFolder;
//    public static File workspace=new File("c://catalogue_workspace");
    public static File workspace=new File("/home/massi/Sviluppo/catalogue_workspace");
    public static ServletVars getInstance()
    {
        return instance;
    }
}
