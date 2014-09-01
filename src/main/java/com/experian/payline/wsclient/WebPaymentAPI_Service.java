package com.experian.payline.wsclient;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.7.2
 * 2013-05-30T11:14:07.247+02:00
 * Generated source version: 2.7.2
 * 
 */
@WebServiceClient(name = "WebPaymentAPI", 
                  wsdlLocation = "WebPaymentAPI-homologation.wsdl",
                  targetNamespace = "http://impl.ws.payline.experian.com") 
public class WebPaymentAPI_Service extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://impl.ws.payline.experian.com", "WebPaymentAPI");
    public final static QName WebPaymentAPI = new QName("http://impl.ws.payline.experian.com", "WebPaymentAPI");
    static {
        URL url = WebPaymentAPI_Service.class.getResource("WebPaymentAPI-homologation.wsdl");
        if (url == null) {
            java.util.logging.Logger.getLogger(WebPaymentAPI_Service.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "WebPaymentAPI-homologation.wsdl");
        }       
        WSDL_LOCATION = url;
    }

    public WebPaymentAPI_Service(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public WebPaymentAPI_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public WebPaymentAPI_Service() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public WebPaymentAPI_Service(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public WebPaymentAPI_Service(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public WebPaymentAPI_Service(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns WebPaymentAPI
     */
    @WebEndpoint(name = "WebPaymentAPI")
    public WebPaymentAPI getWebPaymentAPI() {
        return super.getPort(WebPaymentAPI, WebPaymentAPI.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns WebPaymentAPI
     */
    @WebEndpoint(name = "WebPaymentAPI")
    public WebPaymentAPI getWebPaymentAPI(WebServiceFeature... features) {
        return super.getPort(WebPaymentAPI, WebPaymentAPI.class, features);
    }

}
