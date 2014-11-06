/*
	Exemple d'envoi d'une requête de commande StopServer vers l'API Server.

	Exemple of a sending of a StopServer request to the API Server

*/

// Imports

import java.io.*;
import java.net.*;


public class GetStatus {

  public static void main(String args[])
  {
   /**** INDIQUER LE PORT UTILISE PAR LE SERVEUR D'API (7181 par défaut) ***/
   /**** INDICATE THE API SERVER PORT USED (7181 by default) ***/
   int port = 7181;
   
   Socket sock;
   BufferedReader in;
   PrintWriter serverStream;
   String result = "";


   try {
	    // création de la socket
        // creation of the socket
           sock = new Socket("*** INDICATE HERE THE API SERVER IP USED ***",port);

        //création des flux d'entrée et de sortie
        //creation of out and in flow
           in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
           serverStream = new PrintWriter(sock.getOutputStream(), true);

	    // l'envoi du message
        // the sending of the message
        try {
            serverStream.println("<command login=\"admin\" password=\"admin\" name=\"StopServer NOW\"></command>\n");
            }catch (Exception e)
            {throw new Exception("the sending of the message failed");}

        // fermeture de la socket
        // socket close
        try {
            sock.close();
            }catch (Exception e)
            {throw new Exception("socket close failed");}

   }catch (Exception e)
   {System.out.println("Error: " + e.getMessage());}
  }
}
