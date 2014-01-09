/* Copyright (c) 2013 Intecs - www.intecs.it. All rights reserved.
 * This code is licensed under the GPL 3.0 license, available at the root
 * application directory.
*/
package it.intecs.pisa.openCatalogue.filesystem;

//import it.intecs.pisa.archivingserver.log.Log;
import org.apache.commons.net.ftp.FTPSClient;

/**
 *
 * @author Massimiliano Fanciulli
 */
public class FTPSFilesystem extends FTPFilesystem{
        
    
    public FTPSFilesystem(String uri)
    {
        super(uri);
    }
    
    protected void connect()
    {
        
        try
        {
            if(c==null)
            {
//                Log.info("CONNECTING TO FTPS SERVER");
                
                c = new FTPSClient();
                c.connect(host, port);
                c.login(username, password);
            }
        }
        catch(Exception e)
        {
            c=null;
        }
    }
    
   
}
