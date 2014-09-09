<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

<xsl:template match="/">
<html>
<head><title>Liste des composants</title><META HTTP-EQUIV="ListComponent.class" CONTENT="revealTrans(Duration=3,Transition=1)"/></head>
<xsl:for-each select="/response/listComponents/APIITem" />
<body bgcolor="#ffffff">
<center><h1>Liste des composants</h1></center>
<form>
<center><xsl:apply-templates select="/response/listComponents/APIITem" /></center>
<center><input type="button" value="Retour" name="retour" OnClick="history.back()"/></center>
</form>
</body>
</html>
</xsl:template>

<xsl:template match="/response/listComponents/APIITem">
						<p>
							<table width="80%">
								<tr>
								<td  bgcolor ="99CCCC"><strong>NOM = </strong>
								<xsl:value-of select="@name"/></td></tr>
									<tr><td><strong>version = </strong> <xsl:value-of select="@version "/></td></tr>
									<tr><td><strong>date de cr&#233;ation = </strong><xsl:value-of select="@creationDate "/>										</td></tr>
									<tr><td><strong>courte description = </strong> <xsl:value-of select="@shortDesc "/></td>								</tr>
													
							<tr><td><strong>nom d'identifiant = </strong><xsl:value-of select="classId/@name"/></td></tr>
							<tr><td><strong>classe = </strong><xsl:value-of select="classId/@class"/></td></tr>
					
								<xsl:for-each select="serviceList/service">  
								<tr><td><strong>nom du service = </strong><xsl:value-of select="@name"/></td></tr>
								<tr><td><strong>fonction du service = </strong><xsl:value-of select="@fonction"/></td></tr>
								</xsl:for-each>
							</table>
						</p>
			</xsl:template>
</xsl:stylesheet>

