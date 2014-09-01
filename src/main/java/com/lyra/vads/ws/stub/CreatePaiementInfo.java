
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
 * <p>Java class for createPaiementInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createPaiementInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="shopId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transmissionDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="transactionId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="paymentMethod" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="orderId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="orderInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orderInfo2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="orderInfo3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="devise" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="presentationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="validationMode" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="cardNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cardNetwork" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cardExpirationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="cvv" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="birthDay" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="contractNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="optionCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="threeDsResult" type="{http://v3.ws.vads.lyra.com/}threeDsResult" minOccurs="0"/>
 *         &lt;element name="subPaymentType" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="subReference" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="subPaymentNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="customerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerTitle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerStatus" type="{http://v3.ws.vads.lyra.com/}custStatus" minOccurs="0"/>
 *         &lt;element name="customerName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerPhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerMail" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerAddressNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerDistrict" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerZipCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerCity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerCountry" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerLanguage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerIP" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerSendEmail" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="customerCellPhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="extInfo" type="{http://v3.ws.vads.lyra.com/}extInfo" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="ctxMode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingCity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingCountry" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingDeliveryCompanyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingPhone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingSpeed" type="{http://v3.ws.vads.lyra.com/}deliverySpeed" minOccurs="0"/>
 *         &lt;element name="shippingState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingStatus" type="{http://v3.ws.vads.lyra.com/}custStatus" minOccurs="0"/>
 *         &lt;element name="shippingStreetNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingStreet" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingStreet2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingDistrict" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shippingType" type="{http://v3.ws.vads.lyra.com/}deliveryType" minOccurs="0"/>
 *         &lt;element name="shippingZipCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createPaiementInfo", propOrder = {
    "shopId",
    "transmissionDate",
    "transactionId",
    "paymentMethod",
    "orderId",
    "orderInfo",
    "orderInfo2",
    "orderInfo3",
    "amount",
    "devise",
    "presentationDate",
    "validationMode",
    "cardNumber",
    "cardNetwork",
    "cardExpirationDate",
    "cvv",
    "birthDay",
    "contractNumber",
    "optionCode",
    "threeDsResult",
    "subPaymentType",
    "subReference",
    "subPaymentNumber",
    "customerId",
    "customerTitle",
    "customerStatus",
    "customerName",
    "customerPhone",
    "customerMail",
    "customerAddressNumber",
    "customerAddress",
    "customerDistrict",
    "customerZipCode",
    "customerCity",
    "customerCountry",
    "customerLanguage",
    "customerIP",
    "customerSendEmail",
    "customerCellPhone",
    "extInfo",
    "ctxMode",
    "comment",
    "shippingCity",
    "shippingCountry",
    "shippingDeliveryCompanyName",
    "shippingName",
    "shippingPhone",
    "shippingSpeed",
    "shippingState",
    "shippingStatus",
    "shippingStreetNumber",
    "shippingStreet",
    "shippingStreet2",
    "shippingDistrict",
    "shippingType",
    "shippingZipCode"
})
public class CreatePaiementInfo {

    @XmlElement(required = true)
    protected String shopId;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar transmissionDate;
    @XmlElement(required = true)
    protected String transactionId;
    @XmlElement(required = true)
    protected String paymentMethod;
    @XmlElement(required = true)
    protected String orderId;
    protected String orderInfo;
    protected String orderInfo2;
    protected String orderInfo3;
    protected long amount;
    protected int devise;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar presentationDate;
    protected Integer validationMode;
    @XmlElement(required = true)
    protected String cardNumber;
    @XmlElement(required = true)
    protected String cardNetwork;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar cardExpirationDate;
    protected String cvv;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar birthDay;
    protected String contractNumber;
    protected String optionCode;
    protected ThreeDsResult threeDsResult;
    protected Integer subPaymentType;
    protected String subReference;
    protected Integer subPaymentNumber;
    protected String customerId;
    protected String customerTitle;
    protected CustStatus customerStatus;
    protected String customerName;
    protected String customerPhone;
    protected String customerMail;
    protected String customerAddressNumber;
    protected String customerAddress;
    protected String customerDistrict;
    protected String customerZipCode;
    protected String customerCity;
    protected String customerCountry;
    protected String customerLanguage;
    protected String customerIP;
    protected Boolean customerSendEmail;
    protected String customerCellPhone;
    protected List<ExtInfo> extInfo;
    @XmlElement(required = true)
    protected String ctxMode;
    protected String comment;
    protected String shippingCity;
    protected String shippingCountry;
    protected String shippingDeliveryCompanyName;
    protected String shippingName;
    protected String shippingPhone;
    protected DeliverySpeed shippingSpeed;
    protected String shippingState;
    protected CustStatus shippingStatus;
    protected String shippingStreetNumber;
    protected String shippingStreet;
    protected String shippingStreet2;
    protected String shippingDistrict;
    protected DeliveryType shippingType;
    protected String shippingZipCode;

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
     * Gets the value of the validationMode property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getValidationMode() {
        return validationMode;
    }

    /**
     * Sets the value of the validationMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setValidationMode(Integer value) {
        this.validationMode = value;
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
     * Gets the value of the cvv property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCvv() {
        return cvv;
    }

    /**
     * Sets the value of the cvv property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCvv(String value) {
        this.cvv = value;
    }

    /**
     * Gets the value of the birthDay property.
     * 
     * @return
     *     possible object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBirthDay() {
        return birthDay;
    }

    /**
     * Sets the value of the birthDay property.
     * 
     * @param value
     *     allowed object is
     *     {@link javax.xml.datatype.XMLGregorianCalendar }
     *     
     */
    public void setBirthDay(XMLGregorianCalendar value) {
        this.birthDay = value;
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
     * Gets the value of the optionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOptionCode() {
        return optionCode;
    }

    /**
     * Sets the value of the optionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOptionCode(String value) {
        this.optionCode = value;
    }

    /**
     * Gets the value of the threeDsResult property.
     * 
     * @return
     *     possible object is
     *     {@link com.lyra.vads.ws.stub.ThreeDsResult }
     *     
     */
    public ThreeDsResult getThreeDsResult() {
        return threeDsResult;
    }

    /**
     * Sets the value of the threeDsResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.lyra.vads.ws.stub.ThreeDsResult }
     *     
     */
    public void setThreeDsResult(ThreeDsResult value) {
        this.threeDsResult = value;
    }

    /**
     * Gets the value of the subPaymentType property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSubPaymentType() {
        return subPaymentType;
    }

    /**
     * Sets the value of the subPaymentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSubPaymentType(Integer value) {
        this.subPaymentType = value;
    }

    /**
     * Gets the value of the subReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubReference() {
        return subReference;
    }

    /**
     * Sets the value of the subReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubReference(String value) {
        this.subReference = value;
    }

    /**
     * Gets the value of the subPaymentNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSubPaymentNumber() {
        return subPaymentNumber;
    }

    /**
     * Sets the value of the subPaymentNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSubPaymentNumber(Integer value) {
        this.subPaymentNumber = value;
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
     * Gets the value of the customerStatus property.
     * 
     * @return
     *     possible object is
     *     {@link com.lyra.vads.ws.stub.CustStatus }
     *     
     */
    public CustStatus getCustomerStatus() {
        return customerStatus;
    }

    /**
     * Sets the value of the customerStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.lyra.vads.ws.stub.CustStatus }
     *     
     */
    public void setCustomerStatus(CustStatus value) {
        this.customerStatus = value;
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
     * Gets the value of the customerAddressNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerAddressNumber() {
        return customerAddressNumber;
    }

    /**
     * Sets the value of the customerAddressNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerAddressNumber(String value) {
        this.customerAddressNumber = value;
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
     * Gets the value of the customerDistrict property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerDistrict() {
        return customerDistrict;
    }

    /**
     * Sets the value of the customerDistrict property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerDistrict(String value) {
        this.customerDistrict = value;
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
     * Gets the value of the customerSendEmail property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCustomerSendEmail() {
        return customerSendEmail;
    }

    /**
     * Sets the value of the customerSendEmail property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCustomerSendEmail(Boolean value) {
        this.customerSendEmail = value;
    }

    /**
     * Gets the value of the customerCellPhone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerCellPhone() {
        return customerCellPhone;
    }

    /**
     * Sets the value of the customerCellPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerCellPhone(String value) {
        this.customerCellPhone = value;
    }

    /**
     * Gets the value of the extInfo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the extInfo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtInfo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link com.lyra.vads.ws.stub.ExtInfo }
     * 
     * 
     */
    public List<ExtInfo> getExtInfo() {
        if (extInfo == null) {
            extInfo = new ArrayList<ExtInfo>();
        }
        return this.extInfo;
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
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the shippingCity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingCity() {
        return shippingCity;
    }

    /**
     * Sets the value of the shippingCity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingCity(String value) {
        this.shippingCity = value;
    }

    /**
     * Gets the value of the shippingCountry property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingCountry() {
        return shippingCountry;
    }

    /**
     * Sets the value of the shippingCountry property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingCountry(String value) {
        this.shippingCountry = value;
    }

    /**
     * Gets the value of the shippingDeliveryCompanyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingDeliveryCompanyName() {
        return shippingDeliveryCompanyName;
    }

    /**
     * Sets the value of the shippingDeliveryCompanyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingDeliveryCompanyName(String value) {
        this.shippingDeliveryCompanyName = value;
    }

    /**
     * Gets the value of the shippingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingName() {
        return shippingName;
    }

    /**
     * Sets the value of the shippingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingName(String value) {
        this.shippingName = value;
    }

    /**
     * Gets the value of the shippingPhone property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingPhone() {
        return shippingPhone;
    }

    /**
     * Sets the value of the shippingPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingPhone(String value) {
        this.shippingPhone = value;
    }

    /**
     * Gets the value of the shippingSpeed property.
     * 
     * @return
     *     possible object is
     *     {@link com.lyra.vads.ws.stub.DeliverySpeed }
     *     
     */
    public DeliverySpeed getShippingSpeed() {
        return shippingSpeed;
    }

    /**
     * Sets the value of the shippingSpeed property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.lyra.vads.ws.stub.DeliverySpeed }
     *     
     */
    public void setShippingSpeed(DeliverySpeed value) {
        this.shippingSpeed = value;
    }

    /**
     * Gets the value of the shippingState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingState() {
        return shippingState;
    }

    /**
     * Sets the value of the shippingState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingState(String value) {
        this.shippingState = value;
    }

    /**
     * Gets the value of the shippingStatus property.
     * 
     * @return
     *     possible object is
     *     {@link com.lyra.vads.ws.stub.CustStatus }
     *     
     */
    public CustStatus getShippingStatus() {
        return shippingStatus;
    }

    /**
     * Sets the value of the shippingStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.lyra.vads.ws.stub.CustStatus }
     *     
     */
    public void setShippingStatus(CustStatus value) {
        this.shippingStatus = value;
    }

    /**
     * Gets the value of the shippingStreetNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingStreetNumber() {
        return shippingStreetNumber;
    }

    /**
     * Sets the value of the shippingStreetNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingStreetNumber(String value) {
        this.shippingStreetNumber = value;
    }

    /**
     * Gets the value of the shippingStreet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingStreet() {
        return shippingStreet;
    }

    /**
     * Sets the value of the shippingStreet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingStreet(String value) {
        this.shippingStreet = value;
    }

    /**
     * Gets the value of the shippingStreet2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingStreet2() {
        return shippingStreet2;
    }

    /**
     * Sets the value of the shippingStreet2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingStreet2(String value) {
        this.shippingStreet2 = value;
    }

    /**
     * Gets the value of the shippingDistrict property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingDistrict() {
        return shippingDistrict;
    }

    /**
     * Sets the value of the shippingDistrict property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingDistrict(String value) {
        this.shippingDistrict = value;
    }

    /**
     * Gets the value of the shippingType property.
     * 
     * @return
     *     possible object is
     *     {@link com.lyra.vads.ws.stub.DeliveryType }
     *     
     */
    public DeliveryType getShippingType() {
        return shippingType;
    }

    /**
     * Sets the value of the shippingType property.
     * 
     * @param value
     *     allowed object is
     *     {@link com.lyra.vads.ws.stub.DeliveryType }
     *     
     */
    public void setShippingType(DeliveryType value) {
        this.shippingType = value;
    }

    /**
     * Gets the value of the shippingZipCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShippingZipCode() {
        return shippingZipCode;
    }

    /**
     * Sets the value of the shippingZipCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShippingZipCode(String value) {
        this.shippingZipCode = value;
    }

}
