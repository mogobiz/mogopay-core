<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

<xsl:template match="/">
<html>
<head><title>Etat du serveur</title></head>
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
						  <tr><td><strong>date de d&#233;marrage = </strong> <xsl:value-of select="getStatus/@startDate "/></td></tr>
						  <tr><td><strong>heure de d&#233;marrage = </strong><xsl:value-of select="getStatus/@startTime "/></td></tr>
						  <tr><td><strong>date du serveur = </strong> <xsl:value-of select="getStatus/@serverDate"/></td></tr>
						  <tr><td><strong>heure du serveur = </strong> <xsl:value-of select="getStatus/@serverTime "/></td></tr>
						  <tr><td><strong>nombre de requ&#234;tes de service = </strong> <xsl:value-of select="getStatus/@serviceCount "/></td></tr>
						 <tr><td><strong>nombre de requ&#234;tes de commande = </strong> <xsl:value-of select="getStatus/@commandCount "/></td></tr>
						<tr><td><strong>nombre d'erreurs = </strong> <xsl:value-of select="getStatus/@errorCount "/></td></tr>
						<tr><td><strong>nombre d'erreurs fatales = </strong> <xsl:value-of select="getStatus/@fatalCount "/></td></tr>        				 	    
								<xsl:element name="lastRequest">  			
							<tr><td><strong>date de la derni&#232;re requ&#234;te de service = </strong><xsl:value-of select="getStatus/lastRequest/@Date"/></td></tr>
							<tr><td><strong>heure de la derni&#232;re requ&#234;te de service = </strong><xsl:value-of select="getStatus/lastRequest/@Time "/></td></tr>
							<tr><td><strong>nom de la derni&#232;re requ&#234;te de service = </strong><xsl:value-of select="getStatus/lastRequest/@Name "/></td></tr>
								</xsl:element>
								
								<xsl:element name="lastCommand">  
							<tr><td><strong>date de la derni&#232;re requ&#234;te de commande = </strong><xsl:value-of select="getStatus/lastCommand/@Date"/></td></tr>
							<tr><td><strong>heure de la derni&#232;re requ&#234;te de commande = </strong><xsl:value-of select="getStatus/lastCommand/@Time "/></td></tr>
							<tr><td><strong>nom de la derni&#232;re requ&#234;te de commande = </strong><xsl:value-of select="getStatus/lastCommand/@Name "/></td></tr>
								</xsl:element>
				 				
							</table>
						</p>
			</xsl:template>
</xsl:stylesheet>
