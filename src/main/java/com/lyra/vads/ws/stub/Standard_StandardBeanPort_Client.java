
package com.lyra.vads.ws.stub;

/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.7.2
 * 2013-05-22T21:59:37.381+02:00
 * Generated source version: 2.7.2
 * 
 */
public final class Standard_StandardBeanPort_Client {

    private static final QName SERVICE_NAME = new QName("http://v3.ws.vads.lyra.com/", "StandardWS");

    private Standard_StandardBeanPort_Client() {
    }

    public static void main(String args[]) throws Exception {
        URL wsdlURL = StandardWS.WSDL_LOCATION;
        if (args.length > 0 && args[0] != null && !"".equals(args[0])) { 
            File wsdlFile = new File(args[0]);
            try {
                if (wsdlFile.exists()) {
                    wsdlURL = wsdlFile.toURI().toURL();
                } else {
                    wsdlURL = new URL(args[0]);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
      
        StandardWS ss = new StandardWS(wsdlURL, SERVICE_NAME);
        Standard port = ss.getStandardBeanPort();  
        
        {
        System.out.println("Invoking validate...");
        String _validate_shopId = "";
        javax.xml.datatype.XMLGregorianCalendar _validate_transmissionDate = null;
        String _validate_transactionId = "";
        int _validate_sequenceNb = 0;
        String _validate_ctxMode = "";
        String _validate_comment = "";
        String _validate_wsSignature = "";
        StandardResponse _validate__return = port.validate(_validate_shopId, _validate_transmissionDate, _validate_transactionId, _validate_sequenceNb, _validate_ctxMode, _validate_comment, _validate_wsSignature);
        System.out.println("validate.result=" + _validate__return);


        }
        {
        System.out.println("Invoking modifyAndValidate...");
        String _modifyAndValidate_shopId = "";
        javax.xml.datatype.XMLGregorianCalendar _modifyAndValidate_transmissionDate = null;
        String _modifyAndValidate_transactionId = "";
        int _modifyAndValidate_sequenceNb = 0;
        String _modifyAndValidate_ctxMode = "";
        long _modifyAndValidate_amount = 0;
        int _modifyAndValidate_devise = 0;
        javax.xml.datatype.XMLGregorianCalendar _modifyAndValidate_remiseDate = null;
        String _modifyAndValidate_comment = "";
        String _modifyAndValidate_wsSignature = "";
        StandardResponse _modifyAndValidate__return = port.modifyAndValidate(_modifyAndValidate_shopId, _modifyAndValidate_transmissionDate, _modifyAndValidate_transactionId, _modifyAndValidate_sequenceNb, _modifyAndValidate_ctxMode, _modifyAndValidate_amount, _modifyAndValidate_devise, _modifyAndValidate_remiseDate, _modifyAndValidate_comment, _modifyAndValidate_wsSignature);
        System.out.println("modifyAndValidate.result=" + _modifyAndValidate__return);


        }
        {
        System.out.println("Invoking force...");
        String _force_shopId = "";
        javax.xml.datatype.XMLGregorianCalendar _force_transmissionDate = null;
        String _force_transactionId = "";
        int _force_sequenceNb = 0;
        String _force_ctxMode = "";
        String _force_autorisationNb = "";
        javax.xml.datatype.XMLGregorianCalendar _force_autorisationDate = null;
        String _force_comment = "";
        String _force_wsSignature = "";
        StandardResponse _force__return = port.force(_force_shopId, _force_transmissionDate, _force_transactionId, _force_sequenceNb, _force_ctxMode, _force_autorisationNb, _force_autorisationDate, _force_comment, _force_wsSignature);
        System.out.println("force.result=" + _force__return);


        }
        {
        System.out.println("Invoking cancel...");
        String _cancel_shopId = "";
        javax.xml.datatype.XMLGregorianCalendar _cancel_transmissionDate = null;
        String _cancel_transactionId = "";
        int _cancel_sequenceNb = 0;
        String _cancel_ctxMode = "";
        String _cancel_comment = "";
        String _cancel_wsSignature = "";
        StandardResponse _cancel__return = port.cancel(_cancel_shopId, _cancel_transmissionDate, _cancel_transactionId, _cancel_sequenceNb, _cancel_ctxMode, _cancel_comment, _cancel_wsSignature);
        System.out.println("cancel.result=" + _cancel__return);


        }
        {
        System.out.println("Invoking getInfo...");
        String _getInfo_shopId = "";
        javax.xml.datatype.XMLGregorianCalendar _getInfo_transmissionDate = null;
        String _getInfo_transactionId = "";
        int _getInfo_sequenceNb = 0;
        String _getInfo_ctxMode = "";
        String _getInfo_wsSignature = "";
        TransactionInfo _getInfo__return = port.getInfo(_getInfo_shopId, _getInfo_transmissionDate, _getInfo_transactionId, _getInfo_sequenceNb, _getInfo_ctxMode, _getInfo_wsSignature);
        System.out.println("getInfo.result=" + _getInfo__return);


        }
        {
        System.out.println("Invoking modify...");
        String _modify_shopId = "";
        javax.xml.datatype.XMLGregorianCalendar _modify_transmissionDate = null;
        String _modify_transactionId = "";
        int _modify_sequenceNb = 0;
        String _modify_ctxMode = "";
        long _modify_amount = 0;
        int _modify_devise = 0;
        javax.xml.datatype.XMLGregorianCalendar _modify_remiseDate = null;
        String _modify_comment = "";
        String _modify_wsSignature = "";
        StandardResponse _modify__return = port.modify(_modify_shopId, _modify_transmissionDate, _modify_transactionId, _modify_sequenceNb, _modify_ctxMode, _modify_amount, _modify_devise, _modify_remiseDate, _modify_comment, _modify_wsSignature);
        System.out.println("modify.result=" + _modify__return);


        }
        {
        System.out.println("Invoking duplicate...");
        String _duplicate_shopId = "";
        javax.xml.datatype.XMLGregorianCalendar _duplicate_transmissionDate = null;
        String _duplicate_transactionId = "";
        int _duplicate_sequenceNb = 0;
        String _duplicate_ctxMode = "";
        String _duplicate_orderId = "";
        String _duplicate_orderInfo = "";
        String _duplicate_orderInfo2 = "";
        String _duplicate_orderInfo3 = "";
        long _duplicate_amount = 0;
        int _duplicate_devise = 0;
        String _duplicate_newTransactionId = "";
        javax.xml.datatype.XMLGregorianCalendar _duplicate_presentationDate = null;
        int _duplicate_validationMode = 0;
        String _duplicate_comment = "";
        String _duplicate_wsSignature = "";
        TransactionInfo _duplicate__return = port.duplicate(_duplicate_shopId, _duplicate_transmissionDate, _duplicate_transactionId, _duplicate_sequenceNb, _duplicate_ctxMode, _duplicate_orderId, _duplicate_orderInfo, _duplicate_orderInfo2, _duplicate_orderInfo3, _duplicate_amount, _duplicate_devise, _duplicate_newTransactionId, _duplicate_presentationDate, _duplicate_validationMode, _duplicate_comment, _duplicate_wsSignature);
        System.out.println("duplicate.result=" + _duplicate__return);


        }
        {
        System.out.println("Invoking refund...");
        String _refund_shopId = "";
        javax.xml.datatype.XMLGregorianCalendar _refund_transmissionDate = null;
        String _refund_transactionId = "";
        int _refund_sequenceNb = 0;
        String _refund_ctxMode = "";
        String _refund_newTransactionId = "";
        long _refund_amount = 0;
        int _refund_devise = 0;
        javax.xml.datatype.XMLGregorianCalendar _refund_presentationDate = null;
        int _refund_validationMode = 0;
        String _refund_comment = "";
        String _refund_wsSignature = "";
        TransactionInfo _refund__return = port.refund(_refund_shopId, _refund_transmissionDate, _refund_transactionId, _refund_sequenceNb, _refund_ctxMode, _refund_newTransactionId, _refund_amount, _refund_devise, _refund_presentationDate, _refund_validationMode, _refund_comment, _refund_wsSignature);
        System.out.println("refund.result=" + _refund__return);


        }
        {
        System.out.println("Invoking create...");
        CreatePaiementInfo _create_createInfo = null;
        String _create_wsSignature = "";
        TransactionInfo _create__return = port.create(_create_createInfo, _create_wsSignature);
        System.out.println("create.result=" + _create__return);


        }

        System.exit(0);
    }

}
