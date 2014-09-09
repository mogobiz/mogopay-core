#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>

#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>


/*
	Exemple d'envoi d'une requete de commande getStatus vers l'API Server 
	Exemple of a sending of a getStatus request to the API Server
*/
main(int argc, char *argv[]) 
{
int 	s;
int	connexion;
int	reception;
int	envoi;

struct 	sockaddr_in 	peeraddr_in;
struct 	hostent 	*hp;

char  message[8192];
char  response[8192];
char*	diag_msg;

	message[0]='\0';
	response[0]='\0';


	/* initialisation du message de la requête
       initialisation of the request message
     */
	
	strcpy(message,"<command component=\"status\" name=\"getStatus\"></command>\n");
	
	/* création de la socket 
       creation of the socket
    */
	s= socket(AF_INET,SOCK_STREAM,0);
	
	if	( s == -1 )
		{
		printf("socket creation failed\n");
		exit (-1);
		}

	/* initialisation des paramêtres de connexion de l'API Server
       initialisation of API Server connexion parameters
     */
	
	/**** INDIQUER LE PORT UTILISE PAR LE SERVEUR D'API (7181 par défaut) ***/
	/**** INDICATE THE API SERVER PORT USED (7181 by default) ***/
	peeraddr_in.sin_port=htons(7181);
		
	hp = gethostbyname("*** INDICATE HERE THE API SERVER NAME OR IP ADDRESS ***");
	if ( hp == NULL )
	{
		printf("Server not found\n");
		exit (-1);
	}

	peeraddr_in.sin_family=AF_INET;
	peeraddr_in.sin_addr.s_addr= ((struct in_addr *)(hp->h_addr))->s_addr;
	memset(peeraddr_in.sin_zero,0,sizeof(peeraddr_in.sin_zero));
	
	/* connexion à l'API Server
       API Server connexion
     */
	
	connexion = connect (s, (struct sockaddr*) &peeraddr_in,sizeof(struct sockaddr_in));
	if (connexion == -1)
		{
		printf("socket connexion failed (%d)\n", connexion);
		printf("error errno: (%d)\n",errno);
		exit (-1);
		}

	/* envoi du message 
       sending the message
    */
	envoi = send ( s, message, strlen(message),0);
	if( envoi == -1 )
		{
		printf("the sending of the message failed\n");
		exit (-1);
		}
	
	/* réception de la réponse
       receipt of the response
    */
	reception = recv(s, response,sizeof(response),0);
	if( reception == 0 || reception ==  -1 )
		{
		printf("the receipt of the response failed\n");
		printf("error errno: (%d)\n",errno);
		exit (-1);
		}

	printf("response of the API Server:(%s)\n", response);
	
	close(s);
	exit (0);

}
