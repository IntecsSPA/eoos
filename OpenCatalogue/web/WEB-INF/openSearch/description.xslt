<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xdt="http://www.w3.org/2005/02/xpath-datatypes" xmlns:rep="http://ngeo.eo.esa.int/schema/browseReport" xmlns:gml="http://www.opengis.net/gml" xmlns:gsc="http://earth.esa.int/gsc" xmlns:bsi="http://ngeo.eo.esa.int/schema/browse/ingestion" xmlns:om="http://www.opengis.net/om/2.0" xmlns:gml32="http://www.opengis.net/gml/3.2" xmlns:eop="http://earth.esa.int/eop" xmlns:opt="http://earth.esa.int/opt" xmlns:atm="http://earth.esa.int/atm" xmlns:sar="http://earth.esa.int/sar" xmlns:eop2="http://www.opengis.net/eop/2.0" xmlns:opt2="http://www.opengis.net/opt/2.0" xmlns:atm2="http://www.opengis.net/atm/2.0" xmlns:sar2="http://www.opengis.net/sar/2.0" xmlns:param="http://a9.com/-/spec/opensearch/extensions/parameters/1.0/">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no" escape-uri-attributes="yes"/>
    <xsl:param name="url">http://myurl.com/</xsl:param>
    <!-- not yet supported trel={time:relation?}&amp;-->
    <xsl:param name="time_ext">startdate={time:start?}&amp;stopdate={time:end?}&amp;</xsl:param>
    <!-- not yet supported &amp;rel={geo:relation?}&amp;loc={geo:name?}&amp;id={geo:uid?}-->
    <xsl:param name="geo_ext">bbox={geo:box?}&amp;geom={geo:geometry?}&amp;lat={geo:lat?}&amp;lon={geo:lon?}&amp;radius={geo:radius?}&amp;</xsl:param>
    <xsl:param name="eo_ext">pt={eo:productType?}&amp;psn={eo:platformShortName?}&amp;psi={eo:platformSerialIdentifier?}&amp;inst={eo:instrument?}&amp;st={eo:sensorType?}&amp;ct={eo:compositeType?}&amp;pl={eo:processingLevel?}&amp;ot={eo:orbitType?}&amp;res={eo:resolution?}&amp;sr={eo:spectralRange?}&amp;wl={eo:wavelengths?}&amp;ul={eo:useLimitation?}&amp;hsc={eo:hasSecurityConstraints?}&amp;orgname={eo:organisationName?}&amp;diss={eo:dissemination?}&amp;pid={eo:parentIdentifier?}&amp;ps={eo:productionStatus?}&amp;at={eo:acquisitionType?}&amp;on={eo:orbitNumber?}&amp;od={eo:orbitDirection?}&amp;tr={eo:track?}&amp;fr={eo:frame?}&amp;si={eo:swathIdentifier?}&amp;cc={eo:cloudCover?}&amp;sc={eo:snowCover?}&amp;pqd={eo:productQualityDegradation?}&amp;pqdt={eo:productQualityDegradationTag?}&amp;pn={eo:processorName?}&amp;pcen={eo:processingCenter?}&amp;pd={eo:processingDate?}&amp;sm={eo:sensorMode?}&amp;ac={eo:archivingCenter?}&amp;procm={eo:processingMode?}&amp;as={eo:acquisitionStation?}&amp;ast={eo:acquisitionSubType?}&amp;stfan={eo:startTimeFromAscendingNode?}&amp;ctfan={eo:completionTimeFromAscendingNode?}&amp;iaa={eo:illuminationAzimuthAngle?}&amp;iza={eo:illuminationZenithAngle?}&amp;iea={eo:illuminationElevationAngle?}&amp;pm={eo:polarisationMode?}&amp;pc={eo:polarizationChannels?}&amp;ald={eo:antennaLookDirection?}&amp;minia={eo:minimumIncidenceAngle?}&amp;maxia={eo:maximumIncidenceAngle?}&amp;df={eo:dopplerFrequency?}&amp;iav={eo:incidenceAngleVariation?}</xsl:param>

    <xsl:template match="/">
        <OpenSearchDescription 
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
        xmlns:eo="http://a9.com/-/opensearch/extensions/eo/1.0/"
        xmlns:time="http://a9.com/-/opensearch/extensions/time/1.0/" 
        xmlns:geo="http://a9.com/-/opensearch/extensions/geo/1.0/" 
        xmlns:sar="http://earth.esa.int/sar" 
        xmlns:dc="http://purl.org/dc/elements/1.1/" 
        xmlns:dct="http://purl.org/dc/terms/" 
        xmlns:dclite4g="http://xmlns.com/2008/dclite4g#" 
        xmlns:ical="http://www.w3.org/2002/12/cal/ical#" 
        xmlns:atom="http://www.w3.org/2005/Atom" 
        xmlns:ws="http://dclite4g.xmlns.com/ws.rdf#" 
        xmlns:os="http://a9.com/-/spec/opensearch/1.1/" 
        xmlns:sru="http://a9.com/-/opensearch/extensions/sru/2.0/" 
        xmlns="http://a9.com/-/spec/opensearch/1.1/">
            <ShortName>openCaralogue</ShortName>
            <LongName>Earth Observation Catalogue</LongName>
            <Description>This OpenSearch Service allows the discovery of Earth Observation data. This search service is in accordance with the OGC 10-032r3 specification. </Description>
            <Tags>ESA, Earth Observation, Digital Repository, Catalogue, OGC</Tags>
            <Contact>openCatalogue@intecs.it</Contact>
            <Image height="75" width="125" type="image/png"><xsl:value-of select="$url"/>_include/img/profile/logo.png</Image>
            <Image height="16" width="16" type="image/vnd.microsoft.icon"><xsl:value-of select="$url"/>favicon.ico</Image>
            <Query role="example" geo:box="-25,30,45,70" />
            <Developer>Intecs team</Developer>
            <Attribution>Intecs</Attribution>
            <SyndicationRight>open</SyndicationRight>
            <AdultContent>false</AdultContent>
            <Language>en-us</Language>
            <OutputEncoding>UTF-8</OutputEncoding>
            <InputEncoding>UTF-8</InputEncoding>
            <Url type="application/atom+xml" indexOffset="1" pageOffset="1" template="" >
                <xsl:attribute name="template"><xsl:value-of select="$url"/>service/opensearch/atom/?q={searchTerm}&amp;count={count}&amp;startIndex={startIndex}&amp;startPage={startPage?}&amp;<xsl:value-of select="$geo_ext"/><xsl:value-of select="$time_ext"/><xsl:for-each select="//lst[@name='stats_fields']/lst"><xsl:if test="@name != 'beginPosition' and @name != 'endPosition'"><xsl:value-of select="@name"/>={eo:<xsl:value-of select="@name"/>?}&amp;</xsl:if></xsl:for-each><xsl:for-each select="//lst[@name='facet_fields']/lst"><xsl:if test="int[1] != '' "><xsl:value-of select="@name"/>={eo:<xsl:value-of select="@name"/>?}&amp;</xsl:if></xsl:for-each>&amp;recordSchema={sru:recordSchema?}</xsl:attribute>
            </Url>
            <Url type="application/vnd.google-earth.kml+xml" indexOffset="1" pageOffset="1" template="" >
                <xsl:attribute name="template"><xsl:value-of select="$url"/>service/opensearch/kml/?q={searchTerm}&amp;count={count}&amp;startIndex={startIndex}&amp;startPage={startPage?}&amp;<xsl:value-of select="$geo_ext"/><xsl:value-of select="$time_ext"/><xsl:for-each select="//lst[@name='stats_fields']/lst"><xsl:if test="@name != 'beginPosition' and @name != 'endPosition'"><xsl:value-of select="@name"/>={eo:<xsl:value-of select="@name"/>?}&amp;</xsl:if></xsl:for-each><xsl:for-each select="//lst[@name='facet_fields']/lst"><xsl:if test="int[1] != '' "><xsl:value-of select="@name"/>={eo:<xsl:value-of select="@name"/>?}&amp;</xsl:if></xsl:for-each>&amp;recordSchema={sru:recordSchema?}</xsl:attribute>
            </Url>
			<xsl:for-each select="//lst[@name='stats_fields']/lst">
				<xsl:choose>
					<xsl:when test="@name = 'beginPosition'">
						<param:Parameter>
								<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute> 
								<xsl:attribute name="value">{time:start}</xsl:attribute> 
								<xsl:attribute name="minimum">0</xsl:attribute> 
								<xsl:attribute name="minInclusive"><xsl:value-of select="*[@name='min']"/></xsl:attribute> 
								<xsl:attribute name="maxExclusive"><xsl:value-of select="*[@name='max']"/></xsl:attribute> 
						</param:Parameter>								
					</xsl:when>
					<xsl:when test="@name = 'endPosition'">
						<param:Parameter>
								<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute> 
								<xsl:attribute name="value">{time:end}</xsl:attribute> 
								<xsl:attribute name="minimum">0</xsl:attribute> 
								<xsl:attribute name="minInclusive"><xsl:value-of select="*[@name='min']"/></xsl:attribute> 
								<xsl:attribute name="maxExclusive"><xsl:value-of select="*[@name='max']"/></xsl:attribute> 
						</param:Parameter>			
					</xsl:when>
					<xsl:otherwise>
							<param:Parameter>
									<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute> 
									<xsl:attribute name="value">{eo:<xsl:value-of select="@name"/>}</xsl:attribute> 
									<xsl:attribute name="minimum">0</xsl:attribute> 
									<xsl:attribute name="minInclusive"><xsl:value-of select="*[@name='min']"/></xsl:attribute> 
									<xsl:attribute name="maxExclusive"><xsl:value-of select="*[@name='max']"/></xsl:attribute> 
							</param:Parameter>			
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			<xsl:for-each select="//lst[@name='facet_fields']/lst">
				<xsl:if test="int[1] != '' ">
				<param:Parameter>
						<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute> 
						<xsl:attribute name="value">{eo:<xsl:value-of select="@name"/>}</xsl:attribute> 
						<xsl:attribute name="minimum">0</xsl:attribute> 
						<xsl:attribute name="title"><xsl:value-of select="@name"/></xsl:attribute> 
						<xsl:for-each select="int">
							<param:Option>
								<xsl:attribute name="value"><xsl:value-of select="@name"/></xsl:attribute> 
								<xsl:attribute name="label"><xsl:value-of select="@name"/></xsl:attribute> 						
							</param:Option>
						</xsl:for-each>
				</param:Parameter>							
				</xsl:if>
			</xsl:for-each>            
        </OpenSearchDescription>
    </xsl:template>
</xsl:stylesheet>
