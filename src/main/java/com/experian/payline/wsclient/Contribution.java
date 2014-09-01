
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 						This element contains all information about
 * 						contrinution
 * 					
 * 
 * <p>Java class for contribution complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contribution">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="enable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nbFreeTransaction" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="minAmountTransaction" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="maxAmountTransaction" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contribution", namespace = "http://obj.ws.payline.experian.com", propOrder = {
    "enable",
    "type",
    "value",
    "nbFreeTransaction",
    "minAmountTransaction",
    "maxAmountTransaction"
})
public class Contribution {

    protected boolean enable;
    @XmlElement(required = true, nillable = true)
    protected String type;
    @XmlElement(required = true, nillable = true)
    protected String value;
    @XmlElement(required = true, nillable = true)
    protected String nbFreeTransaction;
    @XmlElement(required = true, nillable = true)
    protected String minAmountTransaction;
    @XmlElement(required = true, nillable = true)
    protected String maxAmountTransaction;

    /**
     * Gets the value of the enable property.
     * 
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * Sets the value of the enable property.
     * 
     */
    public void setEnable(boolean value) {
        this.enable = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the nbFreeTransaction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNbFreeTransaction() {
        return nbFreeTransaction;
    }

    /**
     * Sets the value of the nbFreeTransaction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNbFreeTransaction(String value) {
        this.nbFreeTransaction = value;
    }

    /**
     * Gets the value of the minAmountTransaction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMinAmountTransaction() {
        return minAmountTransaction;
    }

    /**
     * Sets the value of the minAmountTransaction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMinAmountTransaction(String value) {
        this.minAmountTransaction = value;
    }

    /**
     * Gets the value of the maxAmountTransaction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMaxAmountTransaction() {
        return maxAmountTransaction;
    }

    /**
     * Sets the value of the maxAmountTransaction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMaxAmountTransaction(String value) {
        this.maxAmountTransaction = value;
    }

}
