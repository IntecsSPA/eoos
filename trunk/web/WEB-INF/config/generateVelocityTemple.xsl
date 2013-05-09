<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rep="http://ngeo.eo.esa.int/schema/metadataReport" xmlns:alt="http://www.opengis.net/alt/2.0" xmlns:eop="http://www.opengis.net/eop/2.0" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:om="http://www.opengis.net/om/2.0" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:opt="http://www.opengis.net/opt/2.0" xmlns:atm="http://www.opengis.net/atm/2.0" xmlns:ows="http://www.opengis.net/ows/2.0" xmlns:sar="http://www.opengis.net/sar/2.0" xmlns:lmb="http://www.opengis.net/lmb/2.0" xmlns:stl="http://pisa.intecs.it/stl" version="2.0">
	<!--xsl:param name="sType">RADAR LIMB ATMOSPHERIC ALTIMETRIC OPTICAL</xsl:param-->
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no"/>		
    <xsl:param name="sType">OPTICAL</xsl:param>
	<xsl:template match="model">
		<xsl:apply-templates select="generalConstant"/>
		<!--File generated by the Feed via Velocity-->
<xsl:choose>
				<xsl:when test="$sType = 'RADAR' ">
					<sar:EarthObservation gml:id="ID_$velocityCount">
						<xsl:call-template name="fillEO"/>
					</sar:EarthObservation>
				</xsl:when>
				<xsl:when test="$sType  = 'OPTICAL'">
					<opt:EarthObservation gml:id="ID_$velocityCount">
						<xsl:call-template name="fillEO"/>
					</opt:EarthObservation>
				</xsl:when>
				<xsl:when test="$sType  = 'ATMOSPHERIC'">
					<atm:EarthObservation gml:id="ID_$velocityCount">
						<xsl:call-template name="fillEO"/>
					</atm:EarthObservation>
				</xsl:when>
				<xsl:when test="$sType  = 'ALTIMETRIC'">
					<alt:EarthObservation gml:id="ID_$velocityCount">
						<xsl:call-template name="fillEO"/>
					</alt:EarthObservation>
				</xsl:when>
				<xsl:when test="$sType  = 'LIMB'">
					<lmb:EarthObservation gml:id="ID_$velocityCount">
						<xsl:call-template name="fillEO"/>
					</lmb:EarthObservation>
				</xsl:when>
				<xsl:otherwise>
					<eop:EarthObservation gml:id="ID_$velocityCount">
						<xsl:call-template name="fillEO"/>
					</eop:EarthObservation>
				</xsl:otherwise>
			</xsl:choose>
	</xsl:template>
	<xsl:template name="fillEO">
		<om:phenomenonTime>
			<gml:TimePeriod gml:id="tp_$velocityCount">
				<gml:beginPosition><xsl:apply-templates select="attribute[@id='beginAcquisition']"/>
				</gml:beginPosition>
				<gml:endPosition><xsl:apply-templates select="attribute[@id='endAcquisition']"/>
				</gml:endPosition>
			</gml:TimePeriod>
		</om:phenomenonTime>
		<om:resultTime>
			<gml:TimeInstant gml:id="archivingdate_$velocityCount">
				<gml:timePosition>
					<xsl:apply-templates select="attribute[@id='availabilityTime']"/>
				</gml:timePosition>
			</gml:TimeInstant>
		</om:resultTime>
		<om:procedure>
			<xsl:choose>
				<xsl:when test="$sType  = 'ALTIMETRIC'">
					<alt:EarthObservationEquipment gml:id="eoe_$velocityCount">
						<xsl:call-template name="fillEOE"/>
					</alt:EarthObservationEquipment>
				</xsl:when>
				<xsl:when test="$sType  = 'LIMB'">
					<lmb:EarthObservationEquipment gml:id="eoe_$velocityCount">
						<xsl:call-template name="fillEOE"/>
					</lmb:EarthObservationEquipment>
				</xsl:when>
				<xsl:otherwise>
					<eop:EarthObservationEquipment gml:id="eoe_$velocityCount">
						<xsl:call-template name="fillEOE"/>
					</eop:EarthObservationEquipment>
				</xsl:otherwise>
			</xsl:choose>
		</om:procedure>
		<om:observedProperty nilReason="inapplicable"/>
		<om:featureOfInterest>
			<xsl:choose>
				<xsl:when test="$sType  = 'ALTIMETRIC'">
					<alt:Footprint gml:id="fp_$velocityCount">
						<xsl:call-template name="fillFP"/>
					</alt:Footprint>
				</xsl:when>
				<xsl:when test="$sType  = 'LIMB'">
					<lmb:Footprint gml:id="fp_$velocityCount">
						<xsl:call-template name="fillFP"/>
					</lmb:Footprint>
				</xsl:when>
				<xsl:otherwise>
					<eop:Footprint gml:id="fp_$velocityCount">
						<xsl:call-template name="fillFP"/>
					</eop:Footprint>
				</xsl:otherwise>
			</xsl:choose>
		</om:featureOfInterest>
		<om:result>
			<xsl:choose>
				<xsl:when test="$sType  = 'RADAR'">
					<sar:EarthObservationResult gml:id="eor_$velocityCount">
						<xsl:call-template name="fillEOR"/>
					</sar:EarthObservationResult>
				</xsl:when>
				<xsl:when test="$sType  = 'OPTICAL'">
					<opt:EarthObservationResult gml:id="eor_$velocityCount">
						<xsl:call-template name="fillEOR"/>
					</opt:EarthObservationResult>
				</xsl:when>
				<xsl:when test="$sType  = 'ATMOSPHERIC'">
					<atm:EarthObservationResult gml:id="eor_$velocityCount">
						<xsl:call-template name="fillEOR"/>
					</atm:EarthObservationResult>
				</xsl:when>
				<xsl:when test="$sType  = 'ALTIMETRIC'">
					<alt:EarthObservationResult gml:id="eor_$velocityCount">
						<xsl:call-template name="fillEOR"/>
					</alt:EarthObservationResult>
				</xsl:when>
				<xsl:when test="$sType  = 'LIMB'">
					<lmb:EarthObservationResult gml:id="eor_$velocityCount">
						<xsl:call-template name="fillEOR"/>
					</lmb:EarthObservationResult>
				</xsl:when>
				<xsl:otherwise>
					<eop:EarthObservationResult gml:id="eor_$velocityCount">
						<xsl:call-template name="fillEOR"/>
					</eop:EarthObservationResult>
				</xsl:otherwise>
			</xsl:choose>
		</om:result>
		<eop:metaDataProperty>
			<eop:EarthObservationMetaData>
				<eop:identifier>
					<xsl:apply-templates select="attribute[@id='productId']"/>
				</eop:identifier>
				<xsl:if test="attribute[@id='parentIdentifier'] !=''">
					<eop:parentIdentifier>
						<xsl:apply-templates select="attribute[@id='parentIdentifier']"/>
					</eop:parentIdentifier>
				</xsl:if>
				<eop:acquisitionType>
					<xsl:apply-templates select="attribute[@id='acquisitionType']"/>
				</eop:acquisitionType>
				<xsl:if test="attribute[@id='acquisitionSubType'] !=''">
					<eop:acquisitionSubType>
						<xsl:apply-templates select="attribute[@id='acquisitionSubType']"/>
					</eop:acquisitionSubType>
				</xsl:if>
				<eop:productType>
					<xsl:apply-templates select="attribute[@id='productType']"/>
				</eop:productType>
				<eop:status>
					<xsl:apply-templates select="attribute[@id='status']"/>
				</eop:status>
				<xsl:if test="attribute[@id='imageQualityDegradation'] !=''">
					<eop:imageQualityDegradation uom="%">
						<xsl:apply-templates select="attribute[@id='imageQualityDegradation']"/>
					</eop:imageQualityDegradation>
				</xsl:if>
				<xsl:if test="attribute[@id='imageQualityStatus'] !=''">
					<eop:imageQualityStatus>
						<xsl:apply-templates select="attribute[@id='imageQualityStatus']"/>
					</eop:imageQualityStatus>
				</xsl:if>
				<xsl:if test="attribute[@id='imageQualityDegradationTag'] !=''">
					<eop:imageQualityDegradationTag>
						<xsl:apply-templates select="attribute[@id='imageQualityDegradationTag']"/>
					</eop:imageQualityDegradationTag>
				</xsl:if>
				<xsl:if test="attribute[@id='imageQualityReportURL'] !=''">
					<eop:imageQualityReportURL>
						<xsl:apply-templates select="attribute[@id='imageQualityReportURL']"/>
					</eop:imageQualityReportURL>
				</xsl:if>
				<eop:processing>
					<xsl:if test="attribute[@id='processingMode'] !=''">
						<xsl:choose>
							<xsl:when test="$sType  = 'ALTIMETRIC'">
								<alt:ProcessingInformation>
									<eop:processingMode>
										<xsl:apply-templates select="attribute[@id='processingMode']"/>
									</eop:processingMode>
								</alt:ProcessingInformation>
							</xsl:when>
							<xsl:otherwise>
								<eop:ProcessingInformation>
									<eop:processingMode>
										<xsl:apply-templates select="attribute[@id='processingMode']"/>
									</eop:processingMode>
								</eop:ProcessingInformation>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:if>
				</eop:processing>
				<xsl:if test="attribute[@id='productGroupId'] !=''">
					<eop:productGroupId>
						<xsl:apply-templates select="attribute[@id='productGroupId']"/>
					</eop:productGroupId>
				</xsl:if>
				<xsl:for-each select="attribute[substring-before(@id,'-')='add']">
					<eop:vendorSpecific>
						<eop:SpecificInformation>
							<eop:localAttribute>
								<xsl:value-of select="substring-after(@id,'-')"/>
							</eop:localAttribute>
							<eop:localValue>
								<xsl:apply-templates select="."/>
							</eop:localValue>
						</eop:SpecificInformation>
					</eop:vendorSpecific>
				</xsl:for-each>
			</eop:EarthObservationMetaData>
		</eop:metaDataProperty>
	</xsl:template>
	<xsl:template name="fillAcq">
		<xsl:if test="attribute[@id='orbitNumber'] != ''">
			<eop:orbitNumber>
				<xsl:apply-templates select="attribute[@id='orbitNumber']"/>
			</eop:orbitNumber>
		</xsl:if>
		<xsl:if test="attribute[@id='orbitDirection'] != ''">
			<eop:orbitDirection>
				<xsl:apply-templates select="attribute[@id='orbitDirection']"/>
			</eop:orbitDirection>
		</xsl:if>
		<xsl:if test="attribute[@id='wrsLongitudeGrid'] != ''">
			<eop:wrsLongitudeGrid codeSpace="">
				<xsl:apply-templates select="attribute[@id='wrsLongitudeGrid']"/>
			</eop:wrsLongitudeGrid>
		</xsl:if>
		<xsl:if test="attribute[@id='wrsLatitudeGrid'] != ''">
			<eop:wrsLatitudeGrid codeSpace="">
				<xsl:apply-templates select="attribute[@id='wrsLatitudeGrid']"/>
			</eop:wrsLatitudeGrid>
		</xsl:if>
		<xsl:if test="attribute[@id='startTimeFromAscendingNode'] != ''">
			<eop:startTimeFromAscendingNode uom="ms">
				<xsl:apply-templates select="attribute[@id='startTimeFromAscendingNode']"/>
			</eop:startTimeFromAscendingNode>
		</xsl:if>
		<xsl:if test="attribute[@id='completionTimeFromAscendingNode'] != ''">
			<eop:completionTimeFromAscendingNode uom="ms">
				<xsl:apply-templates select="attribute[@id='completionTimeFromAscendingNode']"/>
			</eop:completionTimeFromAscendingNode>
		</xsl:if>
		<xsl:if test="attribute[@id='illuminationAzimuthAngle'] != ''">
			<eop:illuminationAzimuthAngle uom="deg">
				<xsl:apply-templates select="attribute[@id='illuminationAzimuthAngle']"/>
			</eop:illuminationAzimuthAngle>
		</xsl:if>
		<xsl:if test="attribute[@id='illuminationZenithAngle'] != ''">
			<eop:illuminationZenithAngle uom="deg">
				<xsl:apply-templates select="attribute[@id='illuminationZenithAngle']"/>
			</eop:illuminationZenithAngle>
		</xsl:if>
		<xsl:if test="attribute[@id='illuminationElevationAngle'] != ''">
			<eop:illuminationElevationAngle uom="deg">
				<xsl:apply-templates select="attribute[@id='illuminationElevationAngle']"/>
			</eop:illuminationElevationAngle>
		</xsl:if>
		<!-- THIS IS SPECIFIC FOR THE SAR-->
		<xsl:if test="$sType  = 'RADAR'">
			<xsl:if test="attribute[@id='polarisationMode'] != ''">
				<sar:polarisationMode>
					<xsl:apply-templates select="attribute[@id='polarisationMode']"/>
				</sar:polarisationMode>
			</xsl:if>
			<xsl:if test="attribute[@id='polarisationChannels'] != ''">
				<sar:polarisationChannels>
					<xsl:apply-templates select="attribute[@id='polarisationChannels']"/>
				</sar:polarisationChannels>
			</xsl:if>
			<xsl:if test="attribute[@id='antennaLookDirection'] != ''">
				<sar:antennaLookDirection>
					<xsl:apply-templates select="attribute[@id='antennaLookDirection']"/>
				</sar:antennaLookDirection>
			</xsl:if>
			<xsl:if test="attribute[@id='minimumIncidenceAngle'] != ''">
				<sar:minimumIncidenceAngle uom="deg">
					<xsl:apply-templates select="attribute[@id='minimumIncidenceAngle']"/>
				</sar:minimumIncidenceAngle>
			</xsl:if>
			<xsl:if test="attribute[@id='maximumIncidenceAngle'] != ''">
				<sar:maximumIncidenceAngle uom="deg">
					<xsl:apply-templates select="attribute[@id='maximumIncidenceAngle']"/>
				</sar:maximumIncidenceAngle>
			</xsl:if>
			<xsl:if test="attribute[@id='incidenceAngleVariation'] != ''">
				<sar:incidenceAngleVariation uom="deg">
					<xsl:apply-templates select="attribute[@id='incidenceAngleVariation']"/>
				</sar:incidenceAngleVariation>
			</xsl:if>
			<xsl:if test="attribute[@id='dopplerFrequency'] != ''">
				<sar:dopplerFrequency uom="deg">
					<xsl:apply-templates select="attribute[@id='dopplerFrequency']"/>
				</sar:dopplerFrequency>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$sType  = 'ATMOSPHERIC'">
			<xsl:if test="attribute[@id='multiViewAngles'] != ''">
				<atm:multiViewAngles>
					<xsl:apply-templates select="attribute[@id='multiViewAngles']"/>
				</atm:multiViewAngles>
			</xsl:if>
			<xsl:if test="attribute[@id='centreViewAngles'] != ''">
				<atm:centreViewAngles>
					<xsl:apply-templates select="attribute[@id='centreViewAngles']"/>
				</atm:centreViewAngles>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<xsl:template name="fillEOR">
		<eop:product>
			<eop:ProductInformation>
				<eop:fileName>
					<ows:ServiceReference xlink:href="">
						<xsl:attribute name="xlink:href"><xsl:apply-templates select="attribute[@id='productURI']"/></xsl:attribute>
						<ows:RequestMessage/>
					</ows:ServiceReference>
				</eop:fileName>
				<!-- CHECK IF THIS IS THE RIGHT POSITION-->
				<xsl:if test="attribute[@id='ProductVersion'] != ''">
					<eop:version>
						<xsl:apply-templates select="attribute[@id='ProductVersion']"/>
					</eop:version>
				</xsl:if>
				<!-- CHECK IF THIS IS THE RIGHT POSITION-->
				<xsl:if test="attribute[@id='productSize'] != ''">
					<eop:size uom="kb">
						<xsl:apply-templates select="attribute[@id='productSize']"/>
					</eop:size>
				</xsl:if>
			</eop:ProductInformation>
		</eop:product>
		<xsl:if test="$sType  = 'OPTICAL'">
			<xsl:if test="attribute[@id='cloudCoverPercentage'] != ''">
				<opt:cloudCoverPercentage uom="%">
					<xsl:apply-templates select="attribute[@id='cloudCoverPercentage']"/>
				</opt:cloudCoverPercentage>
			</xsl:if>
			<xsl:if test="attribute[@id='snowCoverPercentage'] != ''">
				<opt:snowCoverPercentage uom="%">
					<xsl:apply-templates select="attribute[@id='snowCoverPercentage']"/>
				</opt:snowCoverPercentage>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$sType  = 'ATMOSPHERIC'">
			<xsl:if test="attribute[@id='cloudCoverPercentage'] != ''">
				<atm:cloudCoverPercentage uom="%">
					<xsl:apply-templates select="attribute[@id='cloudCoverPercentage']"/>
				</atm:cloudCoverPercentage>
			</xsl:if>
			<xsl:if test="attribute[@id='snowCoverPercentage'] != ''">
				<atm:snowCoverPercentage uom="%">
					<xsl:apply-templates select="attribute[@id='snowCoverPercentage']"/>
				</atm:snowCoverPercentage>
			</xsl:if>
		</xsl:if>
	</xsl:template>
	<xsl:template name="fillEOE">
		<eop:platform>
			<eop:Platform>
				<eop:shortName>
					<xsl:apply-templates select="attribute[@id='platformShortName']"/>
				</eop:shortName>
				<xsl:if test="attribute[@id='serialIdentifier'] != ''">
					<eop:serialIdentifier>
						<xsl:apply-templates select="attribute[@id='serialIdentifier']"/>
					</eop:serialIdentifier>
				</xsl:if>
			</eop:Platform>
		</eop:platform>
		<eop:instrument>
			<eop:Instrument>
				<eop:shortName>ASAR</eop:shortName>
			</eop:Instrument>
		</eop:instrument>
		<xsl:choose>
				<xsl:when test="$sType  = 'LIMB'"></xsl:when>
				<xsl:otherwise>
                                        <eop:sensor>
					<eop:Sensor>
						<xsl:if test="attribute[@id='sensorType'] != ''">
							<eop:sensorType>
								<xsl:apply-templates select="attribute[@id='sensorType']"/>
							</eop:sensorType>
						</xsl:if>
						<eop:operationalMode codeSpace="any:sensorMode">
							<xsl:apply-templates select="attribute[@id='operationalMode']"/>
						</eop:operationalMode>
						<xsl:if test="attribute[@id='swathIdentifier'] != ''">
							<eop:swathIdentifier>
								<xsl:apply-templates select="attribute[@id='swathIdentifier']"/>
							</eop:swathIdentifier>
						</xsl:if>
					</eop:Sensor>
                                        
                        </eop:sensor>
				</xsl:otherwise>
			</xsl:choose> 
		<!-- we should include here an IF to check if at least one of the element is included in the config file-->
                    
			<xsl:choose>
				<xsl:when test="$sType  = 'RADAR'">
                                    <eop:acquisitionParameters>
					<sar:Acquisition>
						<xsl:call-template name="fillAcq"/>
					</sar:Acquisition>   
                                        </eop:acquisitionParameters>
				</xsl:when>
				<!--xsl:when test="$sType  = 'OPTICAL'">
                              
                                        <opt:Acquisition>
						<xsl:call-template name="fillAcq"/>
					</opt:Acquisition>
				</xsl:when-->
				<xsl:when test="$sType  = 'ATMOSPHERIC'">
					<eop:acquisitionParameters>
                                            <atm:Acquisition>
						<xsl:call-template name="fillAcq"/>
                                            </atm:Acquisition>
                                            </eop:acquisitionParameters>
				</xsl:when>
				<xsl:when test="$sType  = 'ALTIMETRIC'">
                                            <eop:acquisitionParameters>
                                            <alt:Acquisition>
						<xsl:call-template name="fillAcq"/>
                                            </alt:Acquisition>
                                            </eop:acquisitionParameters>
				</xsl:when>
				<xsl:when test="$sType  = 'LIMB'">
                                            <lmb:acquisitionParameters>
                                                <lmb:Acquisition>
                                                    <xsl:call-template name="fillAcq"/>
                                            </lmb:Acquisition>
                                            </lmb:acquisitionParameters>
				</xsl:when>
				<xsl:otherwise>
                                    <eop:acquisitionParameters>
                                            <eop:Acquisition>
                                                    <xsl:call-template name="fillAcq"/>
                                            </eop:Acquisition>
                                            </eop:acquisitionParameters>
				</xsl:otherwise>
			</xsl:choose>
                       
			<xsl:choose>
				<xsl:when test="$sType  = 'LIMB'">
                                     <lmb:sensor>
                                    	<lmb:Sensor>
						<xsl:if test="attribute[@id='sensorType'] != ''">
							<eop:sensorType>
								<xsl:apply-templates select="attribute[@id='sensorType']"/>
							</eop:sensorType>
						</xsl:if>
						<eop:operationalMode codeSpace="any:sensorMode">
							<xsl:apply-templates select="attribute[@id='operationalMode']"/>
						</eop:operationalMode>
						<xsl:if test="attribute[@id='swathIdentifier'] != ''">
							<eop:swathIdentifier>
								<xsl:apply-templates select="attribute[@id='swathIdentifier']"/>
							</eop:swathIdentifier>
						</xsl:if>
					</lmb:Sensor>
                                     </lmb:sensor>
				</xsl:when>
				<xsl:otherwise></xsl:otherwise>
			</xsl:choose>
	</xsl:template>
	<xsl:template name="fillFP">
		<eop:multiExtentOf>
			<gml:MultiSurface gml:id="ms_$velocityCount" srsName="EPSG:4326">
				<gml:surfaceMembers>
					<gml:Polygon gml:id="fppoly_$velocityCount">
						<gml:exterior>
							<gml:LinearRing>
								<gml:posList>
									<xsl:apply-templates select="attribute[@id='footprint']"/>
								</gml:posList>
							</gml:LinearRing>
						</gml:exterior>
					</gml:Polygon>
				</gml:surfaceMembers>
			</gml:MultiSurface>
		</eop:multiExtentOf>
		<xsl:if test="$sType  = 'ALTIMETRIC'">
			<xsl:if test="attribute[@id='nominalTrack'] != ''">
				<alt:nominalTrack>
					<xsl:apply-templates select="attribute[@id='nominalTrack']"/>
				</alt:nominalTrack>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$sType  = 'LIMB'">
			<xsl:if test="attribute[@id='nominalTrack'] != ''">
				<lmb:nominalTrack>
					<xsl:apply-templates select="attribute[@id='nominalTrack']"/>
				</lmb:nominalTrack>
			</xsl:if>
			<xsl:if test="attribute[@id='occultationPoints'] != ''">
				<lmb:occultationPoints>
					<xsl:apply-templates select="attribute[@id='occultationPoints']"/>
				</lmb:occultationPoints>
			</xsl:if>
		</xsl:if>
	</xsl:template>


	<xsl:template match="generalConstant ">
#set( $<xsl:value-of select="@name"/> = "<xsl:value-of select="."/>" )
</xsl:template>
	<xsl:template match="attribute">
		<xsl:apply-templates select="*"/>
	</xsl:template>
	<xsl:template match="defaultValue">
		<xsl:value-of select="."/>
	</xsl:template>
        <!--xsl:template match="indexFieldName">$metadata.<xsl:value-of select="."/></xsl:template-->
        <xsl:template match="indexFieldName">#if( $metadata.<xsl:value-of select="."/> != '' )$metadata.<xsl:value-of select="."/>#end</xsl:template>        
        <xsl:template match="formula">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template name="extract">
		<xsl:param name="attribute"/>
		<xsl:choose>
			<xsl:when test="$attribute/indexFieldName != ''">$<xsl:value-of select="$attribute/indexFieldName"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="$attribute"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:transform>
