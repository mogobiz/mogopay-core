package com.experian.payline.wsclient;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.7.2
 * 2013-05-30T11:14:07.229+02:00
 * Generated source version: 2.7.2
 * 
 */
@WebService(targetNamespace = "http://impl.ws.payline.experian.com", name = "WebPaymentAPI")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface WebPaymentAPI {

    @WebResult(name = "getWebPaymentDetailsResponse", targetNamespace = "http://impl.ws.payline.experian.com", partName = "parameters")
    @WebMethod(action = "getWebPaymentDetails")
    public GetWebPaymentDetailsResponse getWebPaymentDetails(
            @WebParam(partName = "parameters", name = "getWebPaymentDetailsRequest", targetNamespace = "http://impl.ws.payline.experian.com")
            GetWebPaymentDetailsRequest parameters
    );

    @WebResult(name = "createWebWalletResponse", targetNamespace = "http://impl.ws.payline.experian.com", partName = "parameters")
    @WebMethod(action = "createWebWallet")
    public CreateWebWalletResponse createWebWallet(
            @WebParam(partName = "parameters", name = "createWebWalletRequest", targetNamespace = "http://impl.ws.payline.experian.com")
            CreateWebWalletRequest parameters
    );

    @WebResult(name = "doWebPaymentResponse", targetNamespace = "http://impl.ws.payline.experian.com", partName = "parameters")
    @WebMethod(action = "doWebPayment")
    public DoWebPaymentResponse doWebPayment(
            @WebParam(partName = "parameters", name = "doWebPaymentRequest", targetNamespace = "http://impl.ws.payline.experian.com")
            DoWebPaymentRequest parameters
    );

    @WebResult(name = "getWebWalletResponse", targetNamespace = "http://impl.ws.payline.experian.com", partName = "parameters")
    @WebMethod(action = "getWebWallet")
    public GetWebWalletResponse getWebWallet(
            @WebParam(partName = "parameters", name = "getWebWalletRequest", targetNamespace = "http://impl.ws.payline.experian.com")
            GetWebWalletRequest parameters
    );

    @WebResult(name = "updateWebWalletResponse", targetNamespace = "http://impl.ws.payline.experian.com", partName = "parameters")
    @WebMethod(action = "updateWebWallet")
    public UpdateWebWalletResponse updateWebWallet(
            @WebParam(partName = "parameters", name = "updateWebWalletRequest", targetNamespace = "http://impl.ws.payline.experian.com")
            UpdateWebWalletRequest parameters
    );
}
