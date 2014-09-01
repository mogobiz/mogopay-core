
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 						This element contains information about the
 * 						paymentAdditional
 * 					
 * 
 * <p>Java class for paymentAdditional complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="paymentAdditional">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="transaction" type="{http://obj.ws.payline.experian.com}transaction"/>
 *         &lt;element name="payment" type="{http://obj.ws.payline.experian.com}payment"/>
 *         &lt;element name="authorization" type="{http://obj.ws.payline.experian.com}authorization"/>
 *         &lt;element name="authentication3DSecure" type="{http://obj.ws.payline.experian.com}authentication3DSecure"/>
 *         &lt;element name="card" type="{http://obj.ws.payline.experian.com}cardOut"/>
 *         &lt;element name="extendedCard" type="{http://obj.ws.payline.experian.com}extendedCardType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "paymentAdditional", namespace = "http://obj.ws.payline.experian.com", propOrder = {
    "transaction",
    "payment",
    "authorization",
    "authentication3DSecure",
    "card",
    "extendedCard"
})
public class PaymentAdditional {

    @XmlElement(required = true)
    protected Transaction transaction;
    @XmlElement(required = true)
    protected Payment payment;
    @XmlElement(required = true)
    protected Authorization authorization;
    @XmlElement(required = true, nillable = true)
    protected Authentication3DSecure authentication3DSecure;
    @XmlElement(required = true, nillable = true)
    protected CardOut card;
    @XmlElement(required = true, nillable = true)
    protected ExtendedCardType extendedCard;

    /**
     * Gets the value of the transaction property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Transaction }
     *     
     */
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * Sets the value of the transaction property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Transaction }
     *     
     */
    public void setTransaction(Transaction value) {
        this.transaction = value;
    }

    /**
     * Gets the value of the payment property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Payment }
     *     
     */
    public Payment getPayment() {
        return payment;
    }

    /**
     * Sets the value of the payment property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Payment }
     *     
     */
    public void setPayment(Payment value) {
        this.payment = value;
    }

    /**
     * Gets the value of the authorization property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Authorization }
     *     
     */
    public Authorization getAuthorization() {
        return authorization;
    }

    /**
     * Sets the value of the authorization property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Authorization }
     *     
     */
    public void setAuthorization(Authorization value) {
        this.authorization = value;
    }

    /**
     * Gets the value of the authentication3DSecure property.
     * 
     * @return
     *     possible object is
     *     {@link Authentication3DSecure }
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
     *     {@link Authentication3DSecure }
     *     
     */
    public void setAuthentication3DSecure(Authentication3DSecure value) {
        this.authentication3DSecure = value;
    }

    /**
     * Gets the value of the card property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.CardOut }
     *     
     */
    public CardOut getCard() {
        return card;
    }

    /**
     * Sets the value of the card property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.CardOut }
     *     
     */
    public void setCard(CardOut value) {
        this.card = value;
    }

    /**
     * Gets the value of the extendedCard property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.ExtendedCardType }
     *     
     */
    public ExtendedCardType getExtendedCard() {
        return extendedCard;
    }

    /**
     * Sets the value of the extendedCard property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.ExtendedCardType }
     *     
     */
    public void setExtendedCard(ExtendedCardType value) {
        this.extendedCard = value;
    }

}
