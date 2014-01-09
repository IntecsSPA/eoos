/*
 * This file is part of the source of
 * 
 * Probatron4J - a Schematron validator for Java(tm)
 * 
 * Copyright (C) 2009 Griffin Brown Digitial Publishing Ltd
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.probatron;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.log4j.Logger;

public class Session
{
    static Logger logger = Logger.getLogger( Session.class );

    private boolean physicalLocators = true;
    private String phase;
    private String schemaDoc;
    private SchematronSchema theSchema;
    private UUID uuid;
    private int reportFormat;


    public Session()
    {
        uuid = UUID.randomUUID();
    }


    public ValidationReport doValidation( String candidate ) throws MalformedURLException
    {
        ValidationReport vr = null;

        theSchema = new SchematronSchema( this, new URL( this.schemaDoc ) );
        vr = theSchema.validateCandidate( new URL( candidate ) );
        
        if( physicalLocators )
        {
            vr.annotateWithLocators( this, new URL( candidate ) );
        }

        if( getReportFormat() == ValidationReport.REPORT_SVRL_MERGED )
        {
            vr.mergeSvrlIntoCandidate( this, new URL( candidate ) );
        }

        return vr;

    }


    public String getPhase()
    {
        return phase;
    }


    public void setPhase( String phase )
    {
        this.phase = phase;
        logger.debug( "Using phase: " + phase );
    }


    public String getSchemaDoc()
    {
        return schemaDoc;
    }


    public void setSchemaDoc( String schemaDoc )
    {
        this.schemaDoc = schemaDoc;
        logger.debug( "Schema document is:" + schemaDoc );
    }


    public boolean usesPhysicalLocators()
    {
        return physicalLocators;
    }


    public void setUsePhysicalLocators( boolean physicalLocators )
    {
        this.physicalLocators = physicalLocators;
        logger.debug( "Setting option (use physical locators): " + physicalLocators );
    }


    public UUID getUuid()
    {
        return uuid;
    }


    public int getReportFormat()
    {
        return reportFormat;
    }


    public void setReportFormat( int reportFormat )
    {
        this.reportFormat = reportFormat;
        logger.debug( "Setting option (report format): " + reportFormat );
    }

}
