
package com.lyra.vads.ws.stub;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for transactionInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="transactionInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="timestamp" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="signature" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="errorCode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="extendedErrorCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transactionStatus" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="shopId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="paymentMethod" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orderId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orderInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orderInfo2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orderInfo3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transmissionDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="transactionId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sequenceNb" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="initialAmount" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="devise" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="cvAmount" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="cvDevise" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="presentationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="multiplePaiement" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ctxMode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cardNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cardNetwork" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cardType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cardCountry" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="cardExpirationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="customerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerTitle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerPhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerMail" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerZipCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerCity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerCountry" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerLanguage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerIP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="transactionCondition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vadsEnrolled" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vadsStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vadsECI" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vadsXID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vadsCAVVAlgorithm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vadsCAVV" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vadsSignatureValid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="directoryServer" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="authMode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="markAmount" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="markDevise" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="markDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="markNb" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="markResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="markCVV2_CVC2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="authAmount" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="authDevise" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="authDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="authNb" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="authResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="authCVV2_CVC2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="warrantlyResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="localControl" type="{http://v3.ws.vads.lyra.com/}localControl" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="captureDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="captureNumber" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="rapprochementStatut" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="refoundAmount" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="refundDevise" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="litige" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "transactionInfo", propOrder = {
    "timestamp",
    "signature",
    "errorCode",
    "extendedErrorCode",
    "transactionStatus",
    "shopId",
    "paymentMethod",
    "contractNumber",
    "orderId",
    "orderInfo",
    "orderInfo2",
    "orderInfo3",
    "transmissionDate",
    "transactionId",
    "sequenceNb",
    "amount",
    "initialAmount",
    "devise",
    "cvAmount",
    "cvDevise",
    "presentationDate",
    "type",
    "multiplePaiement",
    "ctxMode",
    "cardNumber",
    "cardNetwork",
    "cardType",
    "cardCountry",
    "cardExpirationDate",
    "customerId",
    "customerTitle",
    "customerName",
    "customerPhone",
    "customerMail",
    "customerAddress",
    "customerZipCode",
    "customerCity",
    "customerCountry",
    "customerLanguage",
    "customerIP",
    "transactionCondition",
    "vadsEnrolled",
    "vadsStatus",
    "vadsECI",
    "vadsXID",
    "vadsCAVVAlgorithm",
    "vadsCAVV",
    "vadsSignatureValid",
    "directoryServer",
    "authMode",
    "markAmount",
    "markDevise",
    "markDate",
    "markNb",
    "markResult",
    "markCVV2CVC2",
    "authAmount",
    "authDevise",
    "authDate",
    "authNb",
    "authResult",
    "authCVV2CVC2",
    "warrantlyResult",
    "localControl",
    "captureDate",
    "captureNumber",
    "rapprochementStatut",
    "refoundAmount",
    "refundDevise",
    "litige"
})
public class TransactionInfo {

    protected long timestamp;
    protected String signature;
    protected int errorCode;
    protected String extendedErrorCode;
    protected int transactionStatus;
    protected String shopId;
    protected String paymentMethod;
    protected String contractNumber;
    protected String orderId;
    protected String orderInfo;
    protected String orderInfo2;
    protected String orderInfo3;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar transmissionDate;
    protected String transactionId;
    protected int sequenceNb;
    protected long amount;
    protected long initialAmount;
    protected int devise;
    protected long cvAmount;
    protected int cvDevise;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar presentationDate;
    protected int type;
    protected int multiplePaiement;
    protected String ctxMode;
    protected String cardNumber;
    protected String cardNetwork;
    protected String cardType;
    protected Long cardCountry;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar cardExpirationDate;
    protected String customerId;
    protected String customerTitle;
    protected String customerName;
    protected String customerPhone;
    protected String customerMail;
    protected String customerAddress;
    protected String customerZipCode;
    protected String customerCity;
    protected String customerCountry;
    protected String customerLanguage;
    protected String customerIP;
    protected String transactionCondition;
    protected String vadsEnrolled;
    protected String vadsStatus;
    protected String vadsECI;
    protected String vadsXID;
    protected String vadsCAVVAlgorithm;
    protected String vadsCAVV;
    protected String vadsSignatureValid;
    protected String directoryServer;
    protected String authMode;
    protected long markAmount;
    protected int markDevise;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar markDate;
    protected String markNb;
    protected int markResult;
    @XmlElement(name = "markCVV2_CVC2")
    protected String markCVV2CVC2;
    protected long authAmount;
    protected int authDevise;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar authDate;
    protected String authNb;
    protected int authResult;
    @XmlElement(name = "authCVV2_CVC2")
    protected String authCVV2CVC2;
    protected String warrantlyResult;
    @XmlElement(nillable = true)
    protected List<LocalControl> localControl;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar captureDate;
    protected int captureNumber;
    protected int rapprochementStatut;
    protected long refoundAmount;
    protected int refundDevise;
    protected Boolean litige;

    /**
     * Gets the value of the timestamp property.
     * 
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     */
    public void setTimestamp(long value) {
        this.timestamp = value;
    }

    /**
     * Gets the value of the signature property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets the value of the signature property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSignature(String value) {
        this.signature = value;
    }

    /**
     * Gets the value of the errorCode property.
     * 
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the value of the errorCode property.
     * 
     */
    public void setErrorCode(int value) {
        this.errorCode = value;
    }

    /**
     * Gets the value of the extendedErrorCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtendedErrorCode() {
        return extendedErrorCode;
    }

    /**
     * Sets the value of the extendedErrorCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtendedErrorCode(String value) {
        this.extendedErrorCode = value;
    }

    /**
     * Gets the value of the transactionStatus property.
     * 
     */
    public int getTransactionStatus() {
        return transactionStatus;
    }

    /**
     * Sets the value of the transactionStatus property.
     * 
     */
    public void setTransactionStatus(int value) {
        this.transactionStatus = value;
    }

    /**
     * Gets the value of the shopId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShopId() {
        return shopId;
    }

    /**
     * Sets the value of the shopId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShopId(String value) {
        this.shopId = value;
    }

    /**
     * Gets the value of the paymentMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Sets the value of the paymentMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPaymentMethod(String value) {
        this.paymentMethod = value;
    }

    /**
     * Gets the value of the contractNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContractNumber() {
        return contractNumber;
    }

    /**
     * Sets the value of the contractNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContractNumber(String value) {
        this.contractNumber = value;
    }

    /**
     * Gets the value of the orderId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the value of the orderId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderId(String value) {
        this.orderId = value;
    }

    /**
     * Gets the value of the orderInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderInfo() {
        return orderInfo;
    }

    /**
     * Sets the value of the orderInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderInfo(String value) {
        this.orderInfo = value;
    }

    /**
     * Gets the value of the orderInfo2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderInfo2() {
        return orderInfo2;
    }

    /**
     * Sets the value of the orderInfo2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderInfo2(String value) {
        this.orderInfo2 = value;
    }

    /**
     * Gets the value of the orderInfo3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrderInfo3() {
        return orderInfo3;
    }

    /**
     * Sets the value of the orderInfo3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrderInfo3(String value) {
        this.orderInfo3 = value;
    }

    /**
     * Gets the value of the transmissionDate property.
     * 
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTransmissionDate() {
        return transmissionDate;
    }

    /**
     * Sets the value of the transmissionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public void setTransmissionDate(XMLGregorianCalendar value) {
        this.transmissionDate = value;
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
     * Gets the value of the sequenceNb property.
     * 
     */
    public int getSequenceNb() {
        return sequenceNb;
    }

    /**
     * Sets the value of the sequenceNb property.
     * 
     */
    public void setSequenceNb(int value) {
        this.sequenceNb = value;
    }

    /**
     * Gets the value of the amount property.
     * 
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     */
    public void setAmount(long value) {
        this.amount = value;
    }

    /**
     * Gets the value of the initialAmount property.
     * 
     */
    public long getInitialAmount() {
        return initialAmount;
    }

    /**
     * Sets the value of the initialAmount property.
     * 
     */
    public void setInitialAmount(long value) {
        this.initialAmount = value;
    }

    /**
     * Gets the value of the devise property.
     * 
     */
    public int getDevise() {
        return devise;
    }

    /**
     * Sets the value of the devise property.
     * 
     */
    public void setDevise(int value) {
        this.devise = value;
    }

    /**
     * Gets the value of the cvAmount property.
     * 
     */
    public long getCvAmount() {
        return cvAmount;
    }

    /**
     * Sets the value of the cvAmount property.
     * 
     */
    public void setCvAmount(long value) {
        this.cvAmount = value;
    }

    /**
     * Gets the value of the cvDevise property.
     * 
     */
    public int getCvDevise() {
        return cvDevise;
    }

    /**
     * Sets the value of the cvDevise property.
     * 
     */
    public void setCvDevise(int value) {
        this.cvDevise = value;
    }

    /**
     * Gets the value of the presentationDate property.
     * 
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getPresentationDate() {
        return presentationDate;
    }

    /**
     * Sets the value of the presentationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public void setPresentationDate(XMLGregorianCalendar value) {
        this.presentationDate = value;
    }

    /**
     * Gets the value of the type property.
     * 
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     */
    public void setType(int value) {
        this.type = value;
    }

    /**
     * Gets the value of the multiplePaiement property.
     * 
     */
    public int getMultiplePaiement() {
        return multiplePaiement;
    }

    /**
     * Sets the value of the multiplePaiement property.
     * 
     */
    public void setMultiplePaiement(int value) {
        this.multiplePaiement = value;
    }

    /**
     * Gets the value of the ctxMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCtxMode() {
        return ctxMode;
    }

    /**
     * Sets the value of the ctxMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCtxMode(String value) {
        this.ctxMode = value;
    }

    /**
     * Gets the value of the cardNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCardNumber() {
        return cardNumber;
    }

    /**
     * Sets the value of the cardNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCardNumber(String value) {
        this.cardNumber = value;
    }

    /**
     * Gets the value of the cardNetwork property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCardNetwork() {
        return cardNetwork;
    }

    /**
     * Sets the value of the cardNetwork property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCardNetwork(String value) {
        this.cardNetwork = value;
    }

    /**
     * Gets the value of the cardType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCardType() {
        return cardType;
    }

    /**
     * Sets the value of the cardType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCardType(String value) {
        this.cardType = value;
    }

    /**
     * Gets the value of the cardCountry property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCardCountry() {
        return cardCountry;
    }

    /**
     * Sets the value of the cardCountry property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCardCountry(Long value) {
        this.cardCountry = value;
    }

    /**
     * Gets the value of the cardExpirationDate property.
     * 
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCardExpirationDate() {
        return cardExpirationDate;
    }

    /**
     * Sets the value of the cardExpirationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public void setCardExpirationDate(XMLGregorianCalendar value) {
        this.cardExpirationDate = value;
    }

    /**
     * Gets the value of the customerId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * Sets the value of the customerId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerId(String value) {
        this.customerId = value;
    }

    /**
     * Gets the value of the customerTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerTitle() {
        return customerTitle;
    }

    /**
     * Sets the value of the customerTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerTitle(String value) {
        this.customerTitle = value;
    }

    /**
     * Gets the value of the customerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Sets the value of the customerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerName(String value) {
        this.customerName = value;
    }

    /**
     * Gets the value of the customerPhone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerPhone() {
        return customerPhone;
    }

    /**
     * Sets the value of the customerPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerPhone(String value) {
        this.customerPhone = value;
    }

    /**
     * Gets the value of the customerMail property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerMail() {
        return customerMail;
    }

    /**
     * Sets the value of the customerMail property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerMail(String value) {
        this.customerMail = value;
    }

    /**
     * Gets the value of the customerAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerAddress() {
        return customerAddress;
    }

    /**
     * Sets the value of the customerAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerAddress(String value) {
        this.customerAddress = value;
    }

    /**
     * Gets the value of the customerZipCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerZipCode() {
        return customerZipCode;
    }

    /**
     * Sets the value of the customerZipCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerZipCode(String value) {
        this.customerZipCode = value;
    }

    /**
     * Gets the value of the customerCity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerCity() {
        return customerCity;
    }

    /**
     * Sets the value of the customerCity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerCity(String value) {
        this.customerCity = value;
    }

    /**
     * Gets the value of the customerCountry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerCountry() {
        return customerCountry;
    }

    /**
     * Sets the value of the customerCountry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerCountry(String value) {
        this.customerCountry = value;
    }

    /**
     * Gets the value of the customerLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerLanguage() {
        return customerLanguage;
    }

    /**
     * Sets the value of the customerLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerLanguage(String value) {
        this.customerLanguage = value;
    }

    /**
     * Gets the value of the customerIP property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerIP() {
        return customerIP;
    }

    /**
     * Sets the value of the customerIP property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerIP(String value) {
        this.customerIP = value;
    }

    /**
     * Gets the value of the transactionCondition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionCondition() {
        return transactionCondition;
    }

    /**
     * Sets the value of the transactionCondition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionCondition(String value) {
        this.transactionCondition = value;
    }

    /**
     * Gets the value of the vadsEnrolled property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVadsEnrolled() {
        return vadsEnrolled;
    }

    /**
     * Sets the value of the vadsEnrolled property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVadsEnrolled(String value) {
        this.vadsEnrolled = value;
    }

    /**
     * Gets the value of the vadsStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVadsStatus() {
        return vadsStatus;
    }

    /**
     * Sets the value of the vadsStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVadsStatus(String value) {
        this.vadsStatus = value;
    }

    /**
     * Gets the value of the vadsECI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVadsECI() {
        return vadsECI;
    }

    /**
     * Sets the value of the vadsECI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVadsECI(String value) {
        this.vadsECI = value;
    }

    /**
     * Gets the value of the vadsXID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVadsXID() {
        return vadsXID;
    }

    /**
     * Sets the value of the vadsXID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVadsXID(String value) {
        this.vadsXID = value;
    }

    /**
     * Gets the value of the vadsCAVVAlgorithm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVadsCAVVAlgorithm() {
        return vadsCAVVAlgorithm;
    }

    /**
     * Sets the value of the vadsCAVVAlgorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVadsCAVVAlgorithm(String value) {
        this.vadsCAVVAlgorithm = value;
    }

    /**
     * Gets the value of the vadsCAVV property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVadsCAVV() {
        return vadsCAVV;
    }

    /**
     * Sets the value of the vadsCAVV property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVadsCAVV(String value) {
        this.vadsCAVV = value;
    }

    /**
     * Gets the value of the vadsSignatureValid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVadsSignatureValid() {
        return vadsSignatureValid;
    }

    /**
     * Sets the value of the vadsSignatureValid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVadsSignatureValid(String value) {
        this.vadsSignatureValid = value;
    }

    /**
     * Gets the value of the directoryServer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDirectoryServer() {
        return directoryServer;
    }

    /**
     * Sets the value of the directoryServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDirectoryServer(String value) {
        this.directoryServer = value;
    }

    /**
     * Gets the value of the authMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthMode() {
        return authMode;
    }

    /**
     * Sets the value of the authMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthMode(String value) {
        this.authMode = value;
    }

    /**
     * Gets the value of the markAmount property.
     * 
     */
    public long getMarkAmount() {
        return markAmount;
    }

    /**
     * Sets the value of the markAmount property.
     * 
     */
    public void setMarkAmount(long value) {
        this.markAmount = value;
    }

    /**
     * Gets the value of the markDevise property.
     * 
     */
    public int getMarkDevise() {
        return markDevise;
    }

    /**
     * Sets the value of the markDevise property.
     * 
     */
    public void setMarkDevise(int value) {
        this.markDevise = value;
    }

    /**
     * Gets the value of the markDate property.
     * 
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getMarkDate() {
        return markDate;
    }

    /**
     * Sets the value of the markDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public void setMarkDate(XMLGregorianCalendar value) {
        this.markDate = value;
    }

    /**
     * Gets the value of the markNb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMarkNb() {
        return markNb;
    }

    /**
     * Sets the value of the markNb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMarkNb(String value) {
        this.markNb = value;
    }

    /**
     * Gets the value of the markResult property.
     * 
     */
    public int getMarkResult() {
        return markResult;
    }

    /**
     * Sets the value of the markResult property.
     * 
     */
    public void setMarkResult(int value) {
        this.markResult = value;
    }

    /**
     * Gets the value of the markCVV2CVC2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMarkCVV2CVC2() {
        return markCVV2CVC2;
    }

    /**
     * Sets the value of the markCVV2CVC2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMarkCVV2CVC2(String value) {
        this.markCVV2CVC2 = value;
    }

    /**
     * Gets the value of the authAmount property.
     * 
     */
    public long getAuthAmount() {
        return authAmount;
    }

    /**
     * Sets the value of the authAmount property.
     * 
     */
    public void setAuthAmount(long value) {
        this.authAmount = value;
    }

    /**
     * Gets the value of the authDevise property.
     * 
     */
    public int getAuthDevise() {
        return authDevise;
    }

    /**
     * Sets the value of the authDevise property.
     * 
     */
    public void setAuthDevise(int value) {
        this.authDevise = value;
    }

    /**
     * Gets the value of the authDate property.
     * 
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAuthDate() {
        return authDate;
    }

    /**
     * Sets the value of the authDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public void setAuthDate(XMLGregorianCalendar value) {
        this.authDate = value;
    }

    /**
     * Gets the value of the authNb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthNb() {
        return authNb;
    }

    /**
     * Sets the value of the authNb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthNb(String value) {
        this.authNb = value;
    }

    /**
     * Gets the value of the authResult property.
     * 
     */
    public int getAuthResult() {
        return authResult;
    }

    /**
     * Sets the value of the authResult property.
     * 
     */
    public void setAuthResult(int value) {
        this.authResult = value;
    }

    /**
     * Gets the value of the authCVV2CVC2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthCVV2CVC2() {
        return authCVV2CVC2;
    }

    /**
     * Sets the value of the authCVV2CVC2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthCVV2CVC2(String value) {
        this.authCVV2CVC2 = value;
    }

    /**
     * Gets the value of the warrantlyResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWarrantlyResult() {
        return warrantlyResult;
    }

    /**
     * Sets the value of the warrantlyResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWarrantlyResult(String value) {
        this.warrantlyResult = value;
    }

    /**
     * Gets the value of the localControl property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the localControl property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocalControl().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link com.lyra.vads.ws.stub.LocalControl }
     * 
     * 
     */
    public List<LocalControl> getLocalControl() {
        if (localControl == null) {
            localControl = new ArrayList<LocalControl>();
        }
        return this.localControl;
    }

    /**
     * Gets the value of the captureDate property.
     * 
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCaptureDate() {
        return captureDate;
    }

    /**
     * Sets the value of the captureDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public void setCaptureDate(XMLGregorianCalendar value) {
        this.captureDate = value;
    }

    /**
     * Gets the value of the captureNumber property.
     * 
     */
    public int getCaptureNumber() {
        return captureNumber;
    }

    /**
     * Sets the value of the captureNumber property.
     * 
     */
    public void setCaptureNumber(int value) {
        this.captureNumber = value;
    }

    /**
     * Gets the value of the rapprochementStatut property.
     * 
     */
    public int getRapprochementStatut() {
        return rapprochementStatut;
    }

    /**
     * Sets the value of the rapprochementStatut property.
     * 
     */
    public void setRapprochementStatut(int value) {
        this.rapprochementStatut = value;
    }

    /**
     * Gets the value of the refoundAmount property.
     * 
     */
    public long getRefoundAmount() {
        return refoundAmount;
    }

    /**
     * Sets the value of the refoundAmount property.
     * 
     */
    public void setRefoundAmount(long value) {
        this.refoundAmount = value;
    }

    /**
     * Gets the value of the refundDevise property.
     * 
     */
    public int getRefundDevise() {
        return refundDevise;
    }

    /**
     * Sets the value of the refundDevise property.
     * 
     */
    public void setRefundDevise(int value) {
        this.refundDevise = value;
    }

    /**
     * Gets the value of the litige property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLitige() {
        return litige;
    }

    /**
     * Sets the value of the litige property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLitige(Boolean value) {
        this.litige = value;
    }

}
