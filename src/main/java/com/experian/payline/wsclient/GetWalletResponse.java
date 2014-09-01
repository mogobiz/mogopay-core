
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the reponse from the
 * 							getWallet
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
 *         &lt;element name="wallet" type="{http://obj.ws.payline.experian.com}wallet"/>
 *         &lt;element name="owner" type="{http://obj.ws.payline.experian.com}owner"/>
 *         &lt;element name="isDisabled" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="disableDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="disableStatus" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="privateDataList" type="{http://obj.ws.payline.experian.com}privateDataList"/>
 *         &lt;element name="extendedCard" type="{http://obj.ws.payline.experian.com}extendedCardType"/>
 *         &lt;element name="contractNumberWalletList" type="{http://obj.ws.payline.experian.com}contractNumberWalletList"/>
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
    "wallet",
    "owner",
    "isDisabled",
    "disableDate",
    "disableStatus",
    "privateDataList",
    "extendedCard",
    "contractNumberWalletList",
    "media"
})
@XmlRootElement(name = "getWalletResponse")
public class GetWalletResponse {

    @XmlElement(required = true)
    protected Result result;
    @XmlElement(required = true, nillable = true)
    protected Wallet wallet;
    @XmlElement(required = true, nillable = true)
    protected Owner owner;
    @XmlElement(required = true, nillable = true)
    protected String isDisabled;
    @XmlElement(required = true, nillable = true)
    protected String disableDate;
    @XmlElement(required = true, nillable = true)
    protected String disableStatus;
    @XmlElement(required = true, nillable = true)
    protected PrivateDataList privateDataList;
    @XmlElement(required = true, nillable = true)
    protected ExtendedCardType extendedCard;
    @XmlElement(required = true, nillable = true)
    protected ContractNumberWalletList contractNumberWalletList;
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
     * Gets the value of the isDisabled property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsDisabled() {
        return isDisabled;
    }

    /**
     * Sets the value of the isDisabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsDisabled(String value) {
        this.isDisabled = value;
    }

    /**
     * Gets the value of the disableDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisableDate() {
        return disableDate;
    }

    /**
     * Sets the value of the disableDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisableDate(String value) {
        this.disableDate = value;
    }

    /**
     * Gets the value of the disableStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisableStatus() {
        return disableStatus;
    }

    /**
     * Sets the value of the disableStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisableStatus(String value) {
        this.disableStatus = value;
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
