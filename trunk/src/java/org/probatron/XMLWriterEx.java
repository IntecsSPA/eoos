package org.probatron;


public class XMLWriterEx extends com.megginson.sax.XMLWriter
{
    
    public XMLWriterEx()
    {
        ETAGO = "<span class='etag'>&lt;";
        ETAGC = "></span>";
        PIO = "<span class='pi'>&lt;?";
        PIC = "?></span>";
    }

}
