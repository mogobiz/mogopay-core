
package com.experian.payline.wsclient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 						This element contains all information about contract
 * 					
 * 
 * <p>Java class for contract complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="contract">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cardType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="label" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contractNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="currency" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="settlementType">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *               &lt;enumeration value="Manual"/>
 *               &lt;enumeration value="Now"/>
 *               &lt;enumeration value="1Day"/>
 *               &lt;enumeration value="2Day"/>
 *               &lt;enumeration value="3Day"/>
 *               &lt;enumeration value="4Day"/>
 *               &lt;enumeration value="5Day"/>
 *               &lt;enumeration value="6Day"/>
 *               &lt;enumeration value="7Day"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/element>
 *         &lt;element name="maxAmountPerTransaction" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="technicalData" type="{http://obj.ws.payline.experian.com}technicalData"/>
 *         &lt;element name="bankAccount" type="{http://obj.ws.payline.experian.com}bankAccount"/>
 *         &lt;element name="acquirerInterlocutor" type="{http://obj.ws.payline.experian.com}interlocutor"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="logoEnable" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="smallLogoMime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="smallLogo" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="normalLogoMime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="normalLogo" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="contribution" type="{http://obj.ws.payline.experian.com}contribution" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "contract", namespace = "http://obj.ws.payline.experian.com", propOrder = {
    "cardType",
    "label",
    "contractNumber",
    "currency",
    "settlementType",
    "maxAmountPerTransaction",
    "technicalData",
    "bankAccount",
    "acquirerInterlocutor",
    "description",
    "logoEnable",
    "smallLogoMime",
    "smallLogo",
    "normalLogoMime",
    "normalLogo",
    "contribution"
})
public class Contract {

    @XmlElement(required = true, nillable = true)
    protected String cardType;
    @XmlElementRef(name = "label", namespace = "http://obj.ws.payline.experian.com", type = JAXBElement.class, required = false)
    protected JAXBElement<String> label;
    @XmlElement(required = true, nillable = true)
    protected String contractNumber;
    @XmlElement(required = true, nillable = true)
    protected String currency;
    @XmlElement(required = true, defaultValue = "Manual")
    protected String settlementType;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer maxAmountPerTransaction;
    @XmlElement(required = true, nillable = true)
    protected TechnicalData technicalData;
    @XmlElement(required = true, nillable = true)
    protected BankAccount bankAccount;
    @XmlElement(required = true, nillable = true)
    protected Interlocutor acquirerInterlocutor;
    @XmlElement(required = true, nillable = true)
    protected String description;
    protected boolean logoEnable;
    protected String smallLogoMime;
    protected byte[] smallLogo;
    protected String normalLogoMime;
    protected byte[] normalLogo;
    protected Contribution contribution;

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
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link javax.xml.bind.JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setLabel(JAXBElement<String> value) {
        this.label = value;
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
     * Gets the value of the currency property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Sets the value of the currency property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrency(String value) {
        this.currency = value;
    }

    /**
     * Gets the value of the settlementType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSettlementType() {
        return settlementType;
    }

    /**
     * Sets the value of the settlementType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSettlementType(String value) {
        this.settlementType = value;
    }

    /**
     * Gets the value of the maxAmountPerTransaction property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxAmountPerTransaction() {
        return maxAmountPerTransaction;
    }

    /**
     * Sets the value of the maxAmountPerTransaction property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxAmountPerTransaction(Integer value) {
        this.maxAmountPerTransaction = value;
    }

    /**
     * Gets the value of the technicalData property.
     * 
     * @return
     *     possible object is
     *     {@link TechnicalData }
     *     
     */
    public TechnicalData getTechnicalData() {
        return technicalData;
    }

    /**
     * Sets the value of the technicalData property.
     * 
     * @param value
     *     allowed object is
     *     {@link TechnicalData }
     *     
     */
    public void setTechnicalData(TechnicalData value) {
        this.technicalData = value;
    }

    /**
     * Gets the value of the bankAccount property.
     * 
     * @return
     *     possible object is
     *     {@link BankAccount }
     *     
     */
    public BankAccount getBankAccount() {
        return bankAccount;
    }

    /**
     * Sets the value of the bankAccount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BankAccount }
     *     
     */
    public void setBankAccount(BankAccount value) {
        this.bankAccount = value;
    }

    /**
     * Gets the value of the acquirerInterlocutor property.
     * 
     * @return
     *     possible object is
     *     {@link Interlocutor }
     *     
     */
    public Interlocutor getAcquirerInterlocutor() {
        return acquirerInterlocutor;
    }

    /**
     * Sets the value of the acquirerInterlocutor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Interlocutor }
     *     
     */
    public void setAcquirerInterlocutor(Interlocutor value) {
        this.acquirerInterlocutor = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the logoEnable property.
     * 
     */
    public boolean isLogoEnable() {
        return logoEnable;
    }

    /**
     * Sets the value of the logoEnable property.
     * 
     */
    public void setLogoEnable(boolean value) {
        this.logoEnable = value;
    }

    /**
     * Gets the value of the smallLogoMime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSmallLogoMime() {
        return smallLogoMime;
    }

    /**
     * Sets the value of the smallLogoMime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSmallLogoMime(String value) {
        this.smallLogoMime = value;
    }

    /**
     * Gets the value of the smallLogo property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getSmallLogo() {
        return smallLogo;
    }

    /**
     * Sets the value of the smallLogo property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setSmallLogo(byte[] value) {
        this.smallLogo = value;
    }

    /**
     * Gets the value of the normalLogoMime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNormalLogoMime() {
        return normalLogoMime;
    }

    /**
     * Sets the value of the normalLogoMime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNormalLogoMime(String value) {
        this.normalLogoMime = value;
    }

    /**
     * Gets the value of the normalLogo property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getNormalLogo() {
        return normalLogo;
    }

    /**
     * Sets the value of the normalLogo property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setNormalLogo(byte[] value) {
        this.normalLogo = value;
    }

    /**
     * Gets the value of the contribution property.
     * 
     * @return
     *     possible object is
     *     {@link Contribution }
     *     
     */
    public Contribution getContribution() {
        return contribution;
    }

    /**
     * Sets the value of the contribution property.
     * 
     * @param value
     *     allowed object is
     *     {@link Contribution }
     *     
     */
    public void setContribution(Contribution value) {
        this.contribution = value;
    }

}
