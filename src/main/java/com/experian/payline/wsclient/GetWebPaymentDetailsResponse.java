
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the reponse from the
 * 							doWebPayment
 * 							method
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
 *         &lt;element name="privateDataList" type="{http://obj.ws.payline.experian.com}privateDataList"/>
 *         &lt;element name="paymentRecordId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="billingRecordList" type="{http://obj.ws.payline.experian.com}billingRecordList"/>
 *         &lt;element name="authentication3DSecure" type="{http://obj.ws.payline.experian.com}authentication3DSecure"/>
 *         &lt;element name="card" type="{http://obj.ws.payline.experian.com}cardOut"/>
 *         &lt;element name="extendedCard" type="{http://obj.ws.payline.experian.com}extendedCardType"/>
 *         &lt;element name="order" type="{http://obj.ws.payline.experian.com}order"/>
 *         &lt;element name="paymentAdditionalList" type="{http://obj.ws.payline.experian.com}paymentAdditionalList"/>
 *         &lt;element name="media" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="numberOfAttempt" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="wallet" type="{http://obj.ws.payline.experian.com}wallet"/>
 *         &lt;element name="contractNumberWalletList" type="{http://obj.ws.payline.experian.com}contractNumberWalletList"/>
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
    "privateDataList",
    "paymentRecordId",
    "billingRecordList",
    "authentication3DSecure",
    "card",
    "extendedCard",
    "order",
    "paymentAdditionalList",
    "media",
    "numberOfAttempt",
    "wallet",
    "contractNumberWalletList"
})
@XmlRootElement(name = "getWebPaymentDetailsResponse")
public class GetWebPaymentDetailsResponse {

    @XmlElement(required = true)
    protected Result result;
    @XmlElement(required = true)
    protected Transaction transaction;
    @XmlElement(required = true)
    protected Payment payment;
    @XmlElement(required = true)
    protected Authorization authorization;
    @XmlElement(required = true, nillable = true)
    protected PrivateDataList privateDataList;
    @XmlElement(required = true)
    protected String paymentRecordId;
    @XmlElement(required = true, nillable = true)
    protected BillingRecordList billingRecordList;
    @XmlElement(required = true, nillable = true)
    protected Authentication3DSecure authentication3DSecure;
    @XmlElement(required = true)
    protected CardOut card;
    @XmlElement(required = true)
    protected ExtendedCardType extendedCard;
    @XmlElement(required = true)
    protected Order order;
    @XmlElement(required = true, nillable = true)
    protected PaymentAdditionalList paymentAdditionalList;
    @XmlElement(required = true, nillable = true)
    protected String media;
    @XmlElement(required = true, nillable = true)
    protected String numberOfAttempt;
    @XmlElement(required = true, nillable = true)
    protected Wallet wallet;
    @XmlElement(required = true, nillable = true)
    protected ContractNumberWalletList contractNumberWalletList;

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
     * Gets the value of the paymentRecordId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaymentRecordId() {
        return paymentRecordId;
    }

    /**
     * Sets the value of the paymentRecordId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaymentRecordId(String value) {
        this.paymentRecordId = value;
    }

    /**
     * Gets the value of the billingRecordList property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.BillingRecordList }
     *     
     */
    public BillingRecordList getBillingRecordList() {
        return billingRecordList;
    }

    /**
     * Sets the value of the billingRecordList property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.BillingRecordList }
     *     
     */
    public void setBillingRecordList(BillingRecordList value) {
        this.billingRecordList = value;
    }

    /**
     * Gets the value of the authentication3DSecure property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Authentication3DSecure }
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
     *     {@link com.experian.payline.wsclient.Authentication3DSecure }
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
     * Gets the value of the paymentAdditionalList property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.PaymentAdditionalList }
     *     
     */
    public PaymentAdditionalList getPaymentAdditionalList() {
        return paymentAdditionalList;
    }

    /**
     * Sets the value of the paymentAdditionalList property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.PaymentAdditionalList }
     *     
     */
    public void setPaymentAdditionalList(PaymentAdditionalList value) {
        this.paymentAdditionalList = value;
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

    /**
     * Gets the value of the numberOfAttempt property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumberOfAttempt() {
        return numberOfAttempt;
    }

    /**
     * Sets the value of the numberOfAttempt property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumberOfAttempt(String value) {
        this.numberOfAttempt = value;
    }

    /**
     * Gets the value of the wallet property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.Wallet }
     *     
     */
    public Wallet getWallet() {
        return wallet;
    }

    /**
     * Sets the value of the wallet property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.Wallet }
     *     
     */
    public void setWallet(Wallet value) {
        this.wallet = value;
    }

    /**
     * Gets the value of the contractNumberWalletList property.
     * 
     * @return
     *     possible object is
     *     {@link com.experian.payline.wsclient.ContractNumberWalletList }
     *     
     */
    public ContractNumberWalletList getContractNumberWalletList() {
        return contractNumberWalletList;
    }

    /**
     * Sets the value of the contractNumberWalletList property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.experian.payline.wsclient.ContractNumberWalletList }
     *     
     */
    public void setContractNumberWalletList(ContractNumberWalletList value) {
        this.contractNumberWalletList = value;
    }

}
