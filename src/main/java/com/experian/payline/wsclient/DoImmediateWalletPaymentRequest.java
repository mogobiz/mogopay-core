
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the request for the
 * 							doImmediateWalletPayment method
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
 *         &lt;element name="payment" type="{http://obj.ws.payline.experian.com}payment"/>
 *         &lt;element name="order" type="{http://obj.ws.payline.experian.com}order"/>
 *         &lt;element name="walletId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cardInd" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="privateDataList" type="{http://obj.ws.payline.experian.com}privateDataList"/>
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
    "order",
    "walletId",
    "cardInd",
    "privateDataList",
    "media"
})
@XmlRootElement(name = "doImmediateWalletPaymentRequest")
public class DoImmediateWalletPaymentRequest {

    @XmlElement(required = true)
    protected String version;
    @XmlElement(required = true)
    protected Payment payment;
    @XmlElement(required = true)
    protected Order order;
    @XmlElement(required = true)
    protected String walletId;
    @XmlElement(required = true, nillable = true)
    protected String cardInd;
    @XmlElement(required = true, nillable = true)
    protected PrivateDataList privateDataList;
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
     * Gets the value of the walletId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWalletId() {
        return walletId;
    }

    /**
     * Sets the value of the walletId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWalletId(String value) {
        this.walletId = value;
    }

    /**
     * Gets the value of the cardInd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCardInd() {
        return cardInd;
    }

    /**
     * Sets the value of the cardInd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCardInd(String value) {
        this.cardInd = value;
    }

    /**
     * Gets the value of the privateDataList property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.PrivateDataList }
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
     *     {@link com.experian.payline.wsclient.PrivateDataList }
     *     
     */
    public void setPrivateDataList(PrivateDataList value) {
        this.privateDataList = value;
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
