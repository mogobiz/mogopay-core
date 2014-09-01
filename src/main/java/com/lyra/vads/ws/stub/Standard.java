package com.lyra.vads.ws.stub;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.7.2
 * 2013-05-22T21:59:37.443+02:00
 * Generated source version: 2.7.2
 * 
 */
@WebService(targetNamespace = "http://v3.ws.vads.lyra.com/", name = "Standard")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface Standard {

    @WebResult(name = "return", targetNamespace = "http://v3.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public StandardResponse validate(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "transmissionDate", name = "transmissionDate")
            javax.xml.datatype.XMLGregorianCalendar transmissionDate,
            @WebParam(partName = "transactionId", name = "transactionId")
            String transactionId,
            @WebParam(partName = "sequenceNb", name = "sequenceNb")
            int sequenceNb,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "comment", name = "comment")
            String comment,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );

    @WebResult(name = "return", targetNamespace = "http://v3.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public StandardResponse modifyAndValidate(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "transmissionDate", name = "transmissionDate")
            javax.xml.datatype.XMLGregorianCalendar transmissionDate,
            @WebParam(partName = "transactionId", name = "transactionId")
            String transactionId,
            @WebParam(partName = "sequenceNb", name = "sequenceNb")
            int sequenceNb,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "amount", name = "amount")
            long amount,
            @WebParam(partName = "devise", name = "devise")
            int devise,
            @WebParam(partName = "remiseDate", name = "remiseDate")
            javax.xml.datatype.XMLGregorianCalendar remiseDate,
            @WebParam(partName = "comment", name = "comment")
            String comment,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );

    @WebResult(name = "return", targetNamespace = "http://v3.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public StandardResponse force(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "transmissionDate", name = "transmissionDate")
            javax.xml.datatype.XMLGregorianCalendar transmissionDate,
            @WebParam(partName = "transactionId", name = "transactionId")
            String transactionId,
            @WebParam(partName = "sequenceNb", name = "sequenceNb")
            int sequenceNb,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "autorisationNb", name = "autorisationNb")
            String autorisationNb,
            @WebParam(partName = "autorisationDate", name = "autorisationDate")
            javax.xml.datatype.XMLGregorianCalendar autorisationDate,
            @WebParam(partName = "comment", name = "comment")
            String comment,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );

    @WebResult(name = "return", targetNamespace = "http://v3.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public StandardResponse cancel(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "transmissionDate", name = "transmissionDate")
            javax.xml.datatype.XMLGregorianCalendar transmissionDate,
            @WebParam(partName = "transactionId", name = "transactionId")
            String transactionId,
            @WebParam(partName = "sequenceNb", name = "sequenceNb")
            int sequenceNb,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "comment", name = "comment")
            String comment,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );

    @WebResult(name = "return", targetNamespace = "http://v3.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public TransactionInfo getInfo(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "transmissionDate", name = "transmissionDate")
            javax.xml.datatype.XMLGregorianCalendar transmissionDate,
            @WebParam(partName = "transactionId", name = "transactionId")
            String transactionId,
            @WebParam(partName = "sequenceNb", name = "sequenceNb")
            int sequenceNb,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );

    @WebResult(name = "return", targetNamespace = "http://v3.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public StandardResponse modify(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "transmissionDate", name = "transmissionDate")
            javax.xml.datatype.XMLGregorianCalendar transmissionDate,
            @WebParam(partName = "transactionId", name = "transactionId")
            String transactionId,
            @WebParam(partName = "sequenceNb", name = "sequenceNb")
            int sequenceNb,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "amount", name = "amount")
            long amount,
            @WebParam(partName = "devise", name = "devise")
            int devise,
            @WebParam(partName = "remiseDate", name = "remiseDate")
            javax.xml.datatype.XMLGregorianCalendar remiseDate,
            @WebParam(partName = "comment", name = "comment")
            String comment,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );

    @WebResult(name = "return", targetNamespace = "http://v3.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public TransactionInfo duplicate(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "transmissionDate", name = "transmissionDate")
            javax.xml.datatype.XMLGregorianCalendar transmissionDate,
            @WebParam(partName = "transactionId", name = "transactionId")
            String transactionId,
            @WebParam(partName = "sequenceNb", name = "sequenceNb")
            int sequenceNb,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "orderId", name = "orderId")
            String orderId,
            @WebParam(partName = "orderInfo", name = "orderInfo")
            String orderInfo,
            @WebParam(partName = "orderInfo2", name = "orderInfo2")
            String orderInfo2,
            @WebParam(partName = "orderInfo3", name = "orderInfo3")
            String orderInfo3,
            @WebParam(partName = "amount", name = "amount")
            long amount,
            @WebParam(partName = "devise", name = "devise")
            int devise,
            @WebParam(partName = "newTransactionId", name = "newTransactionId")
            String newTransactionId,
            @WebParam(partName = "presentationDate", name = "presentationDate")
            javax.xml.datatype.XMLGregorianCalendar presentationDate,
            @WebParam(partName = "validationMode", name = "validationMode")
            int validationMode,
            @WebParam(partName = "comment", name = "comment")
            String comment,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );

    @WebResult(name = "return", targetNamespace = "http://v3.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public TransactionInfo refund(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "transmissionDate", name = "transmissionDate")
            javax.xml.datatype.XMLGregorianCalendar transmissionDate,
            @WebParam(partName = "transactionId", name = "transactionId")
            String transactionId,
            @WebParam(partName = "sequenceNb", name = "sequenceNb")
            int sequenceNb,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "newTransactionId", name = "newTransactionId")
            String newTransactionId,
            @WebParam(partName = "amount", name = "amount")
            long amount,
            @WebParam(partName = "devise", name = "devise")
            int devise,
            @WebParam(partName = "presentationDate", name = "presentationDate")
            javax.xml.datatype.XMLGregorianCalendar presentationDate,
            @WebParam(partName = "validationMode", name = "validationMode")
            int validationMode,
            @WebParam(partName = "comment", name = "comment")
            String comment,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );

    @WebResult(name = "return", targetNamespace = "http://v3.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public TransactionInfo create(
            @WebParam(partName = "createInfo", name = "createInfo")
            CreatePaiementInfo createInfo,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );
}
