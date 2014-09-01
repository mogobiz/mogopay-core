
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the reponse from the
 * 							doWebPayment
 * 							method
 * 						
 * 
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://obj.ws.payline.experian.com}result"/>
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="redirectURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="stepCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="reqCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="method" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "result",
    "token",
    "redirectURL",
    "stepCode",
    "reqCode",
    "method"
})
@XmlRootElement(name = "doWebPaymentResponse")
public class DoWebPaymentResponse {

    @XmlElement(required = true)
    protected Result result;
    @XmlElement(required = true)
    protected String token;
    @XmlElement(required = true)
    protected String redirectURL;
    @XmlElement(required = true, nillable = true)
    protected String stepCode;
    @XmlElement(required = true, nillable = true)
    protected String reqCode;
    @XmlElement(required = true, nillable = true)
    protected String method;

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Result }
     *     
     */
    public Result getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Result }
     *     
     */
    public void setResult(Result value) {
        this.result = value;
    }

    /**
     * Gets the value of the token property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the value of the token property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToken(String value) {
        this.token = value;
    }

    /**
     * Gets the value of the redirectURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRedirectURL() {
        return redirectURL;
    }

    /**
     * Sets the value of the redirectURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRedirectURL(String value) {
        this.redirectURL = value;
    }

    /**
     * Gets the value of the stepCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStepCode() {
        return stepCode;
    }

    /**
     * Sets the value of the stepCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStepCode(String value) {
        this.stepCode = value;
    }

    /**
     * Gets the value of the reqCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReqCode() {
        return reqCode;
    }

    /**
     * Sets the value of the reqCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReqCode(String value) {
        this.reqCode = value;
    }

    /**
     * Gets the value of the method property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMethod(String value) {
        this.method = value;
    }

}
