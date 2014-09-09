
/*----------------------------------------------------------------------
  Topic		: Exemple SERVLET de traitement de la réponse
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
 |   	main programm 	: traitement de la réponse manuelle
 |			  Dans cet exemple, les données de la transaction
 |			  sont décryptées et affichées sur le navigateur
 |			  de l'internaute.
 |
 |======================================================================
 */

public class ResponseServlet extends HttpServlet {

	public void service (HttpServletRequest request,
				 HttpServletResponse response) throws ServletException, IOException{

    try{

	/* 	Redirection de la sortie DEBUG vers le navigateur. 
		Par défaut le mode DEBUG est redirigé vers la sortie standard du serveur d'application.
	*/
	//DebugOutputStream.setOut(response.getOutputStream());
	
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


      // Initialisation de l'objet réponse
      SIPSDataObject resp = (SIPSDataObject) new SIPSResponseParm();

      // Récupération de la variable cryptée postée
      String cypheredtxt = request.getParameter("DATA");

      // Décryptage de la réponse
      resp = api.sipsPaymentResponseFunc(cypheredtxt);

      // Affichage des données de la réponse du serveur

	  out.println("<center>\n");
	  out.println("<H3>R&eacute;ponse manuelle du serveur SIPS</H3>\n");
	  out.println("</center>\n");
	  out.println("<b><h4>\n");
	  out.println("<br><hr>\n");
		    out.println("merchant_id = "           + resp.getValue("merchant_id")           + "<br>");
		    out.println("merchant_country = "      + resp.getValue("merchant_country")      + "<br>");
		    out.println("amount = "                + resp.getValue("amount")                + "<br>");
		    out.println("transaction_id = "        + resp.getValue("transaction_id")        + "<br>");
		    out.println("transmission_date = "     + resp.getValue("transmission_date")     + "<br>");
		    out.println("payment_means = "         + resp.getValue("payment_means")         + "<br>");
		    out.println("payment_time = "          + resp.getValue("payment_time")          + "<br>");
		    out.println("payment_date = "          + resp.getValue("payment_date")          + "<br>");
		    out.println("response_code = "         + resp.getValue("response_code")         + "<br>");
		    out.println("payment_certificate = "   + resp.getValue("payment_certificate")   + "<br>");
		    out.println("authorisation_id = "      + resp.getValue("authorisation_id")      + "<br>");
		    out.println("currency_code = "         + resp.getValue("currency_code")         + "<br>");
		    out.println("card_number = "           + resp.getValue("card_number")           + "<br>");
		    out.println("cvv_flag = "              + resp.getValue("cvv_flag")	     	      + "<br>");
		    out.println("cvv_response_code = "     + resp.getValue("cvv_response_code")     + "<br>");
		    out.println("bank_response_code = "    + resp.getValue("bank_response_code")    + "<br>");
		    out.println("complementary_code = "    + resp.getValue("complementary_code")    + "<br>");
		    out.println("complementary_info = "    + resp.getValue("complementary_info")    + "<br>");
		    out.println("return_context = "        + resp.getValue("return_context")        + "<br>");
		    out.println("caddie = "                + resp.getValue("caddie")                + "<br>");
		    out.println("receipt_complement = "    + resp.getValue("receipt_complement")    + "<br>");
		    out.println("merchant_language = "     + resp.getValue("merchant_language")     + "<br>");
		    out.println("language = "              + resp.getValue("language")              + "<br>");
		    out.println("customer_id = "           + resp.getValue("customer_id")           + "<br>");
		    out.println("order_id = "              + resp.getValue("order_id")              + "<br>");
		    out.println("customer_email = "        + resp.getValue("customer_email")        + "<br>");
		    out.println("customer_ip_address = "   + resp.getValue("customer_ip_address")   + "<br>");
		    out.println("capture_day = "           + resp.getValue("capture_day")           + "<br>");
		    out.println("capture_mode = "          + resp.getValue("capture_mode")          + "<br>");
		    out.println("data = "                  + resp.getValue("data")                  + "<br>");
		    out.println("order_validity = "        + resp.getValue("order_validity")        + "<br>");
		    out.println("transaction_condition = " + resp.getValue("transaction_condition") + "<br>");
		    out.println("statement_reference = "   + resp.getValue("statement_reference")   + "<br>");
		    out.println("card_validity = "         + resp.getValue("card_validity")         + "<br>");
		    out.println("score_color = "           + resp.getValue("score_color")           + "<br>");
		    out.println("score_info = "            + resp.getValue("score_info")            + "<br>");
		    out.println("score_value = "           + resp.getValue("score_value")           + "<br>");
		    out.println("score_threshold = "       + resp.getValue("score_threshold")       + "<br>");
	  out.println("<br><br><hr></b></h4>");


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


