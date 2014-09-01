
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
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
 *         &lt;element name="payment" type="{http://obj.ws.payline.experian.com}payment"/>
 *         &lt;element name="card" type="{http://obj.ws.payline.experian.com}card"/>
 *         &lt;element name="order" type="{http://obj.ws.payline.experian.com}order"/>
 *         &lt;element name="buyer" type="{http://obj.ws.payline.experian.com}buyer"/>
 *         &lt;element name="owner" type="{http://obj.ws.payline.experian.com}owner"/>
 *         &lt;element name="privateDataList" type="{http://obj.ws.payline.experian.com}privateDataList"/>
 *         &lt;element name="authentication3DSecure" type="{http://obj.ws.payline.experian.com}authentication3DSecure"/>
 *         &lt;element name="authorization" type="{http://obj.ws.payline.experian.com}authorization"/>
 *         &lt;element name="media" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "payment",
    "card",
    "order",
    "buyer",
    "owner",
    "privateDataList",
    "authentication3DSecure",
    "authorization",
    "media"
})
@XmlRootElement(name = "doDebitRequest")
public class DoDebitRequest {

    @XmlElement(required = true)
    protected String version;
    @XmlElement(required = true)
    protected Payment payment;
    @XmlElement(required = true)
    protected Card card;
    @XmlElement(required = true)
    protected Order order;
    @XmlElement(required = true, nillable = true)
    protected Buyer buyer;
    @XmlElement(required = true, nillable = true)
    protected Owner owner;
    @XmlElement(required = true, nillable = true)
    protected PrivateDataList privateDataList;
    @XmlElement(required = true, nillable = true)
    protected Authentication3DSecure authentication3DSecure;
    @XmlElement(required = true)
    protected Authorization authorization;
    @XmlElement(required = true, nillable = true)
    protected String media;

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
     * Gets the value of the card property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Card }
     *     
     */
    public Card getCard() {
        return card;
    }

    /**
     * Sets the value of the card property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Card }
     *     
     */
    public void setCard(Card value) {
        this.card = value;
    }

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Order }
     *     
     */
    public Order getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Order }
     *     
     */
    public void setOrder(Order value) {
        this.order = value;
    }

    /**
     * Gets the value of the buyer property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Buyer }
     *     
     */
    public Buyer getBuyer() {
        return buyer;
    }

    /**
     * Sets the value of the buyer property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Buyer }
     *     
     */
    public void setBuyer(Buyer value) {
        this.buyer = value;
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Owner }
     *     
     */
    public Owner getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Owner }
     *     
     */
    public void setOwner(Owner value) {
        this.owner = value;
    }

    /**
     * Gets the value of the privateDataList property.
     * 
     * @return
     *     possible object is
     *     {@link PrivateDataList }
     *     
     */
    public PrivateDataList getPrivateDataList() {
        return privateDataList;
    }

    /**
     * Sets the value of the privateDataList property.
     * 
     * @param value
     *     allowed object is
     *     {@link PrivateDataList }
     *     
     */
    public void setPrivateDataList(PrivateDataList value) {
        this.privateDataList = value;
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
     * Gets the value of the media property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMedia() {
        return media;
    }

    /**
     * Sets the value of the media property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMedia(String value) {
        this.media = value;
    }

}
