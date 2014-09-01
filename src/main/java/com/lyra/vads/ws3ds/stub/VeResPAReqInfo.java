
package com.lyra.vads.ws3ds.stub;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for veResPAReqInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="veResPAReqInfo">
 *   &lt;complexContent>
 *     &lt;extension base="{http://threedsecure.ws.vads.lyra.com/}abstractThreeDSecureResponse">
 *       &lt;sequence>
 *         &lt;element name="acctId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="acsUrl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="brand" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="encodedPareq" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="enrolled" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "veResPAReqInfo", propOrder = {
    "acctId",
    "acsUrl",
    "brand",
    "encodedPareq",
    "enrolled",
    "requestId"
})
public class VeResPAReqInfo
    extends AbstractThreeDSecureResponse
{

    protected String acctId;
    protected String acsUrl;
    protected String brand;
    protected String encodedPareq;
    protected String enrolled;
    protected String requestId;

    /**
     * Gets the value of the acctId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcctId() {
        return acctId;
    }

    /**
     * Sets the value of the acctId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcctId(String value) {
        this.acctId = value;
    }

    /**
     * Gets the value of the acsUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAcsUrl() {
        return acsUrl;
    }

    /**
     * Sets the value of the acsUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcsUrl(String value) {
        this.acsUrl = value;
    }

    /**
     * Gets the value of the brand property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Sets the value of the brand property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBrand(String value) {
        this.brand = value;
    }

    /**
     * Gets the value of the encodedPareq property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncodedPareq() {
        return encodedPareq;
    }

    /**
     * Sets the value of the encodedPareq property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncodedPareq(String value) {
        this.encodedPareq = value;
    }

    /**
     * Gets the value of the enrolled property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnrolled() {
        return enrolled;
    }

    /**
     * Sets the value of the enrolled property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnrolled(String value) {
        this.enrolled = value;
    }

    /**
     * Gets the value of the requestId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the requestId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestId(String value) {
        this.requestId = value;
    }

}
