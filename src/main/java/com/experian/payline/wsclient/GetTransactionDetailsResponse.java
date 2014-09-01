
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the response for the
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
 *         &lt;element name="result" type="{http://obj.ws.payline.experian.com}result"/>
 *         &lt;element name="transaction" type="{http://obj.ws.payline.experian.com}transaction"/>
 *         &lt;element name="payment" type="{http://obj.ws.payline.experian.com}payment"/>
 *         &lt;element name="authorization" type="{http://obj.ws.payline.experian.com}authorization"/>
 *         &lt;element name="order" type="{http://obj.ws.payline.experian.com}order"/>
 *         &lt;element name="buyer" type="{http://obj.ws.payline.experian.com}buyer"/>
 *         &lt;element name="privateDataList" type="{http://obj.ws.payline.experian.com}privateDataList"/>
 *         &lt;element name="card" type="{http://obj.ws.payline.experian.com}cardOut"/>
 *         &lt;element name="extendedCard" type="{http://obj.ws.payline.experian.com}extendedCardType"/>
 *         &lt;element name="associatedTransactionsList" type="{http://obj.ws.payline.experian.com}associatedTransactionsList"/>
 *         &lt;element name="statusHistoryList" type="{http://obj.ws.payline.experian.com}statusHistoryList"/>
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
    "result",
    "transaction",
    "payment",
    "authorization",
    "order",
    "buyer",
    "privateDataList",
    "card",
    "extendedCard",
    "associatedTransactionsList",
    "statusHistoryList",
    "media"
})
@XmlRootElement(name = "getTransactionDetailsResponse")
public class GetTransactionDetailsResponse {

    @XmlElement(required = true)
    protected Result result;
    @XmlElement(required = true, nillable = true)
    protected Transaction transaction;
    @XmlElement(required = true, nillable = true)
    protected Payment payment;
    @XmlElement(required = true, nillable = true)
    protected Authorization authorization;
    @XmlElement(required = true, nillable = true)
    protected Order order;
    @XmlElement(required = true, nillable = true)
    protected Buyer buyer;
    @XmlElement(required = true, nillable = true)
    protected PrivateDataList privateDataList;
    @XmlElement(required = true)
    protected CardOut card;
    @XmlElement(required = true)
    protected ExtendedCardType extendedCard;
    @XmlElement(required = true, nillable = true)
    protected AssociatedTransactionsList associatedTransactionsList;
    @XmlElement(required = true, nillable = true)
    protected StatusHistoryList statusHistoryList;
    @XmlElement(required = true, nillable = true)
    protected String media;

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

    /**
     * Gets the value of the associatedTransactionsList property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.AssociatedTransactionsList }
     *     
     */
    public AssociatedTransactionsList getAssociatedTransactionsList() {
        return associatedTransactionsList;
    }

    /**
     * Sets the value of the associatedTransactionsList property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.AssociatedTransactionsList }
     *     
     */
    public void setAssociatedTransactionsList(AssociatedTransactionsList value) {
        this.associatedTransactionsList = value;
    }

    /**
     * Gets the value of the statusHistoryList property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.StatusHistoryList }
     *     
     */
    public StatusHistoryList getStatusHistoryList() {
        return statusHistoryList;
    }

    /**
     * Sets the value of the statusHistoryList property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.StatusHistoryList }
     *     
     */
    public void setStatusHistoryList(StatusHistoryList value) {
        this.statusHistoryList = value;
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
