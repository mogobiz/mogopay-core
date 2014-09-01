
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the request for the
 * 							getTransactionDetails method
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
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transactionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="orderRef" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="startDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="endDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transactionHistory" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="archiveSearch" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "version",
    "transactionId",
    "orderRef",
    "startDate",
    "endDate",
    "transactionHistory",
    "archiveSearch"
})
@XmlRootElement(name = "getTransactionDetailsRequest")
public class GetTransactionDetailsRequest {

    @XmlElement(required = true)
    protected String version;
    @XmlElement(required = true, nillable = true)
    protected String transactionId;
    @XmlElement(required = true, nillable = true)
    protected String orderRef;
    @XmlElement(required = true, nillable = true)
    protected String startDate;
    @XmlElement(required = true, nillable = true)
    protected String endDate;
    @XmlElement(required = true, nillable = true)
    protected String transactionHistory;
    @XmlElement(required = true, nillable = true)
    protected String archiveSearch;

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the transactionId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the value of the transactionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionId(String value) {
        this.transactionId = value;
    }

    /**
     * Gets the value of the orderRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderRef() {
        return orderRef;
    }

    /**
     * Sets the value of the orderRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderRef(String value) {
        this.orderRef = value;
    }

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartDate(String value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the endDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * Sets the value of the endDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndDate(String value) {
        this.endDate = value;
    }

    /**
     * Gets the value of the transactionHistory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionHistory() {
        return transactionHistory;
    }

    /**
     * Sets the value of the transactionHistory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionHistory(String value) {
        this.transactionHistory = value;
    }

    /**
     * Gets the value of the archiveSearch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArchiveSearch() {
        return archiveSearch;
    }

    /**
     * Sets the value of the archiveSearch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArchiveSearch(String value) {
        this.archiveSearch = value;
    }

}
