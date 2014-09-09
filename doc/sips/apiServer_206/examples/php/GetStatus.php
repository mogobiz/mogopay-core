<?php
	// Exemple d'envoi d'une requete de commande getStatus vers l'API Server.
	// Exemple of a sending of a getStatus request to the API Server

	/**** INDIQUER LE PORT UTILISE PAR LE SERVEUR D'API (7181 par d�faut) ***/
	/**** INDICATE THE API SERVER PORT USED (7181 by default) ***/
   $port = 7181;

   $adress = "*** INDICATE HERE THE API SERVER IP USED ***";

	 // initialisation du message de la requ�te
   // initialisation of the request message

   $buffer = "<command component=\"status\" name=\"getStatus\"></command>\n";

	 // connexion � l'API Server
   // API Server connexion
   $sock = fsockopen($adress, $port, $errno, $errstr, 10);

   if(!$sock) 
	 {
      echo "Connexion failed\n<br>";
      echo "erreur.$errstr ($errno)<br>\n";
   }
	 else
	 {
    // envoi du message
    // sending the message
    fputs($sock,$buffer);

    // r�ception de la r�ponse
    // receipt of the response
    $result = fgets($sock,8192);

		echo "response of the API Server:".$result;

    // fermeture de la socket
    // socket close
    fclose($sock);
    }
?>
