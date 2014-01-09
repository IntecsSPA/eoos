<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="/">
    <add>
        <doc>
            <field name="id">
                <xsl:value-of select="generate-id()"/>
            </field>
            <field name="id">
                <xsl:value-of select="generate-id()"/>
            </field>
            
      <xsl:apply-templates select="*" />
		</doc>
    </add>
  </xsl:template>
  
  <!--
  parentIdentifier
  multiExtentOf 
  beginPosition
  endPosition
  imageQualityDegradation
  acquisitionType
  acquisitionSubType
  orbitDirection
  Status
  orbitNumber
  productType
  acquisitionStation
  lastOrbitNumber
 archivingCenter

AcquisitionPlatform

instrumentShortName
platformSerialIdentifier
rim:Name (mapped with the eop:Platform/eop:shortName of the O&M Metadata)
sensorType
sensorOperationalMode
sensorResolution
swathIdentifier


OPT
cloudCoverPercentage
snowCoverPercentage

SAR
polarisationMode
polarisationChannel
minimumIncidenceAngle
maximumIncidenceAngle

ATM
cloudCoverPercentage
snowCoverPercentage

SSP
cloudCoverPercentage
snowCoverPercentage


  -->
  
 <xsl:template match="text()">
     <field name="{name(..)}">
       <xsl:value-of select="."/>
     </field>
 </xsl:template>
</xsl:stylesheet>