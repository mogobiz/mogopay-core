package com.lyra.vads.ws.stub;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.7.2
 * 2013-05-22T21:59:37.456+02:00
 * Generated source version: 2.7.2
 * 
 */
@WebServiceClient(name = "StandardWS", 
                  wsdlLocation = "SystemPay_WSAPI.wsdl",
                  targetNamespace = "http://v3.ws.vads.lyra.com/") 
public class StandardWS extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://v3.ws.vads.lyra.com/", "StandardWS");
    public final static QName StandardBeanPort = new QName("http://v3.ws.vads.lyra.com/", "StandardBeanPort");
    static {
        URL url = StandardWS.class.getResource("SystemPay_WSAPI.wsdl");
        if (url == null) {
            java.util.logging.Logger.getLogger(StandardWS.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "SystemPay_WSAPI.wsdl");
        }       
        WSDL_LOCATION = url;
    }

    public StandardWS(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public StandardWS(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public StandardWS() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public StandardWS(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public StandardWS(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public StandardWS(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns Standard
     */
    @WebEndpoint(name = "StandardBeanPort")
    public Standard getStandardBeanPort() {
        return super.getPort(StandardBeanPort, Standard.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns Standard
     */
    @WebEndpoint(name = "StandardBeanPort")
    public Standard getStandardBeanPort(WebServiceFeature... features) {
        return super.getPort(StandardBeanPort, Standard.class, features);
    }

}
