<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes" xmlns:rep="http://ngeo.eo.esa.int/schema/browseReport" xmlns:gml="http://www.opengis.net/gml" xmlns:gsc="http://earth.esa.int/gsc" xmlns:bsi="http://ngeo.eo.esa.int/schema/browse/ingestion" xmlns:om="http://www.opengis.net/om/2.0" xmlns:gml32="http://www.opengis.net/gml/3.2" xmlns:eop="http://earth.esa.int/eop" xmlns:opt="http://earth.esa.int/opt" xmlns:atm="http://earth.esa.int/atm" xmlns:sar="http://earth.esa.int/sar" xmlns:eop2="http://www.opengis.net/eop/2.0" xmlns:opt2="http://www.opengis.net/opt/2.0" xmlns:atm2="http://www.opengis.net/atm/2.0" xmlns:sar2="http://www.opengis.net/sar/2.0">
    <xsl:template match="/">
                            <xsl:value-of select="//doc[1]/str[@name='id']"/>
            </xsl:template>
        </xsl:stylesheet>
