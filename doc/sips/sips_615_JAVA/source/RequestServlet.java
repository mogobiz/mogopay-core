
/*----------------------------------------------------------------------
  Topic		: Exemple SERVLET traitement de la requête de paiement
  Version 	: P615
 *---------------------------------------------------------------------*/

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.atosorigin.services.cad.apipayment.*;
import com.atosorigin.services.cad.apipayment.web.*;
import com.atosorigin.services.cad.common.*;

/*
 |======================================================================
 |
 |   	main programm 	: traitement de la requête de paiement
 |					 	: Dans cet exemple, on affiche un formulaire de
 |						connection au serveur de paiement à l'internaute.
 |
 |======================================================================
 */

public class RequestServlet extends HttpServlet {

	public void service (HttpServletRequest request,
				 HttpServletResponse response) throws ServletException, IOException{

    try{

			PrintWriter out;
			response.setContentType("text/html");
			out = response.getWriter();

			out.println ("<HTML><HEAD><TITLE>SIPS - Paiement Securise sur Internet</TITLE></HEAD>");
			out.println ("<BODY bgcolor=#ffffff>");
			out.println ("<Font color=#000000>");
			out.println ("<center><H1>Test de l'API JAVA SIPS</H1></center><br><br>");

      /* Initialisation du chemin du fichier pathfile (à modifier)
         ex :
          -> Windows : SIPSApiWeb api = new SIPSApiWeb("c:\\repertoire\\pathfile");
          -> Unix    : SIPSApiWeb api = new SIPSApiWeb("/home/repertoire/pathfile");
      */
      SIPSApiWeb api = new SIPSApiWeb("chemin complet du pathfile");


      /* Initialisation de l'objet d'appel */
      SIPSDataObject call = (SIPSDataObject) new SIPSCallParm();

      // Paramètres obligatoires
      // ex : merchant_id = 011223344551111
      call.setValue("merchant_id","011223344551111");

      // ex : merchant_country = fr
      call.setValue("merchant_country","fr");

      // Affectation du montant de la transaction dans la plus petite unité
      // monétaire du pays
      // ex : 123,00 Euros ==> 12300 (currency_code = 978)
      call.setValue("amount","12300");

      // Affectation du code monétaire ISO 4217 pour la transaction
      // ex : Euro ==> 978
      call.setValue("currency_code","978");

      // Affectation d'un numéro identifiant pour la transaction
      // Attention aux reserves sur l'affectation automatique
      // cf Guide du developpeur
      // call.setValue("transaction_id","123456");

      // Valorisation des autres données de la transaction
      // facultatives, à compléter au besoin
      // Les valeurs proposées ne sont que des exemples
      // Les champs et leur utilisation sont expliqués dans le Dictionnaire des données
	  //
      // call.setValue("normal_return_url","http://www.maboutique.fr/servlet/ResponseServlet");
      // call.setValue("cancel_return_url","http://www.maboutique.fr/servlet/ResponseServlet");
      // call.setValue("automatic_response_url","http://www.maboutique.fr/servlet/AutoResponseServlet");
      // call.setValue("language","fr");
      // call.setValue("payment_means","CB,2,VISA,2,MASTERCARD,2");
      // call.setValue("header_flag","no");
      // call.setValue("capture_day","");
      // call.setValue("capture_mode","");
      // call.setValue("bgcolor","");
      // call.setValue("block_align","");
      // call.setValue("block_order","");
      // call.setValue("textcolor","");
      // call.setValue("receipt_complement","");
      // call.setValue("caddie","mon caddie");
      // call.setValue("customer_id","");
      // call.setValue("customer_email","");
      // call.setValue("data","");
      // call.setValue("return_context","");
      // call.setValue("target","");
      // call.setValue("order_id","");


  	  // Les valeurs suivantes ne sont utilisables qu'en pré-production
  	  // Elles nécessitent l'installation de vos logos et templates sur
  	  // le serveur de paiement
  	  //
  	  // call.setValue("normal_return_logo","");
      // call.setValue("cancel_return_logo","");
      // call.setValue("submit_logo","");
      // call.setValue("logo_id","");
      // call.setValue("logo_id2","");
      // call.setValue("advert","");
      // call.setValue("background_id","");
      // call.setValue("templatefile","mon_template");


      // Insertion de la commande dans votre base de données
      // avec le status "en cours"
      // ...

      // Appel de l'api SIPS payment
      out.println(api.sipsPaymentCallFunc(call));

			out.println ("<BR><BR>");
			out.println ("</BODY>");
			out.println ("</HTML>");

			out.close();

    }

    catch(Exception e){

			PrintWriter out;
			response.setContentType("text/html");
			out = response.getWriter();

			out.println ("<CENTER>");
			out.println ("<BR>");
      out.println("Error = "+e);
			out.println ("</CENTER>");
			out.println ("</BODY>");
			out.println ("</HTML>");

			out.close();

    }
  }
}
