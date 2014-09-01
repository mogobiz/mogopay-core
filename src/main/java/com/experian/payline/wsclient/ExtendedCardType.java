
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for extendedCardType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="extendedCardType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="country" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="isCvd" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="bank" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="type " type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="network " type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="product " type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "extendedCardType", namespace = "http://obj.ws.payline.experian.com", propOrder = {
    "country",
    "isCvd",
    "bank",
    "type0020",
    "network0020",
    "product0020"
})
public class ExtendedCardType {

    @XmlElement(required = true, nillable = true)
    protected String country;
    @XmlElement(required = true, nillable = true)
    protected String isCvd;
    @XmlElement(required = true, nillable = true)
    protected String bank;
    @XmlElement(name = "type ", required = true, nillable = true)
    protected String type0020;
    @XmlElement(name = "network ", required = true, nillable = true)
    protected String network0020;
    @XmlElement(name = "product ", required = true, nillable = true)
    protected String product0020;

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the isCvd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsCvd() {
        return isCvd;
    }

    /**
     * Sets the value of the isCvd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsCvd(String value) {
        this.isCvd = value;
    }

    /**
     * Gets the value of the bank property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBank() {
        return bank;
    }

    /**
     * Sets the value of the bank property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBank(String value) {
        this.bank = value;
    }

    /**
     * Gets the value of the type0020 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType_0020() {
        return type0020;
    }

    /**
     * Sets the value of the type0020 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType_0020(String value) {
        this.type0020 = value;
    }

    /**
     * Gets the value of the network0020 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNetwork_0020() {
        return network0020;
    }

    /**
     * Sets the value of the network0020 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNetwork_0020(String value) {
        this.network0020 = value;
    }

    /**
     * Gets the value of the product0020 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProduct_0020() {
        return product0020;
    }

    /**
     * Sets the value of the product0020 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProduct_0020(String value) {
        this.product0020 = value;
    }

}
