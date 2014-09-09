<%@ LANGUAGE = VBScript%>

<!-- 
  Exemple d'envoi d'une requete de commande getStatus vers l'API Server.
	Le composant ActiveX utilisé dans cet exemple ASP (w3 socket)
	peut être téléchargé sur le site www.dimac.net 

	Exemple of a sending of a getStatus request to the API Server
	The ActiveX Component used in this example (w3 socket)
	can be download on the site www.dimac.net
-->
<HTML><HEAD><TITLE>Test de l'API Server</TITLE>
<META content="text/html; charset=windows-1252" http-equiv=Content-Type>
<META content="MSHTML 5.00.2920.0" name=GENERATOR></HEAD>
<BODY bgColor=#ffffff>
<CENTER>
<FONT color=#009900 size=6>resultat de l'appel de l'API Server</FONT></CENTER>
<HR>

<%
dim XMLInput
dim HTMLInput

dim XMLOutput
dim HTMLOutput

REM -- Cette fonction modifie un buffer XML pour l'afficher sur une page HTML
REM -- This function modify a XML buffer to print it on a HTML page
function XML2HTML(XMLInput)
intLongueur = len(XMLInput)
for intcptChar = 1 to intLongueur
testChar = mid(XMLInput, intcptChar, 1)
	select case testChar
		case ">" 	XML2HTML = XML2HTML & "<br>"
		case "<"	XML2HTML = XML2HTML
		case else	XML2HTML = XML2HTML + testChar
	end select
next
end function

REM-- initialisation du message de la requête
REM-- initialisation of the request message
XMLInput = "<command component=""status"" name=""getStatus""></command>"



HTMLInput = XML2HTML(XMLInput)
response.write ("requete :<BR>" & HTMLInput)


dim socket
dim buffer

REM-- Création de la socket
REM-- Creation of the socket
response.write ("<br><b>connexion serveur</b>")
set socket = server.CreateObject("Socket.TCP")
socket.timeout = 100000

REM -- initialisation des paramêtres de connexion de l'API Server
REM -- initialisation of API Server connexion parameters

REM -- INDIQUER LE PORT ET L'ADRESSE IP UTILISES PAR LE SERVEUR D'API (port par défaut 7181)
REM -- INDICATE THE API SERVER PORT and IP ADDRESS USED (port by default 7181)

socket.host = "*** INDICATE HERE THE API SERVER IP USED ***:7181"

REM-- connexion à l'API Server
REM-- API Server connexion
socket.open()

REM-- envoi du message
REM-- sending the message
socket.Sendline( XMLInput )
socket.WaitForDisconnect()

REM-- réception de la réponse
REM-- receipt of the response
XMLOutput = socket.Buffer
HTMLOutput = XML2HTML(XMLOutput)

response.write ("<BR>reponse: <BR>" & HTMLOutput )

REM-- fermeture de la socket
REM-- socket close
socket.close()
response.write ("<br><b>Fermeture de la socket</b><br><br>")

%>

<HR>
</BODY></HTML>
