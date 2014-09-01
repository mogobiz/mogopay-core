
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the request for the
 * 							updateWebWallet
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
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="contractNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cardInd" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="walletId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="updatePersonalDetails" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="updateOwnerDetails" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="updatePaymentDetails" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="languageCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="customPaymentPageCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="securityMode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="returnURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="cancelURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="notificationURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="privateDataList" type="{http://obj.ws.payline.experian.com}privateDataList"/>
 *         &lt;element name="customPaymentTemplateURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "version",
    "contractNumber",
    "cardInd",
    "walletId",
    "updatePersonalDetails",
    "updateOwnerDetails",
    "updatePaymentDetails",
    "languageCode",
    "customPaymentPageCode",
    "securityMode",
    "returnURL",
    "cancelURL",
    "notificationURL",
    "privateDataList",
    "customPaymentTemplateURL",
    "contractNumberWalletList"
})
@XmlRootElement(name = "updateWebWalletRequest")
public class UpdateWebWalletRequest {

    @XmlElement(required = true)
    protected String version;
    @XmlElement(required = true)
    protected String contractNumber;
    @XmlElement(required = true, nillable = true)
    protected String cardInd;
    @XmlElement(required = true)
    protected String walletId;
    @XmlElement(required = true, nillable = true)
    protected String updatePersonalDetails;
    @XmlElement(required = true, nillable = true)
    protected String updateOwnerDetails;
    @XmlElement(required = true, nillable = true)
    protected String updatePaymentDetails;
    @XmlElement(required = true, nillable = true)
    protected String languageCode;
    @XmlElement(required = true, nillable = true)
    protected String customPaymentPageCode;
    @XmlElement(required = true, nillable = true)
    protected String securityMode;
    @XmlElement(required = true)
    protected String returnURL;
    @XmlElement(required = true)
    protected String cancelURL;
    @XmlElement(required = true, nillable = true)
    protected String notificationURL;
    @XmlElement(required = true, nillable = true)
    protected PrivateDataList privateDataList;
    @XmlElement(required = true, nillable = true)
    protected String customPaymentTemplateURL;
    @XmlElement(required = true, nillable = true)
    protected ContractNumberWalletList contractNumberWalletList;

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
     * Gets the value of the updatePersonalDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUpdatePersonalDetails() {
        return updatePersonalDetails;
    }

    /**
     * Sets the value of the updatePersonalDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUpdatePersonalDetails(String value) {
        this.updatePersonalDetails = value;
    }

    /**
     * Gets the value of the updateOwnerDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUpdateOwnerDetails() {
        return updateOwnerDetails;
    }

    /**
     * Sets the value of the updateOwnerDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUpdateOwnerDetails(String value) {
        this.updateOwnerDetails = value;
    }

    /**
     * Gets the value of the updatePaymentDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUpdatePaymentDetails() {
        return updatePaymentDetails;
    }

    /**
     * Sets the value of the updatePaymentDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUpdatePaymentDetails(String value) {
        this.updatePaymentDetails = value;
    }

    /**
     * Gets the value of the languageCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguageCode() {
        return languageCode;
    }

    /**
     * Sets the value of the languageCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguageCode(String value) {
        this.languageCode = value;
    }

    /**
     * Gets the value of the customPaymentPageCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomPaymentPageCode() {
        return customPaymentPageCode;
    }

    /**
     * Sets the value of the customPaymentPageCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomPaymentPageCode(String value) {
        this.customPaymentPageCode = value;
    }

    /**
     * Gets the value of the securityMode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecurityMode() {
        return securityMode;
    }

    /**
     * Sets the value of the securityMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurityMode(String value) {
        this.securityMode = value;
    }

    /**
     * Gets the value of the returnURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReturnURL() {
        return returnURL;
    }

    /**
     * Sets the value of the returnURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReturnURL(String value) {
        this.returnURL = value;
    }

    /**
     * Gets the value of the cancelURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCancelURL() {
        return cancelURL;
    }

    /**
     * Sets the value of the cancelURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCancelURL(String value) {
        this.cancelURL = value;
    }

    /**
     * Gets the value of the notificationURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotificationURL() {
        return notificationURL;
    }

    /**
     * Sets the value of the notificationURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotificationURL(String value) {
        this.notificationURL = value;
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
     * Gets the value of the customPaymentTemplateURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomPaymentTemplateURL() {
        return customPaymentTemplateURL;
    }

    /**
     * Sets the value of the customPaymentTemplateURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomPaymentTemplateURL(String value) {
        this.customPaymentTemplateURL = value;
    }

    /**
     * Gets the value of the contractNumberWalletList property.
     * 
     * @return
     *     possible object is
     *     {@link ContractNumberWalletList }
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
     *     {@link ContractNumberWalletList }
     *     
     */
    public void setContractNumberWalletList(ContractNumberWalletList value) {
        this.contractNumberWalletList = value;
    }

}
