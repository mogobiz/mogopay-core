<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

<xsl:template match="/">
<html>
<head><title>Enregistrement des composants</title></head>
<body bgcolor="#ffffff">
<xsl:for-each select="/response" />
<center><h1>Etat du serveur</h1></center>
<form><center>
<xsl:apply-templates select="/response" /></center>
<center><input type="button" value="Retour" name="retour" OnClick="history.back()"/></center>
</form>
</body>
</html>
</xsl:template>

<xsl:template match="response">
			<p>
				<table width="80%">
					<tr>
						<td  bgcolor ="99CCCC"><strong>COMMANDE = </strong>
						<xsl:value-of select="@component"/> / <xsl:value-of select="@name"/></td></tr>
						  <tr><td><strong>Message = <xsl:value-of select="refresh/@message "/> </strong></td></tr>				 				
						</table>
						</p>
			</xsl:template>
</xsl:stylesheet>
