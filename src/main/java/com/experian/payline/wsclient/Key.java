
package com.experian.payline.wsclient;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 						This element contains information about the
 * 						encryptionKey
 * 					
 * 
 * <p>Java class for key complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="key">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="keyId" type="{http://www.w3.org/2001/XMLSchema}integer"/>
 *         &lt;element name="modulus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="publicExponent" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="expirationDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "key", namespace = "http://obj.ws.payline.experian.com", propOrder = {
    "keyId",
    "modulus",
    "publicExponent",
    "expirationDate"
})
public class Key {

    @XmlElement(required = true)
    protected BigInteger keyId;
    @XmlElement(required = true)
    protected String modulus;
    @XmlElement(required = true)
    protected String publicExponent;
    @XmlElement(required = true)
    protected String expirationDate;

    /**
     * Gets the value of the keyId property.
     * 
     * @return
     *     possible object is
     *     {@link java.math.BigInteger }
     *     
     */
    public BigInteger getKeyId() {
        return keyId;
    }

    /**
     * Sets the value of the keyId property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.math.BigInteger }
     *     
     */
    public void setKeyId(BigInteger value) {
        this.keyId = value;
    }

    /**
     * Gets the value of the modulus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModulus() {
        return modulus;
    }

    /**
     * Sets the value of the modulus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModulus(String value) {
        this.modulus = value;
    }

    /**
     * Gets the value of the publicExponent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPublicExponent() {
        return publicExponent;
    }

    /**
     * Sets the value of the publicExponent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPublicExponent(String value) {
        this.publicExponent = value;
    }

    /**
     * Gets the value of the expirationDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the value of the expirationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpirationDate(String value) {
        this.expirationDate = value;
    }

}
