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
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Driver
{
    private final static String PROPERTY_LOGLVL = "property://probatron.org/log-level";
    private final static String DEFAULT_LOGLVL = "WARN";
    static Logger logger = Logger.getLogger( Driver.class );
    static int APP_EXIT_FAIL = - 1;
    static int APP_EXIT_OKAY = 0;    
    static Session theSession = new Session();

    static
    {
        // set up log message format, etc.
        String logLvl = System.getProperty( PROPERTY_LOGLVL ) == null ? DEFAULT_LOGLVL : System
                .getProperty( PROPERTY_LOGLVL );
        Properties p = new Properties();
        p.setProperty( "log4j.rootCategory", logLvl + ",stderr" );
        p.setProperty( "log4j.appender.stderr", "org.apache.log4j.ConsoleAppender" );
        p.setProperty( "log4j.appender.stderr.layout", "org.apache.log4j.PatternLayout" );
        p.setProperty( "log4j.appender.stderr.target", "System.err" );
        p.setProperty( "log4j.appender.stderr.layout.ConversionPattern", "%p %m%n" );
        PropertyConfigurator.configure( p );
    }


    public static void showUsage()
    {
        System.err.println( "Usage: probatron.jar [options] candidate-doc schema-doc" );
        System.err.println( "Options:" );
        System.err.println( "-n0|1     Do not [or do] emit line/col numbers in report" );
        System.err.println( "-p<phase> Validate using the phase named <phase>" );
        // TODO        
        //        System.err.println( "-q0|1     Do not [or do] validate the Schematron schema itself" );
        System.err.println( "-r0       Output report as terse SVRL" );
        System.err.println( "-r1       Output report as verbose SVRL" );
       // System.err.println( "-r2       Output report as the original instance with SVRL merged in situ" );
        System.err.println( "-v        Show version info and halt" );
    }


    private static void handleCommandLineArg( String arg )
    {
        logger.debug( "Handling command line argument: " + arg );
        if( arg.equals( "-n1" ) || arg.equals( "-n0" ) )
        {
            theSession.setUsePhysicalLocators( arg.equals( "-n1" ) );
        }
        else if( arg.startsWith( "-r" ) )
        {
            theSession.setReportFormat( new Integer(arg.substring( 2, arg.length())).intValue() );
        }
        else if( arg.startsWith( "-p" ) )
        {
            theSession.setPhase( arg.substring( 2, arg.length() ) );
        }
        else
        {
            logger.fatal( "Unrecognized command-line option \"" + arg + "\". Aborting." );
            System.exit( APP_EXIT_FAIL );
        }
    }


    static String fixArg( String arg )
    {
        // user concession, if no URL scheme assume these are files
        return arg.indexOf( ":" ) == - 1 ? "file:" + arg : arg;
    }


    public static void main( String[] args )
    {
        long t = System.currentTimeMillis();
        logger.info( "Starting Probatron" );

        if( args.length == 1 && ( args[ 0 ].equals( "-v" ) || args[ 0 ].equals( "-version" ) ) )
        {
            String ver = Package.getPackage( "org.probatron" ).getImplementationVersion();
            System.out.println( ver );
            System.exit( APP_EXIT_OKAY );
        }

        if( args.length < 2 )
        {
            showUsage();
            System.exit( 0 );
        }

        for( int i = 0; i < args.length - 2; i++ )
        {
            String arg = args[ i ];
            if( ! arg.startsWith( "-" ) )
            {
                logger.fatal( "Unrecognized command line argument: " + arg );
                System.exit( APP_EXIT_FAIL );
            }
            else
            {
                handleCommandLineArg( arg );
            }
        }

        String candidate = fixArg( args[ args.length - 2 ] );
        theSession.setSchemaDoc( fixArg( args[ args.length - 1 ] ) );

        try
        {
            ValidationReport vr = theSession.doValidation( candidate );
            vr.streamOut( System.out );
        }
        catch( MalformedURLException e )
        {
            logger.fatal( e );
        }

        logger.info( "Done. Elapsed time (ms):" + ( System.currentTimeMillis() - t ) );

    }

}
