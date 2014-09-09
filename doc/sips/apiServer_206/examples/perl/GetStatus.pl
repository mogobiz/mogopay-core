#!perl

# Exemple d'envoi d'une requête de commande getStatus vers l'API Server.
#
# Exemple of a sending of a getStatus request to the API Server

use IO::Socket;
$EOL = "\015\012";
$BLANK = $EOL x 2;

# Constitution du message
# Creation of the message
$message="<command component=\"status\" name=\"getStatus\"></command>\n";

# Création de la socket
# Le champ PeerAddr contient l'adresse IP de l'API Server
# Le champ PeerPort contient le port de commande (7181 par defaut)
#
# Creation of the socket
# the field PeerAddr contain the API Server IP address
# the field PeerPort contain the API Server port (7181 by default)
$remote = IO::Socket::INET->new(PeerAddr => '*** INDICATE HERE THE API SERVER IP USED ***',
                                PeerPort   => '7181',
                                Proto      => 'tcp');

die "Could not create socket: $!\n" unless $remote; 

# Envoi de la requête        
# Sending the request        
print $remote $message . $BLANK;
 
# lecture de la réponse       
# read the response       
read($remote, $response, 8192);

print "response of the API Server: ".$response ;
    	
# fermeture de la socket     
# close the socket      
close $remote;
 
