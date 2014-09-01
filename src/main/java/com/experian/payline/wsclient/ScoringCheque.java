
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 						This element contains the scoring cheque parameters
 * 					
 * 
 * <p>Java class for scoringCheque complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="scoringCheque">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="chequeNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="additionalDataResponse" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="terminalId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="additionalPrivateData" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scoringCheque", namespace = "http://obj.ws.payline.experian.com", propOrder = {
    "chequeNumber",
    "additionalDataResponse",
    "terminalId",
    "additionalPrivateData"
})
public class ScoringCheque {

    @XmlElement(required = true)
    protected String chequeNumber;
    @XmlElement(required = true)
    protected String additionalDataResponse;
    @XmlElement(required = true)
    protected String terminalId;
    @XmlElement(required = true)
    protected String additionalPrivateData;

    /**
     * Gets the value of the chequeNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChequeNumber() {
        return chequeNumber;
    }

    /**
     * Sets the value of the chequeNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChequeNumber(String value) {
        this.chequeNumber = value;
    }

    /**
     * Gets the value of the additionalDataResponse property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdditionalDataResponse() {
        return additionalDataResponse;
    }

    /**
     * Sets the value of the additionalDataResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdditionalDataResponse(String value) {
        this.additionalDataResponse = value;
    }

    /**
     * Gets the value of the terminalId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTerminalId() {
        return terminalId;
    }

    /**
     * Sets the value of the terminalId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerminalId(String value) {
        this.terminalId = value;
    }

    /**
     * Gets the value of the additionalPrivateData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAdditionalPrivateData() {
        return additionalPrivateData;
    }

    /**
     * Sets the value of the additionalPrivateData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAdditionalPrivateData(String value) {
        this.additionalPrivateData = value;
    }

}
