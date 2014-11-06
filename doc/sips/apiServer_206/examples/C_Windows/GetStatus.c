#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>

#include <winsock.h>

/*
	Exemple d'envoi d'une requete de commande getStatus vers l'API Server.
	Pour compiler cet exemple vous devez ajouter la librairie wsock32.lib
	à l'éditeur de liens.
	Exemple of a sending of a getStatus request to the API Server
	To compile this example you need to add the wsock32.lid library
    to the link editor.
*/
main(int argc, char *argv[])
{
int 	s;
int	connexion;
int	reception;

struct 	sockaddr_in 	peeraddr_in;
struct 	hostent 	*hp;

char  message[8192];
char  response[8192];
char  result[8192];

WORD	wVersionRequested;
WSADATA	wsaData;


	message[0]='\0';
	response[0]='\0';
	result[0]='\0';

	/* initialisation du message de la requête
       initialisation of the request message
     */

	strcpy(message,"<command component=\"status\" name=\"getStatus\"></command>\n");


	/*  Démarage de la socket
		Startup socket
	*/

	wVersionRequested = MAKEWORD(1,1);

	if ( WSAStartup(wVersionRequested, &wsaData) != 0 )
		{
		printf("error WSAStartup");
		exit (-1);
		}

     /* initialisation des paramêtres de connexion de l'API Server
       initialisation of API Server connexion parameters
     */

	/**** INDIQUER LE PORT UTILISE PAR LE SERVEUR D'API (7181 par défaut) ***/
	/**** INDICATE THE API SERVER PORT USED (7181 by default) ***/
	peeraddr_in.sin_port=htons(7181);

	hp = gethostbyname("*** INDICATE HERE THE API SERVER IP USED ***");
	if ( hp == NULL )
	{
		printf("Server not found\n");
		errno=WSAGetLastError();
		printf("error errno: (%d)\n",errno);
		WSACleanup();
		exit (-1);
	}

	peeraddr_in.sin_family=AF_INET;
	peeraddr_in.sin_addr.s_addr= ((struct in_addr *)(hp->h_addr))->s_addr;
	memset(peeraddr_in.sin_zero,0,sizeof(peeraddr_in.sin_zero));

	/* création de la socket
       creation of the socket
    */
	s= socket(AF_INET,SOCK_STREAM,0);

	if	( s == -1 )
		{
		printf("socket creation failed (%d)\n",s);
		errno=WSAGetLastError();
		printf("error errno: (%d)\n",errno);
		WSACleanup();
		exit (-1);
		}

	 /* connexion à l'API Server
        API Server connexion
     */

	connexion = connect (s, (struct sockaddr*) &peeraddr_in,sizeof(struct sockaddr_in));
	if (connexion == -1)
		{
		printf("socket connexion failed (%d)\n", connexion);
		errno=WSAGetLastError();
		printf("error errno: (%d)\n",errno);
		closesocket(s);
		WSACleanup();
		exit (-1);
		}

	/* envoi du message
       sending the message
    */
	envoi = send ( s, message, strlen(message),0);
	if( envoi == -1 )
		{
		printf("the sending of the message failed\n");
		errno=WSAGetLastError();
		printf("error errno: (%d)\n",errno);
		closesocket(s);
		WSACleanup();
		exit (-1);
		}

	/* réception de la réponse
       receipt of the response
    */
	reception = recv(s, response,sizeof(response),0);
	response[reception]= '\0';

	if( reception == 0 || reception ==  -1 )
		{
		printf("the receipt of the response failed\n");
		errno=WSAGetLastError();
		printf("error errno: (%d)\n",errno);
		closesocket(s);
		WSACleanup();
		exit (-1);
		}
	printf("response of the API Server:(%s)\n", response);


    /* fermeture de la socket
       socket close
    */
	closesocket(s);
	WSACleanup();
	exit (0);

}
