package com.lyra.vads.ws3ds.stub;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.7.2
 * 2013-05-22T22:03:56.332+02:00
 * Generated source version: 2.7.2
 * 
 */
@WebService(targetNamespace = "http://threedsecure.ws.vads.lyra.com/", name = "ThreeDSecure")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ThreeDSecure {

    @WebResult(name = "return", targetNamespace = "http://threedsecure.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public VeResPAReqInfo sendVEReqAndbuildPAReqTx(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "contractNumber", name = "contractNumber")
            String contractNumber,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "cardNumber", name = "cardNumber")
            String cardNumber,
            @WebParam(partName = "browserUserAgent", name = "browserUserAgent")
            String browserUserAgent,
            @WebParam(partName = "browserAccept", name = "browserAccept")
            String browserAccept,
            @WebParam(partName = "purchaseAmount", name = "purchaseAmount")
            String purchaseAmount,
            @WebParam(partName = "purchaseCurrency", name = "purchaseCurrency")
            String purchaseCurrency,
            @WebParam(partName = "cardExpiry", name = "cardExpiry")
            String cardExpiry,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );

    @WebResult(name = "return", targetNamespace = "http://threedsecure.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public VeResPAReqInfo sendVEReqAndbuildPAReqByIdentifierTx(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "contractNumber", name = "contractNumber")
            String contractNumber,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "identifier", name = "identifier")
            String identifier,
            @WebParam(partName = "browserUserAgent", name = "browserUserAgent")
            String browserUserAgent,
            @WebParam(partName = "browserAccept", name = "browserAccept")
            String browserAccept,
            @WebParam(partName = "purchaseAmount", name = "purchaseAmount")
            String purchaseAmount,
            @WebParam(partName = "purchaseCurrency", name = "purchaseCurrency")
            String purchaseCurrency,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );

    @WebResult(name = "return", targetNamespace = "http://threedsecure.ws.vads.lyra.com/", partName = "return")
    @WebMethod
    public PaResInfo analyzePAResTx(
            @WebParam(partName = "shopId", name = "shopId")
            String shopId,
            @WebParam(partName = "contractNumber", name = "contractNumber")
            String contractNumber,
            @WebParam(partName = "ctxMode", name = "ctxMode")
            String ctxMode,
            @WebParam(partName = "requestId", name = "requestId")
            String requestId,
            @WebParam(partName = "pares", name = "pares")
            String pares,
            @WebParam(partName = "wsSignature", name = "wsSignature")
            String wsSignature
    );
}
