
package com.experian.payline.wsclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 							This element is the request for the
 * 							updateWallet
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
 *         &lt;element name="wallet" type="{http://obj.ws.payline.experian.com}wallet"/>
 *         &lt;element name="owner" type="{http://obj.ws.payline.experian.com}owner"/>
 *         &lt;element name="privateDataList" type="{http://obj.ws.payline.experian.com}privateDataList"/>
 *         &lt;element name="authentication3DSecure" type="{http://obj.ws.payline.experian.com}authentication3DSecure"/>
 *         &lt;element name="media" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "wallet",
    "owner",
    "privateDataList",
    "authentication3DSecure",
    "media",
    "contractNumberWalletList"
})
@XmlRootElement(name = "updateWalletRequest")
public class UpdateWalletRequest {

    @XmlElement(required = true)
    protected String version;
    @XmlElement(required = true)
    protected String contractNumber;
    @XmlElement(required = true, nillable = true)
    protected String cardInd;
    @XmlElement(required = true)
    protected Wallet wallet;
    @XmlElement(required = true, nillable = true)
    protected Owner owner;
    @XmlElement(required = true, nillable = true)
    protected PrivateDataList privateDataList;
    @XmlElement(required = true, nillable = true)
    protected Authentication3DSecure authentication3DSecure;
    @XmlElement(required = true, nillable = true)
    protected String media;
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
