
/*----------------------------------------------------------------------
  Topic		: Exemple SERVLET de traitement de la r�ponse automatique
  Version 	: P615
 *---------------------------------------------------------------------*/

import javax.servlet.*;
import javax.servlet.http.*;

/*
 |======================================================================
 |
 |   	main programm 	: traitement de la r�ponse automatique
 |			  Dans cet exemple, les donn�es de la transaction
 |			  sont d�crypt�es et sauvegard�es dans un fichier log.
 |
 |======================================================================
 */

public class AutoResponseServlet extends HttpServlet {

	public void service (HttpServletRequest request,
				 HttpServletResponse response) throws ServletException, IOException{

    try{

       /* Initialisation du chemin du fichier pathfile (� modifier)
           ex :
            -> Windows : SIPSApiWeb api = new SIPSApiWeb("c:\\repertoire\\pathfile");
            -> Unix    : SIPSApiWeb api = new SIPSApiWeb("/home/repertoire/pathfile");
        */
      SIPSApiWeb api = new SIPSApiWeb("chemin complet du pathfile");

      // Initialisation de l'objet r�ponse
      SIPSDataObject resp = (SIPSDataObject) new SIPSResponseParm();

      // R�cup�ration de la variable crypt�e post�e
      String cypheredtxt = request.getParameter("DATA");

      // D�cryptage de la r�ponse
      resp = api.sipsPaymentResponseFunc(cypheredtxt);

      // Sauvegarde des donn�es dans un fichier
      // � modifier pour mettre � jour une base de donn�es, etc...
      // ...
	  /* Initialisation du chemin du fichier de log (� modifier)
	  	   ex :
	  	-> Windows : String log_file = new String("c:\\repertoire\\logfile");
	        -> Unix    : String log_file = new String("/home/repertoire/logfile");
	   */

	  String log_file = new String("chemin du fichier de log");

	  PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(log_file)));

      out.println("merchant_id = "           + resp.getValue("merchant_id")           );
      out.println("merchant_country = "      + resp.getValue("merchant_country")      );
      out.println("amount = "                + resp.getValue("amount")                );
      out.println("transaction_id = "        + resp.getValue("transaction_id")        );
      out.println("transmission_date = "     + resp.getValue("transmission_date")     );
      out.println("payment_means = "         + resp.getValue("payment_means")         );
      out.println("payment_time = "          + resp.getValue("payment_time")          );
      out.println("payment_date = "          + resp.getValue("payment_date")          );
      out.println("response_code = "         + resp.getValue("response_code")         );
      out.println("payment_certificate = "   + resp.getValue("payment_certificate")   );
      out.println("authorisation_id = "      + resp.getValue("authorisation_id")      );
      out.println("currency_code = "         + resp.getValue("currency_code")         );
      out.println("card_number = "           + resp.getValue("card_number")           );
      out.println("cvv_flag = "           	 + resp.getValue("cvv_flag")	     	  ); 
      out.println("cvv_response_code = "     + resp.getValue("cvv_response_code")     );
      out.println("bank_response_code = "    + resp.getValue("bank_response_code")    );
      out.println("complementary_code = "    + resp.getValue("complementary_code")    );
      out.println("complementary_info = "    + resp.getValue("complementary_info")    );
      out.println("return_context = "        + resp.getValue("return_context")        );
      out.println("caddie = "                + resp.getValue("caddie")                );
      out.println("receipt_complement = "    + resp.getValue("receipt_complement")    );
      out.println("merchant_language = "     + resp.getValue("merchant_language")     );
      out.println("language = "              + resp.getValue("language")              );
      out.println("customer_id = "           + resp.getValue("customer_id")           );
      out.println("order_id = "              + resp.getValue("order_id")              );
      out.println("customer_email = "        + resp.getValue("customer_email")        );
      out.println("customer_ip_address = "   + resp.getValue("customer_ip_address")   );
      out.println("capture_day = "           + resp.getValue("capture_day")           );
      out.println("capture_mode = "          + resp.getValue("capture_mode")          );
      out.println("data = "                  + resp.getValue("data")                  );
      out.println("order_validity = "        + resp.getValue("order_validity")        );
      out.println("transaction_condition = " + resp.getValue("transaction_condition") );
      out.println("statement_reference = "   + resp.getValue("statement_reference")   );
      out.println("card_validity = "         + resp.getValue("card_validity")         );
      out.println("score_color = "           + resp.getValue("score_color")           );
      out.println("score_info = "            + resp.getValue("score_info")            );
      out.println("score_value = "           + resp.getValue("score_value")           );
      out.println("score_threshold = "       + resp.getValue("score_threshold")       );

    	out.close();

    } catch(Exception e){
	/* Initialisation du chemin du fichier de log (� modifier)
		ex :
		-> Windows : "c:\\repertoire\\logfile"
		-> Unix    : "/home/repertoire/logfile"
	*/

	  		String log_file = new String("chemin du fichier de log");
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(log_file)));
			out.println ("Error = "+e);
			out.close();
    }
  }
}


