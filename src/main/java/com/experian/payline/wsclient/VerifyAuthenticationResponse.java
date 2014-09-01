
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the reponse from the
 * 							doAuthentication method
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
 *         &lt;element name="authentication3DSecure" type="{http://obj.ws.payline.experian.com}authentication3DSecure"/>
 *         &lt;element name="mpiResult" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "authentication3DSecure",
    "mpiResult"
})
@XmlRootElement(name = "verifyAuthenticationResponse")
public class VerifyAuthenticationResponse {

    @XmlElement(required = true)
    protected Result result;
    @XmlElement(required = true, nillable = true)
    protected Authentication3DSecure authentication3DSecure;
    @XmlElement(required = true, nillable = true)
    protected String mpiResult;

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
     * Gets the value of the authentication3DSecure property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Authentication3DSecure }
     *     
     */
    public Authentication3DSecure getAuthentication3DSecure() {
        return authentication3DSecure;
    }

    /**
     * Sets the value of the authentication3DSecure property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Authentication3DSecure }
     *     
     */
    public void setAuthentication3DSecure(Authentication3DSecure value) {
        this.authentication3DSecure = value;
    }

    /**
     * Gets the value of the mpiResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMpiResult() {
        return mpiResult;
    }

    /**
     * Sets the value of the mpiResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMpiResult(String value) {
        this.mpiResult = value;
    }

}
