<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan">
	<xsl:output method="xml" omit-xml-declaration="yes"
		encoding="UTF-8" indent="yes" xalan:indent-amount="2"
		xalan:line-separator="&#10;" />
	<xsl:strip-space elements="*" />
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="/">
		<xsl:apply-templates />
		<xsl:text>&#10;</xsl:text>
		<xsl:text>&#10;</xsl:text>
	</xsl:template>
</xsl:stylesheet>
