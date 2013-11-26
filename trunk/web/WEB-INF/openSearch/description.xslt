<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes" xmlns:rep="http://ngeo.eo.esa.int/schema/browseReport" xmlns:gml="http://www.opengis.net/gml" xmlns:gsc="http://earth.esa.int/gsc" xmlns:bsi="http://ngeo.eo.esa.int/schema/browse/ingestion" xmlns:om="http://www.opengis.net/om/2.0" xmlns:gml32="http://www.opengis.net/gml/3.2" xmlns:eop="http://earth.esa.int/eop" xmlns:opt="http://earth.esa.int/opt" xmlns:atm="http://earth.esa.int/atm" xmlns:sar="http://earth.esa.int/sar" xmlns:eop2="http://www.opengis.net/eop/2.0" xmlns:opt2="http://www.opengis.net/opt/2.0" xmlns:atm2="http://www.opengis.net/atm/2.0" xmlns:sar2="http://www.opengis.net/sar/2.0">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no" escape-uri-attributes="yes"/>
    <xsl:param name="url">http://pippo.com/</xsl:param>
    <xsl:param name="time_ext">startdate={time:start?}&amp;stopdate={time:end?}&amp;trel={time:relation?}&amp;</xsl:param>
    <xsl:param name="geo_ext">bbox={geo:box}&amp;geom={geo:geometry?}&amp;id={geo:uid?}&amp;lat={geo:lat?}&amp;lon={geo:lon?}&amp;radius={geo:radius?}&amp;rel={geo:relation?}&amp;loc={geo:name?}&amp;</xsl:param>
    <xsl:param name="eo_ext">pid={eo:parentIdentifier?}&amp;psn={eo:platformShortName?}&amp;psi={eo:platformSerialIdentifier?}&amp;ot={eo:orbitType?}&amp;isn={eo:instrumentShortName?}&amp;st={eo:sensorType?}&amp;som={eo:sensorOperationalMode?}&amp;si={eo:swathIdentifier?}</xsl:param>

    <xsl:template match="/">

        <OpenSearchDescription xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:eop="http://www.genesi-dr.eu/spec/opensearch/extensions/eop/1.0/" xmlns:time="http://a9.com/-/opensearch/extensions/time/1.0/" xmlns:geo="http://a9.com/-/opensearch/extensions/geo/1.0/" xmlns:sar="http://earth.esa.int/sar" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dclite4g="http://xmlns.com/2008/dclite4g#" xmlns:ical="http://www.w3.org/2002/12/cal/ical#" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:ws="http://dclite4g.xmlns.com/ws.rdf#" xmlns:os="http://a9.com/-/spec/opensearch/1.1/" xmlns:sru="http://a9.com/-/opensearch/extensions/sru/2.0/" xmlns="http://a9.com/-/spec/opensearch/1.1/">
            <ShortName>openCaralogue</ShortName>
            <LongName>Earth Observation Catalogue</LongName>
            <Description>This OpenSearch Service allows the discovery of Earth Observation data. This search service is in accordance with the OGC 10-032r3 specification. </Description>
            <Tags>ESA, Earth Observation, Digital Repository, Catalogue, OGC</Tags>
            <Contact>openCatalogue@intecs.it</Contact>
            <!--Image height="75" width="125" type="image/png">http://eo-virtual-archive4.esa.int/images/geo.png</Image>
            <Image height="16" width="16" type="image/vnd.microsoft.icon">http://eo-virtual-archive4.esa.int/favicon.ico</Image-->
            <Query role="example" geo:box="-25,30,45,70" />
            <Developer>Intecs team</Developer>
            <Attribution>Intecs</Attribution>
            <SyndicationRight>open</SyndicationRight>
            <AdultContent>false</AdultContent>
            <Language>en-us</Language>
            <OutputEncoding>UTF-8</OutputEncoding>
            <InputEncoding>UTF-8</InputEncoding>
            <Url type="application/atom+xml" indexOffset="0" pageOffset="0" template="" >
                <xsl:attribute name="template">
                    <xsl:value-of select="$url"/>/atom/?q={searchTerm}&amp;count={count}&amp;startIndex={startIndex?}&amp;startPage={startPage?}&amp;<xsl:value-of select="$geo_ext"/><xsl:value-of select="$time_ext"/><xsl:value-of select="$eo_ext"/>
                </xsl:attribute>
            </Url>
        </OpenSearchDescription>
    </xsl:template>
</xsl:stylesheet>
