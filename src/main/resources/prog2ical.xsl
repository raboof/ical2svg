<?xml version='1.0' encoding='ISO-8859-1'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version='1.0'>
<xsl:template match="tv">
<xsl:for-each select="channel">
<xsl:variable name="channel-id" select="@id"/>
<xsl:document method="text" encoding="UTF-8" indent="no" href="{$channel-id}.ics">BEGIN:VCALENDAR
CALSCALE:GREGORIAN
X-WR-CALNAME: <xsl:value-of select="display-name" />
PRODID: Irvin TV ical generator
VERSION:2.0
<xsl:for-each select="/tv/programme"><xsl:if test="@channel = $channel-id">BEGIN:VEVENT
UID:<xsl:value-of select="generate-id()" />
SUMMARY:<xsl:call-template name="ical_text_format"><xsl:with-param name="text" select="title" /></xsl:call-template> 
DESCRIPTION:<xsl:call-template name="ical_text_format"><xsl:with-param name="text" select="desc" /></xsl:call-template> 
DTSTART:<xsl:value-of select="substring(@start,1,8)" />T<xsl:value-of select="substring(@start,9,6) + 100 * substring(@start,18,4)" />Z
DTEND:<xsl:value-of select="substring(@stop,1,8)" />T<xsl:value-of select="substring(@stop,9,6) + 100 * substring(@stop,18,4)" />Z
END:VEVENT
</xsl:if></xsl:for-each>END:VCALENDAR
</xsl:document>
</xsl:for-each>
</xsl:template>

<xsl:template name="ical_text_format">
<xsl:param name="text"/>
<xsl:choose>
<xsl:when test="contains($text, ',')">
<xsl:variable name="before" select="substring-before($text, ',')"/>
<xsl:variable name="after" select="substring-after($text, ',')"/>
<xsl:variable name="prefix" select="concat($before, '\,')"/>
<xsl:value-of select="normalize-space($before)"/>
<xsl:value-of select="'\,'"/>
<xsl:call-template name="ical_text_format">
<xsl:with-param name="text" select="$after"/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="normalize-space($text)"/>
</xsl:otherwise>
</xsl:choose>            
</xsl:template>
</xsl:stylesheet>
