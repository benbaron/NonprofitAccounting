<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:jr="http://jasperreports.sourceforge.net/jasperreports"
  exclude-result-prefixes="jr">

  <!-- Identity copy -->
  <xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>

  <!-- Fix style boolean aliases on any element (most often jr:style, but allow inlined styling too) -->
  <xsl:template match="@isBold">
    <xsl:attribute name="bold"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

  <xsl:template match="@isItalic">
    <xsl:attribute name="italic"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

  <xsl:template match="@isUnderline">
    <xsl:attribute name="underline"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

  <xsl:template match="@isStrikethrough">
    <xsl:attribute name="strikeThrough"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

  <!-- Align alias -->
  <xsl:template match="@hAlign">
    <xsl:attribute name="hTextAlign"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

</xsl:stylesheet>
