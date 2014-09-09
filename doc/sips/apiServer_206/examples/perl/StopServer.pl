#!perl

# Exemple d'envoi d'une requ�te de commande StopServer vers l'API Server.
#
# Exemple of a sending of a StopServer request to the API Server

use IO::Socket;
$EOL = "\015\012";
$BLANK = $EOL x 2;

# Constitution du message
# Creation of the message
$message="<command login=\"admin\" password=\"admin\" name=\"StopServer NOW\"></command>\n";

# Cr�ation de la socket
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

# Envoi de la requ�te        
# Sending the request        
print $remote $message . $BLANK;
 
# fermeture de la socket     
# close the socket      
close $remote;
 
